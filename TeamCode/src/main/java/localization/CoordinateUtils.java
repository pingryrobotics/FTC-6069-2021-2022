package localization;

import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import org.firstinspires.ftc.robotcore.external.matrices.VectorF;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import annotations.AnyCoordinateRange;
import annotations.DistanceValues;
import annotations.FieldCoordinates;

/**
 * Class containing helper methods for coordinate transformations
 * All methods should be static
 */
public class CoordinateUtils {

    private static final String TAG = "vuf.test.coordinate_utils";
    public static final long nanoToMilli = 1000000;

    // region merge coordinate forms

    /**
     * Merge integer coordinate and OpenGLMatrix hashtables into one with only integer coordinates
     *
     * @param coordsInt the integer coordinate hashtable
     * @param coordsGL  the OpenGLMatrix hashtable
     */
    @NonNull
    @FieldCoordinates
    protected static Map<SpaceMap.Space, ArrayList<int[]>> joinCoordinateHashtables(
            @Nullable @FieldCoordinates HashMap<SpaceMap.Space, ArrayList<int[]>> coordsInt,
            @Nullable @FieldCoordinates HashMap<SpaceMap.Space, ArrayList<OpenGLMatrix>> coordsGL) {

        // set each hashtable to an empty table if theyre null
        coordsGL = (coordsGL == null) ? new HashMap<SpaceMap.Space, ArrayList<OpenGLMatrix>>() : coordsGL;
        // create a deep copy of the int coordinate hashtable
        HashMap<SpaceMap.Space, ArrayList<int[]>> newCoordsInt = deepCopyCoordsInt(coordsInt);
        // now both hashtables are non null

        Log.d(FieldMap.TAG, "joining values");
        // loop through all values of spaces and join the coords from each
        for (SpaceMap.Space space : SpaceMap.Space.values()) {
            // create arraylists for both
            ArrayList<OpenGLMatrix> arrayListGL = new ArrayList<>();
            ArrayList<int[]> arrayListInt = new ArrayList<>();

            // try and get arraylist values from both hashtables
            try {
                arrayListGL = coordsGL.get(space);
                arrayListInt = newCoordsInt.get(space);
            } catch (NoSuchElementException ignored) {
            }

            // join both arraylists into the int list
            arrayListInt = joinCoordinateArrayLists(arrayListGL, arrayListInt);
            // if theres no elements, dont add to hashtable
            if (arrayListInt.size() > 0) {
                newCoordsInt.put(space, arrayListInt);
            }
        }
        return newCoordsInt;
    }

    /**
     * Join arraylists of GL and int coordinates into one
     *
     * @param coordsGL  the GL coordinates to join
     * @param coordsInt the integer coordiantes to join
     * @return an arraylist of both sets of coordinates joined into an integer arraylist
     */
    @NonNull
    @FieldCoordinates
    protected static ArrayList<int[]> joinCoordinateArrayLists(
            @Nullable @FieldCoordinates ArrayList<OpenGLMatrix> coordsGL,
            @Nullable @FieldCoordinates ArrayList<int[]> coordsInt) {
        ArrayList<int[]> joinedList = new ArrayList<>();

        if (coordsGL != null) {
            joinedList.addAll(convertGLtoInt(coordsGL));
        }
        if (coordsInt != null) {
            joinedList.addAll(coordsInt);
        }

        return joinedList;

    }

    // endregion merge coordinate forms

    // region coordinate form conversions

    /**
     * Converts an OpenGLMatrix array to an array of int coords
     *
     * @param matrices the matrix array to convert
     * @return the coordinates as integers
     */
    @NonNull
    @FieldCoordinates
    protected static ArrayList<int[]> convertGLtoInt(@NonNull @FieldCoordinates ArrayList<OpenGLMatrix> matrices) {
        ArrayList<int[]> intCoords = new ArrayList<>();

        for (OpenGLMatrix matrix : matrices) {
            intCoords.add(convertGLtoInt(matrix));
        }
        return intCoords;
    }

    /**
     * Converts OpenGLMatrix coordinates to integer coordinates
     *
     * @param matrix the OpenGLMatrix to convert
     * @return the matrix's x,y coordinates as an [x,y] integer array
     */
    @NonNull
    @FieldCoordinates
    protected static int[] convertGLtoInt(@NonNull @FieldCoordinates OpenGLMatrix matrix) {
        VectorF transformation = matrix.getTranslation();
        // x and y are 0 and 1, z is 2 but we dont need z
        int[] transformationArr = new int[2];
        transformationArr[0] = Math.round(transformation.get(0));
        transformationArr[1] = Math.round(transformation.get(1));

        return transformationArr;
    }


    // endregion coordinate form conversions


    // region misc utilities

