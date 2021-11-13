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

package opmodes;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import org.firstinspires.ftc.robotcore.external.matrices.VectorF;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackable;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackableDefaultListener;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackables;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvWebcam;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;


import java.util.ArrayList;
import java.util.List;

import static org.firstinspires.ftc.robotcore.external.navigation.AngleUnit.DEGREES;
import static org.firstinspires.ftc.robotcore.external.navigation.AxesOrder.XYZ;
import static org.firstinspires.ftc.robotcore.external.navigation.AxesOrder.YZX;
import static org.firstinspires.ftc.robotcore.external.navigation.AxesReference.EXTRINSIC;
import static org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer.CameraDirection.BACK;

import mechanisms.Carousel;
import mechanisms.DriveControl;
import mechanisms.Intake;
import mechanisms.LinearSlide;
import vision.ContourPipeline;

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


@Autonomous(name="AutoRed", group ="Autonomous")

public class AutoRed extends LinearOpMode {

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
        driveControl = new DriveControl(hardwareMap);
		intake = new Intake(hardwareMap);
		linearSlide = new LinearSlide(hardwareMap);
		carousel = new Carousel(hardwareMap);
		webcamName = hardwareMap.get(WebcamName.class, "Webcam 1");
		int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
		OpenCvWebcam webcam = OpenCvCameraFactory.getInstance().createWebcam(webcamName, cameraMonitorViewId);
		webcam.setPipeline(new ContourPipeline(webcam));
		webcam.setMillisecondsPermissionTimeout(2500); // Timeout for obtaining permission is configurable. Set before opening.
		webcam.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener()
		{
			@Override
			public void onOpened()
			{
				webcam.startStreaming(1280, 720, OpenCvCameraRotation.UPRIGHT);
			}
			@Override
			public void onError(int errorCode)
			{
			/*
			* This will be called if the camera could not be opened
			*/
			}
		});
		waitForStart();

		if (opModeIsActive()) {
			telemetry.addData("Level found", ContourPipeline.getObjLevel());
            int objLevel = ContourPipeline.getObjLevel();

            // move to linear slide and put square on level
            driveControl.moveYDist(1, 100); // change
            driveControl.turnAngle(20, 100); // change
//            if (objLevel == 0) {
//                linearSlide.level1();
//            } else if (objLevel == 1) {
//                linearSlide.level2();
//            } else if (objLevel == 2){
//                linearSlide.level3();
//            } else {
//
//            }
            linearSlide.dump();
            sleep(1000);
            linearSlide.undump();

            // move to carousel and spin it
            driveControl.turnAngle(-100, 100); // change
            driveControl.moveYDist(1, 100); // change
            carousel.spinAngle(360);

            // park in warehouse
            driveControl.turnAngle(80, 100); // change
            driveControl.moveYDist(1, 100); // change

            telemetry.update();
		}
		while (opModeIsActive()) {

			telemetry.update();
			sleep(100);
		}
    }

    public void old() {
        /*
         * Retrieve the camera we are to use.
         */
        initVuforia();
        webcamName = hardwareMap.get(WebcamName.class, "Webcam 1");

        //Create new Mecanum Drive for encoder movement.

        /*
         * Configure Vuforia by creating a Parameter object, and passing it to the Vuforia engine.
         * We can pass Vuforia the handle to a camera preview resource (on the RC phone);
         * If no camera monitor is desired, use the parameter-less constructor instead (commented out below).
         */
        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters(cameraMonitorViewId);

        // VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters();

        parameters.vuforiaLicenseKey = VUFORIA_KEY;

        /**
         * We also indicate which camera on the RC we wish to use.
         */
        parameters.cameraName = webcamName;

        // Make sure extended tracking is disabled for this example.
        parameters.useExtendedTracking = false;

        //  Instantiate the Vuforia engine
        vuforia = ClassFactory.getInstance().createVuforia(parameters);

        // Load the data sets for the trackable objects. These particular data
        // sets are stored in the 'assets' part of our application.
        VuforiaTrackables targetsUltimateGoal = this.vuforia.loadTrackablesFromAsset("UltimateGoal");
        VuforiaTrackable blueTowerGoalTarget = targetsUltimateGoal.get(0);
        blueTowerGoalTarget.setName("Blue Tower Goal Target");
        VuforiaTrackable redTowerGoalTarget = targetsUltimateGoal.get(1);
        redTowerGoalTarget.setName("Red Tower Goal Target");
        VuforiaTrackable redAllianceTarget = targetsUltimateGoal.get(2);
        redAllianceTarget.setName("Red Alliance Target");
        VuforiaTrackable blueAllianceTarget = targetsUltimateGoal.get(3);
        blueAllianceTarget.setName("Blue Alliance Target");
        VuforiaTrackable frontWallTarget = targetsUltimateGoal.get(4);
        frontWallTarget.setName("Front Wall Target");

        // For convenience, gather together all the trackable objects in one easily-iterable collection */
        List<VuforiaTrackable> allTrackables = new ArrayList<VuforiaTrackable>();
        allTrackables.addAll(targetsUltimateGoal);

        /**
         * In order for localization to work, we need to tell the system where each target is on the field, and
         * where the phone resides on the robot.  These specifications are in the form of <em>transformation matrices.</em>
         * Transformation matrices are a central, important concept in the math here involved in localization.
         * See <a href="https://en.wikipedia.org/wiki/Transformation_matrix">Transformation Matrix</a>
         * for detailed information. Commonly, you'll encounter transformation matrices as instances
         * of the {@link OpenGLMatrix} class.
         *
         * If you are standing in the Red Alliance Station looking towards the center of the field,
         *     - The X axis runs from your left to the right. (positive from the center to the right)
         *     - The Y axis runs from the Red Alliance Station towards the other side of the field
         *       where the Blue Alliance Station is. (Positive is from the center, towards the BlueAlliance station)
         *     - The Z axis runs from the floor, upwards towards the ceiling.  (Positive is above the floor)
         *
         * Before being transformed, each target image is conceptually located at the origin of the field's
         *  coordinate system (the center of the field), facing up.
         */

        //Set the position of the perimeter targets with relation to origin (center of field)
        redAllianceTarget.setLocation(OpenGLMatrix
                .translation(0, -halfField, mmTargetHeight)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0, 180)));

        blueAllianceTarget.setLocation(OpenGLMatrix
                .translation(0, halfField, mmTargetHeight)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0, 0)));
        frontWallTarget.setLocation(OpenGLMatrix
                .translation(-halfField, 0, mmTargetHeight)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0 , 90)));

        // The tower goal targets are located a quarter field length from the ends of the back perimeter wall.
        blueTowerGoalTarget.setLocation(OpenGLMatrix
                .translation(halfField, quadField, mmTargetHeight)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0 , -90)));
        redTowerGoalTarget.setLocation(OpenGLMatrix
                .translation(halfField, -quadField, mmTargetHeight)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0, -90)));




        //
        // Create a transformation matrix describing where the phone is on the robot.
        //
        // NOTE !!!!  It's very important that you turn OFF your phone's Auto-Screen-Rotation option.
        // Lock it into Portrait for these numbers to work.
        //
        // Info:  The coordinate frame for the robot looks the same as the field.
        // The robot's "forward" direction is facing out along X axis, with the LEFT side facing out along the Y axis.
        // Z is UP on the robot.  This equates to a bearing angle of Zero degrees.
        //
        // The phone starts out lying flat, with the screen facing Up and with the physical top of the phone
        // pointing to the LEFT side of the Robot.
        // The two examples below assume that the camera is facing forward out the front of the robot.

        // We need to rotate the camera around it's long axis to bring the correct camera forward.
        if (CAMERA_CHOICE == BACK) {
            phoneYRotate = -90;
        } else {
            phoneYRotate = 90;
        }

        // Rotate the phone vertical about the X axis if it's in portrait mode
        if (PHONE_IS_PORTRAIT) {
            phoneXRotate = 90 ;
        }

        // Next, translate the camera lens to where it is on the robot.
        // In this example, it is centered (left to right), but forward of the middle of the robot, and above ground level.
        final float CAMERA_FORWARD_DISPLACEMENT  = 4.0f * mmPerInch;   // eg: Camera is 4 Inches in front of robot-center
        final float CAMERA_VERTICAL_DISPLACEMENT = 8.0f * mmPerInch;   // eg: Camera is 8 Inches above ground
        final float CAMERA_LEFT_DISPLACEMENT     = 0;     // eg: Camera is ON the robot's center line

        OpenGLMatrix robotFromCamera = OpenGLMatrix
                .translation(CAMERA_FORWARD_DISPLACEMENT, CAMERA_LEFT_DISPLACEMENT, CAMERA_VERTICAL_DISPLACEMENT)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, YZX, DEGREES, phoneYRotate, phoneZRotate, phoneXRotate));

        /**  Let all the trackable listeners know where the phone is.  */
        for (VuforiaTrackable trackable : allTrackables) {
            ((VuforiaTrackableDefaultListener) trackable.getListener()).setPhoneInformation(robotFromCamera, parameters.cameraDirection);
        }

        // WARNING:
        // In this sample, we do not wait for PLAY to be pressed.  Target Tracking is started immediately when INIT is pressed.
        // This sequence is used to enable the new remote DS Camera Preview feature to be used with this sample.
        // CONSEQUENTLY do not put any driving commands in this loop.
        // To restore the normal opmode structure, just un-comment the following line:

        //waitForStart();


        // First the amount of rings are found using the get rings function
        // The square variable is updated to the A,B, or C square
