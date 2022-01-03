package ca.hapke.util;

/**
 * @author Mr. Hapke
 *
 */
public class TextFormat {

	public static final int T = -1;
	public static final int M = 0;
	public static final int B = 1;
	public static final int L = -1;
	public static final int R = 1;

	public static final int NONE = 0;
	public static final int NO_ANTI_ALIASING = 1;
	public static final int FIRST_LINE_VISIBLE = 2;

	public static boolean isEnabled(int format, int property) {
		return (format & property) == property;
	}

}
