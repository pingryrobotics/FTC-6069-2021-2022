//package opmodes;
//
//import android.util.Log;
//
//import com.qualcomm.robotcore.eventloop.opmode.OpMode;
//import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
//import com.qualcomm.robotcore.hardware.DcMotor;
//
//import org.firstinspires.ftc.teamcode.GamepadController;
//import org.firstinspires.ftc.teamcode.GamepadController.ButtonState;
//import org.firstinspires.ftc.teamcode.GamepadController.ToggleButton;
//
//import motor.MotorController;
//import teamcode.GamepadController;
//import testing.MotorController;
//
//
//@TeleOp(name="Template: Motor Test OpMode", group="Testing")
//public class MotorOpMode extends OpMode {
//    // tag is used in logcat logs (Log.d()) to identify where the log is coming from
//    // logcat is basically like System.out.print (standard output) except through adb
//    private static final String TAG = "teamcode.motor_opmode"; // put the name of the opmode
//
//    // put any outside classes you need to use here
//    private GamepadController movementController;
//    private DcMotor testMotor;
//    private MotorController motorController;
//
//
//    // put any measurements here
//    private final double inchesToMM = 25.4; // this is correct
//    private final double toCameraCenter = 1.25; // inches from bottom of logitech c615 to actual camera
//    private final double cameraPlatform = 10.5; // random value
//    private final double cameraHeight = (cameraPlatform + toCameraCenter) * inchesToMM;
//    private static final int fieldLength = 3660; // mm (this is correct)
//
//
//
//    // code to run once when driver hits init on phone
//    @Override
//    public void init() {
//        movementController = new GamepadController(gamepad1);
//        testMotor = hardwareMap.get(DcMotor.class, "rightFront");
//        motorController = new MotorController(testMotor);
//    }
//
//    // code to loop after init is pressed and before start is pressed
//    @Override
//    public void init_loop() {
//    }
//
//    // code to run once when driver hits start
//    @Override
//    public void start() {
//    }
//
//    // code to loop while opmode is running
//    @Override
//    public void loop() {
//
//        runControls();
//
//
//        // update telemetry at the end of the loop
//        telemetry.update();
//    }
//
//    /**
//     * Updates buttons and does controls when buttons are pressed
//     */
//    public void runControls() {
//
//        Log.d(TAG, "Position: " + testMotor.getCurrentPosition());
//        telemetry.addData("Position", testMotor.getCurrentPosition());
//
//        // button states need to be updated each loop for controls to work
//        movementController.updateButtonStates();
//
//        // do different things depending on the button's state
//        // dont forget to break
//        if (movementController.getButtonState(GamepadController.ToggleButton.A) == ButtonState.KEY_DOWN) {
//            Log.d(TAG, "button a pressed");
//            motorController.spin();
//        }
//
//        if (movementController.getButtonState(ToggleButton.B) == ButtonState.KEY_DOWN) {
//            motorController.stop();
//        }
//
//        if (movementController.getButtonState(ToggleButton.X) == ButtonState.KEY_DOWN) {
//            motorController.logInfo();
//        }
//
//        if (movementController.getButtonState(ToggleButton.Y) == ButtonState.KEY_DOWN) {
//            motorController.runToTarget();
//        }
//
//    }
//
//
//    /*
//     * Code to run ONCE after the driver hits STOP
//     */
//    @Override
//    public void stop() {
//    }
//
//
//}
