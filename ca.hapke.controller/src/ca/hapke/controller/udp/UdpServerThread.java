//package ca.hapke.controller.udp;
//
//import java.io.IOException;
//import java.net.DatagramPacket;
//import java.net.DatagramSocket;
//
//import ca.hapke.controller.data.IDataReceiveListener;
//import ca.hapke.controller.data.ServerThread;
//import ca.hapke.gyro.data.DataCluster;
//
///**
// * TODO Change architecture so that you activateServer() first, which will then launch the thread after
// * @Deprecated Replaced by UdpDataReceive
// * @author Mr. Hapke
// */
//@Deprecated
//public class UdpServerThread extends ServerThread {
//
//	private final int port;
//	private DatagramSocket serverSocket;
//	public UdpServerThread(int port, DataCluster cluster) {
//		this.port = port;
//		this.cluster = cluster;
//	}
//
//	@Override
//	protected byte[] receivePacket(byte[] recvData) throws IOException {
//		DatagramPacket recvP = new DatagramPacket(recvData, recvData.length);
//		serverSocket.receive(recvP);
//		byte[] rawData = recvP.getData();
//		return rawData;
//	}
//
//	@Override
//	protected boolean activateServer() {
//		try {
//			serverSocket = new DatagramSocket(port);
//			for (IDataReceiveListener listener : listeners)
//				listener.serverOnline();
//			return true;
//
//		} catch (Exception e) {
//			kill = true;
//
//			for (IDataReceiveListener listener : listeners)
//				listener.serverAbort("Failed to bind port: " + e.toString());
//		}
//		return false;
//	}
//
//}
