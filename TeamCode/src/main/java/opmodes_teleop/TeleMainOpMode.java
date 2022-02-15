package opmodes_teleop;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.Range;

import mechanisms.BucketSensor;
import mechanisms.CappingArm;
import mechanisms.Carousel;
import mechanisms.DriveControl;
import mechanisms.Intake;
import mechanisms.LinearSlide;
import mechanisms.RoadRunnerMecanumDrive;
import teamcode.GamepadController;
import teamcode.GamepadController.ButtonState;
import teamcode.GamepadController.ToggleButton;
import teamcode.Radio;


@TeleOp(name="TeleMainOpMode: TeleOp OpMode", group="TeleOp")
public class TeleMainOpMode extends OpMode {
    // tag is used in logcat logs (Log.d()) to identify where the log is coming from
    // logcat is basically like System.out.print (standard output) except through adb
    private static final String TAG = "teamcode.drive-opmode"; // put the name of the opmode

    // put any outside classes you need to use here
    private GamepadController movementController;
    private GamepadController mechanismController;
    private ColorSensor colorSensor;
    private DriveControl driveControl;
    private Intake intake;
    private LinearSlide linearSlide;
    private Carousel carousel;
    private double velocity = 0;
    private int direc = 1;
    private int offsetAngle;
    private double servoPos = 1;
    private int direction = 1;
    private int factor = 1;
    private CappingArm cappingArm;
    private RoadRunnerMecanumDrive drive;
    private BucketSensor bucketSensor;
    private boolean freightIn = false;
    private Radio radio;
    private Pose2d curr;
    private boolean dumped;


    // code to run once when driver hits init on phone
    @Override
    public void init() {
        movementController = new GamepadController(gamepad1);
        mechanismController = new GamepadController(gamepad2);
        bucketSensor = new BucketSensor(hardwareMap, telemetry);
        driveControl = new DriveControl(hardwareMap, telemetry);
        intake = new Intake(hardwareMap);
        cappingArm = new CappingArm(hardwareMap, telemetry);
        linearSlide = new LinearSlide(hardwareMap, telemetry);
        carousel = new Carousel(hardwareMap);
        radio = new Radio(hardwareMap);

        colorSensor = hardwareMap.get(ColorSensor.class, "Color Sensor 1");
        offsetAngle = 0;

        drive = new RoadRunnerMecanumDrive(hardwareMap);

        drive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

    }

    // code to loop after init is pressed and before start is pressed
    @Override
    public void init_loop() {
    }

