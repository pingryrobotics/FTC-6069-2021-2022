/* Copyright (c) 2019 FIRST. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of FIRST nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package opmodes_auto;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackableDefaultListener;
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;


import static org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer.CameraDirection.BACK;

import java.util.ArrayList;
import java.util.HashMap;

import RoadRunner.DriveConstants;
import localization.FieldMap;
import localization.SpaceMap;
import localization.VuforiaManager;
import mechanisms.CappingArm;
import mechanisms.Carousel;
import mechanisms.DriveControl;
import mechanisms.Intake;
import mechanisms.LinearSlide;
import mechanisms.AutoQueue;
import mechanisms.LinearSlide.SlideAction.SlideOption;
import mechanisms.RoadRunnerMecanumDrive;
import vision.CVManager;
import vision.ColorSensorManager;
import vision.ElementCVPipeline;
import vision.IntakeCVPipeline;

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


@Autonomous(name="AutoRedStorageSmart", group ="Autonomous")

public class AutoRedStorageSmart extends LinearOpMode {

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
    private ElementCVPipeline pipeline;
    private CVManager cvManager;
    private AutoQueue autoQueue;
    private CappingArm cappingArm;
    private ColorSensorManager colorSensor;
    private RoadRunnerMecanumDrive mecanumDrive;

    public void initialize() {
        driveControl = new DriveControl(hardwareMap, telemetry);
        intake = new Intake(hardwareMap);
        linearSlide = new LinearSlide(hardwareMap, telemetry);
        carousel = new Carousel(hardwareMap);
        cappingArm = new CappingArm(hardwareMap, telemetry);
        autoQueue = new AutoQueue();
        cvManager = new CVManager(hardwareMap, "Webcam 2", true);
//        intakeCvManager = new CVManager(hardwareMap, "Webcam 1", false);
        colorSensor = new ColorSensorManager(hardwareMap, "Color Sensor 1");
        pipeline = new ElementCVPipeline(cvManager.getWebcam());
//        intakePipeline = new IntakeCVPipeline(intakeCvManager.getWebcam());
        cvManager.initializeCamera(pipeline);
        mecanumDrive = new RoadRunnerMecanumDrive(hardwareMap);
//        intakeCvManager.initializeCamera(intakePipeline);

    }

    @Override
    public void runOpMode() {
        telemetry.addData("Initialization status", "In progress");
        telemetry.update();
        initialize();



        telemetry.addData("Initialization status", "Complete");
        telemetry.update();

        cappingArm.spinIn();
        linearSlide.tilt();
        linearSlide.calibrateSlide();

        waitForStart();
        if (opModeIsActive()) {

            double cnt = 0;
            for (int i = 0; i < 20; i++) {
                cnt += pipeline.getObjLevel();
            }

            int objLevel = (int) (cnt / 20);

            telemetry.addData("Level found", objLevel);
            telemetry.update();



            cvManager.stopPipeline();

            telemetry.addData("starting angle", driveControl.getGyroAngle());
            telemetry.update();

            ElapsedTime rtime = new ElapsedTime();
            rtime.reset();

            Pose2d startPose = new Pose2d(-26.8, -72.1, Math.toRadians(90));
            mecanumDrive.setPoseEstimate(startPose);

            Trajectory toCarousel = mecanumDrive.trajectoryBuilder(startPose)
                    .splineToLinearHeading(new Pose2d(-70, -67, Math.toRadians(90)), Math.toRadians(90),
                            mecanumDrive.getVelocityConstraint(30, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                            mecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL))
                    .build();

            mecanumDrive.followTrajectory(toCarousel);
//            mecanumDrive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
//            mecanumDrive.setWeightedDrivePower(
//                    new Pose2d(0,
//                            0.5,
//                            0
//                    )
//            );
//            sleep(500);
//            mecanumDrive.setWeightedDrivePower(
//                    new Pose2d(0,
//                            0,
//                            0
//                    )
//            );
//            mecanumDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            carousel.spinPower(-0.25);
            sleep(4000);
            carousel.stop();

            mecanumDrive.setPoseEstimate(new Pose2d(-51.761,-64.169, -4.78));

            Trajectory aroundTSE = mecanumDrive.trajectoryBuilder(mecanumDrive.getPoseEstimate())
                    .splineToLinearHeading(new Pose2d(-60, -25, Math.toRadians(90)), Math.toRadians(90))
                    .build();
            mecanumDrive.followTrajectory(aroundTSE);

            Trajectory dump;
            if(objLevel == 0){
                linearSlide.level1();
                dump = mecanumDrive.trajectoryBuilder(aroundTSE.end())
                        .splineToLinearHeading(new Pose2d(-20.1,-19.1, Math.toRadians(-20)),Math.toRadians(90))
                        .build();
            }
            else if(objLevel == 1){
                linearSlide.level2();
                dump = mecanumDrive.trajectoryBuilder(aroundTSE.end())
                        .splineToLinearHeading(new Pose2d(-19.6 ,-19.3,  Math.toRadians(-20)),Math.toRadians(90))
                        .build();
            }
            else{
                linearSlide.level3();
                dump = mecanumDrive.trajectoryBuilder(aroundTSE.end())
                        .splineToLinearHeading(new Pose2d(-18.1 ,-19.6,  Math.toRadians(-20)),Math.toRadians(90))
                        .build();
            }
            mecanumDrive.followTrajectory(dump);
            linearSlide.dump();
            sleep(500);
            linearSlide.undump();
            sleep(500);
            linearSlide.level0();
            sleep(1500);

            Trajectory straighten = mecanumDrive.trajectoryBuilder(dump.end())
                    .splineToLinearHeading(new Pose2d(dump.end().getX(), dump.end().getY(), Math.toRadians(0)), Math.toRadians(-20))
                    .build();

            mecanumDrive.followTrajectory(straighten);

            Trajectory moveLeft = mecanumDrive.trajectoryBuilder(straighten.end())
                    .strafeLeft(35)
                    .build();
            mecanumDrive.followTrajectory(moveLeft);

            Trajectory aroundHub = mecanumDrive.trajectoryBuilder(moveLeft.end())
                    .splineToLinearHeading(new Pose2d(10, 0, Math.toRadians(0)), Math.toRadians(0))
                    .build();
            mecanumDrive.followTrajectory(aroundHub);

            Trajectory toBars = mecanumDrive.trajectoryBuilder(aroundHub.end())
                    .strafeRight(50)
                    .build();
            mecanumDrive.followTrajectory(toBars);

            Trajectory finalParkOverBars = mecanumDrive.trajectoryBuilder(toBars.end())
                    .forward(60)
                    .build();
            mecanumDrive.followTrajectory(finalParkOverBars);
        }
    }
    /**
     * Runs the queued actions to completion
     * also updates vuforia
     * @param autoQueue the queue to run
     */
    public void runQueue(AutoQueue autoQueue) {
        while (autoQueue.updateQueue()) {
            sleep(100);
            telemetry.update();
        }
    }
}