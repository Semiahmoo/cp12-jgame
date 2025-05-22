package jgame;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mr. Hapke
 */
public class JGRectangle {
	private List<RectangleData> datas = new ArrayList<>();
	private final RectangleData bounds = new RectangleData();

	public JGRectangle() {
		this(0, 0, 0, 0);
	}

	public JGRectangle(int x, int y, int width, int height) {
		setAll(x, y, width, height);
	}

	public void setAll(int x, int y, int width, int height) {
		datas.clear();
		RectangleData data = new RectangleData(x, y, width, height);
		datas.add(data);
		bounds.copyFrom(data);
	}

	/**
	 * Copy Constructor
	 */
	public JGRectangle(JGRectangle r) {
		copyFrom(r);
		updateBoundingBox();
	}

	public JGRectangle clone() {
		RectangleData[] clonedDatas = new RectangleData[datas.size()];
		for (int i = 0; i < datas.size(); i++) {
			RectangleData mine = datas.get(i);
			RectangleData cloned = new RectangleData(mine);
			clonedDatas[i] = cloned;
		}
		return new JGRectangle(clonedDatas);
	}

	public JGRectangle(RectangleData[] inputs) {

		for (RectangleData otherData : inputs)
			datas.add(new RectangleData(otherData));
		updateBoundingBox();
	}

	private void updateBoundingBox() {
		int xLeft = Integer.MAX_VALUE;
		int yTop = Integer.MAX_VALUE;

		int xRight = Integer.MIN_VALUE;
		int yBottom = Integer.MIN_VALUE;
		// boolean firstPass = true;

		for (RectangleData d : datas) {
			int dx = d.x;
			if (dx < xLeft) {
				xLeft = dx;
			}
			int dy = d.y;
			if (dy < yTop) {
				yTop = dy;
			}

			int dRight = dx + d.width;
			int dHeight = dy + d.height;
			if (dRight > xRight) {
				xRight = dRight;
			}
			if (dHeight > yBottom) {
				yBottom = dHeight;
			}
		}

		bounds.x = xLeft;
		bounds.y = yTop;
		bounds.width = xRight - xLeft;
		bounds.height = yBottom - yTop;
	}

	public void copyFrom(JGRectangle r) {
		copyFrom(r, 0, 0);
	}

	public void copyFrom(JGRectangle r, int x2, int y2) {
		datas.clear();
		for (RectangleData otherData : r.datas)
			datas.add(new RectangleData(otherData, x2, y2));
	}

	public int getX() {
		return bounds.x;
	}

	public void setX(int x) {
		this.datas.get(0).x = x;

		if (datas.size() > 1)
			debugMultiSet("X", x);
	}

	public int getY() {
		return bounds.y;
	}

	public void setY(int y) {
		this.datas.get(0).y = y;
		if (datas.size() > 1)
			debugMultiSet("Y", y);
	}

	public int getWidth() {
		return bounds.width;
	}

	public void setWidth(int width) {
		this.datas.get(0).width = width;
		if (datas.size() > 1)
			debugMultiSet("W", width);
	}

	public int getHeight() {
		return bounds.height;
	}

	public void setHeight(int height) {
		this.datas.get(0).height = height;
		if (datas.size() > 1)
			debugMultiSet("H", height);

	}

	public List<RectangleData> getDatas() {
		return datas;
	}

	public RectangleData getIntersecting(JGRectangle other) {
		return datas.stream().flatMap(r -> other.datas.stream().filter((RectangleData r2) -> r.intersects(r2)))
				.findFirst().orElse(null);
	}

	public boolean intersects(JGRectangle other) {
		return getIntersecting(other) != null;
	}

	public void debugMultiSet(String var, int value) {
		System.err.println("TELL MR. HAPKE RIGHT NOW! SET-" + var + " ON MULTI: " + value);

		StackTraceElement[] stackTraceElements = Thread.getAllStackTraces().get(Thread.currentThread());
		for (StackTraceElement e : stackTraceElements)
			System.out.println(e + "Line: " + e.getLineNumber());
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("JGRectangle [(");
		builder.append(getX());
		builder.append(", ");
		builder.append(getY());
		builder.append(") + <");
		builder.append(getWidth());
		builder.append(", ");
		builder.append(getHeight());
		builder.append(">]");
		return builder.toString();
	}
}
