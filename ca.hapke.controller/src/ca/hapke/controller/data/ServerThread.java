//package ca.hapke.controller.data;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
//import ca.hapke.controller.udp.UdpUtil;
//import ca.hapke.gyro.data.DataCluster;
//import ca.hapke.util.ByteUtil;
//import ca.hapke.util.RunKillThread;
//
///**
// * @author Mr. Hapke
// */
//public abstract class ServerThread extends RunKillThread {
//
//	protected DataCluster cluster;
//
//	protected abstract boolean activateServer();
//
//	/**
//	 * Blocking until a packet is received.
//	 */
//	protected abstract byte[] receivePacket(byte[] recvData) throws IOException;
//
//	protected List<IDataReceiveListener> listeners = new ArrayList<>();
//
//	@Override
//	public void doWork() {
//			byte[] recvData = new byte[256];
//			try {
//				boolean online = activateServer();
//				kill = !online;
//	
//				while (!kill) {
//					byte[] rawData = receivePacket(recvData);
//					byte[] data = ByteUtil.removeFrontPadding(rawData, UdpUtil.PREFIX_BYTES);
//	
//					String sentence = "";
//					TransmitMode mode = TransmitMode.fromMode(rawData[0]);
//					switch (mode) {
//					case Bytes:
//						sentence = "(raw data un-processed)";
//						break;
//					case AccelGyroData:
//	//					double[] doubles = ByteUtil.bytesToDouble(data);
//						// for (int i = 0; i < doubles.length; i++) {
//						// double d = doubles[i];
//						//
//						// if (sentence.length() > 0)
//						// sentence = sentence + ", ";
//						// sentence = sentence + nf.format(d);
//						// }
//	//					cluster.update(data);
//						DataReceive.update(cluster, data);
//						for (IDataReceiveListener listener : listeners)
//							listener.accelGyroUpdated();
//						break;
//					case String:
//						sentence = new String(data);
//						for (IDataReceiveListener listener : listeners)
//							listener.sentenceReceived(sentence);
//						break;
//	
//					}
//				}
//			} catch (Exception e) {
//				for (IDataReceiveListener listener : listeners)
//					listener.serverAbort("ABORT:" + e.getMessage());
//			}
//	
//			for (IDataReceiveListener listener : listeners)
//				listener.serverOffline();
//		}
//
//	public void add(IDataReceiveListener l) {
//		listeners.add(l);
//	}
//
//}