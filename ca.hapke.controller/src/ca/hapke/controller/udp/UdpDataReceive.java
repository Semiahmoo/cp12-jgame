package ca.hapke.controller.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import ca.hapke.controller.data.DataReceive;
import ca.hapke.controller.data.IDataReceiveListener;
import ca.hapke.gyro.data.DataCluster;
import ca.hapke.util.RunKillThread;

/**
 * @author Mr. Hapke
 */
public class UdpDataReceive extends DataReceive {
	private class UdpPollingThread extends RunKillThread {
		byte[] recvData = new byte[256];

		@Override
		protected void doWork() {
			while (!kill) {
				try {
					byte[] rawData = receivePacket(recvData);
					processIncomingTransmission(rawData);
				} catch (IOException e) {
					kill = true;
					for (IDataReceiveListener listener : listeners)
						listener.serverAbort("ABORT:" + e.getMessage());
				}
			}

			for (IDataReceiveListener listener : listeners)
				listener.serverOffline();
		}

	}

	private final int port;
	private DatagramSocket serverSocket;
	private UdpPollingThread pollingThread;

	public UdpDataReceive(DataCluster cluster, int port) {
		super(cluster);
		this.port = port;
	}

	protected byte[] receivePacket(byte[] recvData) throws IOException {
		DatagramPacket recvP = new DatagramPacket(recvData, recvData.length);
		serverSocket.receive(recvP);
		byte[] rawData = recvP.getData();
		return rawData;
	}

	@Override
	public boolean activateServer() {
		try {
			if (pollingThread == null || pollingThread.isFinished()) {
				serverSocket = new DatagramSocket(port);
				pollingThread = new UdpPollingThread();
				pollingThread.start();

				for (IDataReceiveListener listener : listeners)
					listener.serverOnline();
				running = true;
				return true;
			}

		} catch (Exception e) {
			for (IDataReceiveListener listener : listeners)
				listener.serverAbort("Failed to bind port: " + e.toString());
		}
		return false;
	}

	@Override
	public boolean shutdownServer() {
		if (pollingThread != null || serverSocket != null) {
			if (pollingThread != null) {
				pollingThread.kill();
				pollingThread = null;
			}
			if (serverSocket != null) {
				serverSocket.close();
				serverSocket = null;
			}
			running = false;
			return true;
		} else {
			return false;
		}
	}
}
