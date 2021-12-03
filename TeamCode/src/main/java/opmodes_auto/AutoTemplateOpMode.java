package opmodes_auto;

import static org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer.CameraDirection.BACK;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackableDefaultListener;
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector;

import mechanisms.AutoQueue;
import mechanisms.Carousel;
import mechanisms.DriveControl;
import mechanisms.Intake;
import mechanisms.LinearSlide;
import mechanisms.LinearSlide.SlideAction.SlideOption;


@Autonomous(name="Template Auto OpMode", group ="Autonomous")

public class AutoTemplateOpMode extends LinearOpMode {

    // put outside classes here
    private DriveControl driveControl;
    private Intake intake;
    private LinearSlide linearSlide;
    private Carousel carousel;
    private AutoQueue autoQueue;

    /**
     * Initialize outside classes during initialization
     */
    private void initialize() {
        driveControl = new DriveControl(hardwareMap, telemetry);
        intake = new Intake(hardwareMap);
        linearSlide = new LinearSlide(hardwareMap);
        carousel = new Carousel(hardwareMap);
        autoQueue = new AutoQueue();
    }

    @Override
    public void runOpMode() {

        // code to run once after initialization but prior to start being pressed
        if (!isStarted()) {
            initialize();
            linearSlide.calibrateSlide();
        }

        // code to loop before the opmode is started but after initialization
        while (!isStarted()) {
            autoQueue.addAutoAction(driveControl.getForwardAction(10, .5));
            autoQueue.addAutoAction(linearSlide.getLevelAction(SlideOption.LEVEL_1));
        }

        waitForStart();

        // runs once when the opmode is activated
        if (opModeIsActive()) {
            telemetry.update();
        }

        // loop while the opmode is active
        while (opModeIsActive()) {

            telemetry.update();
            sleep(100);
        }

        // code to run once when stop is called
        if (isStopRequested()) {

        }
    }

    /**
     * Runs the queued actions to completion
     * @param autoQueue the queue to run
     */
    public void runQueue(AutoQueue autoQueue) {
        while (autoQueue.updateQueue()) {
            sleep(100);
        }
    }
}