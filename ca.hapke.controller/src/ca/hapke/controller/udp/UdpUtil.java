package ca.hapke.controller.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import ca.hapke.controller.data.TransmitMode;

/**
 * 
 * @author Mr. Hapke
 *
 */
public abstract class UdpUtil {

	public static void send(String ip, int port, String text) throws IOException {
		byte[] buffer = new byte[Math.min(255, text.length() + 1)];

		int i = 0;
		for (; i < buffer.length - 1 && i < text.length(); i++) {
			buffer[i] = (byte) text.charAt(i);
		}
		// null terminate
		buffer[i] = 0;

		send(ip, port, buffer, TransmitMode.String.mode);
	}

	/**
	 * sends first (up to) 256-PREFIX_BYTES bytes, prefixed with PREFIX_BYTES
	 * telling the mode and length after it.
	 */
	public static void send(String ip, int port, byte[] buffer, byte mode) throws IOException {
		InetAddress target = InetAddress.getByName(ip);

//		byte[] out = new byte[Math.min(256, buffer.length + PREFIX_BYTES)];
//		// byte[] out = Arrays.copyOfRange(buffer, 0, 1);
//		int length = Math.min(255, buffer.length);
//		out[0] = mode;
//		out[1] = (byte) length;
//		for (int i = 0; i < length; i++)
//			out[i + PREFIX_BYTES] = buffer[i];

		DatagramPacket packet = new DatagramPacket(buffer, buffer.length, target, port);
		DatagramSocket datagramSocket = new DatagramSocket();
		datagramSocket.send(packet);

		datagramSocket.close();
	}

}
