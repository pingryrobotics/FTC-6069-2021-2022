package vision;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvPipeline;
import org.openftc.easyopencv.OpenCvWebcam;

public class CVManager {

    private OpenCvWebcam webcam;

    /**
     * Initialize the CV manager with the provided hardware map.
     * The webcam gets a viewport, so the camera stream is displayed on the robot.
     * @param hardwareMap the hardware map
     */
    public CVManager(HardwareMap hardwareMap) {
        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        webcam = OpenCvCameraFactory.getInstance().createWebcam(hardwareMap.get(WebcamName.class, "Webcam 1"), cameraMonitorViewId);
    }

    /**
     * Initialize the cv manager
     * @param pipeline the pipeline to use
     */
    public void initializeCamera(OpenCvPipeline pipeline) {
        webcam.setPipeline(pipeline);
        webcam.setMillisecondsPermissionTimeout(2500); // Timeout for obtaining permission is configurable. Set before opening.
        webcam.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener()
        {
            @Override
            public void onOpened()
            {
                /*
                    Tell the webcam to start streaming at the specified resolution (must be supported by the camera)
                    and at the specified rotation. See original OpenCV comments for more.
                 */
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
    }


    /**
     * Get the currently used webcam
     * @return the webcam
     */
    public OpenCvCamera getWebcam() {
        return webcam;
    }
}
