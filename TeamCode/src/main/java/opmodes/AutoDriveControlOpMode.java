package opmodes;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

import mechanisms.Carousel;
import mechanisms.DriveControl;
import mechanisms.Intake;
import mechanisms.LinearSlide;
import teamcode.GamepadController;
import teamcode.GamepadController.ButtonState;
import teamcode.GamepadController.ToggleButton;



@TeleOp(name="Drive: Auto Drive Control OpMode", group="Testing")
public class AutoDriveControlOpMode extends OpMode {
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

    int ticks = -1;
    /**
     * Updates buttons and does controls when buttons are pressed
     */
    public void runControls() {


        telemetry.addData("velocity", velocity);

        // button states need to be updated each loop for controls to work
        movementController.updateButtonStates();
		mechanismController.updateButtonStates();

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

        if (movementController.getButtonState(ToggleButton.DPAD_LEFT) == ButtonState.KEY_DOWN) {
            driveControl.moveXDist(12, .5);
        }


        if (movementController.getButtonState(ToggleButton.Y) == ButtonState.KEY_DOWN) {
            driveControl.setStraightTarget(1000);
            driveControl.setStraightVelocity(.5);
            driveControl.setMotorMode(DcMotor.RunMode.RUN_TO_POSITION);

        }

        if (movementController.getButtonState(ToggleButton.X) == ButtonState.KEY_DOWN) {
            ticks = driveControl.moveYDist(12, .5);
            telemetry.addData("ticks", ticks);
        }

        if (movementController.getButtonState(ToggleButton.DPAD_RIGHT) == ButtonState.KEY_DOWN) {
            driveControl.setMotorMode(DcMotor.RunMode.RUN_USING_ENCODER);
            driveControl.gyroTurn(90, .5);
        }


    }


    /*
     * Code to run ONCE after the driver hits STOP
     */
    @Override
    public void stop() {
    }


}
