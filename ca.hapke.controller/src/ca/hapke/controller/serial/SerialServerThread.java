//package ca.hapke.controller.serial;
//
//import java.io.IOException;
//import java.util.Arrays;
//
//import com.fazecast.jSerialComm.SerialPort;
//
//import ca.hapke.controller.data.ServerThread;
//
///**
// * @author Mr. Hapke
// */
//public class SerialServerThread extends ServerThread {
//
//	private ConnectedSerialPort connection;
//	private SerialPort serial;
//
//	public SerialServerThread(ConnectedSerialPort port) {
//		this.connection = port;
//		this.serial = connection.getPort();
//	}
//
//	@Override
//	protected boolean activateServer() {
//		return connection.getStatus() == SerialPortStatus.Open;
//	}
//
//	@Override
//	protected byte[] receivePacket(byte[] recvData) throws IOException {
//		int available = serial.bytesAvailable();
//		
//		byte[] readBuffer = new byte[available];
//		int numRead = serial.readBytes(readBuffer, readBuffer.length);
//
//		byte[] output = Arrays.copyOf(readBuffer, numRead);
//		return output;
//	}
//
//}
