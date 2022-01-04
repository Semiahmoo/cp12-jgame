package ca.hapke.gyro;

import java.io.IOException;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

/**
 * 
 * @author Mr. Hapke
 */
public class Mpu6050Controller {
	private static final int MPU6050_I2C_ADDR = 0x68;
	private I2CBus bus = null;
	private I2CDevice device = null;

	private double accelAccelerationX;
	private double accelAccelerationY;
	private double accelAccelerationZ;
	private double accelAngleX;
	private double accelAngleY;
	private double accelAngleZ;
	private double gyroAngularSpeedX;
	private double gyroAngularSpeedY;
	private double gyroAngularSpeedZ;
	private double gyroAngleX;
	private double gyroAngleY;
	private double gyroAngleZ;
	private double gyroAngularSpeedOffsetX;
	private double gyroAngularSpeedOffsetY;
	private double gyroAngularSpeedOffsetZ;
	private double filteredAngleX;
	private double filteredAngleY;
	private double filteredAngleZ;

	private long lastUpdateTime;
	private int dlpfCfg;
	private int smplrtDiv;
	private double accelLSBSensitivity;
	private double gyroLSBSensitivity;

	private Thread updatingThread;
	private boolean updatingThreadStopped;

	public Mpu6050Controller() throws Exception {
		System.out.println("Creating I2C bus");
		bus = I2CFactory.getInstance(I2CBus.BUS_1);
		System.out.println("Creating I2C device");
		device = bus.getDevice(MPU6050_I2C_ADDR);

		this.dlpfCfg = 6;
		this.smplrtDiv = 0;

		this.writeRegister(107, 0);
		this.writeRegister(25, smplrtDiv);
		this.setDLPFConfig(dlpfCfg);
		byte fsSel = 0;
		this.gyroLSBSensitivity = 131.0D;
		this.writeRegister(27, fsSel);
		byte afsSel = 0;
		this.accelLSBSensitivity = 16384.0D;
		this.writeRegister(28, afsSel);
		this.writeRegister(26, 0);
		this.writeRegister(108, 0);
		this.calibrateSensors();
	}

	public void setDLPFConfig(int dlpfConfig) throws IOException {
		if (dlpfConfig <= 7 && dlpfConfig >= 0) {
			this.dlpfCfg = dlpfConfig;
			this.writeRegister(26, this.dlpfCfg);
		} else {
			throw new IllegalArgumentException("The DLPF config must be in the 0..7 range.");
		}
	}

	private void calibrateSensors() throws IOException {
		System.out.println("Calibration starting in 5 seconds (don't move the sensor).");
		sleepMilliseconds(5000L);
		System.out.println("Calibration started (~5s) (don't move the sensor)");
		int nbReadings = 50;
		this.gyroAngularSpeedOffsetX = 0.0D;
		this.gyroAngularSpeedOffsetY = 0.0D;
		this.gyroAngularSpeedOffsetZ = 0.0D;

		for (int i = 0; i < nbReadings; ++i) {
			double[] angularSpeeds = this.readScaledGyroscopeValues();
			this.gyroAngularSpeedOffsetX += angularSpeeds[0];
			this.gyroAngularSpeedOffsetY += angularSpeeds[1];
			this.gyroAngularSpeedOffsetZ += angularSpeeds[2];
			sleepMilliseconds(100L);
		}

		this.gyroAngularSpeedOffsetX /= nbReadings;
		this.gyroAngularSpeedOffsetY /= nbReadings;
		this.gyroAngularSpeedOffsetZ /= nbReadings;
		System.out.println("Calibration ended");
	}

