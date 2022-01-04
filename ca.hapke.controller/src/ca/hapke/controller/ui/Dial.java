package ca.hapke.controller.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Stroke;

import ca.hapke.util.AngleFormatter;
import ca.hapke.util.NumberUtil;
import ca.hapke.util.TextAlignment;
import ca.hapke.util.TextFormat;

/**
 * @author Mr. Hapke
 *
 */
public class Dial {
	private static final double DEFAULT_ANGLE = 3 * Math.PI / 4;
	private static final double PI_2 = Math.PI / 2;
	private static final double PI_4 = Math.PI / 4;
	private static final int SEGMENTS = 16;
	private static final double DOTTED_LINE_SPACING = 0.2;
	private static final double DOTTED_LINE_LENGTH = 0.03;
	private static final double DIAL_PCT = 1;
//	private static final double OUTER_PCT = 1.04;
//	private static final double INNER_PCT = 0.96;
	public static final int DIAL_RADIUS = 50;
	private static final int SCALE_MAX = 30;
	private String label;
	private Angle th;
	private AngleFormatter af;
	private double valueRadius;
	private DialRescale scaleUp = DialRescale.Normal;
	private int scaleStep;

	private double angleLimit;
	private double lowerValue;
	private double upperValue;
	private double minorValueDelta;
	private double valueRadiusAdjustment;

	private double lowerAngle;
	private double upperAngle;
	private double minorAngleDelta;
	private double angleLimitAdjustment;

	private Stroke pinStroke;
	private Stroke normalStroke;
	private Font font;

	public Dial(String label, Angle th, Font font) {
		this.label = label;
		this.th = th;
		this.font = font;
		af = new AngleFormatter(3, 1, 0.1);
		pinStroke = new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		normalStroke = new BasicStroke();

		valueRadius = 30;
		angleLimit = DEFAULT_ANGLE;
		rescale();
	}

	public void draw(Graphics2D g, Font f) {
		// value
		double theta = th.getTheta();
		double limitedValue = NumberUtil.limit(theta, lowerValue, upperValue);
		if (theta < lowerValue || theta > upperValue) {
			scaleUp = DialRescale.Begin;
			scaleStep = 0;
		}
		animate();

		g.setColor(Color.black);
		g.drawOval(10, 20, DIAL_RADIUS * 2, DIAL_RADIUS * 2);

		int xCen = 10 + DIAL_RADIUS;
		int yCen = 20 + DIAL_RADIUS;
		drawCenteredString(g, label, xCen, 0, f);

		int i = 0;
//		for (double x = 0; x <= upperAngle; x += minorAngleDelta) {
//			drawAxis(g, xCen, yCen, x, i % 4 == 0);
//			i++;
//		}
		double x = 0;
		double v = 0;
		while (x <= upperAngle) {
			drawAxis(g, xCen, yCen, x, v, i % 4 == 0);

			i++;
			x += minorAngleDelta;
			v += minorValueDelta;
		}
		i = 0;
//		for (x = 0; x >= lowerAngle; x -= minorAngleDelta) {
//			drawAxis(g, xCen, yCen, x, -99, i % 2 == 0);
//			i++;
//		}
		x = 0;
		v = 0;
		i = 0;
		while (x >= lowerAngle) {
			drawAxis(g, xCen, yCen, x, v, i % 4 == 0);

			i++;
			x -= minorAngleDelta;
			v -= minorValueDelta;
		}

		double pct = limitedValue / valueRadius;
		double rad = pct * angleLimit;

		int dx = (int) (DIAL_PCT * DIAL_RADIUS * Math.sin(rad));
		int dy = (int) (DIAL_PCT * DIAL_RADIUS * Math.cos(rad));

		g.setStroke(pinStroke);
		if (scaleUp == DialRescale.Run) {
			g.setColor(Color.red);
		} else {
			g.setColor(Color.blue);
		}
		g.drawLine(xCen, yCen, xCen - dx, yCen - dy);
		g.setStroke(normalStroke);

		drawCenteredString(g, af.format(theta), xCen, yCen + DIAL_RADIUS - f.getSize(), f);
		g.setColor(Color.black);
	}

	private void drawAxis(Graphics2D g, int xCen, int yCen, double theta, double value, boolean major) {
		double xFull = DIAL_RADIUS * Math.sin(theta);
		double yFull = DIAL_RADIUS * Math.cos(theta);

//		double innerPct = INNER_PCT;
//		double outerPct = OUTER_PCT;
		drawTick(g, xCen, yCen, xFull, yFull, 1 - DOTTED_LINE_LENGTH, 1 + DOTTED_LINE_LENGTH);

		if (major) {
			for (double pct = 1; pct >= 0; pct -= DOTTED_LINE_SPACING) {
				drawTick(g, xCen, yCen, xFull, yFull, pct - DOTTED_LINE_LENGTH, pct + DOTTED_LINE_LENGTH);
			}
//			g.drawString(af.format(theta), (int) (xFull * (1 + DOTTED_LINE_SPACING)),
//					(int) (yFull * (1 + DOTTED_LINE_SPACING)));
			int h = 0, v;
			if (Math.abs(theta) < PI_2) {
				v = TextFormat.T;
			} else {
				v = TextFormat.B;
			}
			if (theta < 0) {
				h = TextFormat.L;
			} else if (theta > 0) {
				h = TextFormat.R;
			}
			TextAlignment align = TextAlignment.find(v, h);
			TextAlignment.drawString(g, af.format(value), font, (int) (xCen - xFull), (int) (yCen - yFull), align);
		}
	}

	private void drawTick(Graphics2D g, int xCen, int yCen, double xFull, double yFull, double innerPct,
			double outerPct) {
		int xIn = (int) (innerPct * xFull);
		int yIn = (int) (innerPct * yFull);
		int xOut = (int) (outerPct * xFull);
		int yOut = (int) (outerPct * yFull);
		g.drawLine(xCen - xIn, yCen - yIn, xCen - xOut, yCen - yOut);
	}

	private void rescale() {
		valueRadius += valueRadiusAdjustment;

		upperValue = valueRadius;
		lowerValue = -upperValue;
		minorValueDelta = (upperValue - lowerValue) / SEGMENTS;

//		angleLimit = 2 * Math.PI / 3;
		angleLimit += angleLimitAdjustment;

		upperAngle = angleLimit;
		lowerAngle = -upperAngle;
		minorAngleDelta = (upperAngle - lowerAngle) / SEGMENTS;

	}

	private void animate() {
		switch (scaleUp) {
		case Begin:
			valueRadiusAdjustment = valueRadius / SCALE_MAX;
			angleLimitAdjustment = -PI_4 / SCALE_MAX;
			scaleUp = DialRescale.Run;
			// do fall down
		case Run:
			scaleStep++;
			rescale();
			if (scaleStep >= SCALE_MAX) {
				scaleUp = DialRescale.Normal;
				valueRadiusAdjustment = 0;
				angleLimitAdjustment = 0;
				angleLimit = DEFAULT_ANGLE;
			}
			break;
		case Normal:
			break;
		}
	}

	public static void drawCenteredString(Graphics2D g, String text, int x, int y, Font font) {
		// Get the FontMetrics
		FontMetrics metrics = g.getFontMetrics(font);
		// Determine the X coordinate for the text
		int x2 = x - (metrics.stringWidth(text) / 2);
		// Determine the Y coordinate for the text (note we add the ascent, as in java
		// 2d 0 is top of the screen)
		int y2 = y - (metrics.getHeight() / 2) + metrics.getAscent();

		// Draw the String
		g.drawString(text, x2, y2);

	}
}
