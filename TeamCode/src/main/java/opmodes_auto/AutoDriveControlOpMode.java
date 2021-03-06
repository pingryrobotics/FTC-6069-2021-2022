package opmodes_auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackableDefaultListener;
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;


import static org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer.CameraDirection.BACK;

import mechanisms.Carousel;
import mechanisms.DriveControl;
import mechanisms.Intake;
import mechanisms.LinearSlide;

/**
 * TODO:
 * - Test this code with printed VuMarks on field
 * - Find Camera mounting spot and put in the code
 * - Figure out PID loop stuff
 */

/**
 * This 2020-2021 OpMode illustrates the basics of using the Vuforia localizer to determine
 * positioning and orientation of robot on the ULTIMATE GOAL FTC field.
 * The code is structured as a LinearOpMode
 *
 * When images are located, Vuforia is able to determine the position and orientation of the
 * image relative to the camera.  This sample code then combines that information with a
 * knowledge of where the target images are on the field, to determine the location of the camera.
 *
 * From the Audience perspective, the Red Alliance station is on the right and the
 * Blue Alliance Station is on the left.
 * There are a total of five image targets for the ULTIMATE GOAL game.
 * Three of the targets are placed in the center of the Red Alliance, Audience (Front),
 * and Blue Alliance perimeter walls.
 * Two additional targets are placed on the perimeter wall, one in front of each Tower Goal.
 * Refer to the Field Setup manual for more specific location details
 *
 * A final calculation then uses the location of the camera on the robot to determine the
 * robot's location and orientation on the field.
 *
 * @see VuforiaLocalizer
 * @see VuforiaTrackableDefaultListener
 * see  ultimategoal/doc/tutorial/FTC_FieldCoordinateSystemDefinition.pdf
 *
 * Use Android Studio to Copy this Class, and Paste it into your team's code folder with a new name.
 * Remove or comment out the @Disabled line to add this opmode to the Driver Station OpMode list.
 *
 * IMPORTANT: In order to use this OpMode, you need to obtain your own Vuforia license key as
 * is explained below.
 */


@Autonomous(name="AutoRed Place Bucket", group ="Autonomous")

public class AutoDriveControlOpMode extends LinearOpMode {

    // IMPORTANT: If you are using a USB WebCam, you must select CAMERA_CHOICE = BACK; and PHONE_IS_PORTRAIT = false;
    private static final VuforiaLocalizer.CameraDirection CAMERA_CHOICE = BACK;
    private static final boolean PHONE_IS_PORTRAIT = false;


    private static final String TFOD_MODEL_ASSET = "FreightFrenzy.tflite";
    private static final String LABEL_FIRST_ELEMENT = "Quad";
    private static final String LABEL_SECOND_ELEMENT = "Single";
    /*
     * IMPORTANT: You need to obtain your own license key to use Vuforia. The string below with which
     * 'parameters.vuforiaLicenseKey' is initialized is for illustration only, and will not function.
     * A Vuforia 'Development' license key, can be obtained free of charge from the Vuforia developer
     * web site at https://developer.vuforia.com/license-manager.
     *
     * Vuforia license keys are always 380 characters long, and look as if they contain mostly
     * random data. As an example, here is a example of a fragment of a valid key:
     *      ... yIgIzTqZ4mWjk9wd3cZO9T1axEqzuhxoGlfOOI2dRzKS4T0hQ8kT ...
     * Once you've obtained a license key, copy the string from the Vuforia web site
     * and paste it in to your code on the next line, between the double quotes.
     */
    private static final String VUFORIA_KEY = "AVnPa5X/////AAABmUhfO30V7UvEiFRLKEAy25cwZ/uQDK2M0Z8GllUIhUOhFey2tkv1iKXqY4JdAjTHq4vlEUqn4F9sgeh+1ZiBsoPbGnSCdRnnHyQKmIU1hRoCyh24OvMfaG+6JQnpWlHorMoGWAqcEGt1+GXI9x3v2GLwooT1Dv/biDVn2DKar6tKms7EEEwIWkMN5YVaiQo53rbSSajpWuEROYYIrUrgzmgyorf4ngUWmjPrWHPES0OkUW6YVrZXoGT3Rwkiyl0Y7j5Rc5qT7iFBmI4v6E9udfPpnIsYrGzlhcL7GqHBntY8TuMYMTNIcklCO+ATWT4guojTwEOaNK+bVHG3XXxJsodhBK+Tbf7QX262rIbWvQto";

