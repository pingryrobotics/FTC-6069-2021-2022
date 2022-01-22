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

import android.util.Log;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackableDefaultListener;

import java.util.ArrayList;
import java.util.HashMap;

import localization.FieldMap;
import localization.SpaceMap;
import localization.VuforiaManager;
import mechanisms.AutoQueue;
import mechanisms.CappingArm;
import mechanisms.Carousel;
import mechanisms.DriveControl;
import mechanisms.Intake;
import mechanisms.LinearSlide;
import mechanisms.LinearSlide.SlideAction.SlideOption;
import vision.CVManager;
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


@Autonomous(name="AutoBlueWarehouseSide", group ="Autonomous")

public class AutoBlueWarehouseSide extends LinearOpMode {


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
    private AutoQueue autoQueue;

    private CVManager cvManager;
    private CVManager intakeCvManager;
    private ElementCVPipeline pipeline;
    private IntakeCVPipeline intakePipeline;
    private CappingArm cappingArm;

    private FieldMap fieldMap;
    private static final int fieldLength = 3660;
    private static final long nanoToMilli = 1000000;
    private static final String TAG = "teamcode.autoredvuf";


    public void initialize() {
        driveControl = new DriveControl(hardwareMap, telemetry);
        intake = new Intake(hardwareMap);
        linearSlide = new LinearSlide(hardwareMap, telemetry);
        carousel = new Carousel(hardwareMap);
        autoQueue = new AutoQueue();
        cappingArm = new CappingArm(hardwareMap, telemetry);
        cvManager = new CVManager(hardwareMap, "Webcam 2", true);
        intakeCvManager = new CVManager(hardwareMap, "Webcam 1", false);
        pipeline = new ElementCVPipeline(cvManager.getWebcam());
        intakePipeline = new IntakeCVPipeline(intakeCvManager.getWebcam());
        cvManager.initializeCamera(pipeline);
        intakeCvManager.initializeCamera(intakePipeline);
    }

