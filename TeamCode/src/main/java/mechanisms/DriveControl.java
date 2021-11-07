package mechanisms;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

public class DriveControl {

    private DcMotor leftFront;
    private DcMotor leftRear;
    private DcMotor rightFront;
    private DcMotor rightRear;

    // gobilda yellowjacket 312 rpm, technically 537.7 but yk
    private static final int ENCODER_ROTATION_312RPM = 538;
    private static final double WHEEL_DIAMETER_96MM = 96;
    // 2 pi r
    private static final double WHEEL_CIRCUMFERENCE_MM = 2 * Math.PI * (WHEEL_DIAMETER_96MM/2.0);
    private static final double INCH_TO_MM = 25.4;

    public DriveControl(DcMotor leftFront, DcMotor leftRear, DcMotor rightFront, DcMotor rightRear) {
        this.leftFront = leftFront;
        this.leftRear = leftRear;
        this.rightFront = rightFront;
        this.rightRear = rightRear;

        setMotorDirection();
    }

    public void setTargetInches(double targetInches) {
        double targetMM = targetInches * INCH_TO_MM;
        double targetRotations = targetMM/WHEEL_CIRCUMFERENCE_MM;
        double encoderTicks = targetRotations/ENCODER_ROTATION_312RPM;
        setMotorTarget((int)Math.round(encoderTicks));
    }

    public void runToPosition() {
        setMotorMode(DcMotor.RunMode.RUN_TO_POSITION);
        setMotorVelocity(.5);
    }

    public void runAtSpeed(double velocity) {
        setMotorMode(DcMotor.RunMode.RUN_USING_ENCODER);
        setMotorVelocity(velocity);
    }

    public void setMotorTarget(int targetEncoderTicks) {
        setMotorMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        leftFront.setTargetPosition(targetEncoderTicks);
        leftRear.setTargetPosition(targetEncoderTicks);
        rightFront.setTargetPosition(-targetEncoderTicks);
        rightRear.setTargetPosition(-targetEncoderTicks);
    }


    private void setMotorMode(DcMotor.RunMode runMode) {
        leftFront.setMode(runMode);
        leftRear.setMode(runMode);
        rightFront.setMode(runMode);
        rightRear.setMode(runMode);
    }

    private void setMotorVelocity(double velocity) {
        leftFront.setPower(velocity);
        leftRear.setPower(velocity);
        rightFront.setPower(-velocity);
        rightRear.setPower(-velocity);
    }

    private void setMotorDirection() {
        leftFront.setDirection(DcMotorSimple.Direction.FORWARD);
        leftRear.setDirection(DcMotorSimple.Direction.FORWARD);
        rightFront.setDirection(DcMotorSimple.Direction.FORWARD);
        rightRear.setDirection(DcMotorSimple.Direction.FORWARD);
    }







}
