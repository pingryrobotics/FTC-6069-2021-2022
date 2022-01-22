package mechanisms;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class Carousel {
	private DcMotor carouselMotor;
	public double power;

	public Carousel(HardwareMap hardwareMap) {
		carouselMotor = hardwareMap.get(DcMotor.class, "carouselMotor");
        carouselMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
		power = 0.325; // 1150 rpm
	}

	public void spin() { // spin at speed designated by "power"
		carouselMotor.setPower(power);
	}

	public void spinPower(double power){
		carouselMotor.setPower(power);
	}
	
	public void spinAngle(int angle) { // in radians
        carouselMotor.setTargetPosition((int)((angle * 384.5)/(2*Math.PI))); // check motor PPR and change
        carouselMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
		carouselMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
	}

	public void reverseSpin() {
		carouselMotor.setPower(-power);
	}

	public void stop() {
		carouselMotor.setPower(0);
	}
}

