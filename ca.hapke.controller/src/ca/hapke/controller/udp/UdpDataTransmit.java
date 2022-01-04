package ca.hapke.controller.udp;

import java.io.IOException;

import ca.hapke.controller.data.DataTransmit;
import ca.hapke.controller.data.TransmitMode;
import ca.hapke.gyro.data.DataCluster;

/**
 * @author Mr. Hapke
 *
 */
public class UdpDataTransmit extends DataTransmit {
	public UdpDataTransmit(DataCluster inputs) {
		super(inputs);
	}


	public void setTarget(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}


	protected void sendPacket(byte[] bytes) throws IOException {
		UdpUtil.send(ip, port, bytes, TransmitMode.AccelGyroData.mode);
	}

}
