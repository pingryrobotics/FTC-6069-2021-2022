package opmodes_testing;

import static mechanisms.DriveControl.DriveAction.DriveType;

import android.util.Log;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import mechanisms.AutoQueue;
import mechanisms.DriveControl;
import teamcode.GamepadController;
import teamcode.GamepadController.ButtonState;
import teamcode.GamepadController.FloatButton;
import teamcode.GamepadController.ToggleButton;


@TeleOp(name="AutoTest: AutoQueue OpMode", group="Testing")
public class AutoQueueOpMode extends OpMode {
    // tag is used in logcat logs (Log.d()) to identify where the log is coming from
    // logcat is basically like System.out.print (standard output) except through adb
    private static final String TAG = "teamcode.test_opmode"; // put the name of the opmode

    // put any outside classes you need to use here
    private GamepadController movementController;
    private GamepadController mechanismController;

    private AutoQueue autoQueue;
    private DriveControl driveControl;


    // put any measurements here
    private final double inchesToMM = 25.4; // this is correct
    private final double toCameraCenter = 1.25; // inches from bottom of logitech c615 to actual camera
    private final double cameraPlatform = 10.5; // random value
    private final double cameraHeight = (cameraPlatform + toCameraCenter) * inchesToMM;
    private static final int fieldLength = 3660; // mm (this is correct)



    // code to run once when driver hits init on phone
    @Override
    public void init() {
        movementController = new GamepadController(gamepad1);
        mechanismController = new GamepadController(gamepad2);
        autoQueue = new AutoQueue();
        driveControl = new DriveControl(hardwareMap, telemetry);

    }

    // code to loop after init is pressed and before start is pressed
    @Override
    public void init_loop() {
    }

    // code to run once when driver hits start
    @Override
    public void start() {
    }

    // code to loop while opmode is running
    @Override
    public void loop() {

        runControls();


        // update telemetry at the end of the loop
        telemetry.update();
    }

    /**
     * Updates buttons and does controls when buttons are pressed
     */
    public void runControls() {

        telemetry.addData("caption", "value");

        // button states need to be updated each loop for controls to work
        movementController.updateButtonStates();
        mechanismController.updateButtonStates();
        autoQueue.updateQueue();

        // do something when A is pressed
        if (movementController.getButtonState(ToggleButton.DPAD_UP) == ButtonState.KEY_DOWN) {
            autoQueue.addAutoAction(driveControl.getForwardAction(12, .3));
        }
        if (movementController.getButtonState(ToggleButton.DPAD_DOWN) == ButtonState.KEY_DOWN) {
            autoQueue.addAutoAction(driveControl.getForwardAction(-12, .3));
        }
        if (movementController.getButtonState(ToggleButton.DPAD_LEFT) == ButtonState.KEY_DOWN) {
            autoQueue.addAutoAction(driveControl.getStrafeAction(-12, .3));
        }
        if (movementController.getButtonState(ToggleButton.DPAD_RIGHT) == ButtonState.KEY_DOWN) {
            autoQueue.addAutoAction(driveControl.getStrafeAction(12, .3));
        }

        if (movementController.getButtonState(ToggleButton.X) == ButtonState.KEY_DOWN) {
            autoQueue.addAutoAction(driveControl.getTurnAction(-90, .3));
        }

        if (movementController.getButtonState(ToggleButton.B) == ButtonState.KEY_DOWN) {
            autoQueue.addAutoAction(driveControl.getTurnAction(90, .3));
        }

    }


    /*
     * Code to run ONCE after the driver hits STOP
     */
    @Override
    public void stop() {
    }


}
