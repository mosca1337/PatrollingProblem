import java.util.Random;

public class Exponential {
	private Random randomNumber = new Random();
	double mean;

	public Exponential(double m) {
		mean = m;
		randomNumber.setSeed((long) (Math.random() * 1878892440l));
	}

	public void setMean(double m) {
		this.mean = m;
	}

	public double nextExponential() {
		randomNumber.nextDouble();
		return (-1 * mean * Math.log(1 - randomNumber.nextDouble()));
	}

}