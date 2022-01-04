package ca.hapke.gyro.data;

/**
 * @author Mr. Hapke
 *
 */
public class CsGyroRelayDataType extends AbstractGyroDataType {

	public CsGyroRelayDataType() {
		super(InputType.JavaGyro, "accelAccelerations", "gyroAngularSpeeds");
	}

}