    // code to run once when driver hits start
    @Override
    public void start() {

        linearSlide.undump();
        cappingArm.defaultPosition();
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
        mechanismController.updateButtonStates();

        // polarmove calculations
        double leftStickX = direction * movementController.getButtonState(GamepadController.FloatButton.LEFT_STICK_X);
        double leftStickY = direction * movementController.getButtonState(GamepadController.FloatButton.LEFT_STICK_Y);
        double rightStickX = direction * movementController.getButtonState(GamepadController.FloatButton.RIGHT_STICK_X);
        double rightStickY = direction * movementController.getButtonState(GamepadController.FloatButton.RIGHT_STICK_Y);

//		double theta = Math.atan2(-leftStickY, leftStickX) - Math.PI/4; // go back to subtracting 90?
        double theta = Math.atan2(-leftStickY, leftStickX) - Math.PI/2; // go back to subtracting 90?
        double magnitude = Math.sqrt(Math.pow(leftStickX, 2) + Math.pow(leftStickY, 2)) /factor ;
        double turn = Range.clip(gamepad1.right_stick_x, -1, 1)/factor;

        //driveControl.drive(theta, magnitude, turn);

        drive.setWeightedDrivePower(
                new Pose2d( direction* gamepad1.left_stick_y/factor,
                        direction * gamepad1.left_stick_x/factor,
                            -1*gamepad1.right_stick_x/factor
                )
        );

        telemetry.addData("red", colorSensor.red());
        telemetry.addData("green", colorSensor.green());
        telemetry.addData("blue", colorSensor.blue());
        telemetry.addData("argb", colorSensor.argb());


        Servo servo = linearSlide.getServo();
        telemetry.addData("serv position", servo.getPosition());
        telemetry.addData("serv direction", servo.getDirection());
        telemetry.addData("servo variable position", servoPos);

        telemetry.addData("current pos", linearSlide.getSlideMotor().getCurrentPosition());
        telemetry.addData("target pos", linearSlide.getSlideMotor().getTargetPosition());

        // region movement

        if (movementController.getButtonState(ToggleButton.DPAD_UP) == ButtonState.KEY_DOWN) {
            cappingArm.turnOutwards();
        }
        if (movementController.getButtonState(ToggleButton.DPAD_DOWN) == ButtonState.KEY_DOWN) {
            cappingArm.turnInwards();
        }


        if (movementController.getButtonState(ToggleButton.X) == ButtonState.KEY_DOWN) {
            cappingArm.spinOut();
        }
        if (movementController.getButtonState(ToggleButton.Y) == ButtonState.KEY_DOWN) {
            cappingArm.spinIn();
        }

        if (movementController.getButtonState(ToggleButton.LEFT_TRIGGER) == ButtonState.KEY_DOWN) {
            intake.intakeOut();
        }
        else if (movementController.getButtonState(ToggleButton.LEFT_TRIGGER) == ButtonState.KEY_UP) {
            intake.stop();
        }
        // right trigger: intake reverses while pressed
        if (movementController.getButtonState(ToggleButton.RIGHT_TRIGGER) == ButtonState.KEY_DOWN) {
//            if (intake.power >= 0) {
                intake.intakeIn();
//            } else {
//                intake.stop();
//            }
        }
        else if (movementController.getButtonState(ToggleButton.RIGHT_TRIGGER) == ButtonState.KEY_UP) {
            intake.stop();
        }

        if (movementController.getButtonState(ToggleButton.RIGHT_BUMPER) == ButtonState.KEY_DOWN) {
            carousel.spin();
        } else if (movementController.getButtonState(ToggleButton.RIGHT_BUMPER) == ButtonState.KEY_UP) {
            carousel.stop();
        }

        if (movementController.getButtonState(ToggleButton.LEFT_BUMPER) == ButtonState.KEY_DOWN) {
            carousel.reverseSpin();
        } else if (movementController.getButtonState(ToggleButton.LEFT_BUMPER) == ButtonState.KEY_UP) {
            carousel.stop();
        }



        if(movementController.getButtonState(ToggleButton.A) == ButtonState.KEY_DOWN){
            if(direction == 1){
                direction = -1;
            }

            else{
                direction = 1;
            }
        }

        if(movementController.getButtonState(ToggleButton.B) == ButtonState.KEY_DOWN){
            if(factor == 1){
                factor = 2;
            }
            else{
                factor = 1;
            }
        }

        if (movementController.getButtonState(ToggleButton.DPAD_LEFT) == ButtonState.KEY_DOWN) { // blue side
            // restrictions: DON'T MOVE AT ALL BETWEEN GOING THERE AND RETURNING
            // DON'T MOVE BACK MANUALLY
            if (!dumped) {
                curr = drive.dumpShared(-1);
                dumped = true;
            } else {
                drive.retractShared(-1, curr);
                dumped = false;
            }
        }
        if (movementController.getButtonState(ToggleButton.DPAD_RIGHT) == ButtonState.KEY_DOWN) { // red side
            if (!dumped) {
                curr = drive.dumpShared(1);
                dumped = true;
            } else {
                drive.retractShared(1, curr);
                dumped = false;
            }
        }


        // endregion movement

        // region mechanism

        // right bumper: linearslide extends while pressed
        if (mechanismController.getButtonState(ToggleButton.RIGHT_BUMPER) == ButtonState.KEY_DOWN) {
//            if (linearSlide.getSlideMotor().getCurrentPosition() <= 2160) {
            // THIS IS ACTUALLY RETRACT
                linearSlide.extend();
//            }
        } else if (mechanismController.getButtonState(ToggleButton.RIGHT_BUMPER) == ButtonState.KEY_UP) {
            linearSlide.stop();
        }

        // left bumper: linearslide retracts while pressed
        if (mechanismController.getButtonState(ToggleButton.LEFT_BUMPER) == ButtonState.KEY_DOWN) {
            // THIS IS ACTUALLY EXTEND
            if (linearSlide.getSlideMotor().getCurrentPosition() <= 2160) {
                linearSlide.retract();
            }
        } else if (mechanismController.getButtonState(ToggleButton.LEFT_BUMPER) == ButtonState.KEY_UP) {
            linearSlide.stop();
        }


        // X button; linear slide dumps and then undumps once it's pressed
        if (mechanismController.getButtonState(ToggleButton.RIGHT_TRIGGER) == ButtonState.KEY_HOLD) {
            linearSlide.setPosition(linearSlide.getServo().getPosition() + 0.01);
        }

        // Y button; linear slide dumps and then undumps once it's pressed
        if (mechanismController.getButtonState(ToggleButton.LEFT_TRIGGER) == ButtonState.KEY_HOLD) {
            linearSlide.setPosition(linearSlide.getServo().getPosition() - 0.01);
        }


        if (mechanismController.getButtonState(ToggleButton.DPAD_UP) == ButtonState.KEY_DOWN) {
            linearSlide.level3();
        }


        if (mechanismController.getButtonState(ToggleButton.DPAD_DOWN) == ButtonState.KEY_DOWN) {
            linearSlide.level0();
        }

        if (mechanismController.getButtonState(ToggleButton.DPAD_RIGHT) == ButtonState.KEY_DOWN) {
            linearSlide.level2();
        }

        if (mechanismController.getButtonState(ToggleButton.DPAD_LEFT) == ButtonState.KEY_DOWN) {
            linearSlide.level1();
        }

        if (mechanismController.getButtonState(ToggleButton.START_BUTTON) == ButtonState.KEY_DOWN) {
            linearSlide.getSlideMotor().setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            linearSlide.getSlideMotor().setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }

        if (mechanismController.getButtonState(ToggleButton.Y) == ButtonState.KEY_DOWN) {
            linearSlide.dump();
            freightIn = false;
        }

        if (mechanismController.getButtonState(ToggleButton.B) == ButtonState.KEY_DOWN) {
            linearSlide.tilt();
        }

        if (mechanismController.getButtonState(ToggleButton.A) == ButtonState.KEY_DOWN) {
            linearSlide.undump();
        }

        if(bucketSensor.freightIn() && !freightIn){
            //linearSlide.tilt();
            radio.playSound(Radio.SoundFiles.FreightDetected);
            freightIn = true;

        }
        else if(!bucketSensor.freightIn() && freightIn){
            freightIn = false;
            //linearSlide.undump();
        }

        telemetry.addData("Freight In: ", freightIn);
        // endregion mechanism
    }


    /*
     * Code to run ONCE after the driver hits STOP
     */
    @Override
    public void stop() {
    }


}
