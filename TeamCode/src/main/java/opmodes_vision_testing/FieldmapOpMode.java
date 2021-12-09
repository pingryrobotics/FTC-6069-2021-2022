package opmodes_vision_testing;

import android.annotation.SuppressLint;
import android.util.Log;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import teamcode.GamepadController.ButtonState;
import teamcode.GamepadController.ToggleButton;

import java.util.ArrayList;
import java.util.HashMap;

import localization.FieldMap;
import localization.SpaceMap;
import localization.VuforiaManager;
import teamcode.GamepadController;



@SuppressWarnings("FeatureEnvy")
@TeleOp(name="Fieldmap: Fieldmap OpMode", group="Testing")
public class FieldmapOpMode extends OpMode {
    // field declarations
    private static final String TAG = "vuf.test.tfOpMode";
    private GamepadController movementController;
    private GamepadController mechanismController;
    private static final long nanoToMilli = 1000000;

    private VuforiaManager vuforiaManager;
    private FieldMap fieldMap;
    private final double inchesToMM = 25.4;
    private final double toCameraCenter = 0.5; // c920
    private final double cameraPlatform = 10; // inches
    private final double cameraHeight = (cameraPlatform + toCameraCenter) * inchesToMM;

    private static final int fieldLength = 3660;


    @Override
    public void init() {
        movementController = new GamepadController(gamepad1);
        vuforiaManager = new VuforiaManager(hardwareMap, fieldLength, true);

        HashMap<SpaceMap.Space, ArrayList<OpenGLMatrix>> staticCoordsGL = new HashMap<>();
        staticCoordsGL.put(SpaceMap.Space.IMAGE_TARGET, vuforiaManager.getTrackablePositions());
        fieldMap = new FieldMap(fieldLength, staticCoordsGL, null,true);
    }

    @Override
    public void init_loop() {
    }

    @Override
    public void start() {
    }

    @Override
    public void loop() {

        runControls();
    }

    /**
     * Does controls for the gamepads
     */
    @SuppressLint("DefaultLocale")
    public void runControls() {

        movementController.updateButtonStates();
//        // update map
        OpenGLMatrix robotPosition = vuforiaManager.getUpdatedRobotPosition();
        if (robotPosition != null) {
            long startTime = System.nanoTime();
            fieldMap.update(robotPosition);
            long duration = (System.nanoTime() - startTime)/nanoToMilli;
            Log.d(TAG, "Finished updating map in " + duration + " ms");
            telemetry.addData("Robot position", VuforiaManager.format(robotPosition));
        }

        // get trackable status
        for (VuforiaManager.ImageTarget trackable : VuforiaManager.ImageTarget.cachedValues()) {
            telemetry.addData(trackable.name(), vuforiaManager.isTrackableVisible(trackable) ? "Visible" : "Not Visible");
        }

        if (movementController.getButtonState(ToggleButton.A) == ButtonState.KEY_DOWN) {
            OpenGLMatrix location = vuforiaManager.getUpdatedRobotPosition();
            if (location != null) {
                long startTime = System.nanoTime();
                fieldMap.update(location);
                long duration = (System.nanoTime() - startTime)/nanoToMilli;
                Log.i(TAG, "Finished updating map in " + duration + " ms");
            } else {
                Log.d(TAG, "no location");
            }

        }

        if (movementController.getButtonState(ToggleButton.B) == ButtonState.KEY_DOWN) {
            OpenGLMatrix location = vuforiaManager.getUpdatedRobotPosition();
            if (location != null) {
                Log.d(TAG, "printing transformations");
                fieldMap.setRobotPosition(location);
            }
        }

        if (movementController.getButtonState(ToggleButton.DPAD_UP) == ButtonState.KEY_DOWN) {
            fieldMap.getSpaceMap().catalog();
        }

        if (movementController.getButtonState(ToggleButton.X) == ButtonState.KEY_DOWN) {
            fieldMap.updateDisplay();
        }


        telemetry.update();
    }


    /*
     * Code to run ONCE after the driver hits STOP
     */
    @Override
    public void stop() {
    }


}
