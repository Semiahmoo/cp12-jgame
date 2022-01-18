package ca.hapke.util;

import static ca.hapke.util.TextFormat.B;
import static ca.hapke.util.TextFormat.L;
import static ca.hapke.util.TextFormat.M;
import static ca.hapke.util.TextFormat.R;
import static ca.hapke.util.TextFormat.T;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;

/**
 * @author Mr. Hapke
 *
 */
public enum TextAlignment {

	TOP_LEFT(T, L), TOP_MIDDLE(T, M), TOP_RIGHT(T, R), MIDDLE_LEFT(M, L), MIDDLE_MIDDLE(M, M), MIDDLE_RIGHT(M, R),
	BOTTOM_LEFT(B, L), BOTTOM_MIDDLE(B, M), BOTTOM_RIGHT(B, R);

	private final int v, h;

	private TextAlignment(int v, int h) {
		this.v = v;
		this.h = h;
	}

	public static TextAlignment find(int vertical, int horizontal) {
		for (TextAlignment ta : values())
			if (ta.v == vertical && ta.h == horizontal)
				return ta;
		return MIDDLE_MIDDLE;
	}

	public boolean isMiddle() {
		switch (this) {
		case MIDDLE_MIDDLE:
		case MIDDLE_LEFT:
		case MIDDLE_RIGHT:
			return true;
		default:
			return false;
		}
	}

	public boolean isBottom() {
		switch (this) {
		case BOTTOM_MIDDLE:
		case BOTTOM_LEFT:
		case BOTTOM_RIGHT:
			return true;
		default:
			return false;
		}
	}

	private boolean isCenter() {
		switch (this) {
		case BOTTOM_MIDDLE:
		case MIDDLE_MIDDLE:
		case TOP_MIDDLE:
			return true;
		default:
			return false;
		}
	}

	boolean isRight() {
		switch (this) {
		case BOTTOM_RIGHT:
		case MIDDLE_RIGHT:
		case TOP_RIGHT:
			return true;
		default:
			return false;
		}
	}

	/**
	 * Draws a string inside of the bounds, and does word wrapping
	 */
	public static Rectangle drawStringInBounds(Graphics2D g2D, String text, Font font, Color color, Rectangle bounds,
			TextAlignment align, int format) {
		if (g2D == null)
			throw new NullPointerException("The graphics handle cannot be null.");
		if (text == null)
			throw new NullPointerException("The text cannot be null.");
		if (font == null)
			throw new NullPointerException("The font cannot be null.");
		if (color == null)
			throw new NullPointerException("The text color cannot be null.");
		if (bounds == null)
			throw new NullPointerException("The text bounds cannot be null.");
		if (align == null)
			throw new NullPointerException("The text alignment cannot be null.");
		if (text.length() == 0)
			return new Rectangle(bounds.x, bounds.y, 0, 0);

		AttributedString attributedString = new AttributedString(text);
		attributedString.addAttribute(TextAttribute.FOREGROUND, color);
		attributedString.addAttribute(TextAttribute.FONT, font);

		AttributedCharacterIterator attributedCharIterator = attributedString.getIterator();

		FontRenderContext fontContext = new FontRenderContext(null,
				!TextFormat.isEnabled(format, TextFormat.NO_ANTI_ALIASING), false);
		LineBreakMeasurer lineMeasurer = new LineBreakMeasurer(attributedCharIterator, fontContext);

		Point targetLocation = new Point(bounds.x, bounds.y);
		int nextOffset = 0;

		if (align.isMiddle() || align.isBottom()) {
			if (align.isMiddle())
				targetLocation.y = bounds.y + (bounds.height / 2);
			if (align.isBottom())
				targetLocation.y = bounds.y + bounds.height;

			while (lineMeasurer.getPosition() < text.length()) {
				nextOffset = lineMeasurer.nextOffset(bounds.width);
				nextOffset = nextTextIndex(nextOffset, lineMeasurer.getPosition(), text);

				TextLayout textLayout = lineMeasurer.nextLayout(bounds.width, nextOffset, false);

				if (align.isMiddle())
					targetLocation.y -= (textLayout.getAscent() + textLayout.getLeading() + textLayout.getDescent())
							/ 2;
				if (align.isBottom())
					targetLocation.y -= (textLayout.getAscent() + textLayout.getLeading() + textLayout.getDescent());
			}

			if (TextFormat.isEnabled(format, TextFormat.FIRST_LINE_VISIBLE))
				targetLocation.y = Math.max(0, targetLocation.y);

			lineMeasurer.setPosition(0);

		}

		if (align.isRight() || align.isCenter())
			targetLocation.x = bounds.x + bounds.width;

		Rectangle consumedBounds = new Rectangle(targetLocation.x, targetLocation.y, 0, 0);

		while (lineMeasurer.getPosition() < text.length()) {
			nextOffset = lineMeasurer.nextOffset(bounds.width);
			nextOffset = nextTextIndex(nextOffset, lineMeasurer.getPosition(), text);

			TextLayout textLayout = lineMeasurer.nextLayout(bounds.width, nextOffset, false);
			Rectangle2D textBounds = textLayout.getBounds();

			targetLocation.y += textLayout.getAscent();
			consumedBounds.width = Math.max(consumedBounds.width, (int) textBounds.getWidth());

			switch (align) {
			case TOP_LEFT:
			case MIDDLE_LEFT:
			case BOTTOM_LEFT:
				textLayout.draw(g2D, targetLocation.x, targetLocation.y);
				break;

			case TOP_MIDDLE:
			case MIDDLE_MIDDLE:
			case BOTTOM_MIDDLE:
				targetLocation.x = bounds.x + (bounds.width / 2) - (int) (textBounds.getWidth() / 2);
				consumedBounds.x = Math.min(consumedBounds.x, targetLocation.x);
				textLayout.draw(g2D, targetLocation.x, targetLocation.y);
				break;

			case TOP_RIGHT:
			case MIDDLE_RIGHT:
			case BOTTOM_RIGHT:
				targetLocation.x = bounds.x + bounds.width - (int) textBounds.getWidth();
				textLayout.draw(g2D, targetLocation.x, targetLocation.y);
				consumedBounds.x = Math.min(consumedBounds.x, targetLocation.x);
				break;
			}

			targetLocation.y += textLayout.getLeading() + textLayout.getDescent();
		}

		consumedBounds.height = targetLocation.y - consumedBounds.y;

		return consumedBounds;
	}

	private static int nextTextIndex(int nextOffset, int measurerPosition, String text) {
		for (int i = measurerPosition + 1; i < nextOffset; ++i) {
			if (text.charAt(i) == '\n')
				return i;
		}

		return nextOffset;
	}

	public static void drawString(Graphics2D g, String s, Font f, int xIn, int yIn, TextAlignment align) {
		FontMetrics metrics = g.getFontMetrics();
		int width = metrics.stringWidth(s);
		int height = f.getSize();

		int xOut = xIn;
		int yOut = yIn;
		if (align.isCenter()) {
			xOut -= width / 2;
		} else if (align.isRight()) {
			xOut -= width;
		}

		if (align.isMiddle()) {
			yOut -= height / 2;
		} else if (align.isBottom()) {
			yOut -= height;
		}
		g.drawString(s, xOut, yOut);
	}
}
