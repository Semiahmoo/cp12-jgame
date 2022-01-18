package ca.hapke.util;

import static java.util.Arrays.asList;

import java.util.Iterator;

/**
 * @author Nathan Hapke
 */
public abstract class StringUtil {

	/**
	 * Stolen from jdk.internal.joptsimple.internal.Strings
	 */
	public static String join(Iterable<String> pieces, String separator) {
		StringBuilder buffer = new StringBuilder();
	
		for (Iterator<String> iter = pieces.iterator(); iter.hasNext();) {
			buffer.append(iter.next());
	
			if (iter.hasNext())
				buffer.append(separator);
		}
	
		return buffer.toString();
	}

	/**
	 * Stolen from jdk.internal.joptsimple.internal.Strings
	 */
	public static String join(String[] pieces, String separator) {
		return join(asList(pieces), separator);
	}

	/**
	 * Stolen from jdk.internal.joptsimple.internal.Strings
	 */
	public static String repeat(char ch, int count) {
		StringBuilder buffer = new StringBuilder();
	
		for (int i = 0; i < count; ++i)
			buffer.append(ch);
	
		return buffer.toString();
	}

	public static String[] suffixes = new String[] { "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th" };

	public static String ordinal(int i) {
		switch (i % 100) {
		case 11:
		case 12:
		case 13:
			return i + "th";
		default:
			return i + suffixes[i % 10];
	
		}
	}

	public static boolean endsWithPunctuation(String excl) {
		char last = excl.charAt(excl.length() - 1);
		return last == '.' || last == '!' || last == '?';
	}

	public static boolean matchOne(String target, String... accepts) {
		for (int i = 0; i < accepts.length; i++) {
			String s = accepts[i];
			if (s != null && s.length() > 0 && s.equalsIgnoreCase(target))
				return true;
		}
		return false;
	}

}
