package localization;

import android.graphics.Color;
import android.util.Log;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import annotations.AnyCoordinateRange;
import annotations.MatrixCoordinates;

/**
 * The ftc field can be represented as a set of squares of a fixed size containing various
 * types of spaces.
 * The SpaceMap class is a way of organizing this map and making various operations
 * simpler, faster, and more consistent.
 */
public class SpaceMap {
    private static final String TAG = "vuf.test.spacemap";
    private final Space[][] spaceMap;
    public final int height;
    public final int width;
    private final int minRange;
    private final int maxRange;


    /**
     * Create new spacemap with specified width and height and fill it with clear spots
     * @param xHeight the height of the spacemap
     * @param yWidth the width of the spacemap
     */
    public SpaceMap(int xHeight, int yWidth) {
        spaceMap = new Space[xHeight][yWidth];
        height = xHeight;
        width = yWidth;
        this.minRange = 0;
        this.maxRange = xHeight-1;
        // fill the array with clear spots
        for(Space[] row : spaceMap) {
            Arrays.fill(row, Space.CLEAR);
        }
    }

    /**
     * Create new spacemap with specified width and height and fill it with clear spots
     * Also provide a minimum and maximum range value to round coordinates down to
     * @param xHeight the height of the spacemap
     * @param yWidth the width of the spacemap
     */
    public SpaceMap(int xHeight, int yWidth, int minRange, int maxRange) {
        spaceMap = new Space[xHeight][yWidth];
        height = xHeight;
        width = yWidth;
        minRange = Math.max(0, minRange);
        maxRange = Math.min(height-1, maxRange);
        this.minRange = minRange;
        this.maxRange = maxRange;

        // fill the array with clear spots
        for(Space[] row : spaceMap) {
            Arrays.fill(row, Space.CLEAR);
        }
    }


    /**
     * Create a new spacemap as a deep copy of another spacemap
     * @param otherMap the spacemap to deepcopy
     */
    public SpaceMap(@NonNull SpaceMap otherMap) {
        this.spaceMap = getSpaceMapDC(otherMap.spaceMap);
        height = otherMap.height;
        width = otherMap.width;
        minRange = otherMap.minRange;
        maxRange = otherMap.maxRange;
    }



    @NonNull
    public Space[][] getRawMap() { return spaceMap; }

    /**
     * Find differences between two space maps
     * Compares local spacemap to new spacemap
     * @param otherMap The other spacemap to compare to
     * @param requireStateChange if true, only spaces where the passable status is changed are included
     * @return an arraylist of all different coordinates
     */
    @NonNull
    @MatrixCoordinates
    public ArrayList<int[]> getDifferences(@NonNull SpaceMap otherMap, boolean requireStateChange) {
        ArrayList<int[]> diffList = new ArrayList<>();

        for(int r = 0; r < spaceMap.length; r++) {
            Space[] cRow = spaceMap[r];
            Space[] oRow = otherMap.spaceMap[r];
            if (!Arrays.equals(cRow, oRow)) {
                for(int c = 0; c < spaceMap[0].length; c++) {
                    if (cRow[c] != oRow[c] && // if theyre not equal
                            // and if a state change is required, then theyre different
                            (!requireStateChange || oRow[c].passable != cRow[c].passable)) {
                        diffList.add(new int[] {r, c});
                    }
                }
            }
        }
        return diffList;
    }


    /**
     * Gets a deep copy of the space array
     * @return the a deep copy of the space array
     */
    @NonNull
    public Space[][] getSpaceMapDC(@NonNull Space[][] spaceMap) {
        Space[][] deepCopy = new Space[spaceMap.length][];
        for (int r = 0; r < spaceMap.length; r++) {
            deepCopy[r] = spaceMap[r].clone();
        }
        return deepCopy;
    }

    /**
     * Rounds coordinates to within the spacemap's bounds
     * @param coords the coordinates to round
     * @return an integer array of coordinates within the spacemaps bounds
     */
    @NonNull
    @MatrixCoordinates
    public int[] roundToBounds(@AnyCoordinateRange int[] coords) {
        int[] newCoords = (coords == null) ? new int[2] : coords.clone();
        // get median value from 0, field length, and xyPair
        newCoords[0] = Math.max(Math.min(height-1,0), Math.min(Math.max(height-1,0),newCoords[0]));
        newCoords[1] = Math.max(Math.min(width-1,0), Math.min(Math.max(width-1,0),newCoords[1]));
        return newCoords;
    }