	public void startUpdatingThread() {
		if (this.updatingThread != null && this.updatingThread.isAlive()) {
			System.out.println("Updating thread of the MPU6050 is already started.");
		} else {
			this.updatingThreadStopped = false;
			this.lastUpdateTime = System.currentTimeMillis();
			this.updatingThread = new Thread(() -> {
				while (!this.updatingThreadStopped) {
					try {
						this.updateValues();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			});
			this.updatingThread.start();
		}

	}

	public void stopUpdatingThread() throws InterruptedException {
		this.updatingThreadStopped = true;

		try {
			this.updatingThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		this.updatingThread = null;
	}

	private void updateValues() throws IOException {
		double[] accelerations = this.readScaledAccelerometerValues();
		this.accelAccelerationX = accelerations[0];
		this.accelAccelerationY = accelerations[1];
		this.accelAccelerationZ = accelerations[2];
		this.accelAngleX = this.getAccelXAngle(this.accelAccelerationX, this.accelAccelerationY,
				this.accelAccelerationZ);
		this.accelAngleY = this.getAccelYAngle(this.accelAccelerationX, this.accelAccelerationY,
				this.accelAccelerationZ);
		this.accelAngleZ = this.getAccelZAngle();
		double[] angularSpeeds = this.readScaledGyroscopeValues();
		this.gyroAngularSpeedX = angularSpeeds[0] - this.gyroAngularSpeedOffsetX;
		this.gyroAngularSpeedY = angularSpeeds[1] - this.gyroAngularSpeedOffsetY;
		this.gyroAngularSpeedZ = angularSpeeds[2] - this.gyroAngularSpeedOffsetZ;
		double dt = Math.abs(System.currentTimeMillis() - this.lastUpdateTime) / 1000.0D;
		double deltaGyroAngleX = this.gyroAngularSpeedX * dt;
		double deltaGyroAngleY = this.gyroAngularSpeedY * dt;
		double deltaGyroAngleZ = this.gyroAngularSpeedZ * dt;
		this.lastUpdateTime = System.currentTimeMillis();
		this.gyroAngleX += deltaGyroAngleX;
		this.gyroAngleY += deltaGyroAngleY;
		this.gyroAngleZ += deltaGyroAngleZ;
		double alpha = 0.96D;
		this.filteredAngleX = alpha * (this.filteredAngleX + deltaGyroAngleX) + (1.0D - alpha) * this.accelAngleX;
		this.filteredAngleY = alpha * (this.filteredAngleY + deltaGyroAngleY) + (1.0D - alpha) * this.accelAngleY;
		this.filteredAngleZ += deltaGyroAngleZ;
	}

	public static void sleepMilliseconds(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private int readWord2C(int registerAddress) throws IOException {
		int value = readRegister(registerAddress);
		value <<= 8;
		value += readRegister(registerAddress + 1);
		if (value >= 32768) {
			value = -(65536 - value);
		}

		return value;
	}

	private void writeRegister(int register, int data) throws IOException {
		writeRegister((byte) register, (byte) data);
	}

	public byte[] readValues() throws IOException {
		byte[] out = new byte[120];

		return out;
	}

	private void writeRegister(byte register, byte data) throws IOException {
		device.write(register, data);
	}

	public int readRegister(int register) throws IOException {
		int data = device.read(register);
		return data;
	}

	private static double distance(double a, double b) {
		return Math.sqrt(a * a + b * b);
	}

	public double[] readScaledAccelerometerValues() throws IOException {
		double accelX = this.readWord2C(59);
		accelX /= this.accelLSBSensitivity;
		double accelY = this.readWord2C(61);
		accelY /= this.accelLSBSensitivity;
		double accelZ = this.readWord2C(63);
		accelZ /= this.accelLSBSensitivity;
		return new double[] { accelX, accelY, -accelZ };
	}

	public double[] readScaledGyroscopeValues() throws IOException {
		double gyroX = this.readWord2C(67);
		gyroX /= this.gyroLSBSensitivity;
		double gyroY = this.readWord2C(69);
		gyroY /= this.gyroLSBSensitivity;
		double gyroZ = this.readWord2C(71);
		gyroZ /= this.gyroLSBSensitivity;
		return new double[] { gyroX, gyroY, gyroZ };
	}

	private double getAccelXAngle(double x, double y, double z) {
		double radians = Math.atan2(y, distance(x, z));
		double delta = 0.0D;
		if (y >= 0.0D) {
			if (z < 0.0D) {
				radians *= -1.0D;
				delta = 180.0D;
			}
		} else if (z <= 0.0D) {
			radians *= -1.0D;
			delta = 180.0D;
		} else {
			delta = 360.0D;
		}

		return radians * 57.29577951308232D + delta;
	}

	private double getAccelYAngle(double x, double y, double z) {
		double tan = -1.0D * x / distance(y, z);
		double delta = 0.0D;
		if (x <= 0.0D) {
			if (z < 0.0D) {
				tan *= -1.0D;
				delta = 180.0D;
			}
		} else if (z <= 0.0D) {
			tan *= -1.0D;
			delta = 180.0D;
		} else {
			delta = 360.0D;
		}

		return Math.atan(tan) * 57.29577951308232D + delta;
	}

	private double getAccelZAngle() {
		return 0.0D;
	}

	public double[] getAccelAccelerations() {
		return this.updatingThreadStopped ? new double[] { -1.0D, -1.0D, -1.0D }
				: new double[] { this.accelAccelerationX, this.accelAccelerationY, this.accelAccelerationZ };
	}

	public double[] getAccelAngles() {
		return this.updatingThreadStopped ? new double[] { -1.0D, -1.0D, -1.0D }
				: new double[] { this.accelAngleX, this.accelAngleY, this.accelAngleZ };
	}

	public double[] getGyroAngularSpeeds() {
		return this.updatingThreadStopped ? new double[] { -1.0D, -1.0D, -1.0D }
				: new double[] { this.gyroAngularSpeedX, this.gyroAngularSpeedY, this.gyroAngularSpeedZ };
	}

	public double[] getGyroAngles() {
		return this.updatingThreadStopped ? new double[] { -1.0D, -1.0D, -1.0D }
				: new double[] { this.gyroAngleX, this.gyroAngleY, this.gyroAngleZ };
	}

	public double[] getGyroAngularSpeedsOffsets() {
		return new double[] { this.gyroAngularSpeedOffsetX, this.gyroAngularSpeedOffsetY,
				this.gyroAngularSpeedOffsetZ };
	}

	public double[] getFilteredAngles() {
		return this.updatingThreadStopped ? new double[] { -1.0D, -1.0D, -1.0D }
				: new double[] { this.filteredAngleX, this.filteredAngleY, this.filteredAngleZ };
	}

//	public byte readRegister() throws IOException {
//		int data = mpu6050.read();
//		return (byte) data;
//	}

//	public void writeConfigRegisterAndValidate(String initialText, String successText, byte register, byte registerData)
//			throws IOException {
//		System.out.println(initialText);
//		writeRegister(register, registerData);
//		byte returnedRegisterData = readRegister(register);
//		if (returnedRegisterData == registerData) {
//			System.out.println(successText + formatBinary(returnedRegisterData));
//		} else {
//			throw new RuntimeException("Tried to write " + formatBinary(registerData) + " to " + register
//					+ ", but validiating value returned " + formatBinary(returnedRegisterData));
//		}
//	}
//
//	public static String formatBinary(byte b) {
//		String binaryString = Integer.toBinaryString(b);
//		if (binaryString.length() > 8) {
//			binaryString = binaryString.substring(binaryString.length() - 8);
//		}
//		if (binaryString.length() < 8) {
//			byte fillingZeros = (byte) (8 - binaryString.length());
//			for (int j = 1; j <= fillingZeros; j++) {
//				binaryString = "0" + binaryString;
//			}
//		}
//		return binaryString;
//	}
}
