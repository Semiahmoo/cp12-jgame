package ca.hapke.controller.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ca.hapke.controller.serial.SerialCommManager;
import ca.hapke.controller.udp.UdpUtil;
import ca.hapke.gyro.data.DataCluster;
import ca.hapke.gyro.data.DataType;
import ca.hapke.gyro.data.DataType.InputType;
import ca.hapke.util.ByteUtil;

/**
 * @author Mr. Hapke
 *
 */
public abstract class DataReceive {

	protected final DataCluster cluster;
	protected final List<IDataReceiveListener> listeners = new ArrayList<>();
	protected boolean running = false;

	public DataReceive(DataCluster cluster) {
		this.cluster = cluster;
	}

	public void processIncomingTransmission(byte[] fullPacket) {
		if (fullPacket == null || fullPacket.length < DataTransmit.PREFIX_LENGTH)
			return;
		try {
			byte[] dataOnly = ByteUtil.removeFrontPadding(fullPacket, DataTransmit.PREFIX_LENGTH);

			byte mode = fullPacket[0];
			byte len = fullPacket[1];
			byte packetNum = fullPacket[2];
			byte crcExpected = fullPacket[3];
			byte crcActual = ErrorChecksum.calculateCrc8(fullPacket, DataTransmit.PREFIX_LENGTH);
			if (crcExpected == crcActual) {
				processIncomingTransmission(mode, len, dataOnly);
			} else {
				int exp = Byte.toUnsignedInt(crcExpected);
				int act = Byte.toUnsignedInt(crcActual);
				int delta = exp - act;

				if (SerialCommManager.SERIAL_COMM_DEBUG) {
					System.out
							.println("Crc fail: Expected[" + exp + "] -- Actual[" + act + "] --> Delta[" + delta + "]");
				}
			}
		} catch (Exception e) {
			if (SerialCommManager.SERIAL_COMM_DEBUG) {
				e.printStackTrace();
			}
		}
	}

	private void processIncomingTransmission(byte modeByte, int remainingLen, byte[] dataOnly) {
		TransmitMode mode = TransmitMode.fromMode(modeByte);

//		System.out.println("Mode: [" + modeByte + "] =>" + mode);
//		System.out.println(" Len: [" + remainingLen + "] ");

		String sentence = "";

		if (mode != null) {
			switch (mode) {
			case Bytes:
				sentence = "(raw data un-processed)";
				break;
			case AccelGyroData:
				// double[] doubles = ByteUtil.bytesToDouble(data);
				// for (int i = 0; i < doubles.length; i++) {
				// double d = doubles[i];
				//
				// if (sentence.length() > 0)
				// sentence = sentence + ", ";
				// sentence = sentence + nf.format(d);
				// }
				// cluster.update(data);
				updateCluster(dataOnly);
				for (IDataReceiveListener listener : listeners)
					listener.accelGyroUpdated();
				break;
			case String:
				sentence = new String(dataOnly);
				for (IDataReceiveListener listener : listeners)
					listener.sentenceReceived(sentence);
				break;
			}
		}
	}

	public void updateCluster(byte[] data) {
		try {
			byte sets = data[0];
			byte[] ids = new byte[sets];
			int[] lens = new int[sets];
			for (int i = 0; i < sets; i++) {
				ids[i] = data[2 * i + 1];
				lens[i] = Byte.toUnsignedInt(data[2 * i + 2]);
			}

			int from = 2 * sets + 1;
			for (int i = 0; i < sets; i++) {
				byte id = ids[i];

				DataType<?> dataSet = cluster.getData(InputType.fromId(id));

				int end = from + lens[i];
				byte[] vals = Arrays.copyOfRange(data, from, end);
				dataSet.update(vals);

				from = end;
			}
		} catch (Exception e) {
		}
	}

	public abstract boolean activateServer();

	public abstract boolean shutdownServer();

	public void add(IDataReceiveListener l) {
		if (l != null) {
			listeners.add(l);
		}
	}

	public boolean isRunning() {
		return running;
	}
}
