package se.trollbrook.bryggmester.alarm;

import org.junit.Assert;
import org.junit.Test;

import se.trollbrook.bryggmester.Relay;
import se.trollbrook.bryggmester.alarm.ActiveAlarm;
import se.trollbrook.bryggmester.alarm.Alarm;
import se.trollbrook.bryggmester.alarm.Alarm.Type;
import se.trollbrook.bryggmester.alarm.Alarms;
import se.trollbrook.bryggmester.alarm.Alarms.AlarmListener;
import se.trollbrook.bryggmester.alarm.OneTimeAlarm;
import se.trollbrook.bryggmester.alarm.SoundPlayer;

/**
 * @author jorgen.smas@entercash.com
 */
public class AlarmsTest {

	public class MyListener implements AlarmListener {

		private ActiveAlarm activeAlarm;
		protected long delay;
		private Alarms alarms;

		MyListener(Alarms alarms) {
			this.alarms = alarms;
		}

		@Override
		public void eventNotification(ActiveAlarm event) {
			this.activeAlarm = event;
			new Thread() {
				@Override
				public void run() {
					try {
						Thread.sleep(delay);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					alarms.deactive(activeAlarm.getId());
				}
			}.start();
		}

		public void setAccDelay(int i) {
			this.delay = i;
		}
	}

	@Test
	public void test() throws InterruptedException {
		Alarms as = new Alarms();
		MyListener listener = new MyListener(as);
		as.addListener(listener);
		SoundPlayer player = new SoundPlayer();
		player.setPin(new Relay("Beep"));
		as.setPlayer(player);
		Alarm a = new Alarm("foo", Type.WAIT_FOR_USER_INPUT, new OneTimeAlarm());
		listener.setAccDelay(2000);
		as.fireAlarm(a);
		long start = System.currentTimeMillis();
		Thread.sleep(2000);
		long stop = System.currentTimeMillis();
		Assert.assertEquals(stop - start, 2000, 500);
	}

}
