package ca.hapke.util;

import java.nio.ByteBuffer;

/**
 * @author Mr. Hapke
 *
 */
public abstract class ByteUtil {

	public static byte[] intsToBytes(int[] input) {
		byte[] output = new byte[4 * input.length];
		for (int i = 0; i < input.length; i++) {
			intToBytes(input[i], output, 4 * i);
		}
		return output;
	}

	public static byte[] intsToBytes(Integer[] input) {
		byte[] output = new byte[4 * input.length];
		for (int i = 0; i < input.length; i++) {
			intToBytes(input[i], output, 4 * i);
		}
		return output;
	}

	public static void intToBytes(int d, byte[] output, int from) {

		for (int i = 0; i < 4; i++)
			output[from + i] = (byte) ((d >> ((3 - i) * 8)) & 0xff);
	}

	public static byte[] doublesToBytes(double[] input) {
		byte[] output = new byte[8 * input.length];
		for (int i = 0; i < input.length; i++) {
			doubleToBytes(input[i], output, 8 * i);
		}
		return output;
	}

	public static void doubleToBytes(double d, byte[] output, int from) {
		long lng = Double.doubleToLongBits(d);
		for (int i = 0; i < 8; i++)
			output[from + i] = (byte) ((lng >> ((7 - i) * 8)) & 0xff);
	}

	public static double[] bytesToDouble(byte[] bytes) {
		int doubles = bytes.length / 8;
		double[] output = new double[doubles];

		ByteBuffer buffer = ByteBuffer.wrap(bytes);

		for (int i = 0; i < doubles; i++)
			output[i] = buffer.getDouble();

		return output;
	}

	public static int[] bytesToInt(byte[] bytes) {
		int ints = bytes.length / 4;
		int[] output = new int[ints];

		ByteBuffer buffer = ByteBuffer.wrap(bytes);

		for (int i = 0; i < ints; i++)
			output[i] = buffer.getInt();

		return output;
	}

	/**
	 * TODO switch with Arrays.copySomething(from 1,length-1)?
	 * 
	 * @param data
	 * @return
	 */
	public static byte[] removeFrontPadding(byte[] input, int prefixBytes) {

		int targetLen = Byte.toUnsignedInt(input[1]);
		byte[] output = new byte[Math.max(0, Math.min(targetLen, input.length - prefixBytes))];

		for (int i = 0; i < output.length; i++) {
			output[i] = input[i + prefixBytes];
		}
		return output;
	}

}
