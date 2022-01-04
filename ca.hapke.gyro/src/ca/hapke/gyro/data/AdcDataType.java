package ca.hapke.gyro.data;

import ca.hapke.util.ByteUtil;

/**
 * @author Mr. Hapke
 *
 */
public class AdcDataType extends DataType<Integer> {

	public AdcDataType() {
		super(InputType.ADC, "x", "y", "z");
	}

	@Override
	protected Integer[] createData(int dims) {
		return new Integer[dims];
	}

	@Override
	public byte[] getBytes() {
		return ByteUtil.intsToBytes(data);
	}

//	public void update(int... inputs) {
//		for (int i = 0; i < inputs.length && i < data.length; i++) {
//			data[i] = inputs[i];
//		}
//	}

	@Override
	public void update(byte[] vals) {
		int[] d2 = ByteUtil.bytesToInt(vals);

		for (int i = 0; i < d2.length && i < data.length; i++) {
			data[i] = d2[i];
		}
	}

	@Override
	protected void setDefaults() {
		for (int i = 0; i < data.length; i++) {
			data[i] = new Integer(0);
		}
	}

}
