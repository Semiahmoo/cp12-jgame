package ca.hapke.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Nathan Hapke
 */
public abstract class CollectionUtil {

	public static <T> T getRandom(T[] l) {
		int i = CollectionUtil.getRandomIndex(l);
		if (i == -1)
			return null;
		return l[i];
	}

	public static <T> T getRandom(List<T> l) {
		if (l == null || l.size() == 0)
			return null;

		int i = (int) (Math.random() * l.size());
		return l.get(i);
	}

	public static <T> int getRandomIndex(T[] l) {
		if (l == null)
			return -1;
		int length = l.length;
		return CollectionUtil.getRandomIndex(length);
	}

	public static <T> int getRandomIndex(Collection<T> l) {
		if (l == null)
			return -1;
		int length = l.size();
		return CollectionUtil.getRandomIndex(length);
	}

	public static int getRandomIndex(int length) {
		if (length == 0)
			return -1;
		return (int) (Math.random() * length);
	}

	private static final String PERSON_REGEX = "\\([A-Za-z]+\\)";
	private static final Pattern PERSON = Pattern.compile(PERSON_REGEX);

	public static String search(String searchTerm, List<String> hypes) {
		searchTerm = searchTerm.toLowerCase();
		List<String> possible = new ArrayList<>();
		List<String> possiblePunchlines = null;
		boolean punchline = searchTerm.startsWith("(") && searchTerm.endsWith(")");

		if (punchline) {
			possiblePunchlines = new ArrayList<>();
		}

		for (String s : hypes) {
			String x = s.toLowerCase();
			if (x.contains(searchTerm)) {
				possible.add(s);

				if (punchline) {
					try {
						Matcher m = PERSON.matcher(x);
						String last = null;
						while (m.find()) {
							int groupCount = m.groupCount();
							last = m.group(groupCount);
						}
						if (last != null && last.equals(searchTerm))
							possiblePunchlines.add(s);
					} catch (IllegalStateException e) {
					}
				}
			}
		}

		if (punchline && possiblePunchlines.size() > 0) {
			return getRandom(possiblePunchlines);
		}
		return getRandom(possible);
	}
}
