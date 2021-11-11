//package testing;
//
//import android.util.Log;
//
//import com.qualcomm.robotcore.hardware.DcMotor;
//
//public class MotorController {
//
//    private static final String TAG = "teamcode.motorcontr";
//
//    private DcMotor motor;
//
//    public MotorController(DcMotor motor) {
//
//        this.motor = motor;
//    }
//
//    public void logInfo() {
//        Log.d(TAG, "Position: " + motor.getCurrentPosition());
//        Log.d(TAG, "Motor type: " + motor.getMotorType().getName());
//        Log.d(TAG, "Controller: " + motor.getController().getDeviceName());
//        Log.d(TAG, "Run type: " + motor.getMode());
//    }
//
//    public void spin() {
//        motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
//        motor.setPower(-.8);
//    }
//
//    public void stop() {
//        motor.setPower(0);
//    }
//
//    public void setTarget() {
//        motor.setTargetPosition(4000);
//        Log.d(TAG, "Run type: " + motor.getMode());
//        Log.d(TAG, "Current position: " + motor.getCurrentPosition());
//        Log.d(TAG, "Target position: " + motor.getTargetPosition());
//        motor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
//        Log.d(TAG, "Run type: " + motor.getMode());
//        Log.d(TAG, "Current position: " + motor.getCurrentPosition());
//        Log.d(TAG, "Target position: " + motor.getTargetPosition());
//    }
//
//    public void runToTarget() {
//        motor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
//        motor.setTargetPosition(4000);
//        motor.setPower(.8);
//        motor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
//        Log.d(TAG, "Run type: " + motor.getMode());
//    }
//}
