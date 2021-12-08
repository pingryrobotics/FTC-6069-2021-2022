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

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackableDefaultListener;
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ElapsedTime;


import static org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer.CameraDirection.BACK;

import mechanisms.Carousel;
import mechanisms.DriveControl;
import mechanisms.Intake;
import mechanisms.LinearSlide;
import mechanisms.AutoQueue;
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


@Autonomous(name="AutoRedWarehouseSide", group ="Autonomous")
public class AutoRedWarehouseSide extends LinearOpMode {


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
    private IntakeCVPipeline intakePipeline;
    private CVManager cvManager;
    private CVManager intakeCvManager;
    private AutoQueue autoQueue;

    @Override
    public void runOpMode() {
        telemetry.addData("caption", "value");
        driveControl = new DriveControl(hardwareMap, telemetry);
        intake = new Intake(hardwareMap);
        linearSlide = new LinearSlide(hardwareMap, telemetry);
        carousel = new Carousel(hardwareMap);
        cvManager = new CVManager(hardwareMap, "Webcam 1");
        intakeCvManager = new CVManager(hardwareMap, "Webcam 2");
        pipeline = new ElementCVPipeline(cvManager.getWebcam());
        intakePipeline = new IntakeCVPipeline(intakeCvManager.getWebcam());
        cvManager.initializeCamera(pipeline);
        intakeCvManager.initializeCamera(intakePipeline);
        //waitForStart();

        if (opModeIsActive()) {
            telemetry.addData("Level found", pipeline.getObjLevel());
            //int objLevel = pipeline.getObjLevel();
            int objLevel = 1;
//            ElapsedTime rtime = new ElapsedTime();
//            rtime.reset();

//            driveControl.moveXDist(-28, 0.5);
//            driveControl.moveYDist(80, 1);

            // move to linear slide and put square on level
            autoQueue.addAutoAction(driveControl.getForwardAction(26, 1));
            int firstAngle = 75;
            autoQueue.addAutoAction(driveControl.getTurnAction(-firstAngle, 0.5));
            autoQueue.addAutoAction(driveControl.getForwardAction(2, 1));

            if (objLevel == 0) {
                autoQueue.addAutoAction(linearSlide.getLevelAction(SlideOption.LEVEL_1));
            } else if (objLevel == 1) {
                autoQueue.addAutoAction(linearSlide.getLevelAction(SlideOption.LEVEL_2));
            } else if (objLevel == 2) {
                autoQueue.addAutoAction(linearSlide.getLevelAction(SlideOption.LEVEL_3));
            }


            while (autoQueue.updateQueue()) {
                sleep(100);
            }
            linearSlide.dump();
            sleep(1500);
            linearSlide.undump();
            sleep(1500);
            autoQueue.addAutoAction(linearSlide.getLevelAction(SlideOption.LEVEL_0));

            while (autoQueue.updateQueue()) {
                sleep(100);
            }
////
////            // carousel spin would go here if our partner isn't doing it
////            // we probably wouldn't use this opmode unless partner can do carousel since you have
////            // to move all the way to the carousel and then back in order to park
////
////            // loop iterates once every cycle
////            while (rtime.time() <= 25000) { // this gives us at least 5 seconds to park
////                // move back a bit
////                autoQueue.addAutoAction(driveControl.getForwardAction(-2, 1));
////
////                // robot is now horizontal, faces the wall opposite of the warehouses
////                autoQueue.addAutoAction(driveControl.getTurnAction(-(90 - firstAngle), 0.5));
////
////                // press against the wall in preparation to move back
////                autoQueue.addAutoAction(driveControl.getStrafeAction(-26, 1));
////
////                // finish executing instructions
////                while (autoQueue.updateQueue()) {
////                    sleep(100);
////                }
////                double inchesMoved = 0;
////
////                // start intake
////                intake.intakeIn();
////
////                // moving towards warehouse until getting an element
////                while (!intakePipeline.ifBallExists() && !intakePipeline.ifBlockExists()) {
////                    while (autoQueue.updateQueue()) {
////                        sleep(10);
////                    }
////                    autoQueue.addAutoAction(driveControl.getForwardAction(-2, 1));
////                    inchesMoved += 2;
////                    if (inchesMoved >= 80) { // couldn't intake anything
////                                             // so we can just park for the rest
////                        sleep(100000);
////                    }
////                }
////                intake.intakeOut();
////                autoQueue.addAutoAction(driveControl.getForwardAction(inchesMoved, 1));
////                while (autoQueue.updateQueue()) {
////                    sleep(100);
////                }
////                intake.stop();
////                autoQueue.addAutoAction(driveControl.getStrafeAction(26, 1));
////                autoQueue.addAutoAction(driveControl.getTurnAction(90 - firstAngle, 0.5));
////                autoQueue.addAutoAction(driveControl.getForwardAction(2, 1));
////                autoQueue.addAutoAction(linearSlide.getLevelAction(SlideOption.LEVEL_1));
////                while (autoQueue.updateQueue()) {
////                    sleep(100);
////                }
////                linearSlide.dump();
////                sleep(1500);
////                linearSlide.undump();
////                sleep(1500);
////                autoQueue.addAutoAction(linearSlide.getLevelAction(SlideOption.LEVEL_0));
////                while (autoQueue.updateQueue()) {
////                    sleep(100);
////                }
////            }
////
////            // park in warehouse
////            autoQueue.addAutoAction(driveControl.getTurnAction(-(90 - firstAngle), 0.5));
////            autoQueue.addAutoAction(driveControl.getStrafeAction(-26, 1)); // go against wall
////            autoQueue.addAutoAction(driveControl.getForwardAction(-40, 1)); // drive into warehouse
////            while (autoQueue.updateQueue()) {
////                sleep(100);
////            }
////            telemetry.update();
////        }
////        while (opModeIsActive()) {
////            autoQueue.updateQueue();
////            telemetry.update();
////            sleep(100);
////        }
////    }
        }
    }
}