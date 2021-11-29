package opmodes_vision_testing;

import android.util.Log;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import mechanisms.Intake;
import mechanisms.LinearSlide;
import teamcode.GamepadController;
import teamcode.GamepadController.ButtonState;
import teamcode.GamepadController.ToggleButton;
import vision.CVManager;
import vision.ElementCVPipeline;
import vision.IntakeCVPipeline;
import vision.ObjectCVPipeline;
import vision.RedCVPipeline;


@TeleOp(name="Testing: Intake CV Test", group="Testing")
public class IntakeVisionOpMode extends OpMode {
    // tag is used in logcat logs (Log.d()) to identify where the log is coming from
    // logcat is basically like System.out.print (standard output) except through adb
    private static final String TAG = "teamcode.opencv_opmode"; // put the name of the opmode

    // put any outside classes you need to use here
    private GamepadController movementController;
    private GamepadController mechanismController;
    private CVManager cvManager;


    // put any measurements here
    private final double inchesToMM = 25.4; // this is correct
    private final double toCameraCenter = 1.25; // inches from bottom of logitech c615 to actual camera
    private final double cameraPlatform = 10.5; // random value
    private final double cameraHeight = (cameraPlatform + toCameraCenter) * inchesToMM;
    private static final int fieldLength = 3660; // mm (this is correct)
    private IntakeCVPipeline pipeline;
    private Intake intake;
    private LinearSlide slide;



    // code to run once when driver hits init on phone
    @Override
    public void init() {
        movementController = new GamepadController(gamepad1);
        mechanismController = new GamepadController(gamepad2);
        cvManager = new CVManager(hardwareMap, "Webcam 1");
        pipeline = new IntakeCVPipeline(cvManager.getWebcam());
        cvManager.initializeCamera(pipeline);
        intake = new Intake(hardwareMap);
        slide = new LinearSlide(hardwareMap);
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

        //pipeline.setObject("Ball");
        telemetry.addData("Block Exists: ", pipeline.ifBlockExists());
        telemetry.addData("Ball Exists: ", pipeline.ifBallExists());
        // telemetry.addData("biggestRectCenter", " " + pipeline.biggestRectCenter);
        if(pipeline.ifBallExists() || pipeline.ifBlockExists() && pipeline.frameCount(5)){
            intake.stop();
            if(!slide.tilted){
                slide.tilt();
            }

        }
        else{
            intake.intakeOut();
            slide.undump();
        }
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

        // do something when A is pressed
        if (movementController.getButtonState(ToggleButton.A) == ButtonState.KEY_DOWN) {
            Log.d(TAG, "button a pressed");
        }

    }


    /*
     * Code to run ONCE after the driver hits STOP
     */
    @Override
    public void stop() {
    }


}
