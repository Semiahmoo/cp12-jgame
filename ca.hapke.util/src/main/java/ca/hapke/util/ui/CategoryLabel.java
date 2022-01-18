package ca.hapke.util.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Stroke;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 * From: https://benohead.com/blog/2014/10/15/java-vertical-label-in-swing/
 * 
 * @author Nathan Hapke
 */
public class CategoryLabel extends JLabel {

	private static final int MARGIN = 0;
	private static final int THICKNESS = 2;

	private static final long serialVersionUID = 7594483564128991522L;

	public final static int ROTATE_RIGHT = 1;
	public final static int DONT_ROTATE = 0;
	public final static int ROTATE_LEFT = -1;
	private int rotation = DONT_ROTATE;

	private boolean painting = false;

	private Stroke s;
	private Color barColour;

	public CategoryLabel(String text) {
		this(text, null, SwingConstants.CENTER, Color.blue);
	}

	public CategoryLabel(String text, Color barColour) {
		this(text, null, SwingConstants.CENTER, barColour);
	}

	public CategoryLabel(String text, Icon icon, int horizontalAlignment, Color barColour) {
		super(text, icon, horizontalAlignment);
		setRotation(ROTATE_LEFT);
		this.barColour = barColour;
		s = new BasicStroke(THICKNESS);
	}

	public int getRotation() {
		return rotation;
	}

	public void setRotation(int rotation) {
		this.rotation = rotation;
	}

	public boolean isRotated() {
		return rotation != DONT_ROTATE;
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;

		double rot = Math.toRadians(90 * rotation);
		int w = this.getWidth();
		int h = this.getHeight();

		g2d.setColor(barColour);
		g2d.setStroke(s);
		int x1 = w - (THICKNESS + MARGIN);
		g2d.drawLine(x1, 0, x1, h);

		int right = -w;
		int left = -h;

		if (isRotated())
			g2d.rotate(rot);
		if (rotation == ROTATE_RIGHT)
			g2d.translate(0, right);
		else if (rotation == ROTATE_LEFT)
			g2d.translate(left, 0);

		painting = true;

		super.paintComponent(g2d);

		painting = false;
		if (isRotated())
			g2d.rotate(-rot);
		if (rotation == ROTATE_RIGHT)
			g2d.translate(right, 0);
		else if (rotation == ROTATE_LEFT)
			g2d.translate(0, left);

	}

	@Override
	public Insets getInsets(Insets insets) {
		insets = super.getInsets(insets);
		if (painting) {
			if (rotation == ROTATE_LEFT) {
				int temp = insets.bottom;
				insets.bottom = insets.left;
				insets.left = insets.top;
				insets.top = insets.right;
				insets.right = temp;
			} else if (rotation == ROTATE_RIGHT) {
				int temp = insets.bottom;
				insets.bottom = insets.right;
				insets.right = insets.top;
				insets.top = insets.left;
				insets.left = temp;
			}
		}
		return insets;
	}

	@Override
	public Insets getInsets() {
		Insets insets = super.getInsets();
		if (painting) {
			if (rotation == ROTATE_LEFT) {
				int temp = insets.bottom;
				insets.bottom = insets.left;
				insets.left = insets.top;
				insets.top = insets.right;
				insets.right = temp;
			} else if (rotation == ROTATE_RIGHT) {
				int temp = insets.bottom;
				insets.bottom = insets.right;
				insets.right = insets.top;
				insets.top = insets.left;
				insets.left = temp;
			}
		}
		return insets;
	}

	@Override
	public int getWidth() {
		if ((painting) && (isRotated()))
			return super.getHeight();
		return super.getWidth();
	}

	@Override
	public int getHeight() {
		if ((painting) && (isRotated()))
			return super.getWidth();
		return super.getHeight();
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension d = super.getPreferredSize();
		if (isRotated()) {
			int width = d.width;
			d.width = d.height;
			d.height = width;
		}
		return d;
	}

	@Override
	public Dimension getMinimumSize() {
		Dimension d = super.getMinimumSize();
		if (isRotated()) {
			int width = d.width;
			d.width = d.height;
			d.height = width;
		}
		return d;
	}

	@Override
	public Dimension getMaximumSize() {
		Dimension d = super.getMaximumSize();
		if (isRotated()) {
			int width = d.width;
			d.width = d.height + 10;
			d.height = width + 10;
		}
		return d;
	}
}