    /**
     * Rounds coordinates to within the given range within the spacemap's range
     * If the provided range is out of bounds, it will be rounded down
     * @param coords the coordinates to round
     * @return an integer array of coordinates within the spacemaps range
     */
    @NonNull
    @MatrixCoordinates
    public int[] roundToRange(@AnyCoordinateRange int[] coords) {
        int[] newCoords = (coords == null) ? new int[2] : coords.clone();
        // get median value from max, min, and coordinates
        newCoords[0] = Math.max(Math.min(maxRange, minRange), Math.min(Math.max(maxRange, minRange),newCoords[0]));
        newCoords[1] = Math.max(Math.min(maxRange, minRange), Math.min(Math.max(maxRange, minRange),newCoords[1]));
        return newCoords;
    }

    /**
     * Apply a function to every element of the space map
     * @param allocateNew if true, a new array will be allocated for each coordinate pair per loop,
     *                    so the array can be saved in lists etc. Otherwise, one array will be
     *                    changed to reflect the new coordinates each loop. Only set to true if
     *                    the coordinate array needs to be maintained after use.
     * @param consumer the consumer to apply. the consumer takes in an array of coordinates in
     *                 the form {row, column}
     */
    private void applyToSpaceMap(boolean allocateNew, Consumer<int[]> consumer) {
        int[] coords = new int[2]; // create so we dont have to reallocate
        for (int r = 0; r < spaceMap.length; r++) {
            for (int c = 0; c < spaceMap[0].length; c++) {
                coords[0] = r;
                coords[1] = c;
                int[] newCoords = (allocateNew) ? coords.clone() : coords;
                consumer.accept(newCoords);
            }
        }
    }


    /**
     * Function for testing how long looping through the whole map takes
     * it took about 4 ms when I ran it, so not really worried about time there
     */
    public void catalog() {
        long startTime = System.nanoTime();
        Map<Space, ArrayList<int[]>> hashMap = new HashMap<>();
        for (int r = 0; r < spaceMap.length; r++) {
            for (int c = 0; c < spaceMap[0].length; c++) {
                Space space = spaceMap[r][c];
                if (space != Space.CLEAR) {
                    hashMap.putIfAbsent(space, new ArrayList<>());
                    Objects.requireNonNull(hashMap.get(space)).add(new int[]{r, c});
                }
            }
        }

        long duration = (System.nanoTime() - startTime)/ CoordinateUtils.nanoToMilli;
        Log.d(TAG, "Finished cataloging in " + duration + " ms");
    }

    // region adding/removing


    /**
     * Sets the border of the spacemap to walls
     */
    public void setWalls() {
        // set top and bottom row
        Arrays.fill(spaceMap[0], Space.WALL);
        Arrays.fill(spaceMap[height-1], Space.WALL);
        // set left and right column
        for(Space[] row : spaceMap) {
            row[0] = Space.WALL;
            row[width-1] = Space.WALL;
        }
    }


    /**
     * Set a coordinate to a space
     * @param newSpace the new space to set
     * @param coords the coordinates to replace
     * @param allowStatic if true, allows adding and removing static spaces. Otherwise, attempts to
     *                    remove existing static spaces or add new ones will be ignored.
     */
    public void setSpace(@NonNull Space newSpace, @NonNull @MatrixCoordinates int[] coords, boolean allowStatic) {
        Space oldSpace = getSpace(coords);
        if (!(newSpace.isStatic() || oldSpace.isStatic()) || allowStatic) {
            spaceMap[coords[0]][coords[1]] = newSpace;
        }
    }

    /**
     * Set a coordinate to a space
     * @param newSpace the new space to set
     * @param coords the coordinates to replace
     * @param allowStatic if true, allows adding and removing static spaces. Otherwise, attempts to
     *                    remove existing static spaces or add new ones will be ignored.
     */
    public void setSpace(Space newSpace, @NonNull @MatrixCoordinates List<int[]> coords, boolean allowStatic) {
        // if the space is static and editing statics isnt allowed, exit
        if (newSpace.isStatic() && !allowStatic) {
            return;
        }
        for (int[] xyPair : coords) {
            setSpace(newSpace, xyPair, false);
        }
    }

