package localization;

import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;

import org.firstinspires.ftc.robotcore.external.Function;
import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import org.firstinspires.ftc.robotcore.external.matrices.VectorF;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import annotations.FieldCoordinates;
import annotations.MatrixCoordinates;
import localization.SpaceMap.Space;

/**
 * A class for managing recognitions which are actually mapped onto the field.
 * Determines when a recognition should be mapped and removed from the map, or when its
 * position should be changed
 */
public class DetectionMapper {
    private static final String TAG = "vuf.test.det_mapper";
    private final List<MappedDetection> mappedList;

    public DetectionMapper() {
        mappedList = new ArrayList<>();
    }

    /**
     * Updates the detection mapper with a list of potential detections.
     * Mapped detections that are close to new detections will have their position updated, and
     * detections with conflicts will be overridden.
     * @param potentialList the list of potential detections to update the mapper with
     * @return an arraylist and a hashmap. The arraylist is the list of coordinates to remove, while the
     * hashmap is the coordinates to add with their corresponding space
     */
    @NonNull
    public Pair<ArrayList<int[]>, HashMap<Space, ArrayList<int[]>>> update(@NonNull List<PotentialDetection> potentialList) {
        List<int[]> underConfidentList = removeUnderConfident();
        ArrayList<int[]> removedList = updateMappedRecognitions(potentialList);
        removedList.addAll(underConfidentList);
        HashMap<Space, ArrayList<int[]>> coordMap = mapMappedRecognitions();
        return new Pair<>(removedList, coordMap);
    }

    /**
     * Remove all mapped coordinates that are below the confidence threshold.
     * Remove mapped detections from the list that are below the confidence minimum
     * @return the list of all removed matrix positions
     */
    @NonNull
    private List<int[]> removeUnderConfident() {
        List<int[]> removedCoords = new ArrayList<>();
        for (int i = 0; i < mappedList.size(); i++) {
            MappedDetection mappedDetection = mappedList.get(i);
            if (!mappedDetection.isConfident()) {
                removedCoords.add(mappedDetection.getMatrixPosition());
                if (mappedDetection.isUnderConfident()) {
                    mappedList.remove(i);
                    i--;
                }
            }
        }
        return removedCoords;
    }

    /**
     * Update the list of mapped recognitions with the potential recognitions.
     * If any recognitions are close to the other ones, then they're checked to see if they're the same.
     * If two recognitions of the same type are near each other, the mapped position is updated to
     * the newer recognition's position and confidence is added.
     * If they're of different types and occupy the same space, there can be two separate recognitions
     * and whichever has a higher confidence will be mapped.
     * @param potentialList the list of potential recognitions to check
     * @return the list of removed matrix positions
     */
    @NonNull
    private ArrayList<int[]> updateMappedRecognitions(@NonNull List<PotentialDetection> potentialList) {
        ArrayList<int[]> removedPositions = new ArrayList<>();
        // loop through all potentials and all mapped
        for (PotentialDetection potentialDetection : potentialList) {
            for (MappedDetection mappedDetection : mappedList) {
                int[] removedPos = updateIfClose(mappedDetection, potentialDetection);
                if (removedPos != null) {
                    removedPositions.add(removedPos);
                }
            }
            if (!potentialDetection.isPotentialUsed()) {
                mappedList.add(new MappedDetection(potentialDetection));
            }
        }
        return removedPositions;
    }

    /**
     * Updates the mapped recognition if its close to and of the same type as a potential recognition.
     * If updated, sets the potential as used.
     * @param potentialDetection the other recognition to check
     * @return returns the old matrix position if it was updated, otherwise null
     */
    private int[] updateIfClose(@NonNull MappedDetection mappedDetection, @NonNull PotentialDetection potentialDetection) {
        if (mappedDetection.withinProximityRange(potentialDetection.getFieldPosition())) {
            // if they have the same space, update
            if (potentialDetection.getSpace() == mappedDetection.getSpace()) {
                // if theyre the same, then the recognition is confirmed so we add confidence
                mappedDetection.increaseConfidence();
                potentialDetection.setPotentialUsed();
                return mappedDetection.updateWithPotential(potentialDetection);
            }
        }
        return null;
    }

    /**
     * Maps all the mapped recognitions to a space if they're confident enough to be mapped.
     * @return a hashmap of a space with the coordinates mapped to it
     */
    @NonNull
    private HashMap<Space, ArrayList<int[]>> mapMappedRecognitions() {
        HashMap<Space, ArrayList<int[]>> coordMap = new HashMap<>();
        for (MappedDetection mappedDetection : mappedList) {
            // check if they should be mapped
            if (shouldMap(mappedDetection)) {
                Space space = mappedDetection.getSpace();
                coordMap.putIfAbsent(space, new ArrayList<>());
                // add coords to list
                Objects.requireNonNull(
                        coordMap.get(space)).add(mappedDetection.getMatrixPosition());
            }
        }
        return coordMap;
    }

