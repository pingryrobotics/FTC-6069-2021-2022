package org.firstinspires.ftc.teamcode;
import com.qualcomm.robotcore.hardware.DcMotor;

public class Carousel {
	private DcMotor carouselMotor;

	public Carousel(HardwareMap hardwareMap) {
		carouselMotor = hardwareMap.get(DcMotor.class, "carouselMotor");
        carouselMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
	}

	public void spinCW() { // clockwise

	}

	public void spinCCW() { // counterclockwise

	}

	public void stop() {

	}
}