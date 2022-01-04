package ca.hapke.gyro;

import ca.hapke.gyro.data.AbstractGyroDataType;
import ca.hapke.gyro.data.DataCluster;
import ca.hapke.gyro.data.DataType.InputType;
import ca.hapke.util.RunKillThread;

/**
 * 
 * @author Mr. Hapke
 */
public class GyroDataUpdater {

	protected class UpdatingThread extends RunKillThread {
		private static final int UPDATE_FREQUENCY = 50;

		@Override
		public void doWork() {
			if (mpu == null) {
				try {
					mpu = new Mpu6050Controller();
					mpu.startUpdatingThread();
				} catch (Exception e) {
					kill = true;
					e.printStackTrace();
				}
			}
			while (!kill) {
				updateFromRaspoid();
//				updateFromPi4j();
				try {
					Thread.sleep(UPDATE_FREQUENCY);
				} catch (InterruptedException e) {
					kill = true;
				}
			}
//			try {
//				mpu.stopUpdatingThread();
//			} catch (InterruptedException e) {
//			}
			mpu = null;
		}

//		private void updateFromPi4j() {
//			try {
//				byte[] values = mpu.readValues();
//				status.update(values);
////				for (int j = 0; j < status.getDimensions(); j++) {
////					System.out.println(j + ": " + status.getValueString(j));
////				}
//			} catch (Exception e) {
//				kill = true;
//				e.printStackTrace();
//			}
//		}

	}

	private AbstractGyroDataType status;
	private Mpu6050Controller mpu;
	private UpdatingThread t;

	public GyroDataUpdater(DataCluster cluster) {
		this.status = (AbstractGyroDataType) cluster.getData(InputType.JavaGyro);
		start();
	}

	public void updateFromRaspoid() {
		if (mpu == null)
			return;
		double[] accelAngles = mpu.getAccelAngles();
		double[] accelAccelerations = mpu.getAccelAccelerations();
		double[] gyroAngles = mpu.getGyroAngles();
		double[] gyroAngularSpeeds = mpu.getGyroAngularSpeeds();
		double[] filteredAngles = mpu.getFilteredAngles();

		double[][] fullArray = {accelAngles, accelAccelerations, gyroAngles, gyroAngularSpeeds, filteredAngles};
		status.update(fullArray);
	}

	public void start() {
		if (t == null || !t.isRunning()) {
			t = new UpdatingThread();
			t.start();
		}
	}

	public void stop() {
		if (t != null && t.isRunning()) {
			t.kill();
		}
		if (mpu != null) {
			try {
				mpu.stopUpdatingThread();
			} catch (Exception e) {
			}
		}
	}
}