package display;

import static android.view.View.VISIBLE;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import org.firstinspires.ftc.robotcore.internal.system.AppUtil;

/**
 * DisplaySource is a class for projecting a stream of bitmaps onto the robot's display
 */
public class DisplaySource {

    private final AppUtil appUtil = AppUtil.getInstance();
    private ImageView imageView;
    private final static String TAG = "vuf.test.displaysource";
    private final boolean activeDisplay;

    /**
     * Initialize the display source with a display id
     * the display id, also called the monitorViewIdParent, is the id of the
     * space on screen where the display will be shown. Look at the ftc samples
     * for examples on the display ids
     * @param displayId the display id
     */
    public DisplaySource(int displayId) {
        Log.d(TAG, "display id " + displayId);
        if (displayId != 0) {
            initImageView(displayId);
            activeDisplay = true;
        } else {
            activeDisplay = false;
        }
    }


    /**
     * Initializes an image view for displaying a live feed of frames to the robot
     * @param monitorViewIdParent the id of monitor view to use for displaying frames
     */
    private void initImageView(int monitorViewIdParent) {
        final Activity activity = appUtil.getRootActivity();
        final ViewGroup imageViewParent = activity.findViewById(monitorViewIdParent);

        if (imageViewParent != null) {
            appUtil.synchronousRunOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "Updating ui");
                    imageView = new ImageView(activity);
                    imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    imageView.setLayoutParams(new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT));
                    imageViewParent.addView(imageView);
                    imageViewParent.setVisibility(VISIBLE);
                }
            });
        } else {
            Log.d(TAG, "view parent null");
        }
    }

    /**
     * Updates the image view with a new image
     * @param bitmap the image to project
     */
    public void updateImageView(@NonNull final Bitmap bitmap) {
        if (activeDisplay) {
            appUtil.synchronousRunOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            imageView.setImageBitmap(bitmap);
                        }
                    });
        }
    }
}
