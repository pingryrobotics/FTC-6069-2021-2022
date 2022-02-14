package mechanisms;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class CappingArm {
    private final Servo armServo;
    protected final Telemetry telemetry;

    public CappingArm(HardwareMap hardwareMap, Telemetry telemetry) {
        armServo = hardwareMap.get(Servo.class, "cappingServo");
        armServo.setDirection(Servo.Direction.FORWARD);
        this.telemetry = telemetry;
        armServo.scaleRange(0, 0.7);
    }

    public void turnOutwards() {
        if (armServo.getPosition() < 1) {
            armServo.setPosition(armServo.getPosition() + .05);
        } else {
            armServo.setPosition(1);
        }
    }
    public void turnInwards() {
        if (armServo.getPosition() > 0) {
            armServo.setPosition(armServo.getPosition() - .05);
        } else {
            armServo.setPosition(0);
        }
    }

    public void spinOut() {
        armServo.setPosition(0);
    }

    public void spinIn() {
        armServo.setPosition(1);
    }

    public void defaultPosition() {armServo.setPosition(0.7);}

    public double getServoPos() {
        return armServo.getPosition();
    }

}