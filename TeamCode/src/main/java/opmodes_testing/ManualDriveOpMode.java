package opmodes_testing;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.Range;

import mechanisms.DriveControl;
import teamcode.GamepadController;


@TeleOp(name="Drive: Manual Drive OpMode", group="Testing")
public class ManualDriveOpMode extends OpMode {
    // tag is used in logcat logs (Log.d()) to identify where the log is coming from
    // logcat is basically like System.out.print (standard output) except through adb
    private static final String TAG = "teamcode.drive-opmode"; // put the name of the opmode

    // put any outside classes you need to use here
    private GamepadController movementController;
	private GamepadController mechanismController;
    private DriveControl driveControl;


    // code to run once when driver hits init on phone
    @Override
    public void init() {
        movementController = new GamepadController(gamepad1);
		mechanismController = new GamepadController(gamepad2);
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

        // button states need to be updated each loop for controls to work
        movementController.updateButtonStates();
		mechanismController.updateButtonStates();

        double theta = Math.atan2(-gamepad1.left_stick_y, gamepad1.left_stick_x)-90;
        double magnitude = Math.sqrt(Math.pow(gamepad1.left_stick_x, 2) + Math.pow(gamepad1.left_stick_y, 2));
        double turn = Range.clip(gamepad1.right_stick_x, -1, 1);

        telemetry.addData("angle", theta);
        telemetry.addData("magnitude", magnitude);
        telemetry.addData("rotation", turn);

        driveControl.drive(theta, magnitude, turn);

        if (movementController.getButtonState(GamepadController.ToggleButton.B) == GamepadController.ButtonState.KEY_HOLD) {
            driveControl.drive(Math.PI/3, 3, 0);
        }


        if (movementController.getButtonState(GamepadController.ToggleButton.A) == GamepadController.ButtonState.KEY_HOLD) {
            driveControl.drive(Math.PI/6, 3, 0);
        }

    }


    /*
     * Code to run ONCE after the driver hits STOP
     */
    @Override
    public void stop() {
    }


}
