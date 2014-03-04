package se.trollbrook.bryggmester;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import se.trollbrook.util.Time;

/**
 * @author jorgen.smas@entercash.com
 */
public class TimeLeftCalculator {

	public static Time calculateTimeToWarm(Temperature from, Temperature to) {
		if (from == null || to == null)
			return null;
		if (from.greaterThen(to))
			return Time.ZERO;
		BigDecimal diff = to.getValue().subtract(from.getValue());
		return new Time(diff.longValue(), TimeUnit.MINUTES);
	}

	public static Time calculateTimeToCool(Temperature from, Temperature to) {
		if (from == null || to == null)
			return null;
		if (to.greaterThen(from))
			return Time.ZERO;
		BigDecimal diff = from.getValue().subtract(to.getValue());
		return new Time(diff.longValue() / 4, TimeUnit.MINUTES);
	}
}
