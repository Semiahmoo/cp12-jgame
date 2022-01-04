package ca.hapke.controller.serial;

/**
 * Circular Array that auto-resizes once the capacity is reached
 * 
 * @author Mr. Hapke
 */
public class ByteBuffer {

	public static final int DEFAULT_CAPACITY = 256;
	private byte[] buf;
	private int start = 0;
	private int end = 0;
	private boolean wrapEnabled = false;
	private final byte starter;
	private final byte ender;
	private final byte escaper;
	private int initialCapacity;
	private int sizeIncrements;

	public ByteBuffer(byte starter, byte ender, byte escaper) {
		this(DEFAULT_CAPACITY, starter, ender, escaper);
	}

	public ByteBuffer(int capacity, byte starter, byte ender, byte escaper) {
		this.initialCapacity = capacity;
		this.starter = starter;
		this.ender = ender;
		this.escaper = escaper;
		clear();
	}

	private void clear() {
		buf = new byte[this.initialCapacity];
		sizeIncrements = 0;
		start = 0;
		end = 0;
	}

	public void enqueue(byte[] input) {
		int qty = size();
		int capacity = buf.length;
		int addedLength = input.length;
		int endQty = qty + addedLength;
		if (endQty > capacity) {
			while (capacity < endQty) {
				capacity *= 2;
			}
			increaseCapacity(capacity);
		}

		// HACK BUFFER OVERFLOW ESCAPE?
		if (sizeIncrements > 6) {
			System.err.println("DUMP ALL DATA");
			clear();
		}
		
		int fromIndex = end;
		byte[] targetArray = buf;
		// enqueue without worrying about escaping characters
		copyAndWrap(input, 0, addedLength, targetArray, fromIndex, escaper, false);

		end = (end + addedLength) % targetArray.length;
	}

	private void copyAndWrap(byte[] fromArray, int fromStart, int qty, byte[] targetArray, int targetStart,
			byte escaper, boolean escaperOn) {
		int i = fromStart;
		int j = targetStart;
		int q = 0;
		while (q < qty) {
			byte c = fromArray[i];
			i++;
			if (!escaperOn || c != escaper) {

				targetArray[j] = c;
				j++;
				if (i >= fromArray.length) {
					i = 0;
				}
				if (j >= targetArray.length) {
					j = 0;
				}

				q++;
			}
		}
	}

	private StartEndWrap findStartAndEnd() {
		int start = -1;
		int end = -1;
		int escCount = 0;
		boolean wrap = false;

		int i = this.start;
		while (i < buf.length) {
			byte b = buf[i];
			if (start == -1) {
				if (b == escaper) {
					escCount++;
					// ignore
				} else if (b == starter) {
					start = i;
				}
			} else {
				if (b == escaper) {
					escCount++;
					i++;
				} else if (b == ender) {
					end = i;
					break;
				}
			}
			i++;
		}
		if (wrapEnabled && (start == -1 || end == -1)) {
			if (start != -1) {
				wrap = true;
			}
			i = 0;
			while (i < this.end) {
				byte b = buf[i];
				if (start == -1) {
					if (b == starter) {
						start = i;
					}
				} else {
					if (b == ender) {
						end = i;
						break;
					}
				}
				i++;
			}
		}
		if (start == -1 || end == -1)
			return null;

		return new StartEndWrap(start, end, wrap, escCount);
	}

	public boolean isReady() {
		StartEndWrap locations = findStartAndEnd();
		return locations != null;
	}

	/**
	 * Everything before the first indexOf the starter is dropped on the floor.
	 */
	public byte[] dequeue() {
		StartEndWrap startEnd = findStartAndEnd();
		if (startEnd == null)
			return null;

		int start = startEnd.start + 1;
		int end = startEnd.end - 1;
		boolean wrap = startEnd.wrap;

		int qty;

		if (!wrap) {
			qty = end - start + 1;
		} else {
			qty = buf.length - start + end + 1;

		}
		qty -= startEnd.escCount;
		if (qty < 0)
			return null;
		byte[] output = new byte[qty];
		// the dequeue should remove the escape characters as it copies
		copyAndWrap(buf, start, qty, output, 0, escaper, true);
		this.start = startEnd.end + 1;
		return output;
	}

	private void increaseCapacity(int capacity) {
		byte[] newBuf = new byte[capacity];
		sizeIncrements++;
		System.out.println("increaseCapacity: " + sizeIncrements);

		// copy from start to (end or EndOfArray)
		int i = start;
		int j = 0;
		boolean wrap = end < start;
		int stop = wrap ? buf.length : end;
		while (i < stop) {
			newBuf[j] = buf[i];

			i++;
			j++;
		}

		if (wrap) {
			i = 0;
			while (i < end) {
				newBuf[j] = buf[i];

				i++;
				j++;
			}
		}
		start = 0;
		end = j;
		buf = newBuf;
		wrapEnabled = false;
	}

	public int size() {
		if (start == end)
			return 0;
		else if (start < end) {
			return end - start;
		} else {

			return buf.length - end + start;
		}
	}

	public int getCapacity() {
		return buf.length;
	}
}