    @Override
    public void runOpMode() {
        telemetry.addData("Initialization status", "In progress");
        telemetry.update();
        initialize();



        telemetry.addData("Initialization status", "Complete");
        telemetry.update();


        waitForStart();
        if (opModeIsActive()) {

            double cnt = 0;
            for (int i = 0; i < 20; i++) {
                cnt += pipeline.getObjLevel();
            }

            int objLevel = (int) (cnt / 20);

            telemetry.addData("Level found", objLevel);
            telemetry.update();

            cappingArm.spinIn();

            cvManager.stopPipeline();


            linearSlide.tilt();
            telemetry.addData("starting angle", driveControl.getGyroAngle());
            telemetry.update();

            ElapsedTime rtime = new ElapsedTime();
            rtime.reset();
            sleep(11000);

            autoQueue.addAutoAction(driveControl.getForwardAction(5, 1));
            autoQueue.addAutoAction(driveControl.getStrafeAction(25, 1));
            autoQueue.addAutoAction(driveControl.getTurnPositionAction(0, 1));
//            } else if (objLevel == 1 || objLevel == 2) {
//                autoQueue.addAutoAction(driveControl.getForwardAction(7, 1));
//                autoQueue.addAutoAction(driveControl.getTurnIncrementAction(-35, 0.5));
//                autoQueue.addAutoAction(driveControl.getForwardAction(22, 1));
//
//            }

            if (objLevel == 0) {
                autoQueue.addAutoAction(driveControl.getForwardAction(15.5, 1));
                //autoQueue.addAutoAction(new DriveControl.DriveAction(DriveControl.DriveAction.DriveOption.WAIT, 700, .1, driveControl));
                autoQueue.addAutoAction(linearSlide.getLevelAction(SlideOption.LEVEL_1));
            } else if (objLevel == 1) {
                autoQueue.addAutoAction(driveControl.getForwardAction(15.5, 1));
                //autoQueue.addAutoAction(new DriveControl.DriveAction(DriveControl.DriveAction.DriveOption.WAIT, 700, .1, driveControl));
                autoQueue.addAutoAction(linearSlide.getLevelAction(SlideOption.LEVEL_2));
            } else if (objLevel == 2) {
                autoQueue.addAutoAction(driveControl.getForwardAction(17, 1));
                //autoQueue.addAutoAction(new DriveControl.DriveAction(DriveControl.DriveAction.DriveOption.WAIT, 700, .1, driveControl));
                autoQueue.addAutoAction(linearSlide.getLevelAction(SlideOption.LEVEL_3));
            }
            //autoQueue.addAutoAction(driveControl.getForwardAction(inches, 1));
            runQueue(autoQueue);

            linearSlide.dump();
            sleep(500);
            linearSlide.undump();
            sleep(500);
            autoQueue.addAutoAction(linearSlide.getLevelAction(SlideOption.LEVEL_0));
            autoQueue.addAutoAction(new DriveControl.DriveAction(DriveControl.DriveAction.DriveOption.WAIT, 700, .1, driveControl));
            runQueue(autoQueue);
            if (objLevel == 0) {
                autoQueue.addAutoAction(driveControl.getForwardAction(-14.5, 1));
                autoQueue.addAutoAction(new DriveControl.DriveAction(DriveControl.DriveAction.DriveOption.WAIT, 700, .1, driveControl));
//                autoQueue.addAutoAction(linearSlide.getLevelAction(SlideOption.LEVEL_1));
            } else if (objLevel == 1) {
                autoQueue.addAutoAction(driveControl.getForwardAction(-14.5, 1));
                autoQueue.addAutoAction(new DriveControl.DriveAction(DriveControl.DriveAction.DriveOption.WAIT, 700, .1, driveControl));
//                autoQueue.addAutoAction(linearSlide.getLevelAction(SlideOption.LEVEL_2));
            } else if (objLevel == 2) {
                autoQueue.addAutoAction(driveControl.getForwardAction(-15, 1));
                autoQueue.addAutoAction(new DriveControl.DriveAction(DriveControl.DriveAction.DriveOption.WAIT, 700, .1, driveControl));
//                autoQueue.addAutoAction(linearSlide.getLevelAction(SlideOption.LEVEL_3));
            }
            //autoQueue.addAutoAction(driveControl.getForwardAction(inches, 1));
            autoQueue.addAutoAction(driveControl.getTurnIncrementAction(90, 0.5));
            runQueue(autoQueue);

            driveControl.setStrafeVelocity(.5);
            sleep(1200);
            driveControl.setStrafeVelocity(0);

            autoQueue.addAutoAction(driveControl.getForwardAction(-60, 0.8));
            autoQueue.addAutoAction(driveControl.getStrafeAction(-25, 1));

            runQueue(autoQueue);



//
//
//            driveControl.setStrafeVelocity(.5);
//            sleep(1500);
//            driveControl.setStrafeVelocity(0);
//            if (objLevel == 0) {
//                autoQueue.addAutoAction(driveControl.getForwardAction(-20, 1));
//                //autoQueue.addAutoAction(driveControl.getStrafeAction(24, 1));
//                //autoQueue.addAutoAction(driveControl.getStrafeAction(-5, 1));
////                autoQueue.addAutoAction(driveControl.getTurnAction(90, 1));
////                autoQueue.addAutoAction(driveControl.getStrafeAction(3, 1));
//            } else if (objLevel == 1 || objLevel == 2) {
//                //autoQueue.addAutoAction(driveControl.getStrafeAction(-3, 1));
//                autoQueue.addAutoAction(driveControl.getForwardAction(-25, 0.8));
//                //autoQueue.addAutoAction(driveControl.getStrafeAction(-30, 0.5));
//                //autoQueue.addAutoAction(driveControl.getForwardAction(-22, 0.8));
////                autoQueue.addAutoAction(driveControl.getTurnAction(37, 0.5));
////                autoQueue.addAutoAction(driveControl.getForwardAction(-6, 1));
//
//            }
//
//            // carousel spin would go here if our partner isn't doing it
//            // we probably wouldn't use this opmode unless partner can do carousel since you have
//            // to move all the way to the carousel and then back in order to park
//
//            // loop iterates once every cycle
//            double inchesMoved = 0;
//            while (rtime.time() <= 25000) { // this gives us at least 5 seconds to park
//                // move back a bit
//                autoQueue.addAutoAction(driveControl.getForwardAction(inchesMoved, 1);
//                autoQueue.addAutoAction(driveControl.getTurnAction(-90, 1));
//                autoQueue.addAutoAction(driveControl.getForwardAction(5, 1));
//                autoQueue.addAutoAction(driveControl.getStrafeAction(-24, 1));
//                autoQueue.addAutoAction(driveControl.getForwardAction(20, 1));
//                autoQueue.addAutoAction(driveControl.getForwardAction(-20, 1));
//                autoQueue.addAutoAction(driveControl.getStrafeAction(24, 1));
//                autoQueue.addAutoAction(driveControl.getForwardAction(-5, 1));
//                autoQueue.addAutoAction(driveControl.getTurnAction(90, 1));
//                autoQueue.addAutoAction(driveControl.getStrafeAction(3, 1));
//                // finish executing instructions
//                while (autoQueue.updateQueue()) {
//                    sleep(100);
//                }
//
//
//                // start intake
//                intake.intakeIn();
//
//                // moving towards warehouse until getting an element
//                while (!intakePipeline.ifBallExists() && !intakePipeline.ifBlockExists()) {
//                    while (autoQueue.updateQueue()) {
//                        sleep(10);
//                    }
//                    autoQueue.addAutoAction(driveControl.getForwardAction(-2, 1));
//                    inchesMoved -= 2;
//                    if (inchesMoved >= 80) { // couldn't intake anything
//                                             // so we can just park for the rest
//                        sleep(100000);
//                    }
//                }
//                intake.intakeOut();
//                autoQueue.addAutoAction(driveControl.getForwardAction(inchesMoved, 1));
//                while (autoQueue.updateQueue()) {
//                    sleep(100);
//                }
//                intake.stop();
////                autoQueue.addAutoAction(driveControl.getStrafeAction(26, 1));
////                autoQueue.addAutoAction(driveControl.getTurnAction(90 - firstAngle, 0.5));
////                autoQueue.addAutoAction(driveControl.getForwardAction(2, 1));
//                //utoQueue.addAutoAction(linearSlide.getLevelAction(SlideOption.LEVEL_1));
//                while (autoQueue.updateQueue()) {
//                    sleep(100);
//                }
//                linearSlide.dump();
//                sleep(1500);
//                linearSlide.undump();
//                sleep(1500);
//                autoQueue.addAutoAction(linearSlide.getLevelAction(SlideOption.LEVEL_0));
//                while (autoQueue.updateQueue()) {
//                    sleep(100);
//                }
//            }
//
//            // park in warehouse
//            autoQueue.addAutoAction(driveControl.getTurnAction(-(90 - firstAngle), 0.5));
//            autoQueue.addAutoAction(driveControl.getStrafeAction(-26, 1)); // go against wall
//            autoQueue.addAutoAction(driveControl.getForwardAction(-40, 1)); // drive into warehouse
//            while (autoQueue.updateQueue()) {
//                sleep(100);
//            }
//            telemetry.update();
//        }
//        while (opModeIsActive()) {
//            autoQueue.updateQueue();
//            telemetry.update();
//            sleep(100);
//        }
//    }


        }

        while (opModeIsActive()) {
            telemetry.update();
        }

        // code to run once when stop is called
        if (isStopRequested()) {

        }
    }
    /**
     * Runs the queued actions to completion
     * also updates vuforia
     * @param autoQueue the queue to run
     */
    public void runQueue(AutoQueue autoQueue) {
        while (autoQueue.updateQueue() && opModeIsActive()) {
            sleep(100);
            telemetry.update();
        }
    }
}