package ca.hapke.controller.serial;

/**
 * @author Mr. Hapke
 */
public class StartEndWrap {
	public final int start, end, escCount;
	public final boolean wrap;

	public StartEndWrap(int start, int end, boolean wrap, int escCount) {
		this.start = start;
		this.end = end;
		this.wrap = wrap;
		this.escCount = escCount;
	}

}
