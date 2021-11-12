package mechanisms;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Gyroscope;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;


/**
 * Class for controlling drive motors
 *
 *
 * 1. move forwards a set distance
 * 2. strafe a set distance
 * 3. turn a set amount
 * 3. polar move
 *
 */
public class DriveControl {

    private final DcMotor leftFront;
    private final DcMotor leftRear;
    private final DcMotor rightFront;
    private final DcMotor rightRear;
    private final BNO055IMU imu;

    private double lastAngle = 0;
    private double averageVelocity = 0;
    private double averageGoal = 0;
    private ElapsedTime t;


    // gobilda yellowjacket 312 rpm, technically 537.7 but yk
    private static final int ENCODER_ROTATION_312RPM = 538;
    private static final double WHEEL_DIAMETER_96MM = 96;
    // 2 pi r
    private static final double WHEEL_CIRCUMFERENCE_MM = 2 * Math.PI * (WHEEL_DIAMETER_96MM/2.0);
    private static final double INCH_TO_MM = 25.4;

    /**
     * Initialize the drive controller
     * @param hardwareMap the hardware map to use
     */
    public DriveControl(HardwareMap hardwareMap) {
        DcMotor leftFront = hardwareMap.get(DcMotor.class, "leftFront");
        DcMotor leftRear = hardwareMap.get(DcMotor.class, "leftRear");
        DcMotor rightFront = hardwareMap.get(DcMotor.class, "rightFront");
        DcMotor rightRear = hardwareMap.get(DcMotor.class, "rightRear");
        imu = hardwareMap.get(BNO055IMU.class, "imu");

        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
        parameters.angleUnit           = BNO055IMU.AngleUnit.DEGREES;
        parameters.accelUnit           = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        imu.initialize(parameters);


        this.leftFront = leftFront;
        this.leftRear = leftRear;
        this.rightFront = rightFront;
        this.rightRear = rightRear;

        setMotorDirection(DcMotorSimple.Direction.FORWARD);
        setMotorMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    /**
     * Drive a set distance straight (forwards & backwards) and then stop
     * A positive distance is forward, negative is backwards
     * @param targetInches the distance to move, in inches
     * @param percentSpeed the fraction of the motor's max speed to move, as a decimal
     */
    public void moveYDist(double targetInches, double percentSpeed) {
        int ticks = calculateDirectTicks(targetInches);
        setStraightTarget(ticks);
        runToStraightPosition(percentSpeed);
    }

    /**
     * Drive a set distance by strafing (strafe left & right) and then stop
     * A positive distance is left, negative is right
     * @param targetInches the distance to move, in inches
     * @param percentSpeed the fraction of the motor's max speed to move, as a decimal
     */
    public void moveXDist(double targetInches, double percentSpeed) {
        int ticks = calculateDirectTicks(targetInches);
        setStrafeTarget(ticks);
        runToStrafePosition(percentSpeed);
    }

    public void turnAngle(double newAngle, double percentSpeed) {

    }

    public void polarMove(double angle, double turn, double power) {
		final double v1 = (power) * Math.cos(angle) + turn;
        final double v2 = power * Math.sin(angle) - turn;
        final double v3 = power * Math.sin(angle) + turn;
        final double v4 = power * Math.cos(angle) - turn;

        leftFront.setPower(-v1);
        rightFront.setPower(-v2);
        leftRear.setPower(-v3);
        rightRear.setPower(-v4);
    }

    /**
     * Calculate the amount of encoder ticks that correspond to a certain distance
     * The calculation is dependent on the motor's ticks per rotation and wheel's circumference
     * Essentially, the target inches get converted to mm, then the wheel's circumference is used
     * to figure out how many wheel rotations cover the distance. Finally, the number of wheel
     * rotations is converted to the number of encoder ticks per wheel rotation.
     *
     * This function can be used for direct straight distance and direct strafe distance,
     * though it isn't entirely accurate for strafes.
     *
     * @param targetInches the amount of inches to move
     * @return the number of encoder ticks that covers the set distance
     */
    private int calculateDirectTicks(double targetInches) {
        double targetMM = targetInches * INCH_TO_MM;
        double targetRotations = targetMM/WHEEL_CIRCUMFERENCE_MM;
        double encoderTicks = targetRotations/ENCODER_ROTATION_312RPM;
        return (int)Math.round(encoderTicks);
    }

    /**
     * After a target has been set, run the motors to the target pos at the provided speed
     * @param percentSpeed the percent of the maximum speed to run the motors at [0,1]
     */
    private void runToStraightPosition(double percentSpeed) {
        setMotorMode(DcMotor.RunMode.RUN_TO_POSITION);
        setStraightVelocity(percentSpeed);
    }

    /**
     * After a target has been set, run the motors to the target pos at the provided speed
     * @param percentSpeed the percent of the maximum speed to run the motors at [0,1]
     */
    private void runToStrafePosition(double percentSpeed) {
        setMotorMode(DcMotor.RunMode.RUN_TO_POSITION);
        setStrafeVelocity(percentSpeed);
    }

    /**
     * Run the motor at the specified speed
     * @param percentSpeed the percent of the maximum speed to run the motors at [0,1]
     */
    public void runAtSpeed(double percentSpeed) {
        setMotorMode(DcMotor.RunMode.RUN_USING_ENCODER);
        setStraightVelocity(percentSpeed);
    }

    /**
     * Set the target for moving forward or backward
     * A positive tick value moves forwards, negative goes backwards
     * @param targetEncoderTicks the encoder ticks to move at
     */
    public void setStraightTarget(int targetEncoderTicks) {
        setMotorMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        leftFront.setTargetPosition(targetEncoderTicks);
        leftRear.setTargetPosition(targetEncoderTicks);
        rightFront.setTargetPosition(-targetEncoderTicks);
        rightRear.setTargetPosition(-targetEncoderTicks);
    }

    /**
     * Set the target for strafing left or right
     * A positive tick value strafes right, negative goes left
     * @param targetEncoderTicks the encoder ticks to strafe at
     */
    private void setStrafeTarget(int targetEncoderTicks) {
        setMotorMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        leftFront.setTargetPosition(targetEncoderTicks);
        leftRear.setTargetPosition(-targetEncoderTicks);
        rightFront.setTargetPosition(-targetEncoderTicks);
        rightRear.setTargetPosition(targetEncoderTicks);
    }

    /**
     * Set the motor mode for all drive motors
     * @param runMode the runmode to set
     */
    public void setMotorMode(DcMotor.RunMode runMode) {
        leftFront.setMode(runMode);
        leftRear.setMode(runMode);
        rightFront.setMode(runMode);
        rightRear.setMode(runMode);
    }

    /**
     * Sets the motor velocity for moving forwards or backwards
     * might not be necessary if run to position disregards power sign
     * @param velocity the velocity to set
     */
    public void setStraightVelocity(double velocity) {
        leftFront.setPower(velocity);
        leftRear.setPower(velocity);
        rightFront.setPower(-velocity);
        rightRear.setPower(-velocity);
    }

    /**
     * Sets the motor velocity for strafing
     * might not be necessary if run to position disregards power sign
     * @param velocity the velocity to set
     */
    private void setStrafeVelocity(double velocity) {
        leftFront.setPower(velocity);
        leftRear.setPower(-velocity);
        rightFront.setPower(-velocity);
        rightRear.setPower(velocity);
    }

    /**
     * Set the velocity for all motors
     * This value can have different meanings depending on the motor mode
     * @param velocity the velocity to set
     */
    private void setVelocity(double velocity) {
        leftFront.setPower(velocity);
        leftRear.setPower(velocity);
        rightFront.setPower(velocity);
        rightRear.setPower(velocity);
    }

    /**
     * Set motor direction for all motors
     * @param direction the direction to set the motors to
     */
    private void setMotorDirection(DcMotorSimple.Direction direction) {
        leftFront.setDirection(direction);
        leftRear.setDirection(direction);
        rightFront.setDirection(direction);
        rightRear.setDirection(direction);
    }

    /**
     * Drive the robot using gyro strafe correction
     * @param direction Angle to strafe at (0 is forward, Pi/2 is left...?)
     * @param rotation Value between -1 and 1 representing power to turn at
     * @param magnitude Speed to strafe at
     */
    public void drive(double direction, double magnitude, double rotation){
        leftFront.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        leftRear.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightFront.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightRear.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        //Get the current gyro angle
        double gyroAngle = imu.getAngularOrientation().firstAngle;
        //Find the change since our last angle
        double dAngle = (lastAngle - gyroAngle);
        //Find the change in time since out last measurement
        double dTime = t.seconds();
        t.reset();
        //Find the change in angle over time (angular velocity)
        double velocity = dAngle/dTime;

        //Find the AVERAGE velocity (just a smoothed out velocity that has been averaged to minimize static noise and improve accuracy)
        averageVelocity = (averageVelocity * 3 + velocity)/4;

        //Find the AVERAGE rotational goal (smoothed out)
        averageGoal = (averageGoal * 3 + rotation)/4.0;

        //Update the last angle
        lastAngle = gyroAngle;

        //Update the rotational goal to compensate for how off we are from the goal.
        //Dividing by 300 to convert the degrees per second into power for a motor. We found that about 300 degrees per second is a 1 in turning power.
        //The 1.5x is a multiplier to make sure the offset is applied 2enough to have an actual effect.

        //Commented for now to make drivable (find a new value instead of 300.0 and then uncomment to enable)
        rotation += (averageGoal - averageVelocity / 120.0);


        direction += Math.PI/4.0;  //Strafe direction needs to be offset so that forwards has everything go at the same power

        final double v1 = magnitude * Math.cos(direction) + rotation;
        final double v2 = magnitude * Math.sin(direction) - rotation;
        final double v3 = magnitude * Math.sin(direction) + rotation;
        final double v4 = magnitude * Math.cos(direction) - rotation;

        leftFront.setPower(v1);
        rightFront.setPower(v2);
        leftRear.setPower(v3);
        rightRear.setPower(v4);
    }







}
