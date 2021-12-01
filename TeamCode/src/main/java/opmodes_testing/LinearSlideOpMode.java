package opmodes_testing;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

import mechanisms.Carousel;
import mechanisms.LinearSlide;
import teamcode.GamepadController;

import teamcode.GamepadController.ToggleButton;
import teamcode.GamepadController.ButtonState;


@TeleOp(name="Testing: Slide OpMode", group="Testing")

public class LinearSlideOpMode extends OpMode {
    // tag is used in logcat logs (Log.d()) to identify where the log is coming from
    // logcat is basically like System.out.print (standard output) except through adb
    private static final String TAG = "teamcode.test_opmode"; // put the name of the opmode

    // put any outside classes you need to use here
    private GamepadController mechanismController;
	private LinearSlide linearSlide;
	private Carousel carousel;


    // put any measurements here
    private final double inchesToMM = 25.4; // this is correct
    private final double toCameraCenter = 1.25; // inches from bottom of logitech c615 to actual camera
    private final double cameraPlatform = 10.5; // random value
    private final double cameraHeight = (cameraPlatform + toCameraCenter) * inchesToMM;
    private static final int fieldLength = 3660; // mm (this is correct)



    // code to run once when driver hits init on phone
    @Override
    public void init() {
        mechanismController = new GamepadController(gamepad1);
		linearSlide = new LinearSlide(hardwareMap);
		carousel = new Carousel(hardwareMap);
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
        mechanismController.updateButtonStates();

        // right bumper: linearslide extends while pressed
        if (mechanismController.getButtonState(ToggleButton.RIGHT_BUMPER) == ButtonState.KEY_DOWN) {
            linearSlide.extend();
        } else if (mechanismController.getButtonState(ToggleButton.RIGHT_BUMPER) == ButtonState.KEY_UP) {
            linearSlide.stop();
        }

        // left bumper: linearslide retracts while pressed
        if (mechanismController.getButtonState(ToggleButton.LEFT_BUMPER) == ButtonState.KEY_DOWN) {
            linearSlide.retract();
        } else if (mechanismController.getButtonState(ToggleButton.LEFT_BUMPER) == ButtonState.KEY_UP) {
            linearSlide.stop();
        }

        if (mechanismController.getButtonState(ToggleButton.B) == ButtonState.KEY_DOWN) {
            linearSlide.stop();
        }

		if (mechanismController.getButtonState(ToggleButton.X) == ButtonState.KEY_DOWN) {
            DcMotor slideMotor = linearSlide.getSlideMotor();
            slideMotor.setTargetPosition(0);
            slideMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            slideMotor.setPower(.3);
		}


        if (mechanismController.getButtonState(ToggleButton.A) == ButtonState.KEY_DOWN) {
            DcMotor slideMotor = linearSlide.getSlideMotor();
            slideMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            slideMotor.setTargetPosition(300);
            slideMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            slideMotor.setPower(.3);
        }




    }


    /*
     * Code to run ONCE after the driver hits STOP
     */
    @Override
    public void stop() {
    }


}
