package ca.hapke.controller.serial;

import java.security.cert.CertStoreParameters;

//import com.fazecast.jSerialComm.SerialPortDataListener;
//import com.fazecast.jSerialComm.SerialPortMessageListener;

import ca.hapke.controller.data.DataReceive;
import ca.hapke.controller.data.TransmitMode;
import ca.hapke.gyro.data.DataCluster;
import ca.hapke.util.ByteUtil;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

/**
 * @author Mr. Hapke
 */
public class SerialDataReceive extends DataReceive {

	private ConnectedSerialPort connection;

	public SerialDataReceive(DataCluster cluster, ConnectedSerialPort port) {
		super(cluster);
		this.connection = port;
		connection.addListener(new SerialListener() {

			@Override
			public void dataReady() {
				byte[] packet = connection.dequeue();
				processIncomingTransmission(packet);
			}
		});
	}

	@Override
	public boolean activateServer() {
		return connection.open();
	}

	@Override
	public boolean shutdownServer() {
		connection.close();
		return true;
	}

}
