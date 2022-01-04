package ca.hapke.controller.ui;

/**
 * @author Mr. Hapke
 *
 */
public class Angle {
	private double theta = 0;
	private double baseline = 0;
	private boolean lock = false;

	public double getTheta() {
		return theta - baseline;
	}

	public void setValue(double theta) {
		if (lock)
			return;

		this.theta = theta;
	}

//	public boolean isLock() {
//		return lock;
//	}

	public void setBaseline(double baseline) {
		if (lock)
			return;

		this.baseline = baseline;
	}

	public void setLock(boolean lock) {
		this.lock = lock;
	}

}
