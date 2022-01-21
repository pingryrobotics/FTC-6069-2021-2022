/*
 * Copyright (c) 2019 OpenFTC Team
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package vision;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Core;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvPipeline;

import java.util.ArrayList;
import java.util.List;

import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvPipeline;
import org.openftc.easyopencv.OpenCvWebcam;

public class IntakeCVPipeline extends OpenCvPipeline {
    public boolean viewportPaused;
    private OpenCvCamera webcam;
    private Mat matBlockBucket;
    private Mat matBallBucket;
    private Mat matBallGap;
    private Mat matBlockGap;
    private boolean blockExists = false;
    private boolean ballExists = false;
    private boolean freightInGap = false;
    private int frameCount = 0;

    public IntakeCVPipeline(OpenCvCamera webcam) {
        this.webcam = webcam;
    }

    /*
     * NOTE: if you wish to use additional Mat objects in your   pipeline,
     * it is highly recommended to declare them here as instance variables and
     * re-use them for each invocation of processFrame(), rather than declaring them
     * as new local variables each time through processFrame(). This removes the
     * danger of causing a memory leak by forgetting to call mat.release(), and it
     * also reduces memory pressure by not constantly allocating and freeing large
     * chunks of memory.
     */

    @Override
    public Mat processFrame(Mat input) {
        /*
         * IMPORTANT NOTE: the input Mat that is passed in as a parameter to this method
         * will only dereference to the same image for the duration of this particular
         * invocation of this method. That is, if for some reason you'd like to save a
         * copy of this particular frame for later use, you will need to either clone it
         * or copy it to another Mat.
         */

        /*
         * METHOD OVERVIEW First, we filter out the colors in the image so that we can
         * only see the parts that are red/blue. This gives us a 2d matrix with boolean
         * values if the color at a location is between the color values we've set as
         * the filter. Then, we obtain a set of contours (shapes) in the 2d matrix,
         * which should be the squares on the barcode. Then, we find bounding rectangles
         * for each of these contours, and we can look at the locations of the contours
         * (squares on the barcode) to determine where the shipping element is, and
         * we'll just return the location of the shipping element in the form of an
         * index from 0 to 2.
         */
        int x = input.rows()/3;
        int y = input.cols()/3;

        Mat bucket = input.submat(0, input.rows() - 1, y , input.cols() - 1);
        Mat gap = input.submat(0, input.rows() - 1, 0, y - 1);
        matBlockBucket = new Mat();
        matBallBucket = new Mat();
        matBlockGap = new Mat();
        matBallGap = new Mat();

        Imgproc.cvtColor(bucket, matBlockBucket, Imgproc.COLOR_RGB2HSV);
        Imgproc.cvtColor(bucket, matBallBucket, Imgproc.COLOR_RGB2HSV);
        Imgproc.cvtColor(gap, matBallGap, Imgproc.COLOR_RGB2HSV);
        Imgproc.cvtColor(gap, matBlockGap, Imgproc.COLOR_RGB2HSV);

        // We create a HSV range for yellow to detect regular stones
        // NOTE: In OpenCV's implementation,
        // Hue values are half the real value

        // red hsvs wrap around from 170 to 10 so we need to create 2 and kinda merge
        // them
        Scalar lowHSVBlock;
        Scalar highHSVBlock;
        Scalar lowHSVBall;
        Scalar highHSVBall;

        //Cube Color Values
        lowHSVBlock = new Scalar(10, 70, 200);
        highHSVBlock = new Scalar(40, 150, 255);
        lowHSVBall = new Scalar(55, 0, 240); // lower bound HSV #1 for team shipping element
        highHSVBall= new Scalar(70, 5,  255);
        Mat threshBlock = new Mat();
        Mat threshBall = new Mat();
        Mat threshBlockGap = new Mat();
        Mat threshBallGap = new Mat();

        // We'll get a black and white image. The white regions represent the regular
        // stones.
        // inRange(): thresh[i][j] = {255,255,255} if mat[i][i] is within the range
        Core.inRange(matBlockBucket, lowHSVBlock, highHSVBlock, threshBlock); // goes through image, filters out color based on low&high hsv's
        Core.inRange(matBallBucket, lowHSVBall, highHSVBall, threshBall);
        Core.inRange(matBlockGap, lowHSVBlock, highHSVBlock, threshBlockGap); // goes through image, filters out color based on low&high hsv's
        Core.inRange(matBallGap, lowHSVBall, highHSVBall, threshBallGap);

        // Core.addWeighted(thresh2, 1, thresh1, 1, 0, thresh);
        // Imgproc.GaussianBlur(thresh, thresh, new Size(9, 9), 2, 2); // should smooth
        // out some stuff; if not then it should be caught later

        // Use Canny Edge Detection to find edges
        // you might have to tune the thresholds for hysteresis
        Mat edgesBlock = new Mat();
        Mat edgesBall = new Mat();
        Mat edgesBlockGap = new Mat();
        Mat edgesBallGap = new Mat();
        Imgproc.Canny(threshBlock, edgesBlock, 100, 300);
        Imgproc.Canny(threshBall, edgesBall, 100, 300);
        Imgproc.Canny(threshBlockGap, edgesBlockGap, 100, 300);
        Imgproc.Canny(threshBallGap, edgesBallGap, 100, 300);

        // https://docs.opencv.org/3.4/da/d0c/tutorial_bounding_rects_circles.html
        // Oftentimes the edges are disconnected. findContours connects these edges.
        // We then find the bounding rectangles of those contours
        List<MatOfPoint> contoursBlock = new ArrayList<>();
        Mat hierarchyBlock = new Mat();
        Imgproc.findContours(edgesBlock, contoursBlock, hierarchyBlock, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        List<MatOfPoint> contoursBall = new ArrayList<>();
        Mat hierarchyBall = new Mat();
        Imgproc.findContours(edgesBall, contoursBall, hierarchyBall, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        List<MatOfPoint> contoursBlockGap = new ArrayList<>();
        Mat hierarchyBlockGap = new Mat();
        Imgproc.findContours(edgesBlockGap, contoursBlockGap, hierarchyBlockGap, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        List<MatOfPoint> contoursBallGap = new ArrayList<>();
        Mat hierarchyBallGap = new Mat();
        Imgproc.findContours(edgesBallGap, contoursBallGap, hierarchyBallGap, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);


        int sz = contoursBlock.size();
        MatOfPoint2f[] contoursPolyBlock = new MatOfPoint2f[sz];
        Rect[] boundRectBlock = new Rect[sz]; // contains bounding rectangles for each coutour (object in 2d form)
        for (int i = 0; i < sz; i++) {
            contoursPolyBlock[i] = new MatOfPoint2f();
            Imgproc.approxPolyDP(new MatOfPoint2f(contoursBlock.get(i).toArray()), contoursPolyBlock[i], 3, true);
            boundRectBlock[i] = Imgproc.boundingRect(new MatOfPoint(contoursPolyBlock[i].toArray()));
        }


        int sz2 = contoursBall.size();
        MatOfPoint2f[] contoursPolyBall = new MatOfPoint2f[sz2];
        Rect[] boundRectBall = new Rect[sz2]; // contains bounding rectangles for each coutour (object in 2d form)
        for (int i = 0; i < sz2; i++) {
            contoursPolyBall[i] = new MatOfPoint2f();
            Imgproc.approxPolyDP(new MatOfPoint2f(contoursBall.get(i).toArray()), contoursPolyBall[i], 3, true);
            boundRectBall[i] = Imgproc.boundingRect(new MatOfPoint(contoursPolyBall[i].toArray()));
        }

        int sz3 = contoursBlockGap.size();
        MatOfPoint2f[] contoursPolyBlockGap = new MatOfPoint2f[sz3];
        Rect[] boundRectBlockGap = new Rect[sz3]; // contains bounding rectangles for each coutour (object in 2d form)
        for (int i = 0; i < sz3; i++) {
            contoursPolyBlockGap[i] = new MatOfPoint2f();
            Imgproc.approxPolyDP(new MatOfPoint2f(contoursBlockGap.get(i).toArray()), contoursPolyBlockGap[i], 3, true);
            boundRectBlockGap[i] = Imgproc.boundingRect(new MatOfPoint(contoursPolyBlockGap[i].toArray()));
        }


        int sz4 = contoursBallGap.size();
        MatOfPoint2f[] contoursPolyBallGap = new MatOfPoint2f[sz4];
        Rect[] boundRectBallGap = new Rect[sz4]; // contains bounding rectangles for each coutour (object in 2d form)
        for (int i = 0; i < sz4; i++) {
            contoursPolyBallGap[i] = new MatOfPoint2f();
            Imgproc.approxPolyDP(new MatOfPoint2f(contoursBallGap.get(i).toArray()), contoursPolyBallGap[i], 3, true);
            boundRectBallGap[i] = Imgproc.boundingRect(new MatOfPoint(contoursPolyBallGap[i].toArray()));
        }
        // just look at which third the shipping element is in
        double biggestAreaBlock = 0;
        Rect biggestRectBlock = new Rect();
        for (int i = 0; i < sz; i++) {
            if (boundRectBlock[i].area() >= biggestAreaBlock) {
                biggestAreaBlock = boundRectBlock[i].area();
                biggestRectBlock = boundRectBlock[i];
            }
        }



        double biggestAreaBall = 0;
        Rect biggestRectBall = new Rect();
        for (int i = 0; i < sz2; i++) {
            if (boundRectBall[i].area() >= biggestAreaBall) {
                biggestAreaBall = boundRectBall[i].area();
                biggestRectBall = boundRectBall[i];
            }
        }


        double biggestAreaBlockGap = 0;
        Rect biggestRectBlockGap = new Rect();
        for (int i = 0; i < sz3; i++) {
            if (boundRectBlockGap[i].area() >= biggestAreaBlockGap) {
                biggestAreaBlockGap = boundRectBlockGap[i].area();
                biggestRectBlockGap = boundRectBlockGap[i];
            }
        }



        double biggestAreaBallGap = 0;
        Rect biggestRectBallGap = new Rect();
        for (int i = 0; i < sz4; i++) {
            if (boundRectBallGap[i].area() >= biggestAreaBallGap) {
                biggestAreaBallGap = boundRectBallGap[i].area();
                biggestRectBallGap = boundRectBallGap[i];
            }
        }


        blockExists = (biggestAreaBlock >= 17000);
        ballExists = biggestAreaBall >= 17000;
        freightInGap = (biggestAreaBlockGap >=10000) || (biggestAreaBallGap >= 1000);
        if(ballExists){
            Imgproc.rectangle(bucket, biggestRectBall, new Scalar(255, 0, 0), 4);
            frameCount++;
        }

        if(blockExists){
            Imgproc.rectangle(bucket, biggestRectBlock, new Scalar(255, 0, 0), 4);
            frameCount++;
        }

        if(freightInGap){
            Imgproc.rectangle(gap, biggestRectBlockGap, new Scalar(255, 0, 0), 4);
            Imgproc.rectangle(gap, biggestRectBallGap, new Scalar(255, 0, 0), 4);
            frameCount++;
        }

        if(!(blockExists || ballExists)){
            frameCount = 0;
        }



//        double[] values = mat.get(input.rows()/2, input.cols()/2);
//        System.out.println("HSV: " + values[0] + ", " + values[1]+ ", " + values[2]);
        // should be at least 200x200 pixels
//        double[] values = matBlock.get(input.rows()/2, input.cols()/2);
//        System.out.println("Block HSV: " + values[0] + ", " + values[1]+ ", " + values[2]);
//
//        double[] values2 = matBall.get(input.rows()/2, input.cols()/2);
//        System.out.println("Block HSV: " + values2[0] + ", " + values2[1]+ ", " + values2[2]);


        matBlockBucket.release();
        matBlockGap.release();
        threshBlock.release();
        hierarchyBlock.release();
        edgesBlockGap.release();
        threshBlockGap.release();
        hierarchyBlockGap.release();
        edgesBlockGap.release();

        matBallBucket.release();
        matBallGap.release();
        threshBall.release();
        hierarchyBall.release();
        edgesBall.release();
        threshBallGap.release();
        hierarchyBallGap.release();
        edgesBallGap.release();

        /**
         * NOTE: to see how to get data from your pipeline to your OpMode as well as how
         * to change which stage of the pipeline is rendered to the viewport when it is
         * tapped, please see {@link PipelineStageSwitchingExample}
         */

        return gap;
    }

    @Override
    public void onViewportTapped() {
        /*
         * The viewport (if one was specified in the constructor) can also be
         * dynamically "paused" and "resumed". The primary use case of this is to reduce
         * CPU, memory, and power load when you need your vision pipeline running, but
         * do not require a live preview on the robot controller screen. For instance,
         * this could be useful if you wish to see the live camera preview as you are
         * initializing your robot, but you no longer require the live preview after you
         * have finished your initialization process; pausing the viewport does not stop
         * running your pipeline.
         *
         * Here we demonstrate dynamically pausing/resuming the viewport when the user
         * taps it
         */

        viewportPaused = !viewportPaused;

        if (viewportPaused) {
            webcam.pauseViewport();
        } else {
            webcam.resumeViewport();
        }
    }

    public boolean frameCount(int frames){
       return frameCount >= frames;
    }

    public boolean ifBlockExists() {

        // return objLevel;
        return blockExists;
    }
    public boolean isFreightInGap() {return freightInGap;}
    public boolean ifBallExists(){
        return ballExists;
    }
//    public void setObject(String obj) {
//        if (obj.equals("Ball")) {
//            isCube = false;
//        } else if (obj.equals("Cube")) {
//            isCube = true;
//        }
//    }
}