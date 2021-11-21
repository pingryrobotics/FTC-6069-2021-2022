package opmodes_testing;

import android.util.Log;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.robotcore.external.navigation.Velocity;

import mechanisms.DriveControl;
import teamcode.GamepadController;
import teamcode.GamepadController.ButtonState;
import teamcode.GamepadController.FloatButton;
import teamcode.GamepadController.ToggleButton;


@TeleOp(name="Testing: Auto Drive Test OpMode", group="Testing")
public class AutoDriveOpMode extends OpMode {
    // tag is used in logcat logs (Log.d()) to identify where the log is coming from
    // logcat is basically like System.out.print (standard output) except through adb
    private static final String TAG = "teamcode.test_opmode"; // put the name of the opmode

    // put any outside classes you need to use here
    private GamepadController movementController;
    private GamepadController mechanismController;
    private DriveControl driveControl;
    private BNO055IMU imu;

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
        driveControl = new DriveControl(hardwareMap, telemetry);
        imu = hardwareMap.get(BNO055IMU.class, "imu");

        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
        parameters.angleUnit           = BNO055IMU.AngleUnit.DEGREES;
        parameters.accelUnit           = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        parameters.calibrationDataFile = "BNO055IMUCalibration.json";
        imu.initialize(parameters);

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
        driveControl.updateAutoAction();
//
//        double leftStickX = movementController.getButtonState(GamepadController.FloatButton.LEFT_STICK_X);
//        double leftStickY = movementController.getButtonState(GamepadController.FloatButton.LEFT_STICK_Y);
//        double rightStickX = movementController.getButtonState(GamepadController.FloatButton.RIGHT_STICK_X);
//        double rightStickY = movementController.getButtonState(GamepadController.FloatButton.RIGHT_STICK_Y);
//
//        double theta = Math.atan2(-leftStickY, leftStickX) - Math.PI/2; // go back to subtracting 90?
//        double magnitude = Math.sqrt(Math.pow(leftStickX, 2) + Math.pow(leftStickY, 2));
//        double turn = Range.clip(rightStickX, -1, 1);

//        driveControl.drive(theta, magnitude, turn);

        telemetry.addData("gyro angle", imu.getAngularOrientation());

        // do something when A is pressed
        if (movementController.getButtonState(ToggleButton.Y) == ButtonState.KEY_DOWN) {
            driveControl.addAutoAction(new DriveControl.DriveAction(DriveControl.DriveType.WAIT, 1000, .5));
        }

        if (movementController.getButtonState(ToggleButton.A) == ButtonState.KEY_DOWN) {
            driveControl.addAutoAction(new DriveControl.DriveAction(DriveControl.DriveType.WAIT, 2000, .5));
        }

        if (movementController.getButtonState(ToggleButton.X) == ButtonState.KEY_DOWN) {
            driveControl.addAutoAction(new DriveControl.DriveAction(DriveControl.DriveType.TURN, -90, .5));
        }

        if (movementController.getButtonState(ToggleButton.B) == ButtonState.KEY_DOWN) {
            driveControl.addAutoAction(new DriveControl.DriveAction(DriveControl.DriveType.TURN, 90, .5));
        }

        if (movementController.getButtonState(ToggleButton.DPAD_UP) == ButtonState.KEY_DOWN) {
            driveControl.addAutoAction(new DriveControl.DriveAction(
                    DriveControl.DriveType.FORWARD, 12, .5));
        }

        if (movementController.getButtonState(ToggleButton.DPAD_DOWN) == ButtonState.KEY_DOWN) {
            driveControl.addAutoAction(new DriveControl.DriveAction(
                    DriveControl.DriveType.FORWARD, -12, .5));
        }

        if (movementController.getButtonState(ToggleButton.DPAD_LEFT) == ButtonState.KEY_DOWN) {
            driveControl.addAutoAction(new DriveControl.DriveAction(
                    DriveControl.DriveType.STRAFE, -12, .5));
        }

        if (movementController.getButtonState(ToggleButton.DPAD_RIGHT) == ButtonState.KEY_DOWN) {
            driveControl.addAutoAction(new DriveControl.DriveAction(
                    DriveControl.DriveType.STRAFE, 12, .5));
        }




    }


    /*
     * Code to run ONCE after the driver hits STOP
     */
    @Override
    public void stop() {
    }


}
