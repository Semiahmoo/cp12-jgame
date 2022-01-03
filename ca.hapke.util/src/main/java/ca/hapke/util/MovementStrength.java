package ca.hapke.util;

import java.awt.geom.Point2D;

/**
 * @author Mr. Hapke
 */
public class MovementStrength {
	public double x, y, xPct, yPct;
	private final double xBase, yBase, d, e;
	private double scale;

	// public MovementStrength(double x, double y, double xPct, double yPct) {
	// this.x = x;
	// this.y = y;
	// this.xPct = xPct;
	// this.yPct = yPct;
	// }

	/**
	 * 
	 * @param d
	 *            radius of deadzone (must be positive)
	 * @param e
	 *            extreme: distance from deadzone to be considered full strength
	 * @param s 
	 * @return s strength in [0,e]
	 */
	public MovementStrength(double xBase, double yBase, double xRaw, double yRaw, double d, double e, double s) {
		this.xBase  = xBase;
		this.yBase = yBase;
		this.d = d;
		this.e= e;
		update(xRaw, yRaw);
		setScale(s);
	}

	public MovementStrength(double xBase, double yBase, double xRaw, double yRaw, double d, double e) {
		this(xBase,yBase,xRaw,yRaw,d,e,1);
	}

	/**
	 * @param xRaw
	 * @param yRaw
	 */
	public void update(double xRaw, double yRaw) {
		double dx = xRaw - xBase;
		double dy = yRaw - yBase;
		double hyp = Math.hypot(dx, dy);

		if (hyp < d) {
			// in dead-zone
			// return 0;
			x = 0;
			y = 0;
			xPct = 0;
			yPct = 0;
			return;
		}
		Point2D.Double pcts = NumberUtil.getPercentages(xBase, yBase, xRaw, yRaw);
		double x1 = d * pcts.x;
		double y1 = d * pcts.y;
//		double w;
//		double h;
		if (hyp > d + e) {

			double x2 = (d + e) * pcts.x;
			double y2 = (d + e) * pcts.y;

			x = x2 - x1;
			y = y2 - y1;
		} else {
			x = dx - x1;
			y = dy - y1;
		}

		xPct = x / e;
		yPct = y / e;
	}

	public void setScale(double s) {
		this.scale = s;		
	}

	public double getScaledX() {
		return scale * x;
	}
	public double getScaledY() {
		return scale * y;
	}
}
