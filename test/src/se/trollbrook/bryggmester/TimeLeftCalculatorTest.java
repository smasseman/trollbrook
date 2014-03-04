package se.trollbrook.bryggmester;

import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import se.trollbrook.util.Time;

/**
 * @author jorgen.smas@entercash.com
 */
public class TimeLeftCalculatorTest {

	@Test
	public void testWarm() {
		Temperature from = new Temperature(20);
		Temperature to = new Temperature(50);
		Time expected = new Time(30, TimeUnit.MINUTES);
		checkWarm(from, to, expected);
	}

	@Test
	public void testCool() {
		// Cool from 100 to 20 takes about 20 minutes.
		// That is something like 80/20=4 C/minute
		checkCool(new Temperature(100), new Temperature(20), new Time(20, TimeUnit.MINUTES));
	}

	private void checkWarm(Temperature from, Temperature to, Time expected) {
		Time calculated = TimeLeftCalculator.calculateTimeToWarm(from, to);
		Assert.assertEquals(expected, calculated);
	}

	private void checkCool(Temperature from, Temperature to, Time expected) {
		Time calculated = TimeLeftCalculator.calculateTimeToCool(from, to);
		Assert.assertEquals(expected, calculated);
	}
}
