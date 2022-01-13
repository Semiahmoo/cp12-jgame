package ca.hapke.controller.serial;

import java.util.ArrayList;
import java.util.List;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

/**
 * @author Mr. Hapke
 */
public class ConnectedSerialPort {
	public static final byte START_BYTE = 0x12;
	public static final byte END_BYTE = 0x13;
	public static final byte ESCAPE_BYTE = 0x7D;
	public static final byte[] NEEDS_ESCAPING = { START_BYTE, END_BYTE, ESCAPE_BYTE };

	private SerialPort port;
	private SerialPortStatus status;
	private SerialPortEventListener listener;
	private ByteBuffer buffer = new ByteBuffer(START_BYTE, END_BYTE, ESCAPE_BYTE);
	private List<SerialListener> listeners = new ArrayList<>();

	public ConnectedSerialPort(SerialPort port) throws SerialPortException {
		this.port = port;
		port.setParams(SerialPort.BAUDRATE_57600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
		listener = new SerialPortEventListener() {

			@Override
			public void serialEvent(SerialPortEvent event) {
				if (event.isRXCHAR()) { // data is available
					int len = event.getEventValue();
//					System.out.println("Received [" + len + "] bytes");
					try {
						byte[] incoming = port.readBytes(len);
						buffer.enqueue(incoming);
//						System.out.println("In len[" + len + "]");
						// TODO thread this?
						if (buffer.isReady()) {
							for (SerialListener l : listeners) {
								l.dataReady();
							}
						}

					} catch (SerialPortException e) {
						if (SerialCommManager.SERIAL_COMM_DEBUG) {
							e.printStackTrace();
						}
					}
				} else if (event.isCTS()) { // CTS line has changed state
					if (event.getEventValue() == 1) { // line is ON
						System.out.println("CTS - ON");
					} else {
						System.out.println("CTS - OFF");
					}
				} else if (event.isDSR()) { // DSR line has changed state
					if (event.getEventValue() == 1) { // line is ON
						System.out.println("DSR - ON");
					} else {
						System.out.println("DSR - OFF");
					}
				}
			}
		};

		this.status = SerialPortStatus.Open;
	}

	public boolean open() {
		try {
			port.addEventListener(listener);
		} catch (SerialPortException e) {
			if (SerialCommManager.SERIAL_COMM_DEBUG) {
				e.printStackTrace();
			}
		}

		return true;
	}

	public void close() {
		try {
			if (port.isOpened())
				port.closePort();
		} catch (SerialPortException e) {
			if (SerialCommManager.SERIAL_COMM_DEBUG) {
				e.printStackTrace();
			}
		}
		status = SerialPortStatus.Closed;
	}

	public SerialPortStatus getStatus() {
		return status;
	}

	public void send(byte[] bytes) throws SerialPortException {
		byte[] encapsulated = encapsulate(bytes);
		port.writeBytes(encapsulated);
	}

	/**
	 * Public for testing
	 */
	public static byte[] encapsulate(byte[] input) {
		// one for begin, one for end
		int count = 2;
		int orignalLen = input.length;
		for (int i = 0; i < orignalLen; i++) {
			byte b = input[i];
			for (byte esc : NEEDS_ESCAPING) {
				if (b == esc)
					count++;
			}
		}
		int outputLen = orignalLen + count;
		byte[] output = new byte[outputLen];
		output[0] = START_BYTE;

		int offset = 1;
		for (int i = 0; i < orignalLen; i++) {
			byte b = input[i];
			for (byte esc : NEEDS_ESCAPING) {
				if (b == esc) {
					output[i + offset] = ESCAPE_BYTE;
					offset++;
				}
				output[i + offset] = b;
			}
		}
		output[outputLen - 1] = END_BYTE;
		return output;
	}

	public static byte[] decapsulate(byte[] input) {
		int count = 2;
		int orignalLen = input.length;
		for (int i = 1; i < orignalLen - 1; i++) {
			byte b = input[i];
			if (b == ESCAPE_BYTE) {
				count++;
				i++;
			}
		}

		int outputLen = orignalLen - count;
		byte[] output = new byte[outputLen];

		int offset = 1;
		for (int i = 0; i < output.length; i++) {
			byte b = input[i + offset];
			if (b == ESCAPE_BYTE) {
				// skip this byte
				offset++;

				// now b needs to be the thing that got escaped
				b = input[i + offset];
			}
			output[i] = b;

		}

		return output;
	}

	public byte[] dequeue() {
		return buffer.dequeue();
	}

	public boolean addListener(SerialListener l) {
		return listeners.add(l);
	}

	public boolean removeListener(SerialListener l) {
		return listeners.remove(l);
	}
}
