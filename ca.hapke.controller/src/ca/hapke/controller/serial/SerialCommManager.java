package ca.hapke.controller.serial;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

/**
 * @author Mr. Hapke
 */
public class SerialCommManager {
	public static final boolean SERIAL_COMM_DEBUG = false;
	//	 public static final boolean SERIAL_COMM_DEBUG = true;

	private EventList<String> portsEvents = new BasicEventList<String>();

	public void updatePorts() {
		portsEvents.clear();
		for (String p : SerialPortList.getPortNames()) {
			portsEvents.add(p);
		}
	}

	public ConnectedSerialPort connect(String port) throws SerialPortException {
		ConnectedSerialPort cp = null;
		SerialPort commPort = new SerialPort(port);
		boolean result = commPort.openPort();

		if (result) {
			cp = new ConnectedSerialPort(commPort);
		}
		return cp;
	}

	public EventList<String> getPortsEvents() {
		return portsEvents;
	}
}
