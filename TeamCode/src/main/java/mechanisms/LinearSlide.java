package mechanisms;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo; //only if we need the additional feeder.
import com.qualcomm.robotcore.hardware.HardwareMap;

public class LinearSlide {
	// SKU: 5202-0002-0014
	// 384.5 PPR encoder resolution
	private final DcMotor slideMotor;
	private final Servo bucketServo;
	public double power;
	private int level;

	public LinearSlide(HardwareMap hardwareMap) {
		slideMotor = hardwareMap.get(DcMotor.class, "slideMotor");
		slideMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER); // not sure if needed but sets base state to 0
		//slideMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
		bucketServo = hardwareMap.get(Servo.class, "bucketServo");
		bucketServo.setDirection(Servo.Direction.FORWARD);
		bucketServo.scaleRange(0, .4);
		power = 1;
		level = 3;
	}

	public void level1() { // extend linear slide to level appropriate for the bottom level of shipping hub
		slideMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER); // not sure if needed but sets base state to 0
		if(level == 0) {
			slideMotor.setTargetPosition(200);
		}

		else if(level == 2){
			slideMotor.setTargetPosition(-200);
		}

		else if(level == 3){
			slideMotor.setTargetPosition(-400);
		}

		slideMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
		level = 1;
		 // i have no idea what it should be this is an estimate tho
	}

	public void level2() { // extend linear slide to level appropriate for the middle level of shipping hub
		slideMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER); // not sure if needed but sets base state to 0
		if(level == 0) {
			slideMotor.setTargetPosition(400);
		}

		else if(level == 1){
			slideMotor.setTargetPosition(200);
		}

		else if(level == 3){
			slideMotor.setTargetPosition(-200);
		}
		slideMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
		level = 2;
	}

	public void level3() { // extend linear slide to level appropriate for the top level of shipping hub
		slideMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER); // not sure if needed but sets base state to 0
		if(level == 0) {
			slideMotor.setTargetPosition(600);
		}

		else if(level == 1){
			slideMotor.setTargetPosition(400);
		}

		else if(level == 2){
			slideMotor.setTargetPosition(200);
		}
		slideMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
		level = 3;
	}

	public void level0(){
		slideMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER); // not sure if needed but sets base state to 0
		if(level == 1) {
			slideMotor.setTargetPosition(-200);
		}

		else if(level == 2){
			slideMotor.setTargetPosition(-400);
		}

		else if(level == 3){
			slideMotor.setTargetPosition(-600);
		}
		slideMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
		level = 0;
	}

	public void extend() { // continuously extend linear slide
		slideMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
		slideMotor.setPower(power);
	}
	
	public void retract() { // continuously retract linear slide
		slideMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
		slideMotor.setPower(-power);
	}

	public void dump() { // dump stuff in bucket
		bucketServo.setPosition(1.0);
	}

	public void undump() { // pull bucket back after dumping
		bucketServo.setPosition(0.0);
	}

	public void stop() {
		slideMotor.setTargetPosition(0); // goes back to base state
		slideMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
	}
}
