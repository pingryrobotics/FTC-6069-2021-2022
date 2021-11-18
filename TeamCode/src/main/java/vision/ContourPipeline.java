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

public class ContourPipeline extends OpenCvPipeline {
	public boolean viewportPaused;
	private int objLevel = -1;
	private int location;
	private int width;
	private OpenCvCamera webcam;
	private Mat mat;
	public int biggestRectCenter;
	public int matWidth;

	public ContourPipeline(OpenCvCamera webcam) {
		this.webcam = webcam;
	}

	/*
	 * NOTE: if you wish to use additional Mat objects in your processing pipeline,
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

		mat = new Mat();

		Imgproc.cvtColor(input, mat, Imgproc.COLOR_RGB2HSV);

		// if something is wrong, we assume there's no skystone
		if (mat.empty()) {
			location = -1;
			return input;
		}

		// We create a HSV range for yellow to detect regular stones
		// NOTE: In OpenCV's implementation,
		// Hue values are half the real value

		// red hsv's wrap around from 170 to 10 so we need to create 2 and kinda merge
		// them
		Scalar lowHSV1 = new Scalar(0, 120, 120); // lower bound HSV #1 for red
		Scalar highHSV1 = new Scalar(255, 255, 255); // higher bound HSV for reds
		Mat thresh = new Mat();

		// We'll get a black and white image. The white regions represent the regular
		// stones.
		// inRange(): thresh[i][j] = {255,255,255} if mat[i][i] is within the range
		Core.inRange(mat, lowHSV1, highHSV1, thresh); // goes through image, filters out color based on low&high hsv's
		mat.release();

		// Core.addWeighted(thresh2, 1, thresh1, 1, 0, thresh);
		// Imgproc.GaussianBlur(thresh, thresh, new Size(9, 9), 2, 2); // should smooth
		// out some stuff; if not then it should be caught later

		// Use Canny Edge Detection to find edges
		// you might have to tune the thresholds for hysteresis
		Mat edges = new Mat();
		Imgproc.Canny(thresh, edges, 100, 300);

		// https://docs.opencv.org/3.4/da/d0c/tutorial_bounding_rects_circles.html
		// Oftentimes the edges are disconnected. findContours connects these edges.
		// We then find the bounding rectangles of those contours
		List<MatOfPoint> contours = new ArrayList<>();
		Mat hierarchy = new Mat();
		Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

		int sz = contours.size();
		System.out.println("Contours: " + sz);
		MatOfPoint2f[] contoursPoly = new MatOfPoint2f[sz];
		Rect[] boundRect = new Rect[sz]; // contains bounding rectangles for each coutour (object in 2d form)
		for (int i = 0; i < sz; i++) {
			contoursPoly[i] = new MatOfPoint2f();
			Imgproc.approxPolyDP(new MatOfPoint2f(contours.get(i).toArray()), contoursPoly[i], 3, true);
			boundRect[i] = Imgproc.boundingRect(new MatOfPoint(contoursPoly[i].toArray()));
		}

		// now what we're planning to do is looking at the relative positions of the
		// contours we've found
		// there should be 2 since there are 3 squares in the barcode and one is covered
		// by shipping element
		// then we can look at which thirds of the picture the contours are in, so we
		// can find the square
		// which the shipping element is on by process of elimination

		double biggestArea = 0;
		double secondBiggestArea = 0;

		for (int i = 0; i < sz; i++) {
			if (boundRect[i].area() >= biggestArea) {
				secondBiggestArea = biggestArea;
				biggestArea = boundRect[i].area();
			} else if (boundRect[i].area() >= secondBiggestArea) {
				secondBiggestArea = boundRect[i].area();
			}
		}
		int shippingElementLoc;// start with 0 + 1 + 2 = 6, subtract 0 or 1 or 2 for each barcode square
		// that we find so we're left with the index of the shipping element's square
		int isFirst = 1;
		int isSecond = 1;
		int isThird = 1;
		for (int i = 0; i < sz; i++) {

			if ((int) boundRect[i].area() != (int) secondBiggestArea
					&& (int) boundRect[i].area() != (int) biggestArea) { // incorrectly detected
				continue;
			}

			Imgproc.rectangle(input, boundRect[i], new Scalar(255, 0, 0), 4);

			// look at center of each bounding rectangle, see which thirds of the picture
			// they should be in

			// rectangle is represented in terms of top left point, width, and height
			int rectCenterX = boundRect[i].x + width / 2;
			int imgWidth = mat.width();
			if (rectCenterX < imgWidth / 3) { // leftmost third
				isFirst = 0;
			} else if (rectCenterX >= imgWidth / 3 && rectCenterX <= (2 * imgWidth) / 3) { // middle third
				isSecond = 0;
			} else if (rectCenterX < imgWidth && rectCenterX >= (2 * imgWidth) / 3) { // rightmost third
				isThird = 0;
			}

			if ((int) boundRect[i].area() == (int) biggestArea) {
				matWidth = mat.width();
				biggestRectCenter = rectCenterX;
			}
		}

		if (isFirst + isSecond + isThird > 1) {
			// things went wrong since we somehow have 2 or more thirds that don't have red
			// in them
		}

		if (isFirst == 1) {
			objLevel = 0;
		}

		else if (isSecond == 1) {
			objLevel = 1;
		}

		else if (isThird == 1) {
			objLevel = 2;
		}

		mat.release();
		thresh.release();
		hierarchy.release();
		edges.release();

		/**
		 * NOTE: to see how to get data from your pipeline to your OpMode as well as how
		 * to change which stage of the pipeline is rendered to the viewport when it is
		 * tapped, please see {@link PipelineStageSwitchingExample}
		 */

		return input;
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

	public int getObjLevel() {

		// return objLevel;
		return objLevel;
	}
}