    /**
     * Determines if the provided recognition should be mapped.
     * If the detection is below the confidence threshold, then it shouldnt be mapped.
     * If the provided mapped recognition has a position conflict with another recognition,
     * then this recognition should be mapped if it has a higher confidence.
     * @return True is returned if the current recognition should be mapped, otherwise, false.
     * If there's a conflict and the confidences are the same, then whichever one is checked last
     * will be mapped.
     */
    private boolean shouldMap(@NonNull MappedDetection mappedDetection) {
        // if its not confident enough, dont map
        if (!mappedDetection.isConfident()) {
            return false;
        }
        for (MappedDetection otherDetection : mappedList) {
            if (!mappedDetection.equals(otherDetection) && // if they're the same, skip
                    // if the matrix positions are the same, compare confidences
                    Arrays.equals(mappedDetection.getMatrixPosition(), otherDetection.getMatrixPosition())) {
                // if this confidence is greater than the other one, map it
                return mappedDetection.getConfidence() >= otherDetection.getConfidence();
            }
        }
        return true;
    }

    /**
     * Remove mapped recognitions if they're no longer present.
     * @param isDetectionGone the function to use to check if the recognition is still present
     */
    public void markDisappearances(Function<OpenGLMatrix, Boolean> isDetectionGone) {
        // loop through mapped list
        for (int i = 0; i < mappedList.size(); i++) {
            MappedDetection mappedDetection = mappedList.get(i);
            // check if the detection is gone
            if (isDetectionGone.apply(mappedDetection.getFieldPosition())) {
                // if its gone, decrease its confidence
                mappedDetection.decreaseConfidence();
            }
        }
    }


    /**
     * A class for managing recognitions that are mapped out on the fieldmap
     */
    private static class MappedDetection {
        private static final String TAG = "vuf.test.field_rec";
        private static final int confidenceThreshold = 4;
        private static final int maxConfidence = 10;
        private static final int proximityRange = FieldMap.getScale();
        private final Space space;
        private OpenGLMatrix fieldPosition;
        private int[] matrixPosition;
        private int confidence;

        /**
         * Initialize a mapped recognition
         * @param fieldPosition the field position of the recognition
         * @param matrixPosition the matrix position of the recognition
         * @param space the space of the recognition
         */
        public MappedDetection(@FieldCoordinates @NonNull OpenGLMatrix fieldPosition,
                               @MatrixCoordinates @NonNull int[] matrixPosition,
                               @NonNull Space space) {
            this.fieldPosition = fieldPosition;
            this.matrixPosition = matrixPosition;
            this.space = space;
        }

        /**
         * Creates a mapped detection out of a potential detection
         * @param potentialDetection the potential detection to use
         */
        @SuppressWarnings("FeatureEnvy")
        public MappedDetection(@NonNull PotentialDetection potentialDetection) {
            this(
                    potentialDetection.getFieldPosition(),
                    potentialDetection.getMatrixPosition(),
                    potentialDetection.getSpace());
        }

        /**
         * Determine if another set of field coordinates is within the range of these coordinates
         * @param otherFieldPos the other field coordinates to test
         * @return true if within range, otherwise false
         */
        private boolean withinProximityRange(@FieldCoordinates @NonNull OpenGLMatrix otherFieldPos) {
            VectorF currentTranslation = fieldPosition.getTranslation();
            VectorF newTranslation = otherFieldPos.getTranslation();
            if (Math.abs(currentTranslation.get(0) - newTranslation.get(0)) <= proximityRange) {
                Log.d(TAG, "X is within range");
                if (Math.abs(currentTranslation.get(1) - newTranslation.get(1)) <= proximityRange) {
                    Log.d(TAG, "Y is within range");
                    return true;
                }
            }
            return false;
        }

        /**
         * Update this position with a potential detection's position
         * @param potentialDetection the potential detection to update with
         * @return the old matrix position if it was changed or the space was changed, otherwise null
         */
        private int[] updateWithPotential(PotentialDetection potentialDetection) {
            this.fieldPosition = potentialDetection.getFieldPosition();
            int[] potentialPos = potentialDetection.getMatrixPosition();

            if (!Arrays.equals(matrixPosition, potentialPos)) {
                int[] oldPos = matrixPosition;
                this.matrixPosition = potentialPos;
                return oldPos;
            }
            return null;
        }

        public Space getSpace() { return space; }
        public int[] getMatrixPosition() { return matrixPosition; }
        public OpenGLMatrix getFieldPosition() { return fieldPosition; }
        public boolean isConfident() { return confidence > confidenceThreshold; }
        public boolean isUnderConfident() { return confidence < 0; } // if its too unconfident, it can be removed
        public void decreaseConfidence() { confidence--; }
        // add confidence if its not over the max
        public void increaseConfidence() { confidence = Math.min(confidence+1, maxConfidence); }
        public int getConfidence() { return confidence; }

    }

    /**
     * Class for storing detections that may potentially be mapped
     */
    public static class PotentialDetection {
        private final OpenGLMatrix fieldPosition;
        private final int[] matrixPosition;
        private final Space space;
        private boolean potentialUsed;

        /**
         * Initialize a potential detection
         * @param fieldPosition the field position of the recognition
         * @param matrixPosition the matrix position of the recognition
         * @param space the space of the recognition
         */
        public PotentialDetection(@FieldCoordinates @NonNull OpenGLMatrix fieldPosition,
                               @MatrixCoordinates @NonNull int[] matrixPosition,
                               @NonNull Space space) {
            this.fieldPosition = fieldPosition;
            this.matrixPosition = matrixPosition;
            this.space = space;
            this.potentialUsed = false;
        }

        public Space getSpace() { return space; }
        public int[] getMatrixPosition() { return matrixPosition; }
        public OpenGLMatrix getFieldPosition() { return fieldPosition; }
        public boolean isPotentialUsed() { return potentialUsed; }
        public void setPotentialUsed() { potentialUsed = true; }
    }
}
