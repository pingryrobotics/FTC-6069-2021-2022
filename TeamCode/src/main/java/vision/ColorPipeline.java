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

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvPipeline;

import java.util.ArrayList;
import java.util.List;

public class ColorPipeline extends OpenCvPipeline {
	public boolean viewportPaused;
	private int objLevel;
	private int location;
	private final OpenCvCamera webcam;
	private final static String TAG = "teamcode.cv.colorpl";


//	static final Point REGION1_TOPLEFT_ANCHOR_POINT = new Point(109,98);
//	static final Point REGION2_TOPLEFT_ANCHOR_POINT = new Point(181,98);
//	static final Point REGION3_TOPLEFT_ANCHOR_POINT = new Point(253,98);
//	static final int REGION_WIDTH = 20;
//	static final int REGION_HEIGHT = 20;

//	private int width;

	/**
	 * Initialize the pipeline
	 * @param webcam the opencvcamera to use
	 */
	public ColorPipeline(OpenCvCamera webcam) {
		this.webcam = webcam;
	}

	/*
		* NOTE: if you wish to use additional Mat objects in your processing pipeline, it is
		* highly recommended to declare them here as instance variables and re-use them for
		* each invocation of processFrame(), rather than declaring them as new local variables
		* each time through processFrame(). This removes the danger of causing a memory leak
		* by forgetting to call mat.release(), and it also reduces memory pressure by not
		* constantly allocating and freeing large chunks of memory.
		*/

	@Override
	public Mat processFrame(Mat input)
	{

		// make points to create 3 thirds of the camera
		// first third is the first top left to first bottom right point,
		// the second third is the first bottom right to the third top left,
		// and the third third is the third top left to the third bottom right
		double thirdColWidth = input.cols()/3.0;
//
//		double firstThirdColumn = 0;
//		double secondThirdColumn = thirdColWidth;
//		double thirdThirdColumn = thirdColWidth*2;
//		double endColumn = input.cols();
//
//		double rowTop = 0;
//		double rowBottom = input.rows();

		Point firstTopLeft = new Point(0, 0);
		Point firstBottomRight = new Point(thirdColWidth, input.rows());
		Point thirdTopLeft = new Point(thirdColWidth*2, 0);
		Point thirdBottomRight = new Point(input.cols(), input.rows());

		Rect firstThird = new Rect(firstTopLeft, firstBottomRight);
		Rect secondThird = new Rect(firstBottomRight, thirdTopLeft);
		Rect thirdThird = new Rect(thirdTopLeft, thirdBottomRight);
		Rect entireScreen = new Rect(new Point(0, 0), new Point(input.cols(), input.rows()));

		Mat thirdOneMat = new Mat(input, firstThird);
		Mat thirdTwoMat = new Mat(input, secondThird);
		Mat thirdThreeMat = new Mat(input, thirdThird);

		Imgproc.rectangle(input, firstThird, new Scalar(0, 255, 0), 4);
		Imgproc.rectangle(input, secondThird, new Scalar(255, 0, 0), 4);
		Imgproc.rectangle(input, thirdThird, new Scalar(0, 0, 255), 4);
		Imgproc.rectangle(input, entireScreen, new Scalar(255, 0, 255), 4);

//		Imgproc.rectangle(input, firstTopLeft, firstBottomRight, new Scalar(0, 255, 0), 4);
//		Imgproc.rectangle(input, firstBottomRight, thirdTopLeft, new Scalar(255, 0, 0), 4);
//		Imgproc.rectangle(input, thirdTopLeft, thirdBottomRight, new Scalar(0, 0, 255), 4);
//		Imgproc.rectangle(input, new Point(0, 0), new Point(input.rows(), input.cols()), new Scalar(255, 0, 255), 4);

		Log.d(TAG, "Columns: " + input.cols() + " Rows: " + input.rows());


		return input;
	}
//
//	public double determineColorAmount(Mat mat) {
//		Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2HSV);
//
//
//
//	}

	@Override
	public void onViewportTapped()
	{
		/*
			* The viewport (if one was specified in the constructor) can also be dynamically "paused"
			* and "resumed". The primary use case of this is to reduce CPU, memory, and power load
			* when you need your vision pipeline running, but do not require a live preview on the
			* robot controller screen. For instance, this could be useful if you wish to see the live
			* camera preview as you are initializing your robot, but you no longer require the live
			* preview after you have finished your initialization process; pausing the viewport does
			* not stop running your pipeline.
			*
			* Here we demonstrate dynamically pausing/resuming the viewport when the user taps it
			*/

		viewportPaused = !viewportPaused;

		if(viewportPaused)
		{
			webcam.pauseViewport();
		}
		else
		{
			webcam.resumeViewport();
		}
	}

	public static int getObjLevel() {

//		return objLevel;
		return 0;
	}
}