package se.trollbrook.bryggmester.alarm.testcases;

import junit.framework.Assert;

import org.junit.Test;

import se.trollbrook.bryggmester.alarm.ActiveAlarm;
import se.trollbrook.bryggmester.alarm.Alarm;
import se.trollbrook.bryggmester.alarm.Alarm.Type;
import se.trollbrook.bryggmester.alarm.Alarms;

/**
 * @author jorgen.smas@entercash.com
 */
public class AlarmsTest {

	@Test
	public void test() throws InterruptedException {
		Alarms as = new Alarms();
		Alarm a = new Alarm("foo", Type.WAIT_FOR_USER_INPUT);
		ActiveAlarm aa = as.active(a);
		ack(aa, as, 2000);
		long start = System.currentTimeMillis();
		as.waitForAck(aa, 5000L);
		long stop = System.currentTimeMillis();
		Assert.assertEquals(stop - start, 2000, 500);
	}

	private void ack(final ActiveAlarm aa, final Alarms as, final long delay) {
		new Thread() {
			@Override
			public void run() {
				try {
					sleep(delay);
					as.deactive(aa.getId());
				} catch (InterruptedException e) {
				}
			}
		}.start();
	}
}
