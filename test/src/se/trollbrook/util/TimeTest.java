package se.trollbrook.util;

import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author jorgen.smas@entercash.com
 */
public class TimeTest {

	@Test
	public void testRemoveSeconds() {
		Time t = Time.parse("1h 2m 3s 4ms");
		Assert.assertEquals("1h 2m 3s 4ms", t.toString());
		Assert.assertEquals("1h 2m", t.removeSeconds().toString());
	}

	@Test
	public void testFormat() {
		Assert.assertEquals("0ms", Time.format(0));
		Assert.assertEquals("234ms", Time.format(234));
		Assert.assertEquals("1s", Time.format(1000));
		Assert.assertEquals("50s", Time.format(50000));
		Assert.assertEquals("3m", Time.format(180000));
	}

	@Test
	public void testParse() {
		Assert.assertEquals(400, Time.parse("400ms").toMillis());
		Assert.assertEquals(13000, Time.parse("13s").toMillis());
		Assert.assertEquals(3 * 60 * 1000, Time.parse("3m").toMillis());
	}

	@Test
	public void testFormatParse() {
		String s = Time.format(TimeUnit.MINUTES.toMillis(70));
		Assert.assertEquals(TimeUnit.MINUTES.toMillis(70), Time.parse(s).toMillis());

	}
}
