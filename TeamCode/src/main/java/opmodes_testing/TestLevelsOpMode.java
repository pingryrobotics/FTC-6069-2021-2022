package opmodes_testing;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

import mechanisms.LinearSlide;
import teamcode.GamepadController;
import teamcode.GamepadController.ButtonState;
import teamcode.GamepadController.ToggleButton;


@TeleOp(name="Level testing: Testing levels OpMode", group="Testing")
public class TestLevelsOpMode extends OpMode {
    // tag is used in logcat logs (Log.d()) to identify where the log is coming from
    // logcat is basically like System.out.print (standard output) except through adb
    private static final String TAG = "teamcode.test_opmode"; // put the name of the opmode

    // put any outside classes you need to use here
    private GamepadController movementController;
    private GamepadController mechanismController;
    private LinearSlide linearSlide;

    private DcMotor slideMotor;


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
        linearSlide = new LinearSlide(hardwareMap, telemetry);
        slideMotor = linearSlide.getSlideMotor();
//        slideMotor.setDirection(DcMotorSimple.Direction.REVERSE);
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


        telemetry.addData("motor position", slideMotor.getCurrentPosition());
        telemetry.addData("motor target", slideMotor.getTargetPosition());

        // button states need to be updated each loop for controls to work
        movementController.updateButtonStates();
        mechanismController.updateButtonStates();

        if (movementController.getButtonState(ToggleButton.START_BUTTON) == ButtonState.KEY_DOWN) {
            linearSlide.getSlideMotor().setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            linearSlide.getSlideMotor().setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }


        if (movementController.getButtonState(ToggleButton.DPAD_LEFT) == ButtonState.KEY_HOLD) {
            linearSlide.dump();
        }

        if (movementController.getButtonState(ToggleButton.DPAD_RIGHT) == ButtonState.KEY_HOLD) {
            linearSlide.undump();
        }

        if (movementController.getButtonState(ToggleButton.RIGHT_BUMPER) == ButtonState.KEY_HOLD) {
            linearSlide.retract();
        } else if (movementController.getButtonState(ToggleButton.RIGHT_BUMPER) == ButtonState.KEY_UP) {
            linearSlide.stop();
        }

        if (movementController.getButtonState(ToggleButton.LEFT_BUMPER) == ButtonState.KEY_HOLD) {
            linearSlide.extend();
        } else if (movementController.getButtonState(ToggleButton.LEFT_BUMPER) == ButtonState.KEY_UP) {
            linearSlide.stop();
        }

        if (movementController.getButtonState(ToggleButton.DPAD_UP) == ButtonState.KEY_DOWN) {
            slideMotor.setTargetPosition(slideMotor.getCurrentPosition()+50);
        }

        if (movementController.getButtonState(ToggleButton.DPAD_DOWN) == ButtonState.KEY_DOWN) {
            slideMotor.setTargetPosition(slideMotor.getCurrentPosition()-50);
        }

        if (movementController.getButtonState(ToggleButton.DPAD_DOWN) == ButtonState.KEY_DOWN) {
            slideMotor.setTargetPosition(slideMotor.getCurrentPosition()-50);
        }

        if (movementController.getButtonState(ToggleButton.A) == ButtonState.KEY_DOWN) {
            linearSlide.level0();
        }

        if (movementController.getButtonState(ToggleButton.B) == ButtonState.KEY_DOWN) {
            linearSlide.level1();
        }

        if (movementController.getButtonState(ToggleButton.Y) == ButtonState.KEY_DOWN) {
            linearSlide.level2();
        }

        if (movementController.getButtonState(ToggleButton.X) == ButtonState.KEY_DOWN) {
            linearSlide.level3();
        }

        if (movementController.getButtonState(ToggleButton.RIGHT_TRIGGER) == ButtonState.KEY_DOWN) {
            linearSlide.levelCap();
        }


    }


    /*
     * Code to run ONCE after the driver hits STOP
     */
    @Override
    public void stop() {
    }


}
