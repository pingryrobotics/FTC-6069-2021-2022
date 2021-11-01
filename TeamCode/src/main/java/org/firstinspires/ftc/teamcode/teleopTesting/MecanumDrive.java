package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

/**
 * Created by Big Blue Robotics on 10/4/2017.
 */

public class MecanumDrive {
    public DcMotor leftFront = null;
    public DcMotor leftRear = null;
    public DcMotor rightFront = null;
    public DcMotor rightRear = null;
    public double strafeAdjustment = 0.2;

    //
    // PPR = 28 \\ pulses per revolution for the unreducted motor
    //gearboxReduction = 19.2 \\reduction of the gearbox
    //wheelDiameter = 10/2.54; \\in inches
    //inchMultiplier = PPR*gearboxReduction/wheelDiameter/Math.PI
    private static final double CM_PER_INCH = 2.54;
    private static final double wheelDiameterCentimeters = 10; // diameter of wheel in cm, check specs
    private static final double wheelDiameterInches = wheelDiameterCentimeters / CM_PER_INCH; // cm to Inches
    private static final double gearboxReduction = 19.2; // reduction of gearbox, check specs
    private static final double pulsesPerRevolution = 28; // pulses per revolution for the unreducted motor
    private static final double ticksPerInch = ((pulsesPerRevolution * gearboxReduction) / wheelDiameterInches) / Math.PI;

    public double leftMotorIsBroken = 8;

    public MecanumDrive(HardwareMap hardwareMap){
        leftFront = hardwareMap.get(DcMotor.class, "left_front");
        leftRear = hardwareMap.get(DcMotor.class, "left_rear");
        rightFront = hardwareMap.get(DcMotor.class, "right_front");
        rightRear = hardwareMap.get(DcMotor.class, "right_rear");

        leftFront.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        leftRear.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightFront.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightRear.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        leftFront.setDirection(DcMotor.Direction.REVERSE);
        leftRear.setDirection(DcMotor.Direction.REVERSE);

    }

    public void move(double leftFrontPower, double rightFrontPower, double leftRearPower, double rightRearPower){
        leftFront.setPower(leftFrontPower);
        rightFront.setPower(rightFrontPower);
        leftRear.setPower(leftRearPower);
        rightRear.setPower(rightRearPower);
    }

    public void moveEncoderStrafeRight(double inches, double power){
        power = Math.abs(power);
        leftFront.setTargetPosition((int) (leftFront.getCurrentPosition() + (inches * ticksPerInch))); // should be divided by 4 times pi times 20 times 7
        rightFront.setTargetPosition((int) (rightFront.getCurrentPosition() - (inches * ticksPerInch))); // should be divided by 4 times pi times 20 times 7
        leftRear.setTargetPosition((int) (leftRear.getCurrentPosition() - (inches * ticksPerInch))); // should be divided by 4 times pi times 20 times 7
        rightRear.setTargetPosition((int) (rightRear.getCurrentPosition() + (inches * ticksPerInch))); // should be divided by 4 times pi times 20 times 7

        leftFront.setPower(power * leftMotorIsBroken);
        rightFront.setPower(power);
        leftRear.setPower(power);
        rightRear.setPower(power);
    }

    public void moveEncoderStraight(double inches, double power){


        power = Math.abs(power);
        leftFront.setTargetPosition((int) (leftFront.getCurrentPosition() + (inches * ticksPerInch))); // should be divided by 4 times pi times 20 times 7
        rightFront.setTargetPosition((int) (rightFront.getCurrentPosition() + (inches * ticksPerInch))); // should be divided by 4 times pi times 20 times 7
        leftRear.setTargetPosition((int) (leftRear.getCurrentPosition() + (inches * ticksPerInch))); // should be divided by 4 times pi times 20 times 7
        rightRear.setTargetPosition((int) (rightRear.getCurrentPosition() + (inches * ticksPerInch))); // should be divided by 4 times pi times 20 times 7
        //(inches * 3896 * 2.54 / 10 / Math.PI))
        //537.7 ticks per rotation
        //10 cm diameter
        //10*pi/2.54 inches . rev
        //
        //AndyMark Neverrest 28 ticks per revolution * 20 reduction = 560 tl/rev
        //4in*pi /rev
        if(inches < 0){
            power *= -1;
        }
        leftFront.setPower(power * leftMotorIsBroken);
        rightFront.setPower(power);
        leftRear.setPower(power);
        rightRear.setPower(power);
    }

    public void encoderTurn(double degrees, double power){
        boolean turnRight = degrees > 0;
        power = Math.abs(power);

        double inches = degrees/180 * Math.PI * 11.5;
        int leftFrontTarget = (int) (leftFront.getCurrentPosition() - (inches * ticksPerInch));
        int leftRearTarget = (int) (leftRear.getCurrentPosition() - (inches * ticksPerInch));
        int rightFrontTarget = (int) (rightFront.getCurrentPosition() + (inches * ticksPerInch));
        int rightRearTarget = (int) (rightRear.getCurrentPosition() + (inches * ticksPerInch));

        leftFront.setTargetPosition(leftFrontTarget);
        leftRear.setTargetPosition(leftRearTarget);
        rightFront.setTargetPosition(rightFrontTarget);
        rightRear.setTargetPosition(rightRearTarget);
        if(turnRight){
            leftFront.setPower(-power);
            leftRear.setPower(-power);
            rightFront.setPower(power);
            rightRear.setPower(power);
        }else{
            leftFront.setPower(power);
            leftRear.setPower(power);
            rightFront.setPower(-power);
            rightRear.setPower(-power);
        }
    }

    public boolean encoderDone(){
        return encoderLeftFrontDone() || encoderLeftRearDone() || encoderRightFrontDone() || encoderRightRearDone();
    }

    public boolean encoderLeftFrontDone(){
        if(leftFront.getPower() < 0){
            return leftFront.getCurrentPosition() < leftFront.getTargetPosition();
        }else {
            return leftFront.getCurrentPosition() > leftFront.getTargetPosition();
        }
    }

    public boolean encoderLeftRearDone(){
        if(leftRear.getPower() < 0){
            return leftRear.getCurrentPosition() < leftRear.getTargetPosition();
        }else {
            return leftRear.getCurrentPosition() < leftRear.getTargetPosition();
        }
    }

    public boolean encoderRightFrontDone(){
        if(rightFront.getPower() < 0){
            return rightFront.getCurrentPosition() < rightFront.getTargetPosition();
        }else {
            return rightFront.getCurrentPosition() > rightFront.getTargetPosition();
        }
    }

    public boolean encoderRightRearDone(){
        if(rightRear.getPower() < 0){
            return rightRear.getCurrentPosition() < rightRear.getTargetPosition();
        }else {
            return rightRear.getCurrentPosition() < rightRear.getTargetPosition();
        }
    }

    public void polarMove(double angle, double turn, double power){
        final double v1 = (power * leftMotorIsBroken) * Math.cos(angle) + turn;
        final double v2 = power * Math.sin(angle) - turn;
        final double v3 = power * Math.sin(angle) + turn;
        final double v4 = power * Math.cos(angle) - turn;

        leftFront.setPower(-0.8 * v1);
        rightFront.setPower(-0.8 * v2);
        leftRear.setPower(-0.8 * v3);
        rightRear.setPower(-0.8 * v4);
    }
    public void brake(){
        leftFront.setPower(0);
        rightFront.setPower(0);
        leftRear.setPower(0);
        rightRear.setPower(0);
    }
}
