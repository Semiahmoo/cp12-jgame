package ca.hapke.gyro.data;

import java.text.NumberFormat;

/**
 * @author Mr. Hapke
 *
 */
public abstract class DataType<T> {
	public enum InputType {
		ADC(2), JavaGyro(1), ArcadeButton(3), CsGyroRelay(4);

		public final byte id;

		private InputType(int id) {
			this.id = (byte) id;
		}

		public static InputType fromId(int x) {
			for (InputType dt : values()) {
				if (x == dt.id) {
					return dt;
				}
			}
			return null;
		}
	}

	protected final String[] dimensionNames;
	protected final T[] data;
	public final InputType type;
	protected final int dims;
	private NumberFormat nf;

	protected DataType(InputType type, String... dimensionNames) {
		this.type = type;
		this.dimensionNames = dimensionNames;
		this.dims = dimensionNames.length;
		this.data = createData(dims);
		this.nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(1);
		nf.setMinimumIntegerDigits(3);
		setDefaults();
	}

	protected abstract void setDefaults();

	protected abstract T[] createData(int dims);

	public static DataType create(InputType t) {
		switch (t) {
		case ADC:
			return new AdcDataType();
		case ArcadeButton:
			return new ArcadeButtonDataType();
		case JavaGyro:
			return new JavaGyroDataType();
		case CsGyroRelay:
			return new CsGyroRelayDataType();
		}
		return null;
	}

	public abstract byte[] getBytes();

//	public final void update(byte[] vals) {
//		calcFps();
//		doUpdate(vals);
//	}

	public abstract void update(byte[] vals);

	public T[] getData() {
		return data;
	}

	public T getData(int i) {
		if (i < data.length)
			return data[i];
		else
			return null;
	}

	public void updateByIndex(int i, T value) {
		if (i >= dims)
			return;
		data[i] = value;
	}

	public int getDimensions() {
		return dims;
	}

	public String getName(int i) {
		return dimensionNames[i];
	}

	public String getValueString(int i) {
		T x = data[i];
		if (x != null)
			return x.toString();
		else
			return "null";
	}

	/**
	 * Just for debugging -- refresh rate
	 */
//	private static final int NUM_FRAMES = 100;
//	private double[] tickList = new double[NUM_FRAMES];
//	private int tickI = 0;
//	private double deltaT = 0;
//
//	protected void calcFps() {
//		double newTick = System.currentTimeMillis();
////		System.out.println(newTick);
//		tickI++;
//		if (tickI >= NUM_FRAMES)
//			tickI = 0;
//
//		double prev = tickList[tickI];
//		tickList[tickI] = newTick;
//
//		deltaT = newTick - prev;
////		System.out.println (tickI + " : " + prev + " => " + newTick + " d=" + deltaT);
//	}
//
//	public double getFps() {
//
//		if (deltaT == 0)
//			return -1;
//		double fps = (1000d * NUM_FRAMES) / deltaT;
////		System.out.println("FPS:" + nf.format(fps));
//		return fps;
//	}

}
