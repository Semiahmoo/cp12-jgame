package ca.hapke.controller.serial.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import ca.hapke.controller.serial.ConnectedSerialPort;

/**
 * @author Mr. Hapke
 */
public class ConnectedSerialPortTests {
	public final static byte[] a = new byte[]{ 0x6f, 0x7f, 0x10, 0x0, 0x12, 0x5, 0x13, 0x66 };
	public final static byte[] b = new byte[]{ 0x12, 0x6f, 0x7f, 0x10, 0x0, 0x7d, 0x12, 0x5, 0x7d, 0x13, 0x66, 0x13 };

	@Test
	public void testEncapsulate() {
		assertEquals(8, a.length);
		byte[] output = ConnectedSerialPort.encapsulate(a);
		assertEquals(12, output.length);

		assertEquals(ConnectedSerialPort.START_BYTE, output[0]);
		assertEquals(0x6f, output[1]);
		assertEquals(ConnectedSerialPort.ESCAPE_BYTE, output[5]);
		assertEquals(0x12, output[6]);
		assertEquals(ConnectedSerialPort.ESCAPE_BYTE, output[8]);
		assertEquals(0x13, output[9]);
		assertEquals(0x66, output[10]);
		assertEquals(ConnectedSerialPort.END_BYTE, output[11]);
		
		for(int j  = 0; j < b.length && j < output.length; j++) {
			assertEquals(b[j], output[j]);
		}
	}


	@Test
	public void testDecapsulate() {
		assertEquals(12, b.length);
		byte[] output = ConnectedSerialPort.decapsulate(b);
		assertEquals(8, output.length);


		for(int j  = 0; j < b.length && j < output.length; j++) {
			assertEquals(a[j], output[j]);
		}
	}
}