    /**
     * Set a list of coordinates to spaces
     * @param coordMap a hashmap of spaces and coordinates to set
     * @param allowStatic if true, allows adding and removing static spaces. Otherwise, attempts to
     *                    remove existing static spaces or add new ones will be ignored.
     */
    public void setSpace(@NonNull @MatrixCoordinates HashMap<Space, ArrayList<int[]>> coordMap, boolean allowStatic) {
        for (Space space : coordMap.keySet()) {
            // if its static and editing statics isnt allowed, pass
            if (space.isStatic() && !allowStatic)
                continue;

            List<int[]> coords = coordMap.get(space);
            if (coords != null)
                setSpace(space, coords, allowStatic);
        }
    }

    /**
     * Add a coordinate to a space, but only if the overridden space is clear
     * @param newSpace the new space to add
     * @param coords the coordinates to add
     * @param allowStatic if true, allows adding and removing static spaces. Otherwise, attempts to
     *                    remove existing static spaces or add new ones will be ignored.
     */
    public void addSpace(@NonNull Space newSpace,
                         @NonNull @MatrixCoordinates int[] coords,
                         boolean allowStatic) {
        if (getSpace(coords) == Space.CLEAR) {
            setSpace(newSpace, coords, allowStatic);
        }
    }

    /**
     * Add a list of coordinate to a space, but only if the overridden space is clear
     * @param newSpace the new space to add
     * @param coords the coordinates to add
     * @param allowStatic if true, allows adding and removing static spaces. Otherwise, attempts to
     *                    remove existing static spaces or add new ones will be ignored.
     */
    public void addSpace(Space newSpace,
                         @NonNull @MatrixCoordinates List<int[]> coords,
                         boolean allowStatic) {
        for (int[] xyPair : coords) {
            addSpace(newSpace, xyPair, allowStatic);
        }
    }

    /**
     * Add a map of coordinate to spaces, but only if the overridden space is clear
     * @param coordMap the map coordinates to add
     * @param allowStatic if true, allows adding and removing static spaces. Otherwise, attempts to
     *                    remove existing static spaces or add new ones will be ignored.
     */
    public void addSpace(@NonNull @MatrixCoordinates HashMap<Space, ArrayList<int[]>> coordMap, boolean allowStatic) {
        for (Space space : coordMap.keySet()) {
            List<int[]> coords = coordMap.get(space);
            if (coords != null)
                addSpace(space, coords, allowStatic);
        }
    }

    /**
     * Clears a list of coordinates from the spacemap by setting it to clear
     * @param coordsList the list of coordinates to clear
     * @param allowStatic if true, allows adding and removing static spaces. Otherwise, attempts to
     *                    remove existing static spaces or add new ones will be ignored.
     */
    public void clearSpace(@NonNull @MatrixCoordinates List<int[]> coordsList,
                           boolean allowStatic) {
        setSpace(Space.CLEAR, coordsList, allowStatic);
    }

    /**
     * Clears all coordinates from the spacemap by setting them to clear
     * @param allowStatic if true, allows adding and removing static spaces. Otherwise, attempts to
     *                    remove existing static spaces or add new ones will be ignored.
     *                    If this is true, only non static spaces will be removed
     */
    public void clearSpace(boolean allowStatic) {
        // loop through all coords
        applyToSpaceMap(false, new Consumer<int[]>() {
            @Override
            public void accept(int[] value) {
                Space oldSpace = getSpace(value);
                // if editing statics is allowed or it isnt static, set to clear
                if (!oldSpace.isStatic() || allowStatic) {
                    setSpace(Space.CLEAR, value, allowStatic);
                }
            }
        });
    }


