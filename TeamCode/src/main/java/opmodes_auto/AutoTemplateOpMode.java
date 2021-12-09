package opmodes_auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

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
        linearSlide = new LinearSlide(hardwareMap, telemetry);
        carousel = new Carousel(hardwareMap);
        autoQueue = new AutoQueue();
    }

    @Override
    public void runOpMode() {

        telemetry.addData("caption", "data");

        // code to run once after initialization but prior to start being pressed
        if (!isStarted()) {
            initialize();
        }

        // code to loop before the opmode is started but after initialization
        while (!isStarted()) {
        }

        waitForStart();

        // runs once when the opmode is activated
        if (opModeIsActive()) {
            autoQueue.addAutoAction(driveControl.getForwardAction(10, .5));
            autoQueue.addAutoAction(driveControl.getTurnIncrementAction(-75, .5));
            autoQueue.addAutoAction(linearSlide.getLevelAction(SlideOption.LEVEL_1));
            runQueue(autoQueue);
            telemetry.update();
            sleep(100);

            telemetry.update();
        }

        // loop while the opmode is active
        while (opModeIsActive()) {

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