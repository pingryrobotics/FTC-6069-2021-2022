package mechanisms;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;


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
public class DriveControl implements QueueableMechanism {

    public static final int GYRO_MIN_ANGLE = -180;
    public static final int GYRO_MAX_ANGLE = 180;
    private final DcMotor leftFront;
    private final DcMotor leftRear;
    private final DcMotor rightFront;
    private final DcMotor rightRear;
    private final BNO055IMU imu;


    // gobilda yellowjacket 312 rpm, technically 537.7 but yk
    private static final int ENCODER_ROTATION_312RPM = 538;
    private static final double WHEEL_DIAMETER_96MM = 96;
    // 2 pi r
    private static final double WHEEL_CIRCUMFERENCE_MM = 2 * Math.PI * (WHEEL_DIAMETER_96MM/2.0);
    private static final double INCH_TO_MM = 25.4;
    private static final double CM_PER_INCH = 2.54;
    private static final double wheelDiameterCentimeters = 10; // diameter of wheel in cm, check specs
    private static final double wheelDiameterInches = wheelDiameterCentimeters / CM_PER_INCH; // cm to Inches
    private static final double gearboxReduction = 19.2; // reduction of gearbox, check specs
    private static final double pulsesPerRevolution = 28; // pulses per revolution for the unreducted motor
    private static final double ticksPerInch = ((pulsesPerRevolution * gearboxReduction) / wheelDiameterInches) / Math.PI;
    private final Telemetry telemetry;

    // auto navigation constants
    private static final double CLOSE_ENOUGH_TO_ZERO = .6;



    /**
     * Initialize the drive controller
     * @param hardwareMap the hardware map to use
     */
    public DriveControl(HardwareMap hardwareMap, Telemetry telemetry) {
        DcMotor leftFront = hardwareMap.get(DcMotor.class, "leftFront");
        DcMotor leftRear = hardwareMap.get(DcMotor.class, "leftRear");
        DcMotor rightFront = hardwareMap.get(DcMotor.class, "rightFront");
        DcMotor rightRear = hardwareMap.get(DcMotor.class, "rightRear");
        imu = hardwareMap.get(BNO055IMU.class, "imu");

        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
        parameters.angleUnit           = BNO055IMU.AngleUnit.DEGREES;
        parameters.accelUnit           = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        imu.initialize(parameters);

        this.telemetry = telemetry;
        this.leftFront = leftFront;
        this.leftRear = leftRear;
        this.rightFront = rightFront;
        this.rightRear = rightRear;

//        setMotorDirection(DcMotorSimple.Direction.FORWARD);
        setMotorMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        leftFront.setDirection(DcMotor.Direction.REVERSE);
        leftRear.setDirection(DcMotor.Direction.REVERSE);

    }

    /**
     * Drive a set distance straight (forwards & backwards)
     * and then stop
     * A positive distance is forward, negative is backwards
     * @param targetInches the distance to move, in inches
     * @param percentSpeed the fraction of the motor's max speed to move, as a decimal
     */
    public void moveYDist(double targetInches, double percentSpeed) {
        int ticks = calculateDirectTicks(targetInches);
        setStraightTarget(ticks);
        runToDirectPosition(percentSpeed);
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
        runToDirectPosition(percentSpeed);
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
        double encoderTicks = targetRotations*ENCODER_ROTATION_312RPM;
        return (int)Math.round(encoderTicks);
    }


    /**
     * After a target has been set, run the motors to the target pos at the provided speed
     * @param percentSpeed the percent of the maximum speed to run the motors at [0,1]
     */
    private void runToDirectPosition(double percentSpeed) {
        setVelocity(percentSpeed);
        setMotorMode(DcMotor.RunMode.RUN_TO_POSITION);
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
        rightFront.setTargetPosition(targetEncoderTicks);
        rightRear.setTargetPosition(targetEncoderTicks);
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
        setMotorMode(DcMotor.RunMode.RUN_USING_ENCODER);
        leftFront.setPower(velocity);
        leftRear.setPower(velocity);
        rightFront.setPower(velocity);
        rightRear.setPower(velocity);
    }

