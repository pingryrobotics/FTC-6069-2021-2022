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
        if((colorSensor.argb() < 0 || (colorSensor.red() >= 100 && colorSensor.green() >= 100) && (colorSensor.red() <= 500))){
            return true;
        }
        return false;
    }
}
