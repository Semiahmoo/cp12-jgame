package ca.hapke.controller.serial.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ca.hapke.controller.serial.ByteBuffer;

/**
 * @author Mr. Hapke
 */
public class ByteBufferTest {

	private static final byte START = 0x3;
	private static final byte END = 0x7;
	private static final byte ESC = 0x7d;

	private ByteBuffer bb;
	private static final byte[] fifteen, oneThirtySeven, twentyToForty, tenToOne, twoPackets;

	static {
		fifteen = new byte[15];
		for (int i = 0; i < 15; i++) {
			fifteen[i] = (byte) (i + 1);
		}
		oneThirtySeven = new byte[137];
		for (int i = 0; i < 137; i++) {
			oneThirtySeven[i] = (byte) (i + 1);
		}

		twentyToForty = new byte[21];
		for (int i = 0; i < 21; i++) {
			twentyToForty[i] = (byte) (i + 20);
		}

		tenToOne = new byte[10];
		for (int i = 0; i < 10; i++) {
			tenToOne[i] = (byte) (10 - i);
		}

		twoPackets = new byte[139];
		for (int i = 0; i < 7; i++) {
			twoPackets[i] = (byte) (i + 1);
		}
		twoPackets[7] = START;

		for (int i = 8; i <= 137; i++) {
			twoPackets[i] = (byte) (i);
		}
		twoPackets[138] = END;
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		bb = new ByteBuffer(START, END, ESC);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		bb = null;
	}

	@Test
	public void testEnqueueAndSizeUp() {

		assertEquals(0, bb.size());
		assertEquals(ByteBuffer.DEFAULT_CAPACITY, bb.getCapacity());
		bb.enqueue(fifteen);
		assertEquals(15, bb.size());
		bb.enqueue(fifteen);
		assertEquals(30, bb.size());
		bb.enqueue(oneThirtySeven);
		assertEquals(167, bb.size());
		assertEquals(ByteBuffer.DEFAULT_CAPACITY, bb.getCapacity());
		bb.enqueue(oneThirtySeven);
		assertEquals(304, bb.size());
		assertEquals(2 * ByteBuffer.DEFAULT_CAPACITY, bb.getCapacity());
		bb.enqueue(oneThirtySeven);
		assertEquals(441, bb.size());
		assertEquals(2 * ByteBuffer.DEFAULT_CAPACITY, bb.getCapacity());
	}

	@Test
	public void testDequeueBasic() {
		assertEquals(0, bb.size());
		assertEquals(ByteBuffer.DEFAULT_CAPACITY, bb.getCapacity());
		bb.enqueue(fifteen);
		assertEquals(15, bb.size());

		byte[] result = bb.dequeue();
		assertEquals(3, result.length);
		// 2 bytes get punted: ([0]=1 and [1]=2)
		assertEquals(8, bb.size());

	}

	@Test
	public void testDequeueNotReady() {
		assertEquals(0, bb.size());
		bb.enqueue(twentyToForty);
		byte[] result = bb.dequeue();

		assertNull(result);
	}

	@Test
	public void testDequeueNotWrapping() {
		assertEquals(0, bb.size());
		bb.enqueue(tenToOne);
		byte[] result = bb.dequeue();

		assertNull(result);
	}

	@Test
	public void testEncapsulateDecapsulate() {

		assertEquals(0, bb.size());
		bb.enqueue(twoPackets);

		byte[] result = bb.dequeue();
		assertNotNull(result);
		assertEquals(3, result.length);

		assertEquals(4, result[0]);
		assertEquals(5, result[1]);
		assertEquals(6, result[2]);

		result = bb.dequeue();

		assertNotNull(result);
		assertEquals(129, result.length);
		for (int i = 0; i <= 116; i++) {
			assertEquals(8 + i, result[i]);
		}
		for (int i = 117; i <= 128; i++) {
			int x = Byte.toUnsignedInt(result[i]);
			assertEquals(9 + i, x);
		}

	}

	@Test
	public void testBigBufferWithEscape() {
		byte[] a = new byte[1026];

		for (int i = 0; i < 537; i++) {
			a[i] = 0x43;
		}
		a[537] = START;
		for (int i = 538; i < a.length; i++) {
			a[i] = 0x43;
		}

		bb.enqueue(a);
		assertEquals(bb.size(), 1026);
		assertTrue(bb.getCapacity() > 1024);

		byte[] b = new byte[2100];

		for (int i = 0; i < 126; i++) {
			b[i] = 0x79;
		}
		b[126] = ESC;
		b[127] = END;
		for (int i = 128; i < b.length - 1; i++) {
			b[i] = 0x63;
		}

		b[b.length - 1] = END;

		bb.enqueue(b);
		assertEquals(bb.size(), 3126);
		assertTrue(bb.getCapacity() > 3000);

		byte[] result = bb.dequeue();
		assertNotNull(result);
		// 488 (1026-538) + 2098 (1 end char, 1 esc char) = 2586
		assertEquals(2586, result.length);
		for (int i = 0; i <= 487; i++) {
			assertEquals(0x43, result[i]);
		}

		for (int i = 488; i <= 613; i++) {
			assertEquals(0x79, result[i]);
		}

		assertEquals(END, result[614]);
		for (int i = 615; i <= 2585; i++) {
			assertEquals(0x63, result[i]);
		}
	}
}
