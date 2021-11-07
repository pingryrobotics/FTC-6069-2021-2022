package opmodes;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

import mechanisms.DriveControl;
import teamcode.GamepadController;
import teamcode.GamepadController.FloatButton;
import teamcode.GamepadController.ButtonState;
import teamcode.GamepadController.ToggleButton;



@TeleOp(name="Drive: Drive Control OpMode", group="Testing")
public class DriveControlOpMode extends OpMode {
    // tag is used in logcat logs (Log.d()) to identify where the log is coming from
    // logcat is basically like System.out.print (standard output) except through adb
    private static final String TAG = "teamcode.drive-opmode"; // put the name of the opmode

    // put any outside classes you need to use here
    private GamepadController movementController;
    private DriveControl driveControl;
    private double velocity = 0;


    // code to run once when driver hits init on phone
    @Override
    public void init() {
        movementController = new GamepadController(gamepad1);

        DcMotor leftFront = hardwareMap.get(DcMotor.class, "leftFront");
        DcMotor leftRear = hardwareMap.get(DcMotor.class, "leftRear");
        DcMotor rightFront = hardwareMap.get(DcMotor.class, "rightFront");
        DcMotor rightRear = hardwareMap.get(DcMotor.class, "rightRear");
        driveControl = new DriveControl(leftFront, leftRear, rightFront, rightRear);

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
            driveControl.setTargetInches(12);
            driveControl.runToPosition();
        }

        if (movementController.getButtonState(ToggleButton.Y) == ButtonState.KEY_DOWN) {
            driveControl.setMotorTarget(1000);
            driveControl.runToPosition();
        }
    }


    /*
     * Code to run ONCE after the driver hits STOP
     */
    @Override
    public void stop() {
    }


}
