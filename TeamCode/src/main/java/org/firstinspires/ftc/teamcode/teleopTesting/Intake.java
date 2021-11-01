package org.firstinspires.ftc.teamcode;
import com.qualcomm.robotcore.hardware.DcMotor;

public class Intake {
	private DcMotor intakeMotor;

	public Intake(HardwareMap hardwareMap) {
		intakeMotor = hardwareMap.get(DcMotor.class, "intakeMotor");
        intakeMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
	}

	public void intakeIn() { // intake
		
	}

	public void intakeOut() { // reverse intake in case smth gets stuck

	}
	
	public void stop() {

	}
}