    /**
     * Sets the motor velocity for strafing
     * might not be necessary if run to position disregards power sign
     * @param velocity the velocity to set
     */
    public void setStrafeVelocity(double velocity) {
        setMotorMode(DcMotor.RunMode.RUN_USING_ENCODER);
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

    public void setTurnVelocity(double percentSpeed) {
        setMotorMode(DcMotor.RunMode.RUN_USING_ENCODER);
        leftFront.setPower(percentSpeed);
        leftRear.setPower(percentSpeed);
        rightFront.setPower(-percentSpeed);
        rightRear.setPower(-percentSpeed);
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

    /**
     * Get the gyroscope heading from the imu
     * @return the heading, in degrees [-180, 180]
     */
    public double getGyroAngle(){
        return imu.getAngularOrientation().firstAngle;
    }


    /**
     * Turn an amount of degrees using encoders
     * @return true if turning is completed, otherwise false
     */
    private boolean updateTurnTarget(DriveAction currentAction) {

        // current, target, degrees to turn
        // 90 - 80 = turn 10
        // 80 - 90 = turn -10
        double degreesToTurn = (getGyroAngle() - currentAction.targetPosition) * -1;
        telemetry.addData("degrees to turn pre wrap", degreesToTurn);
        // wrap to -180, 180
        degreesToTurn = wrapAngle(degreesToTurn);
        telemetry.addData("degrees to turn post wrap", degreesToTurn);

        // get sign of degrees to get direction
        double direction = degreesToTurn / Math.abs(degreesToTurn);
        telemetry.addData("turn direction", direction);
        // the greatest distance the robot can be from its target is 180, so its distance from
        // its target can be expressed as the degrees / 180
        double fractionReducer = 45;
        double fractionOfCircleFromTarget = Math.abs(degreesToTurn / fractionReducer);
        telemetry.addData("fraction from target", fractionOfCircleFromTarget);

        // determine whether to turn left or right
        // current, degrees, target
        // 90 + 80 = 110
        // -180 + 80 = -100

        // -180 + 200 = 20
        // -180 + wrap(200) = 20
        // wrap(200) = -20
        // -180 - 20 = -200
        // wrap(-200) = 20

        // -180 - 1 = 180
        // -180 - 1 = -181
        // wrap(-181) = 180

        // a wrapped degree value tells you the direction to turn in based on its sign
        // -180 - 90 = -270
        // wrap(-270) = 90
        // assuming the left side of a circle is negative and the right half is positive,
        // counterclockwise from 0 turns to the negative values and clockwise turns to the positive
        // a negative wrapped degree value should turn counterclockwise, while a positive should turn
        // clockwise

        if (Math.abs(degreesToTurn) <= CLOSE_ENOUGH_TO_ZERO) {
            setVelocity(0);
            return true;
        }

        final double leftPower = -(currentAction.percentSpeed * fractionOfCircleFromTarget * direction);
        final double rightPower = currentAction.percentSpeed * fractionOfCircleFromTarget * direction;

        telemetry.addData("left power", leftPower);
        telemetry.addData("right power", rightPower);
        leftFront.setPower(leftPower);
        leftRear.setPower(leftPower);
        rightFront.setPower(rightPower);
        rightRear.setPower(rightPower);
        return false;
    }

    /**
     * Wraps an angle to between -180 & 180.
     * @param angle the angle to wrap
     * @return the wrapped angle
     */
    public static double wrapAngle(double angle) {

        if (angle > GYRO_MIN_ANGLE && angle < GYRO_MAX_ANGLE) {
            return angle;
        } else if (angle < GYRO_MIN_ANGLE) {
            return wrapAngle((angle - GYRO_MIN_ANGLE) * -1);
        } else {
            return wrapAngle((angle - GYRO_MAX_ANGLE) * -1);
        }
    }


    /**
     * Determine if motors are currently running to a position
     * @return true if any of the motors are running to a position, otherwise false
     */
    private boolean isRunningToPosition() {
        return (leftFront.isBusy() || leftRear.isBusy() || rightFront.isBusy() || rightRear.isBusy());
    }


//
//    /**
//     * Update the auto actions or move to the next one if the current action has completed.
//     */
//    public void updateAction(DriveAction currentAction) {
//
//        if (currentAction == null) {
//            telemetry.addData("Drive type", "null");
//            return;
//        }
//        telemetry.addData("current action", currentAction.driveOption);
//
//        boolean finished = false;
//        switch (currentAction.driveOption) {
//            case FORWARD:
//            case STRAFE:
//                finished = !isRunningToPosition();
//                break;
//            case TURN:
//                finished = updateTurnTarget();
//                break;
//            case WAIT:
//                finished = (System.currentTimeMillis() >= currentAction.targetPosition);
//                break;
//        }
//
//        if (finished) {
//            setMotorMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
//            currentAction = actionQueue.poll();
//            beginAction();
//        }
//
//    }
//
//    /**
//     * Begin the current auto action in the queue
//     */
//    public void beginAction() {
//        if (currentAction == null)
//            return;
//        switch (currentAction.driveOption) {
//            case FORWARD:
//                moveYDist(currentAction.targetIncrement, currentAction.percentSpeed);
//                break;
//            case STRAFE:
//                moveXDist(currentAction.targetIncrement, currentAction.percentSpeed);
//                break;
//            case TURN:
//                setMotorMode(DcMotor.RunMode.RUN_USING_ENCODER);
//                currentAction.setTargetPosition(getGyroAngle());
//                break;
//            case WAIT:
//                setMotorMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
//                currentAction.setTargetPosition(System.currentTimeMillis());
//                break;
//
//        }
//    }

    /**
     * Convenience method to get a drive action for driving forward the specified number of inches
     * @param incrementInches the inches to move straight (positive is forward, negative is backward)
     * @param percentSpeed the percent of the maximum speed to run at
     * @return the DriveAction for a forward movement
     */
    public DriveAction getForwardAction(double incrementInches, double percentSpeed) {
        return new DriveAction(DriveAction.DriveOption.FORWARD, incrementInches, percentSpeed, this);
    }


    /**
     * Convenience method to get a drive action for strafing by the specified number of inches
     * @param incrementInches the inches to strafe (positive is right, negative is left)
     * @param percentSpeed the percent of the maximum speed to run at
     * @return the DriveAction for a strafe movement
     */
    public DriveAction getStrafeAction(double incrementInches, double percentSpeed) {
        return new DriveAction(DriveAction.DriveOption.STRAFE, incrementInches, percentSpeed, this);
    }

    /**
     * Convenience method to get a drive action for turning by the specified angle in degrees
     * @param incrementAngle the angle to turn (positive is right, negative is left).
     *                       The increment is an imu angle within the range of [-180,180], and values
     *                       outside this range will be wrapped to this range.
     * @param percentSpeed the percent of the maximum speed to run at
     * @return the DriveAction for a turn movement
     */
    public DriveAction getTurnIncrementAction(double incrementAngle, double percentSpeed) {
        return new DriveAction(DriveAction.DriveOption.TURN_INCREMENT, incrementAngle, percentSpeed, this);
    }

    /**
     * Convenience method to get a drive action for turning by the specified angle in degrees
     * @param targetAngle the angle to turn to [-180, 180]
     * @param percentSpeed the percent of the maximum speed to run at
     * @return the DriveAction for a turn movement
     */
    public DriveAction getTurnPositionAction(double targetAngle, double percentSpeed) {
        return new DriveAction(DriveAction.DriveOption.TURN_POSITION, targetAngle, percentSpeed, this);
    }



    /**
     * A class to represent auto drive actions to queue for execution
     */
    public static class DriveAction extends AutoQueue.AutoAction {
        private final double percentSpeed;
        private final double targetIncrement;
        private final DriveOption driveOption;
        private double targetPosition;
        private DriveControl driveControl;

        /**
         * Create a DriveAction to be queued for execution by DriveControl
         * @param driveOption the type of action to perform
         * @param targetIncrement the increment to add to the current position. If the driveOption is:
         *                        FORWARD/STRAFE: the increment is in inches
         *                        TURN_INCREMENT: the increment is an imu angle within the range of [-180,180], and values
         *                        outside this range will be wrapped to this range.
         *                        TURN_POSITION: turns to the specified imu angle
         * @param percentSpeed the percent of the motors maximum speed to run at [0,1]
         */
        public DriveAction(DriveOption driveOption, double targetIncrement, double percentSpeed, DriveControl driveControl) {
            this.driveControl = driveControl;
            this.driveOption = driveOption;
            this.percentSpeed = percentSpeed;

            if (driveOption == DriveOption.TURN_INCREMENT) {
                // negative wrapped because its added to the target position in setTargetPosition,
                // so it needs to be negative
                this.targetIncrement = -wrapAngle(targetIncrement);
            } else if (driveOption == DriveOption.TURN_POSITION) {
                this.targetPosition = wrapAngle(targetIncrement);
                this.targetIncrement = -1;
            }
            else {
                this.targetIncrement = targetIncrement;
            }

        }

        /**
         * Begin the auto action by performing different actions depending on which action needs to be started.
         */
        public void beginAutoAction() {
            switch (driveOption) {
                case FORWARD:
                    driveControl.moveYDist(targetIncrement, percentSpeed);
                    break;
                case STRAFE:
                    driveControl.moveXDist(targetIncrement, percentSpeed);
                    break;
                case TURN_INCREMENT:
                    driveControl.setMotorMode(DcMotor.RunMode.RUN_USING_ENCODER);
                    setTargetPosition(driveControl.getGyroAngle());
                    break;
                case TURN_POSITION:
                    driveControl.setMotorMode(DcMotor.RunMode.RUN_USING_ENCODER);
                    break;
                case WAIT:
                    driveControl.setMotorMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                    setTargetPosition(System.currentTimeMillis());
                    break;
            }
        }

        /**
         * Update the auto action depending on the driveOption
         * @return true if the action was completed, false otherwise
         */
        public boolean updateAutoAction() {

            boolean finished = false;
            switch (driveOption) {
                case FORWARD:
                case STRAFE:
                    finished = !driveControl.isRunningToPosition();
                    break;
                case TURN_POSITION:
                case TURN_INCREMENT:
                    finished = driveControl.updateTurnTarget(this);
                    break;
                case WAIT:
                    finished = (System.currentTimeMillis() >= targetPosition);
                    break;
            }

            if (finished) {
                driveControl.setMotorMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            }
            return finished;
        }

        /**
         * Set the target position for the action. This should only be set by beginAction prior to execution.
         * For TURN and WAIT, the current position should be the current angle or current time, respectively.
         * This isn't necessary for FORWARD or STRAFE
         * @param currentPosition the current position of the robot. This value should be different
         *                        depending on the drive action
         */
        private void setTargetPosition(double currentPosition) {
            targetPosition = currentPosition + targetIncrement;
        }


        /**
         * An enum of drive types for drive control to execute autonomously
         */
        public enum DriveOption {
            FORWARD,
            STRAFE,
            TURN_INCREMENT,
            TURN_POSITION,
            WAIT,
        }
    }




}
