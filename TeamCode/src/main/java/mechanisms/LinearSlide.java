package mechanisms;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo; //only if we need the additional feeder.
import com.qualcomm.robotcore.hardware.HardwareMap;

public class LinearSlide {
    // SKU: 5202-0002-0014
    // 384.5 PPR encoder resolution

    // 0.01613 mm of height difference for linearslide for each motor tick
    // Heights of different levels
    // LEVEL 3: 23 cm, 230/0.01613 = 14259 REPLACED WITH 30000
    // LEVEL 2: 17.25 cm, 172.5/0.01613 = 10694 REPLACED WITH 23000
    // LEVEL 1: 17000
    // LEVEL 0: 0

    // Each level calculates encoder ticks to every other level based off of relative ticks

    private final DcMotor slideMotor;
    private final Servo bucketServo;
    public double power;
    private int level;
    private final int level2To3 = 3000;
    private final int level1To2 = 3000;
    private final int level0To1 = 5000;

    public LinearSlide(HardwareMap hardwareMap) {
        slideMotor = hardwareMap.get(DcMotor.class, "slideMotor");
        slideMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER); // not sure if needed but sets base state to 0
        //slideMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        bucketServo = hardwareMap.get(Servo.class, "bucketServo");
        bucketServo.setDirection(Servo.Direction.REVERSE);
        bucketServo.scaleRange(0, .03);

        slideMotor.setPower(1);
        power = 1;
        level = 0;
    }

    public void level1() { // extend linear slide to level appropriate for the bottom level of shipping hub
        slideMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        if(level == 0) {
            slideMotor.setTargetPosition(level0To1);
        }

        else if(level == 2){
            slideMotor.setTargetPosition(-level1To2);
        }

        else if(level == 3){
            slideMotor.setTargetPosition(-(level1To2 + level2To3));
        }

        else {
            slideMotor.setTargetPosition(0);
        }

        slideMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        level = 1;
        // i have no idea what it should be this is an estimate tho
    }

    public void level2() { // extend linear slide to level appropriate for the middle level of shipping hub
        slideMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        if(level == 0) {
            slideMotor.setTargetPosition(level0To1 + level1To2);
        }

        else if(level == 1){
            slideMotor.setTargetPosition(level1To2);
        }

        else if(level == 3){
            slideMotor.setTargetPosition(-level2To3);
        }

        else {
            slideMotor.setTargetPosition(0);
        }

        slideMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        level = 2;
    }

    public void level3() { // extend linear slide to level appropriate for the top level of shipping hub
        slideMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        if(level == 0) {
            slideMotor.setTargetPosition(level1To2 + level2To3 + level0To1);
        }

        else if(level == 1){
            slideMotor.setTargetPosition(level1To2 + level2To3);
        }

        else if(level == 2){
            slideMotor.setTargetPosition(level2To3);
        }

        else {
            slideMotor.setTargetPosition(0);
        }
        slideMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        level = 3;
    }

    public void level0(){
        slideMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        if(level == 1) {
            slideMotor.setTargetPosition(-level0To1);
        }

        else if(level == 2){
            slideMotor.setTargetPosition(-(level0To1 + level1To2));
        }

        else if(level == 3){
            slideMotor.setTargetPosition(-(level0To1 + level1To2 + level2To3));
        }

        else {
            slideMotor.setTargetPosition(0);
        }

        slideMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        level = 0;
    }

    public void extend() { // continuously extend linear slide
        slideMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        slideMotor.setPower(power);
    }

    public void retract() { // continuously retract linear slide
        slideMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        slideMotor.setPower(-power);
    }

    public void dump() { // dump stuff in bucket
        bucketServo.setPosition(1.0);
    }

    public void undump() { // pull bucket back after dumping
        bucketServo.setPosition(0.0);
    }

    public void stop() {
        slideMotor.setPower(0); // stop
    }

    public void resetEncoder() {
        slideMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
    }
}