    /**
     * Creates a deep copy of a hashtable of integer coordinates
     *
     * @param coordsInt the hashtable to deecopy
     * @return the deecopied hashtable. If the provided hashtable was null, a not null one is provided
     */
    @NonNull
    @AnyCoordinateRange
    protected static HashMap<SpaceMap.Space, ArrayList<int[]>> deepCopyCoordsInt(
            @Nullable @AnyCoordinateRange HashMap<SpaceMap.Space, ArrayList<int[]>> coordsInt) {

        HashMap<SpaceMap.Space, ArrayList<int[]>> coordsIntCloned = new HashMap<>();
        // deep copy hashtable
        if (coordsInt != null) {
            // loop through spaces with values
            for (SpaceMap.Space space : coordsInt.keySet()) {
                // make a new arraylist
                ArrayList<int[]> alDeepCopy = new ArrayList<>();
                coordsIntCloned.put(space, alDeepCopy);
                // deep copy coords arrays from arraylist
                for (int[] coordPairs : coordsInt.get(space)) {
                    alDeepCopy.add(coordPairs.clone());
                }
            }
        }

        return coordsIntCloned;
    }


    // endregion misc utilities


    // region matrix transformations


    /**
     * Directly translate a matrix by the given values.
     *
     * @param startPosition the start matrix
     * @param distances     the distances to translate the matrix by
     * @return the translated matrix
     */
    @NonNull
    @FieldCoordinates
    protected static OpenGLMatrix translateMatrix(@NonNull @FieldCoordinates OpenGLMatrix startPosition,
                                                  @NonNull @FieldCoordinates @DistanceValues double[] distances) {
//        Log.d(TAG, "Start position: " + VuforiaManager.format(startPosition));
//        Log.d(TAG, String.format("Transformations: straight: %s, side: %s", distances[0], distances[1]));
        // We need to get a the robot's translation and directly add our values onto it
        // why? idk, .translate() wasnt working and this was
        VectorF startTranslation = startPosition.getTranslation();
        // camera pos needs to be factored in here, probably instead of the robot position

        // minus straight -> forward
        float newX = startTranslation.get(0) - (float) distances[0];
        // minus side -> left
        float newY = startTranslation.get(1) - (float) distances[1];
        // make a new matrix with the recognitions position irrespective of the robot's angle
        OpenGLMatrix positionMatrix = OpenGLMatrix.translation(newX, newY, 0);
        return positionMatrix;
    }

    /**
     * Calculate the distance from a field coordinate to the camera
     * <p>
     * To convert from field position to distances, we need to find the distance from the camera
     * to a the position on the field. However, since the camera can rotate, we need to factor in
     * what points are actually in front of the robot. To do this, we can act as if the camera is a fixed
     * point, incapable of movement. Instead, when the camera "rotates," we can think of it as if
     * the world is actually rotating around the camera. So, we have our camera with no rotation,
     * and we have a position on the actual field. If we want this position to fit into our no-rotation
     * world, then we need to rotate it at the OPPOSITE of the camera's rotation in order to get it
     * to the position where it is relative to the camera.
     * <p>
     * Think of it like you're staring straight ahead, and you want to turn 90 degrees to the left
     * without actually turning. For that to happen, everything around you would have to turn
     * 90 degrees to the right. Then, the distances to everything in front of you are the actual
     * distances to you.
     * <p>
     * This just makes the math easier, because once the position is rotated, you can just
     * calculate the distance (aka translation) from the camera to the position and no longer have to care
     * about the rotation.
     *
     * @param cameraPosition   the position of the camera
     * @param fieldCoordinates the field coordinates to calculate the distance to. this should have angle
     *                         data as well as translation data
     * @return the distance to the camera, in field units
     */
    @NonNull
    @FieldCoordinates
    @DistanceValues
    protected static float[] calculateDistanceToCamera(@NonNull @FieldCoordinates OpenGLMatrix cameraPosition,
                                                       @NonNull @FieldCoordinates OpenGLMatrix fieldCoordinates) {
        // instead of the camera rotating around the world, the world rotates around the camera,
        // so the position needs to move opposite to the camera's angle for the camera to be "rotated"
        // so we rotate the position to the opposite of the camera's angle

        // first get rotation matrix for rotation opposite to robot's angle
        OpenGLMatrix oppositeRotationMatrix = getPointRotationMatrix(cameraPosition, true);

        // rotate position to negative the camera's angle
        fieldCoordinates = oppositeRotationMatrix.multiplied(fieldCoordinates);

        Log.d(FieldMap.TAG, "PM: Rotated to camera: " + VuforiaManager.format(fieldCoordinates));

        // find distance from position to camera
        VectorF cameraTranslation = cameraPosition.getTranslation();
        VectorF objectTranslation = fieldCoordinates.getTranslation();
        // robot: (0, 0) | object: (-75, -75)
        // dist = 0 - (-75) = 75 (forward or left)
        // this only makes sense because im saying subtracting by a positive moves you forwards
        // or left, while subtracting by a negative moves you back or right
        // if this doesnt make sense, look at the field coordinate map
        float straight = cameraTranslation.get(0) - objectTranslation.get(0);
        float side = cameraTranslation.get(1) - objectTranslation.get(1);

        Log.d(FieldMap.TAG, String.format("PM: Distances to camera {straight, side}: {%s, %s}", straight, side));
        return new float[]{straight, side};
    }

