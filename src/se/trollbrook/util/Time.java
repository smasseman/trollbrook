package se.trollbrook.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author jorgen.smas@entercash.com
 */
public class Time implements Comparable<Time> {

	private static Map<String, TimeUnit> map = new LinkedHashMap<>();
	static {
		map.put("h", TimeUnit.HOURS);
		map.put("m", TimeUnit.MINUTES);
		map.put("s", TimeUnit.SECONDS);
		map.put("ms", TimeUnit.MILLISECONDS);
	}

	public static final Time ZERO = fromMillis(0);

	private final long millis;

	public Time(long t, TimeUnit unit) {
		this.millis = unit.toMillis(t);
	}

	public Time subtract(Time time) {
		return new Time(this.millis - time.millis, TimeUnit.MILLISECONDS);
	}

	public Time add(Time time) {
		return new Time(this.millis + time.millis, TimeUnit.MILLISECONDS);
	}

	public long toMillis() {
		return millis;
	}

	public static Time fromMillis(long t) {
		return new Time(t, TimeUnit.MILLISECONDS);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (millis ^ (millis >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Time other = (Time) obj;
		if (millis != other.millis)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return format(millis);
	}

	public static String format(long m) {
		if (m < 0)
			return String.valueOf(m);
		if (m == 0)
			return "0ms";
		StringBuilder s = new StringBuilder();
		for (Map.Entry<String, TimeUnit> e : map.entrySet()) {
			long millis = e.getValue().toMillis(1);
			if (m >= millis) {
				int i = (int) (m / millis);
				m = m - i * millis;
				if (s.length() > 0)
					s.append(" ");
				s.append(i);
				s.append(e.getKey());
			}
		}
		return s.toString();
	}

	public static Time parse(String string) {
		long sum = 0;
		for (String s : string.split(" ")) {
			sum += parseSlot(s);
		}
		return Time.fromMillis(sum);
	}

	private static long parseSlot(String string) {
		Pattern p = Pattern.compile("[0-9]+([a-z]+)");
		Matcher m = p.matcher(string);
		if (!m.matches())
			throw new IllegalArgumentException(string + " can not be parsed.");
		String units = m.group(1);
		for (Map.Entry<String, TimeUnit> e : map.entrySet()) {
			if (units.equals(e.getKey())) {
				long value = new Long(string.substring(0, string.length() - e.getKey().length()));
				return e.getValue().toMillis(value);
			}
		}
		throw new IllegalArgumentException("Invalid time unit '" + string + "'");
	}

	@Override
	public int compareTo(Time other) {
		return Long.compare(this.millis, other.millis);
	}

	public Time removeSeconds() {
		return fromMillis(60000 * (int) (this.millis / 60000));
	}

	public Time removeMillisSeconds() {
		return fromMillis(1000 * (int) (this.millis / 1000));
	}

	public int toMinutes() {
		return (int) (this.millis / 60000);
	}
}
