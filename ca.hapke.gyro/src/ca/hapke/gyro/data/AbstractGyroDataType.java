package ca.hapke.gyro.data;

import javax.vecmath.Point3d;

import ca.hapke.util.AngleFormatter;
import ca.hapke.util.ByteUtil;

/**
 * @author Mr. Hapke
 */
public abstract class AbstractGyroDataType extends DataType<Point3d> {

	private static final boolean DEBUG = false;
	protected AngleFormatter af;

	public AbstractGyroDataType(InputType type, String... dimensionNames) {
		super(type, dimensionNames);
		af = new AngleFormatter(3, 3, 1, 1, 0.02);
	}

	@Override
	protected Point3d[] createData(int dims) {
		return new Point3d[dims];
	}

	/**
	 * FIXME worried about []... vs [][] calling incorrect
	 * 
	 * @param values
	 */
	public void update(double[][] values) {
		for (int i = 0; i < values.length && i < data.length; i++) {
			Point3d axis = data[i];
			if (axis == null)
				axis = data[i] = new Point3d();

			axis.x = values[i][0];
			axis.y = values[i][1];
			axis.z = values[i][2];
			if (DEBUG && af != null) {
				System.out.println(getValueString(i));
			}
		}

//		calcFps();
		if (DEBUG) {
			System.out.println();
		}
	}

	public void update(double[] values) {
		for (int set = 0; set < data.length; set++) {
			int x = set * 3;
			int y = set * 3 + 1;
			int z = set * 3 + 2;

			if (z >= values.length)
				return;

			Point3d axis = data[set];
			axis.x = values[x];
			axis.y = values[y];
			axis.z = values[z];

			if (DEBUG && af != null) {
				System.out.println(getValueString(set));
			}
		}

//		calcFps();
		if (DEBUG) {
			System.out.println();
		}
	}

	@Override
	public byte[] getBytes() {
		int dims = data.length;
		double[] input = new double[3 * dims];
		for (int i = 0; i < dims; i++) {
			Point3d axis = data[i];
			input[3 * i] = axis.x;
			input[3 * i + 1] = axis.y;
			input[3 * i + 2] = axis.z;
		}
		return ByteUtil.doublesToBytes(input);
	}

	@Override
	public void update(byte[] vals) {
		double[] doubles = ByteUtil.bytesToDouble(vals);
		update(doubles);
	}

	@Override
	public String getValueString(int dimension) {
		Point3d axis = data[dimension];
		return dimension + ": [" + af.format(axis.x) + "," + af.format(axis.y) + "," + af.format(axis.z) + "]";
	}

	@Override
	protected void setDefaults() {
		double[][] defaults = new double[dims][3];
		for (int i = 0; i < defaults.length; i++) {
			defaults[i][0] = 0;
			defaults[i][1] = 0;
			defaults[i][2] = 1;
		}
		update(defaults);
	}

}