    /**
     * Get a rotation matrix for rotation to a position's angle around that position
     * <p>
     * A matrix which is first rotated (R) and then translated (T) can be represented as
     * T * R, so the transformations are applied in the reverse order of how they're applied
     * <p>
     * Matrix rotation around a point other than the origin:
     * http://www.euclideanspace.com/maths/geometry/affine/aroundPoint/matrix2d/
     * Basically, we translate the point to the origin, apply the rotation, then translate it
     * away from the origin
     *
     * @param rotationPoint   the point to rotate around
     * @param reverseRotation if true, rotate the opposite amount of the rotation point's rotation.
     *                        Otherwise, rotates to the rotation point's angle
     * @return the rotation matrix for rotation around the robot's position
     */
    @NonNull
    @FieldCoordinates
    protected static OpenGLMatrix getPointRotationMatrix(
            @NonNull @FieldCoordinates OpenGLMatrix rotationPoint, boolean reverseRotation) {
        VectorF pointTranslation = rotationPoint.getTranslation();
        OpenGLMatrix translateToOrigin = OpenGLMatrix.translation(-pointTranslation.get(0),
                -pointTranslation.get(1), 0);

        Orientation robotOrientation = Orientation.getOrientation(rotationPoint,
                AxesReference.EXTRINSIC, AxesOrder.XYZ, AngleUnit.DEGREES);
        // determine rotation direction
        // we only care about z rotation
        float zRotationAngle = (reverseRotation) ?
                -robotOrientation.thirdAngle : robotOrientation.thirdAngle;
        // get rotation matrix for z rotation
        OpenGLMatrix zRotation = OpenGLMatrix.rotation(
                AxesReference.EXTRINSIC,
                AxesOrder.XYZ,
                AngleUnit.DEGREES, 0, 0, zRotationAngle);


        OpenGLMatrix translateFromOrigin = OpenGLMatrix.translation(pointTranslation.get(0),
                pointTranslation.get(1), 0);
        // translate to origin, rotate around origin, translate back
        // multiplied in reverse order of application order
        return translateFromOrigin.multiplied(zRotation).multiplied(translateToOrigin);
    }


    /**
     * Convert a set of field distance values to field coordinates
     * <p>
     * This works by getting the distances to the recognition, and then rotating them around the camera
     * to correctly place them on the field. For example, if the camera is pointing 90 degrees,
     * then the object could be a meter in front of it, but since the camera is turned, we
     * can't just add 1 meter to the robot's position because then it'll end up too far down the
     * field. Instead, we need to find a coordinate that represents the direct distance from the
     * robot to the recognition, assuming the robot isn't turned at all. Then, we can rotate this
     * recognition by the camera's angle in order to get its accurate position on the field.
     *
     * @param cameraPosition the position of the camera, in field coordinates
     * @param distances      the distance values to use
     * @return the field coordinates of the distances based on the robot's rotation
     */
    @NonNull
    @FieldCoordinates
    protected static OpenGLMatrix convertDistancesToFieldPosition(
            @NonNull @FieldCoordinates OpenGLMatrix cameraPosition,
            @NonNull @FieldCoordinates @DistanceValues double[] distances) {

        Log.d(FieldMap.TAG, "Robot position: " + VuforiaManager.format(cameraPosition));
        OpenGLMatrix positionMatrix = translateMatrix(cameraPosition, distances);
        Log.d(FieldMap.TAG, String.format("Distances {straight, side}: {%s, %s}", distances[0], distances[1]));
        // get robot rotation
        Log.d(FieldMap.TAG, "Translated, unrotated: " + VuforiaManager.format(positionMatrix));
        // rotate the recognition position to correspond with the camera's angle
        OpenGLMatrix rotationMatrix = getPointRotationMatrix(cameraPosition, false);
        positionMatrix = rotationMatrix.multiplied(positionMatrix);

        Log.d(FieldMap.TAG, "Rotated: " + VuforiaManager.format(positionMatrix));
        return positionMatrix;
    }

    public static String rectToString(RectF rect) {
        return String.format("Top: %s, Bottom: %s, Left: %s, Right: %s, Area: %s", rect.top, rect.bottom,
                rect.left, rect.right, rect.width() * rect.height());
    }

    public static String rectToString(Rect rect) {
        return String.format("Top: %s, Bottom: %s, Left: %s, Right: %s, Area: %s", rect.top, rect.bottom,
                rect.left, rect.right, rect.width() * rect.height());
    }


    // endregion matrix transformations


}

