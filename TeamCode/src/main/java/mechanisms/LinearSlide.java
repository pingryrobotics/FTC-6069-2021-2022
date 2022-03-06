package mechanisms;
import static android.os.SystemClock.sleep;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo; //only if we need the additional feeder.
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;

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
    protected final Telemetry telemetry;

    public double power;
    private int level;
    private final int LEVEL_CAP = 2248;
    private final int LEVEL_3 = 2900;
    private final int LEVEL_2 = 1562;
    private final int LEVEL_1 = 353;
    private final int LEVEL_0 = 0;
    public boolean tilted = false;

    public LinearSlide(HardwareMap hardwareMap, Telemetry telemetry) {
        slideMotor = hardwareMap.get(DcMotor.class, "slideMotor");
        slideMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER); // not sure if needed but sets base state to 0
        //slideMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        bucketServo = hardwareMap.get(Servo.class, "bucketServo");
        bucketServo.setDirection(Servo.Direction.FORWARD);
        // functional range: .15 - .55
        bucketServo.scaleRange(0.2, 0.6);
        //bucketServo.setPosition(0);

        slideMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        this.telemetry = telemetry;


//        slideMotor.setPower(1);
        power = 1;
        level = 0;
    }

    public void level0() { // extend linear slide to level appropriate for intaking
        slideMotor.setTargetPosition(LEVEL_0);
        slideMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        slideMotor.setPower(1);
    }

    public void level1() { // extend linear slide to level appropriate for the bottom level of shipping hub
        slideMotor.setTargetPosition(LEVEL_1);
        slideMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        slideMotor.setPower(1);
    }

    public void level2() { // extend linear slide to level appropriate for the middle level of shipping hub
        slideMotor.setTargetPosition(LEVEL_2);
        slideMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        slideMotor.setPower(1);
    }

    public void level3() { // extend linear slide to level appropriate for the top level of shipping hub
        slideMotor.setTargetPosition(LEVEL_3);
        slideMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        slideMotor.setPower(1);
    }

    public void levelCap() { // extend linear slide to level appropriate for capping
        slideMotor.setTargetPosition(LEVEL_CAP);
        slideMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        slideMotor.setPower(0.5);
    }



    public void holdSlidePosition() {
        slideMotor.setTargetPosition(slideMotor.getCurrentPosition());
        slideMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        slideMotor.setPower(1);
    }


    /**
     * Bring the slide to the lowest possible position, set the encoders to 0, and hold at that position.
     * WARNING: THIS METHOD BRINGS THE SLIDE ALL THE WAY DOWN. This SHOULD be limited by the bar
     * below the bucket, but if this isn't there, this will DESTROY THE SLIDE.
     *
     * Also, this method likely takes a few seconds to run.
     */
    public void calibrateSlide() {
        slideMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        slideMotor.setPower(-1);
        telemetry.addData("calibrating status", "calibrating");

        double previousPosition = slideMotor.getCurrentPosition();
        int count = 0;

        while (count < 15) {
            telemetry.addData("count", count);
            if (Math.abs(previousPosition - slideMotor.getCurrentPosition()) < 5) {
                count++;
            } else {
                count = 0;
            }
            previousPosition = slideMotor.getCurrentPosition();
            sleep(50);
        }

        slideMotor.setTargetPosition(slideMotor.getCurrentPosition());
        slideMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        sleep(300);
        slideMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        slideMotor.setTargetPosition(0);
        slideMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
    }

    public void extend() { // continuously extend linear slide
        slideMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        slideMotor.setPower(-power);
    }

    public void retract() { // continuously retract linear slide
        slideMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        slideMotor.setPower(power);
    }

    public void dump() { // dump stuff in bucket
        bucketServo.setPosition(0);
    }

    public void undump() { // pull bucket back after dumping
        bucketServo.setPosition(1);
        tilted = false;

    }

    public void increaseTilt() {
        if (bucketServo.getPosition() < 1) {
            bucketServo.setPosition(bucketServo.getPosition() + .05);
        } else {
            bucketServo.setPosition(1);
        }
    }
    public void decreaseTilt() {
        if (bucketServo.getPosition() > 0) {
            bucketServo.setPosition(bucketServo.getPosition() - .05);
        } else {
            bucketServo.setPosition(0);
        }
    }

    public void tilt() {
        bucketServo.setPosition(0.75);
        tilted = true;
    }

    public void stop() {
        slideMotor.setPower(0); // stop
    }

    public void resetEncoder() {
        slideMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
    }

    public int getTicks() {
        return slideMotor.getCurrentPosition();
    }

    public Servo getServo() {
        return bucketServo;
    }

    public void setPosition(double position) {
        bucketServo.setPosition(position);
    }

    public DcMotor getSlideMotor() {
        return slideMotor;
    }

    /**
     * Convenience method to get a slide action that moves the slide to the specified level
     * @param slideOption the slideoption to add
     * @return the slide action
     */
    public SlideAction getLevelAction(SlideAction.SlideOption slideOption) {
        return new SlideAction(slideOption, this);
    }


    /**
     * Class for linear slide actions
     */
    public static class SlideAction extends AutoQueue.AutoAction {

        private SlideOption slideOption;
        private LinearSlide linearSlide;

        /**
         * Create a slide action to be executed autonomously
         * @param slideOption the slide option to go to
         * @param linearSlide the linear slide to use
         */
        public SlideAction(SlideOption slideOption, LinearSlide linearSlide) {
            this.slideOption = slideOption;
            this.linearSlide = linearSlide;
        }

        /**
         * Begin the autonomous action
         */
        @Override
        public void beginAutoAction() {
            switch (slideOption) {
                case LEVEL_0:
                    linearSlide.level0();
                    break;
                case LEVEL_1:
                    linearSlide.level1();
                    break;
                case LEVEL_2:
                    linearSlide.level2();
                    break;
                case LEVEL_3:
                    linearSlide.level3();
                    break;
                case CAP:
                    linearSlide.levelCap();
                    break;
            }
        }

        /**
         * Update the action. This needs to be called each loop
         * @return true if the action has finished, otherwise false
         */
        @Override
        public boolean updateAutoAction() {
            boolean finished = false;
            switch (slideOption) {
                case LEVEL_0:
                case LEVEL_1:
                case LEVEL_2:
                case LEVEL_3:
                case CAP:
                    finished = !linearSlide.getSlideMotor().isBusy();
                    break;
            }
            return finished;
        }


        public enum SlideOption {
            LEVEL_0,
            LEVEL_1,
            LEVEL_2,
            LEVEL_3,
            CAP
        }
    }

}
