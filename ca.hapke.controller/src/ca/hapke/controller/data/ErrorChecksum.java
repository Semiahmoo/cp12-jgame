package ca.hapke.controller.data;

import ca.hapke.util.ByteUtil;

/**
 * @author Mr. Hapke
 */
public abstract class ErrorChecksum {

	private static final byte[] CRC_TABLE = new byte[256];
	private static final byte CRC_GENERATOR = 0x1D;
	static {
		/* iterate over all byte values 0 - 255 */
		for (int divident = 0; divident < 256; divident++) {
			byte currByte = (byte) divident;
			/* calculate the CRC-8 value for current byte */
			for (byte bit = 0; bit < 8; bit++) {
				if ((currByte & 0x80) != 0) {
					currByte <<= 1;
					currByte ^= CRC_GENERATOR;
				} else {
					currByte <<= 1;
				}
			}
			/* store CRC value in lookup table */
			CRC_TABLE[divident] = currByte;
		}
	}

	public static byte calculateCrc8(byte[] output, int startIndex) {
		byte crc = 0;
		for (int i = startIndex; i < output.length; i++) {
			byte b = output[i];
			/* XOR-in next input byte */
			byte data = (byte) (b ^ crc);
			int j = Byte.toUnsignedInt(data);
			/* get current CRC value = remainder */
			crc = (byte) (CRC_TABLE[j]);
		}

		return crc;
	}

}
