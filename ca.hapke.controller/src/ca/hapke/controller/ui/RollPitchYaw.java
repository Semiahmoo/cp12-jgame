package ca.hapke.controller.ui;

/**
 * @author Mr. Hapke
 */
public class RollPitchYaw {
	public final Angle roll;
	public final Angle pitch;
	public final Angle yaw;

	public RollPitchYaw() {

		roll = new Angle();
		pitch = new Angle();
		yaw = new Angle();
	}
}