package opmodes_testing;

import android.util.Log;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;

import mechanisms.LinearSlide;
import teamcode.GamepadController;
import teamcode.GamepadController.ButtonState;
import teamcode.GamepadController.FloatButton;
import teamcode.GamepadController.ToggleButton;


@TeleOp(name="Testing: Servo Control OpMode", group="Testing")
public class ServoControlOpMode extends OpMode {
    // tag is used in logcat logs (Log.d()) to identify where the log is coming from
    // logcat is basically like System.out.print (standard output) except through adb
    private static final String TAG = "teamcode.test_opmode"; // put the name of the opmode

    // put any outside classes you need to use here
    private GamepadController movementController;
    private GamepadController mechanismController;

    private Servo targetServo;


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
        targetServo = hardwareMap.get(Servo.class, "bucketServo");
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
        // button states need to be updated each loop for controls to work
        movementController.updateButtonStates();
        mechanismController.updateButtonStates();

        runControls();


        // update telemetry at the end of the loop
        telemetry.update();
    }

    /**
     * Updates buttons and does controls when buttons are pressed
     */
    public void runControls() {


        telemetry.addData("caption", "value");


        // do something when A is pressed
        if (movementController.getButtonState(ToggleButton.A) == ButtonState.KEY_DOWN) {
            targetServo.setPosition(0);
        }

        if (movementController.getButtonState(ToggleButton.B) == ButtonState.KEY_DOWN) {
            targetServo.setPosition(1);
        }

        if (movementController.getButtonState(ToggleButton.X) == ButtonState.KEY_DOWN) {
            targetServo.getController().setServoPosition(targetServo.getPortNumber(), 0);
        }

        if (movementController.getButtonState(ToggleButton.Y) == ButtonState.KEY_DOWN) {
            targetServo.getController().setServoPosition(targetServo.getPortNumber(), 1);
        }



    }


    /*
     * Code to run ONCE after the driver hits STOP
     */
    @Override
    public void stop() {
    }


}
