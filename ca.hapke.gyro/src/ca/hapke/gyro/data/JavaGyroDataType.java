package ca.hapke.gyro.data;

/**
 * @author Mr. Hapke
 *
 */
public class JavaGyroDataType extends AbstractGyroDataType {

	public JavaGyroDataType() {
		super(InputType.JavaGyro, "accelAngles", "accelAccelerations", "gyroAngles", "gyroAngularSpeeds",
				"filteredAngles");
	}

}
