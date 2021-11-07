package mechanisms;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class Intake {
	private DcMotor intakeMotor;
	private double power;

	public Intake(HardwareMap hardwareMap) {
		intakeMotor = hardwareMap.get(DcMotor.class, "intakeMotor");
        intakeMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
		power = 1.25;
	}

	public void intakeIn(int power) { // intake
		intakeMotor.setPower(power);
	}
	
	public void intakeOut(int power) { // Reverse Intake
		intakeMotor.setPower(-power);
	}
	
	public void stop() {
		intakeMotor.setPower(0);
	}
}