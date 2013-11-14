package se.trollbrook.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author jorgen.smas@entercash.com
 */
public class TimeUnitTest {

	@Test
	public void testFormat() {
		Assert.assertEquals("0ms", TimeUnit.format(0));
		Assert.assertEquals("234ms", TimeUnit.format(234));
		Assert.assertEquals("1s", TimeUnit.format(1000));
		Assert.assertEquals("50s", TimeUnit.format(50000));
		Assert.assertEquals("3m", TimeUnit.format(180000));
	}

	@Test
	public void testParse() {
		Assert.assertEquals(400, TimeUnit.parse("400ms"));
		Assert.assertEquals(13000, TimeUnit.parse("13s"));
		Assert.assertEquals(3 * 60 * 1000, TimeUnit.parse("3m"));
	}

	@Test
	public void testFormatParse() {
		String s = TimeUnit.format(TimeUnit.MINUTE.asMillis(70));
		Assert.assertEquals(TimeUnit.MINUTE.asMillis(70), TimeUnit.parse(s));

	}
}
