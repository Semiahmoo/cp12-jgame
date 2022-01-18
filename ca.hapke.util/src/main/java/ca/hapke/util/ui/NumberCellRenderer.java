package ca.hapke.util.ui;

import java.text.NumberFormat;

import javax.swing.table.DefaultTableCellRenderer;

/**
 * @author Nathan Hapke
 */
public class NumberCellRenderer extends DefaultTableCellRenderer {
	private static final long serialVersionUID = -2469605997570610314L;
	private NumberFormat nf;
	private static final Float FLT_0 = Float.valueOf(0f);
	private static final Integer INT_0 = Integer.valueOf(0);
	private static final Long LNG_0 = Long.valueOf(0l);

	public NumberCellRenderer() {
		nf = NumberFormat.getInstance();
		nf.setMinimumFractionDigits(0);
		nf.setMaximumFractionDigits(2);
	}

	@Override
	public void setValue(Object value) {
		if (value != null && value instanceof Float && !FLT_0.equals(value)) {
			Float d = (Float) value;
			setText(nf.format(d));
		} else if (value != null && value instanceof Integer && !INT_0.equals(value)) {
			Integer d = (Integer) value;
			setText(nf.format(d));
		} else if (value != null && value instanceof Long && !LNG_0.equals(value)) {
			Long d = (Long) value;
			setText(nf.format(d));
		} else {
			setText("");
		}
	}
}
