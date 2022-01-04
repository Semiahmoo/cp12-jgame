package ca.hapke.controller.serial;

import java.io.IOException;

import ca.hapke.controller.data.DataTransmit;
import ca.hapke.gyro.data.DataCluster;
import jssc.SerialPortException;

/**
 * @author Mr. Hapke
 */
public class SerialDataTransmit extends DataTransmit {

	private ConnectedSerialPort serialPort;

	public SerialDataTransmit(DataCluster inputs, ConnectedSerialPort port) {
		super(inputs);
		serialPort = port;
	}

	@Override
	protected void sendPacket(byte[] bytes) throws IOException {
		try {
			serialPort.send(bytes);
		} catch (SerialPortException e) {
		}
	}

}
