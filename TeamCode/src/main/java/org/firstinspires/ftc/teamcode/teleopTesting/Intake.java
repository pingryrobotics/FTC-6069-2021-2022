package org.firstinspires.ftc.teamcode;
import com.qualcomm.robotcore.hardware.DcMotor;

public class Intake {
	private DcMotor intakeMotor;
	private double power;

	public Intake(HardwareMap hardwareMap) {
		intakeMotor = hardwareMap.get(DcMotor.class, "intakeMotor");
        intakeMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
		power = 1.25;
	}

	public void intakeIn() { // intake
		intakeMotor.setPower(power);
	}

	public void intakeOut() { // reverse intake in case smth gets stuck
		intakeMotor.setPower(-1 * power);
	}
	
	public void stop() {
		intakeMotor.setPower(0);
	}
}