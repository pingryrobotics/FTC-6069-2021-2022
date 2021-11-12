package opmodes;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import mechanisms.Carousel;
import mechanisms.DriveControl;
import mechanisms.Intake;
import mechanisms.LinearSlide;
import teamcode.GamepadController;
import teamcode.GamepadController.ButtonState;
import teamcode.GamepadController.ToggleButton;



@TeleOp(name="Drive: Drive Control OpMode", group="Testing")
public class DriveControlOpMode extends OpMode {
    // tag is used in logcat logs (Log.d()) to identify where the log is coming from
    // logcat is basically like System.out.print (standard output) except through adb
    private static final String TAG = "teamcode.drive-opmode"; // put the name of the opmode

    // put any outside classes you need to use here
    private GamepadController movementController;
	private GamepadController mechanismController;
    private DriveControl driveControl;
	private Intake intake;
	private LinearSlide linearSlide;
	private Carousel carousel;
    private double velocity = 0;
	private int direc = 1;
	private int offsetAngle;


    // code to run once when driver hits init on phone
    @Override
    public void init() {
        movementController = new GamepadController(gamepad1);
		mechanismController = new GamepadController(gamepad2);
        driveControl = new DriveControl(hardwareMap);
		intake = new Intake(hardwareMap);
		linearSlide = new LinearSlide(hardwareMap);
		carousel = new Carousel(hardwareMap);
		offsetAngle = 0;

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


        telemetry.addData("velocity", velocity);

        // button states need to be updated each loop for controls to work
        movementController.updateButtonStates();
		mechanismController.updateButtonStates();

		// polarmove calculations
		double leftStickX = movementController.getButtonState(GamepadController.FloatButton.LEFT_STICK_X);
		double leftStickY = movementController.getButtonState(GamepadController.FloatButton.LEFT_STICK_Y);
		double rightStickX = movementController.getButtonState(GamepadController.FloatButton.RIGHT_STICK_X);
		double rightStickY = movementController.getButtonState(GamepadController.FloatButton.RIGHT_STICK_Y);

		double speed = 0.2;
		//if(gamepad1.right_trigger > 0.5){
		//    speed += (1-speed)*(2*(gamepad1.right_trigger - 0.5));
		//}

		double magnitude = Math.hypot(-gamepad1.left_stick_x, gamepad1.left_stick_y);
		double robotAngle = Math.atan2(gamepad1.left_stick_y, -gamepad1.left_stick_x) - Math.PI / 4;
		telemetry.addData("robot angle", robotAngle);
		robotAngle += offsetAngle / 180.0 * Math.PI;
		double rightX = -gamepad1.right_stick_x;
		driveControl.polarMove(robotAngle, rightX, 0.5 * direc * speed * magnitude);

        // do something when A is pressed
        if (movementController.getButtonState(ToggleButton.A) == ButtonState.KEY_DOWN) {
            driveControl.runAtSpeed(velocity);
        }

        if (movementController.getButtonState(ToggleButton.B) == ButtonState.KEY_DOWN) {
            driveControl.runAtSpeed(0);
        }

        if (movementController.getButtonState(ToggleButton.DPAD_UP) == ButtonState.KEY_DOWN) {
            if (velocity < 1)
                velocity += .1;
        }

        if (movementController.getButtonState(ToggleButton.DPAD_DOWN) == ButtonState.KEY_DOWN) {
            if (velocity > -1)
                velocity -= .1;
        }

        if (movementController.getButtonState(ToggleButton.X) == ButtonState.KEY_DOWN) {
            driveControl.moveXDist(12, .5);
        }

		if (movementController.getButtonState(ToggleButton.Y) == ButtonState.KEY_DOWN) { // reverse robot
			direc *= -1;
		}


		// mechanismController button state executions

		// left trigger: intake goes while pressed
		if (mechanismController.getButtonState(ToggleButton.LEFT_TRIGGER) == ButtonState.KEY_DOWN) {
			intake.intakeIn();
		} else if (mechanismController.getButtonState(ToggleButton.LEFT_TRIGGER) == ButtonState.KEY_UP) {
			intake.stop();
		}
		// right trigger: intake reverses while pressed
		if (mechanismController.getButtonState(ToggleButton.RIGHT_TRIGGER) == ButtonState.KEY_DOWN) {
			intake.intakeOut();
		} else if (mechanismController.getButtonState(ToggleButton.RIGHT_TRIGGER) == ButtonState.KEY_UP) {
			intake.stop();
		}

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

		// dpad down: linearslide goes to first level
		if (mechanismController.getButtonState(ToggleButton.DPAD_DOWN) == ButtonState.KEY_DOWN) {
			linearSlide.level1();
		}

		// dpad left: linearslide goes to second level
		if (mechanismController.getButtonState(ToggleButton.DPAD_LEFT) == ButtonState.KEY_DOWN) {
			linearSlide.level2();
		}

		// dpad up: linearslide goes to third level
		if (mechanismController.getButtonState(ToggleButton.DPAD_UP) == ButtonState.KEY_DOWN) {
			linearSlide.level3();
		}

		// X button; linear slide dumps and then undumps once it's pressed
		if (mechanismController.getButtonState(ToggleButton.X) == ButtonState.KEY_DOWN) {
			linearSlide.dump();
//			sleep(1000);
			linearSlide.undump();
		}

		// A button: carousel spins clockwise while button is pressed
		if (mechanismController.getButtonState(ToggleButton.A) == ButtonState.KEY_DOWN) {
			carousel.spin();
		} else if (mechanismController.getButtonState(ToggleButton.A) == ButtonState.KEY_UP) {
			carousel.stop();
		}

		// B button: carousel spins counterclockwise while button is pressed
		if (mechanismController.getButtonState(ToggleButton.B) == ButtonState.KEY_DOWN) {
//			carousel.spinReverse();
		} else if (mechanismController.getButtonState(ToggleButton.B) == ButtonState.KEY_UP) {
			carousel.stop();
		}

    }


    /*
     * Code to run ONCE after the driver hits STOP
     */
    @Override
    public void stop() {
    }


}
