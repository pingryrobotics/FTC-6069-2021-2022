package display;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.teamcode.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

import localization.FieldMap;
import localization.SpaceMap;

public class Visuals {

    private static final File captureDirectory = AppUtil.ROBOT_DATA_DIR;
    private static final String TAG = "vuf.test.visuals";

    // no instances
    private Visuals() {}


    /**
     * Loads a bitmap from a file in a resource directory
     * The file should have a path in format:
     *
     * this function is actually just used to test crashing the robot
     *
     * "/{resource directory}/{pathtofile}.{fileextension}"
     * @return the bitmap
     */
//    @Nullable
//    public static Bitmap loadImageBitmap() {
////        ClassLoader classLoader = getClass().getClassLoader();
////        File file = new File(classLoader.getResource("/res/raw/coordinate_system.png").getFile());
//
//        InputStream inputStream = AppUtil.getDefContext().getResources().openRawResource(R.raw.coordinate_system);
//
////        File file = new File(classLoader.getResource(fileName).getFile());
////        Log.d(TAG, (String.format("File found? %s\n File location: %s", file.exists(), file.toString())));
//
//        return BitmapFactory.decodeStream(inputStream);
//    }

    /**
     * Saves a bitmap to the robot's data directory as a png
     * Saves to "/storage/emulated/0/FIRST/data/{filename}"
     * Saved files can be accessed through android studio's device file explorer
     * @param bitmap the bitmap to save
     * @param filename The name of the file, not including the file extension
     */
    public static void captureBitmapToFile(@Nullable Bitmap bitmap, @NonNull String filename) {
        if (bitmap != null) {
            File file = new File(captureDirectory, String.format("%s.png", filename));
            Objects.requireNonNull(file.getParentFile()).mkdirs();

            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                Log.d(TAG, String.format("Image saved successfully to %s", file));
            } catch (IOException e) {
                Log.d(TAG, "image save failed");
                Log.d(TAG, e.toString());
            }
        } else {
            Log.e(TAG, "invalid bitmap from filename " + filename);
        }
    }


    /**
     * Converts a color array to a bitmap
     * @param colorArray A 1d array of colors
     * @param yWidth The width of the image/bitmap
     * @param xHeight The height of the bitmap
     * @return A bitmap from the color array
     */
    @NonNull
    private static Bitmap colorsToBitmap(@ColorInt @NonNull int[] colorArray, int yWidth, int xHeight) {
        Bitmap bitmap = Bitmap.createBitmap(colorArray, yWidth, xHeight, Bitmap.Config.ARGB_8888);
        Log.d(TAG, "converted colors to bitmap");
        Log.d(TAG, "success");
        return bitmap;
    }

    /**
     * Converts a color array to a bitmap
     * @param colorArray A 1d array of colors
     * @param yWidth The width of the image/bitmap
     * @param xHeight The height of the bitmap
     * @return A bitmap from the color array
     */
    @NonNull
    private static Bitmap colorsToBitmap(@ColorInt @NonNull int[] colorArray, int yWidth, int xHeight, int stride) {
        Bitmap bitmap = Bitmap.createBitmap(colorArray, 0, stride, yWidth, xHeight, Bitmap.Config.ARGB_8888);
        Log.d(TAG, "converted colors to bitmap");
        Log.d(TAG, "success");
        return bitmap;
    }


    /**
     * Converts a FieldMap space array to an array of colors
     * @param spaceMap the spacemap to convert
     * @return A 1d array of the colors corrosponding to pixels on a bitmap
     */
    @NonNull
    private static int[] spaceArrayToColors(@NonNull SpaceMap.Space[][] spaceMap) {

        int rows = spaceMap.length;
        int cols = spaceMap[0].length;
        // make 1d array with as many elements as the field map
        int[] intArr = new int[rows * cols];

        int counter = 0;
        // add each space to the color array with the ColorInt instead of the Space
        for (SpaceMap.Space[] space : spaceMap) {
            for (int c = 0; c < cols; c++) {
                intArr[counter++] = space[c].getColor();
            }
        }
        return intArr;
    }

    /**
     * Converts a spacemap to a bitmap
     * @param spaceMap the spacemap to convert
     * @return the bitmap
     */
    public static Bitmap spaceMapToBitmap(SpaceMap.Space[][] spaceMap) {
        int[] colors = spaceArrayToColors(spaceMap);
        return colorsToBitmap(colors, spaceMap.length, spaceMap[0].length);
    }

    /**
     * Converts a fieldmap to an image
     * First converts the fieldmap's space array to a 1d color array
     * this color array is then transformed into a bitmap
     * the bitmap is then saved to the robot's data directory
     * @param fieldMap the fieldmap to save to an image
     * @param filename the images filename
     */
    public static void fieldMapToImage(@NonNull FieldMap fieldMap, @NonNull String filename) {
        SpaceMap.Space[][] spaceMap = fieldMap.getSpaceMap().getRawMap();
        int xHeight = spaceMap.length;
        int yWidth = spaceMap[0].length;

        int[] colorArr = spaceArrayToColors(spaceMap);
        Bitmap bitmap = colorsToBitmap(colorArr, yWidth, xHeight);
        captureBitmapToFile(bitmap, filename);
    }

    /**
     * Converts a fieldMap's spaceMap to an image
     * First converts the space array to a 1d color array
     * this color array is then transformed into a bitmap
     * the bitmap is then saved to the robot's data directory
     * @param spaceMap the fieldmap to save to an image
     * @param filename the images filename
     */
    public static void fieldMapToImage(@NonNull SpaceMap.Space[][] spaceMap, @NonNull String filename) {
        int xHeight = spaceMap.length;
        int yWidth = spaceMap[0].length;

        int[] colorArr = spaceArrayToColors(spaceMap);
        Bitmap bitmap = colorsToBitmap(colorArr, yWidth, xHeight);
        captureBitmapToFile(bitmap, filename);
    }

}
