package opmodes_testing;

import android.util.Log;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import teamcode.GamepadController;

import teamcode.GamepadController.ToggleButton;
import teamcode.GamepadController.ButtonState;
import teamcode.GamepadController.FloatButton;


@TeleOp(name="Template: Template OpMode", group="Testing")
public class TemplateOpMode extends OpMode {
    // tag is used in logcat logs (Log.d()) to identify where the log is coming from
    // logcat is basically like System.out.print (standard output) except through adb
    private static final String TAG = "teamcode.test_opmode"; // put the name of the opmode

    // put any outside classes you need to use here
    private GamepadController movementController;
    private GamepadController mechanismController;


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

        // do something when A is pressed
        if (movementController.getButtonState(ToggleButton.A) == ButtonState.KEY_DOWN) {
            Log.d(TAG, "button a pressed");
        }

        // do different things depending on the button's state
        // dont forget to break
        switch (movementController.getButtonState(ToggleButton.B)) {
            case KEY_DOWN: // code to run when button is pressed down
                Log.d(TAG, "button b pressed");
                break;
            case KEY_HOLD: // code to run while button is held down
                Log.d(TAG, "button b held");
                break;
            case KEY_UP: // code to run when b is released
                Log.d(TAG, "button b released");
                break;
            case KEY_INACTIVE: // code to run while nothing is happening with button
                break;
        }

        // get joystick positions
        float rightStickX = mechanismController.getButtonState(FloatButton.RIGHT_STICK_X);
        float rightStickY = mechanismController.getButtonState(FloatButton.RIGHT_STICK_Y);
    }


    /*
     * Code to run ONCE after the driver hits STOP
     */
    @Override
    public void stop() {
    }


}
