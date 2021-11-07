package mechanisms;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class Carousel {
	private DcMotor carouselMotor;
	public int power = 1;

	public Carousel(HardwareMap hardwareMap) {
		carouselMotor = hardwareMap.get(DcMotor.class, "carouselMotor");
        carouselMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
	}

	public void spin() { // spin at speed designated by "power"
		carouselMotor.setPower(power);
	}

	public void spinReverse() { // spin at speed designated by "power"
		carouselMotor.setPower(-power);
	}

	public void stop() { //stop the motor
		carouselMotor.setPower(0);
	}
}