    // Since ImageTarget trackables use mm to specifiy their dimensions, we must use mm for all the physical dimension.
    // We will define some constants and conversions here
    private static final float mmPerInch        = 25.4f;
    private static final float mmTargetHeight   = (6) * mmPerInch;          // the height of the center of the target image above the floor

    // Constants for perimeter targets
    private static final float halfField = 72 * mmPerInch;
    private static final float quadField  = 36 * mmPerInch;

    // Class Members
    private OpenGLMatrix lastLocation = null;
    private VuforiaLocalizer vuforia = null;

    public TFObjectDetector tfod;

    /**
     * This is the webcam we are to use. As with other hardware devices such as motors and
     * servos, this device is identified using the robot configuration tool in the FTC application.
     */
    WebcamName webcamName = null;

    private boolean targetVisible = false;
    private float phoneXRotate    = 0;
    private float phoneYRotate    = 0;
    private float phoneZRotate    = 0;

//    private MecanumDrive mecanumDrive = new MecanumDrive(hardwareMap);
//    private Outtake outtake = new Outtake(hardwareMap);
//    private Intake intake = new Intake(hardwareMap);
//    private WobbleMech wobbleMech = new WobbleMech(hardwareMap);

    // Variable of which square for autonomous (A,B,C), (1,2,3) respectivelly
    private int square = 0;

    private DriveControl driveControl;
    private Intake intake;
    private LinearSlide linearSlide;
    private Carousel carousel;

    @Override
    public void runOpMode() {
        driveControl = new DriveControl(hardwareMap, telemetry);
        intake = new Intake(hardwareMap);
        linearSlide = new LinearSlide(hardwareMap, telemetry);
        carousel = new Carousel(hardwareMap);
//        webcamName = hardwareMap.get(WebcamName.class, "Webcam 1");
//        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
//        OpenCvWebcam webcam = OpenCvCameraFactory.getInstance().createWebcam(webcamName, cameraMonitorViewId);
//        webcam.setPipeline(new ContourPipeline(webcam));
//        webcam.setMillisecondsPermissionTimeout(2500); // Timeout for obtaining permission is configurable. Set before opening.
//        webcam.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener()
//        {
//            @Override
//            public void onOpened()
//            {
//                webcam.startStreaming(1280, 720, OpenCvCameraRotation.UPRIGHT);
//            }
//            @Override
//            public void onError(int errorCode)
//            {
//                /*
//                 * This will be called if the camera could not be opened
//                 */
//            }
//        });
        waitForStart();

        if (opModeIsActive()) {
//            telemetry.addData("Level found", ContourPipeline.getObjLevel());
//            int objLevel = ContourPipeline.getObjLevel();
            LinearSlide slide = new LinearSlide(hardwareMap, telemetry);
            driveControl.moveXDist(46, 0.5);
            sleep(3000);
            slide.level3();
            sleep(10000);
            slide.dump();
            sleep(1000);
            driveControl.moveXDist(-21, 0.5);
            sleep(2000);
            driveControl.moveYDist(80, 1);

            // move to linear slide and put square on level
//            driveControl.moveYDist(20, 1); // change
//            sleep(1000);
//            driveControl.turnAngle(20, 1); // change
//            sleep(1000);
//            if (objLevel == 0) {
//                linearSlide.level1();
//            } else if (objLevel == 1) {
//                linearSlide.level2();
//            } else if (objLevel == 2){
//                linearSlide.level3();
//            } else {
//
//            }
//            linearSlide.dump();
//            sleep(1000);
//            linearSlide.undump();
//            sleep(1000);
//
//            // move to carousel and spin it
//            driveControl.turnAngle(-180, 1); // change
//            sleep(1000);
//            driveControl.moveYDist(22, 1); // change
//            sleep(1000);
//            carousel.spinAngle(360);
//            sleep(1000);
//
//            // park in warehouse
//            driveControl.turnAngle(-110, 1); // change
//            sleep(1000);
//            driveControl.moveYDist(100, 1); // change
//            sleep(1000);

            telemetry.update();
        }
        while (opModeIsActive()) {

            telemetry.update();
            sleep(100);
        }
    }
}