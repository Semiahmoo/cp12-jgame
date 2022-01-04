package ca.hapke.controller.data;

import java.io.IOException;

import ca.hapke.gyro.data.DataCluster;
import ca.hapke.gyro.data.DataType;
import ca.hapke.util.RunKillThread;

/**
 * @author Mr. Hapke
 */
public abstract class DataTransmit {
	/**
	 * TODO Change to RunKillThread? Add a delay function?
	 */
	private class SendThread extends RunKillThread {
		@Override
		protected void doWork() {
			while (!kill) {
				byte[] bytes = getPacket();
				try {
					sendPacket(bytes);
				} catch (IOException e) {
					kill = true;
				}
				try {
					Thread.sleep(25);
				} catch (InterruptedException e) {
					kill = true;
				}
			}
		}
	}

	protected static final int PREFIX_LENGTH = 4;

	protected int port;
	protected String ip;
	protected boolean enabled = false;
	protected Thread t;
	protected DataCluster inputs;

	protected byte packetNum = 0;

	public DataTransmit(DataCluster inputs) {
		this.inputs = inputs;
	}

	protected abstract void sendPacket(byte[] bytes) throws IOException;

	public byte[] getPacket() {
		int clusterCount = inputs.size();
		int len = 1 + 2 * clusterCount;
		// 1 byte for the count of clusters at beginning of packet, and 2 bytes for each
		// input: <their id, their length>

		byte[][] datas = new byte[clusterCount][];

		// collect byte[] from every DataCluster
		int i = 0;
		for (DataType<?> input : inputs.getValues()) {
			datas[i] = input.getBytes();
			len += datas[i].length;
			i++;
		}

		byte[] output = new byte[len + PREFIX_LENGTH];
		// Prefix:
		// [0] = Mode
		// [1] = Remaining length of the packet
		output[0] = TransmitMode.AccelGyroData.mode;
		output[1] = (byte) len;
		output[2] = packetNum++;
		output[PREFIX_LENGTH] = (byte) clusterCount;
		// the index is an index into the inputs Collection, not into the output array
		i = 0;
		for (DataType<?> input : inputs.getValues()) {
			byte[] segment = datas[i];
			output[PREFIX_LENGTH + 2 * i + 1] = input.type.id;
			output[PREFIX_LENGTH + 2 * i + 2] = (byte) segment.length;
			i++;
		}

		// copy the individual segments out of the clusters
		int pos = PREFIX_LENGTH + 1 + clusterCount * 2;
		for (i = 0; i < datas.length; i++) {
			int length = datas[i].length;
			for (int j = 0; j < length; j++) {
				output[pos + j] = datas[i][j];
			}
			// move the output iterator to the end of that message that's been added
			pos += length;
		}

		output[3] = ErrorChecksum.calculateCrc8(output, PREFIX_LENGTH);

		return output;
	}

	public void start() {
		if (t == null || !t.isAlive()) {
			enabled = true;
			t = new SendThread();
			t.start();
		}
	}

	public void stop() {
		enabled = false;
	}

	public void add(DataType<?> dt) {
		inputs.add(dt);
	}
}