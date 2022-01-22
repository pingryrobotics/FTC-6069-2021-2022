package vision;

import android.graphics.Color;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DistanceSensor;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvPipeline;
import org.openftc.easyopencv.OpenCvWebcam;

public class ColorSensorManager {
    private ColorSensor sensor;
    private DistanceSensor distSensor;
    final double SCALE_FACTOR = 255;

    public ColorSensorManager(HardwareMap hardwareMap, String sensorName) {
        sensor = hardwareMap.get(ColorSensor.class, sensorName);
        distSensor = hardwareMap.get(DistanceSensor.class, sensorName);
    }
    public double getDist() {
        return distSensor.getDistance(DistanceUnit.CM);
    }
    public double getAlpha() {
        return sensor.alpha();
    }
    public int getRed() {
        return sensor.red();
    }
    public int getBlue() {
        return sensor.blue();
    }
    public int getGreen() {
        return sensor.green();
    }
    public void ledOn() {
        sensor.enableLed(true);
    }
    public void ledOff() {
        sensor.enableLed(false);
    }
    public float hue() {
        float hsvValues[] = {0F, 0F, 0F};

        Color.RGBToHSV((int) (sensor.red() * SCALE_FACTOR),
                (int) (sensor.green() * SCALE_FACTOR),
                (int) (sensor.blue() * SCALE_FACTOR),
                hsvValues);
        return hsvValues[0];
    }
}