//        int rings = getRings();
//
//        if(rings == 4)
//        {
//          square = 3;
//        }
//
//        else if(rings == 1)
//        {
//          square = 2;
//        }
//
//        else
//        {
//          square = 1;
//        }



        // Now we make a case for each of the squares.
        // -----------SQUARE A-------------

        // the distance from the front of the robot
        // to the center of the grabber arm in inches
        // current value is an estimate

//        moveToWobbleSquare(square);
//        openAndDropArm();
//
//        targetsUltimateGoal.activate();
//
//        //DESIRED X AND Y POSITIONS (TENTATIVE --> MUST BE CHANGED)
//        int x = 10;
//        int y = 10;
//
//
//        List<Float> translationRotation = new ArrayList<Float>();
//        //First change angle
//        boolean keepGoingAngle = true;
//        double difference = 0;
//        while(keepGoingAngle)
//        {
//          if(translationRotation.get(5) == 0)
//          {
//            keepGoingAngle = false;
//          }
//
//            // Now caluclate the distance you have to turn
//            difference = 0 - translationRotation.get(5);
//
//          // turn
//          mecanumDrive.encoderTurn(difference, 0.5);
//
//          // update position
//          translationRotation = getRobotLocation(targetsUltimateGoal, allTrackables);
//        }
//
//        boolean keepGoingVertical = true;
//        while(keepGoingVertical)
//        {
//          if(translationRotation.get(1) == y)
//          {
//            keepGoingVertical = false;
//          }
//
//          // Now caluclate the distance you have to turn
//          difference = x - translationRotation.get(1);
//
//          // turn
//          mecanumDrive.moveEncoderStraight(difference, 0.5);
//
//          // update position
//          translationRotation = getRobotLocation(targetsUltimateGoal, allTrackables);
//        }
//
//
//        boolean keepGoingHorizontal = true;
//        while(keepGoingHorizontal)
//        {
//          if(translationRotation.get(0) == x)
//          {
//            keepGoingHorizontal = false;
//          }
//
//          // Now caluclate the distance you have to turn
//          difference = y - translationRotation.get(0);
//
//          // turn
//          mecanumDrive.moveEncoderStrafeRight(difference, 0.5);
//
//          // update position
//          translationRotation = getRobotLocation(targetsUltimateGoal, allTrackables);
//        }
//
//        // Disable Tracking when we are done;
//        targetsUltimateGoal.deactivate();
    }

    public List<Float> getRobotLocation(VuforiaTrackables targetsUltimateGoal, List<VuforiaTrackable> allTrackables){

          // check all the trackable targets to see which one (if any) is visible.
          targetVisible = false;
          for (VuforiaTrackable trackable : allTrackables) {
              if (((VuforiaTrackableDefaultListener)trackable.getListener()).isVisible()) {
                  telemetry.addData("Visible Target", trackable.getName());
                  targetVisible = true;
                  // getUpdatedRobotLocation() will return null if no new information is available since
                  // the last time that call was made, or if the trackable is not currently visible.
                  OpenGLMatrix robotLocationTransform = ((VuforiaTrackableDefaultListener)trackable.getListener()).getUpdatedRobotLocation();
                  if (robotLocationTransform != null) {
                      lastLocation = robotLocationTransform;
                  }
                  break;
              }
          }
        VectorF translation = lastLocation.getTranslation();
        Orientation rotation = Orientation.getOrientation(lastLocation, EXTRINSIC, XYZ, DEGREES);
          // Provide feedback as to where the robot is located (if we know).
          if (targetVisible) {
              // express position (translation) of robot in inches.

              telemetry.addData("Pos (in)", "{X, Y, Z} = %.1f, %.1f, %.1f",
                      translation.get(0) / mmPerInch, translation.get(1) / mmPerInch, translation.get(2) / mmPerInch);

              // express the rotation of the robot in degrees.

              telemetry.addData("Rot (deg)", "{Roll, Pitch, Heading} = %.0f, %.0f, %.0f", rotation.firstAngle, rotation.secondAngle, rotation.thirdAngle);
          }
          else {
              telemetry.addData("Visible Target", "none");
          }
          telemetry.update();

          // Make a new list of floats to store all 6 values in 1.
          List<Float> translationRotation = new ArrayList<Float>();
          translationRotation.add(translation.get(0) / mmPerInch);
          translationRotation.add(translation.get(1) / mmPerInch);
          translationRotation.add(translation.get(2) / mmPerInch);
          translationRotation.add(rotation.firstAngle);
          translationRotation.add(rotation.secondAngle);
          translationRotation.add(rotation.thirdAngle);

          return translationRotation;
    }

    public int getRings()
    {
      int numRings = 0;
      if (tfod != null) {
            tfod.activate();

            // The TensorFlow software will scale the input images from the camera to a lower resolution.
            // This can result in lower detection accuracy at longer distances (> 55cm or 22").
            // If your target is at distance greater than 50 cm (20") you can adjust the magnification value
            // to artificially zoom in to the center of image.  For best results, the "aspectRatio" argument
            // should be set to the value of the images used to create the TensorFlow Object Detection model
            // (typically 1.78 or 16/9).

            // Uncomment the following line if you want to adjust the magnification and/or the aspect ratio of the input images.
            //tfod.setZoom(2.5, 1.78)
        }

        /** Wait for the game to begin */
        if(opModeIsActive())
        {
          List<Recognition> updatedRecognitions = tfod.getUpdatedRecognitions();
            if (updatedRecognitions != null) {
                telemetry.addData("# Object Detected", updatedRecognitions.size());
                // step through the list of recognitions and display boundary info.
            }
            telemetry.update();
            numRings = updatedRecognitions.size();
        }
        return numRings;
    }

    // moves to the specified square
