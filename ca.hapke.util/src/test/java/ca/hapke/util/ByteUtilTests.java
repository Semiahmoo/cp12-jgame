package ca.hapke.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * @author Nathan Hapke
 */
class ByteUtilTests {

	@Test
	void testIntsToBytesIntArray() {
		int[] input = new int[] { 0x1a2b3c4d, 0x907c6d8f };
		byte[] output = ByteUtil.intsToBytes(input);

		assertEquals((byte) 0x1a, output[0]);
		assertEquals((byte) 0x2b, output[1]);
		assertEquals((byte) 0x3c, output[2]);
		assertEquals((byte) 0x4d, output[3]);

		assertEquals((byte) 0x90, output[4]);
		assertEquals((byte) 0x7c, output[5]);
		assertEquals((byte) 0x6d, output[6]);
		assertEquals((byte) 0x8f, output[7]);
	}

}
