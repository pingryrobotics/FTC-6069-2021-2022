package mechanisms;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo; //only if we need the additional feeder.
import com.qualcomm.robotcore.hardware.HardwareMap;

public class LinearSlide {
	private DcMotor slideMotor;
	private Servo bucketServo;

	public LinearSlide(HardwareMap hardwareMap) {
		slideMotor = hardwareMap.get(Dcmotor.class, "slideMotor");
		slideMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
		bucketServo = hardwareMap.get(Servo.class, "bucketServo");
		bucketServo.setDirection(Servo.Direction.FORWARD);
		servo.scaleRange(0, .4);
	}

	public void level1() { // extend linear slide to level appropriate for the bottom level of shipping hub
		
	}

	public void level2() { // extend linear slide to level appropriate for the middle level of shipping hub 

	}

	public void level3() { // extend linear slide to level appropriate for the top level of shipping hub

	}

	public void extend() { // continuously extend linear slide

	}
	
	public void retract() { // continuously retract linear slide

	}

	public void dump() { // dump stuff in bucket

	}

	public void undump() { // pull bucket back after dumping

	}

	public void stop() {
		
	}
}