//    private void moveToWobbleSquare(int square) {
//        float lengthToGrabber = 6;
//        // forward distance to center of square
//        double[] distanceToSquare = {82.625, 106, 125.875};
//        double power = 0.5;
//        float strafeDistance = -6;
//        mecanumDrive.moveEncoderStraight(distanceToSquare[square - 1], power);
//        sleep(100);
//        mecanumDrive.moveEncoderStrafeRight(strafeDistance, 0.5);
//        sleep(100);
//
//    }

      //
      // if(square == 1)
      // {
      //   // First go straight to the square
      //   mecanumDrive.moveEncorderStraight(82.625, 0.5);
      //
      //   // Then turn -90 degrees.
      //   // mecanumDrive.encoderTurn(-90, 0.5);
      //   // move left about 5 inches
      //   mecanumDrive.moveEncoderStrafeRight(-5., 0.5);
      //
      //   // This is an estimate, but then go forward ten inches.
      //   // mecanumDrive.moveEncorderStraight(5, 0.5);
      //
      //   // The wobble now has to be left there, so it should go
      //   mecanumDrive.moveEncorderStraight(-5, 0.5);
      // }
      //
      // else if(square == 2)
      // {
      //   // First go straight to the square
      //   mecanumDrive.moveEncorderStraight(106, 0.5);
      //
      //   // Then turn -90 degrees.
      //   mecanumDrive.encoderTurn(90, 0.5);
      //
      //   // This is an estimate, but then go forward ten inches.
      //   mecanumDrive.moveEncorderStraight(5, 0.5);
      //
      //   // The wobble now has to be left there, so it should go
      //   mecanumDrive.moveEncorderStraight(-5, 0.5);
      // }
      //
      // else if(square == 3)
      // {
      //   // First go straight to the square
      //   mecanumDrive.moveEncorderStraight(125.875, 0.5);
      //
      //   // Then turn -90 degrees.
      //   mecanumDrive.encoderTurn(-90, 0.5);
      //
      //   // This is an estimate, but then go forward ten inches.
      //   mecanumDrive.moveEncorderStraight(5, 0.5);
      //
      //   // The wobble now has to be left there, so it should go
      //   mecanumDrive.moveEncorderStraight(-5, 0.5);
      // }

    private void initTfod() {
       int tfodMonitorViewId = hardwareMap.appContext.getResources().getIdentifier(
               "tfodMonitorViewId", "id", hardwareMap.appContext.getPackageName());
       TFObjectDetector.Parameters tfodParameters = new TFObjectDetector.Parameters(tfodMonitorViewId);
       tfodParameters.minResultConfidence = 0.8f;
        TFObjectDetector tfod = ClassFactory.getInstance().createTFObjectDetector(tfodParameters, vuforia);
       tfod.loadModelFromAsset(TFOD_MODEL_ASSET, LABEL_FIRST_ELEMENT, LABEL_SECOND_ELEMENT);
   }

    /**
     * Initialize the Vuforia localization engine.
     */
    private void initVuforia() {
        /*
         * Configure Vuforia by creating a Parameter object, and passing it to the Vuforia engine.
         */
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters();

        parameters.vuforiaLicenseKey = VUFORIA_KEY;
        parameters.cameraName = hardwareMap.get(WebcamName.class, "Webcam 1");

        //  Instantiate the Vuforia engine
        vuforia = ClassFactory.getInstance().createVuforia(parameters);

        // Loading trackables is not necessary for the TensorFlow Object Detection engine.
    }

//    private void shootRings() {
//        outtake.pushRing();
//        sleep(500);
//        outtake.retract();
//        sleep(500);
//    }
//
//    private void openAndDropArm() {
//
//        wobbleMech.down();
//        sleep(500);
//        wobbleMech.letGo();
//    }
//
//    private void closeAndRaiseArm() {
//        wobbleMech.grab();
//        sleep(300);
//        wobbleMech.up();
//    }

}