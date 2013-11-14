package se.trollbrook.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author jorgen.smas@entercash.com
 */
public enum TimeUnit {

	HOUR(1000L * 60L * 60L, "h"),

	MINUTE(1000L * 60L, "m"),

	SECOND(1000L, "s"),

	MILLIS(1L, "ms");

	private long millis;
	private String unitChars;

	TimeUnit(long millis, String unitChars) {
		this.millis = millis;
		this.unitChars = unitChars;
	}

	public long asMillis(long x) {
		return millis * x;
	}

	public static long parse(String string) {
		long sum = 0;
		for (String s : string.split(" ")) {
			sum += parseSlot(s);
		}
		return sum;
	}

	private static long parseSlot(String string) {
		Pattern p = Pattern.compile("[0-9]+([a-z]+)");
		Matcher m = p.matcher(string);
		if (!m.matches())
			throw new IllegalArgumentException(string + " can not be parsed.");
		String units = m.group(1);
		for (TimeUnit u : values()) {
			if (units.equals(u.unitChars)) {
				long value = new Long(string.substring(0, string.length()
						- u.unitChars.length()));
				return u.asMillis(value);
			}
		}
		throw new IllegalArgumentException("Invalid time unit '" + string + "'");
	}

	public static String format(long m) {
		if (m < 0)
			return String.valueOf(m);
		if (m == 0)
			return "0ms";
		StringBuilder s = new StringBuilder();
		for (TimeUnit u : values()) {
			if (m >= u.millis) {
				int i = (int) (m / u.millis);
				m = m - i * u.millis;
				if (s.length() > 0)
					s.append(" ");
				s.append(i);
				s.append(u.unitChars);
			}
		}
		return s.toString();
	}
}