    /**
     * Clears all coordinates of a specified space from the spacemap by setting them to clear
     * @param allowStatic if true, allows adding and removing static spaces. Otherwise, attempts to
     *                    remove existing static spaces or add new ones will be ignored.
     *                    If this is true, only non static spaces will be removed
     */
    public void clearSpace(Space space, boolean allowStatic) {
        // if the target space is static and editing statics isnt allowed, pass
        if (space.isStatic() && !allowStatic)
            return;

        applyToSpaceMap(false, new Consumer<int[]>() {
            @Override
            public void accept(int[] value) {
                Space oldSpace = getSpace(value);
                // if the old space is the target space, set it to clear
                if (oldSpace == space) {
                    setSpace(Space.CLEAR, value, allowStatic);
                }
            }
        });
    }


    /**
     * Gets all coordinates with the provided space from the spacemap
     * @param space the space to get all coordinates for
     * @return a list of all coordinates with the provided space
     */
    public List<int[]> getSpace(Space space) {
        List<int[]> coordsList = new ArrayList<>();
        applyToSpaceMap(true, new Consumer<int[]>() {
            @Override
            public void accept(int[] value) {
                if (getSpace(value) == space) {
                    coordsList.add(value);
                }
            }
        });
        return coordsList;
    }



    /**
     * Get the space at a coordinate
     * @param coords the coordinate of the space to get, in matrix coordinates
     * @return the space at the coordiante
     */
    @NonNull
    public Space getSpace(@NonNull @MatrixCoordinates int[] coords) {
        return spaceMap[coords[0]][coords[1]];
    }

    /**
     * Sets the robot's position to the specified space
     * @param robotCoords the robot coordinates to set
     */
    public void setRobotPosition(@NonNull @MatrixCoordinates int[] robotCoords) {
        clearSpace(Space.ROBOT, true);
        setSpace(Space.ROBOT, robotCoords, true);
    }

    /**
     * Gets the robot position
     * @return the robot's position, or null if there's no position
     */
    @Nullable
    @MatrixCoordinates
    public int[] getRobotPosition() {
        List<int[]> positionList = getSpace(Space.ROBOT);
        if (!positionList.isEmpty()) {
            return positionList.get(0);
        }
        return null;
    }





    // endregion adding/removing


    /**
     * Spaces are types which represent each type of object on the map
     * Each space has a color which is only important for visualization
     * In pathfinding, the robot can move through certain spaces, but has to navigate around others
     * i.e it has to move around obstacles (obviously)
     *
     * Static filled spaces are completely static spots on the field that do not change for
     * any reason. Examples include walls, image targets, stationary game objects, etc
     * Static fills cannot be removed or overridden by any function after being placed
     * However, during placement, coordinates with conflicts may be overridden
     * Static fills should only be added ONCE, during initialization.
     *
     * Dynamic filled spaces are spaces filled with spots such as the robot, a temporary obstacle,
     * or any other value that is subject to change.
     * Dynamic coordinates can be added, removed, and overridden at any time
     *
     * The robot position is considered static since it can only move under certain circumstances.
     *
     */
    public enum Space {

        // the wall
        @ColorInt
        WALL(Color.BLACK, false, true),
        // the vuforia image targets
        @ColorInt
        IMAGE_TARGET(Color.CYAN, true, true),

        // clear spot
        @ColorInt
        CLEAR(Color.WHITE, true, false),
        // the robot
        @ColorInt
        ROBOT(Color.BLUE, true, true),
        // a field obstacle
        @ColorInt
        OBSTACLE(Color.BLACK, false, false),

        // a target location on the field
        @ColorInt
        TARGET_LOCATION(Color.GREEN, true, false),


        // pathfinding space values
        // only placed by pathfinding algorithms
        @ColorInt
        PF_PATH(Color.GRAY, true, false),
        @ColorInt
        PF_START(Color.BLUE, true, false),
        @ColorInt
        PF_END(Color.GREEN, true, false);


        private final int color;
        private final boolean passable;
        private final boolean isStaticSpace;

        /**
         * Initialize information for the space types
         * @param color the color that the space gets mapped to for visuals
         * @param passable whether the robot can navigate through the space or not
         */
        Space(int color, boolean passable, boolean isStaticSpace) {
            this.color = color;
            this.passable = passable;
            this.isStaticSpace = isStaticSpace;
        }

        public int getColor() {
            return this.color;
        }

        public boolean isPassable() {
            return this.passable;
        }

        public boolean isStatic() { return this.isStaticSpace; }

    }
}
