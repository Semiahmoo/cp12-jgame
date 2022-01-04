package jgame.platform;

import ca.hapke.controller.data.IDataReceiveListener;
import ca.hapke.gyro.api.DataStatus;
import ca.hapke.gyro.api.GyroStatus;

/**
 * @author Mr. Hapke
 */
public class GyroNetworkStatus implements IDataReceiveListener, GyroStatus {


	private static final int STALE_THRESHOLD = 30;

	private int tick = 0;
	private DataStatus status;

	@Override
	public void serverOnline() {
		status = DataStatus.NoData;
	}

	@Override
	public void serverOffline() {
		status = DataStatus.Offline;
	}

	@Override
	public void accelGyroUpdated() {
		tick = 0;
		status = DataStatus.Active;
	}

	@Override
	public void sentenceReceived(String sentence) {
	}

	@Override
	public void serverAbort(String msg) {
		status = DataStatus.Offline;
	}

	@Override
	public DataStatus tick() {
		if (status == DataStatus.Offline || status == DataStatus.NoData) {
			return status;
		}
		tick++;
		if (tick <= STALE_THRESHOLD) {
			return DataStatus.Active;
		} else {
			return DataStatus.Stale;
		}
	}
}
