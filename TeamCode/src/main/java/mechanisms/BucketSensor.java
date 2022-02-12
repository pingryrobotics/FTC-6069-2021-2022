package mechanisms;

import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class BucketSensor {
    private final ColorSensor colorSensor;

    public BucketSensor(HardwareMap hardwareMap, Telemetry telemetry) {
        colorSensor = hardwareMap.get(ColorSensor.class, "Color Sensor 1");
    }

    public boolean freightIn(){
        if((colorSensor.argb() < 0 && colorSensor.argb() <= -10000000)|| (colorSensor.argb() >= 1000000000)){
            return true;
        }
        return false;
    }
}