package se.trollbrook.bryggmester.alarm;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Assert;
import org.junit.Test;

import se.trollbrook.bryggmester.Relay;
import se.trollbrook.bryggmester.Relay.RelayListener;
import se.trollbrook.bryggmester.Relay.RelayState;
import se.trollbrook.bryggmester.alarm.Beep;
import se.trollbrook.bryggmester.alarm.SoundImpl;
import se.trollbrook.bryggmester.alarm.SoundPlayer;

/**
 * @author jorgen.smas@entercash.com
 */
public class SoundPlayerTest {

	@Test
	public void test() throws InterruptedException {
		SoundPlayer p = new SoundPlayer();
		Relay buzzerPin = new Relay("BuzzerPin");
		final AtomicLong lastBuzzerPinStateChangeTimestamp = new AtomicLong();
		final List<RelayState> events = new LinkedList<>();
		buzzerPin.addListener(new RelayListener() {

			@Override
			public void eventNotification(RelayState event) {
				lastBuzzerPinStateChangeTimestamp.set(System.currentTimeMillis());
				events.add(event);
			}
		});
		p.setPin(buzzerPin);
		SoundImpl s = new SoundImpl();
		s.add(new Beep(1000, 1000));
		s.add(new Beep(1000, 1000));
		s.add(new Beep(1000, 0));
		long start = System.currentTimeMillis();
		p.play(s);
		Thread.sleep(7000);
		Assert.assertEquals(6, events.size());
		Assert.assertEquals(5000, lastBuzzerPinStateChangeTimestamp.get() - start, 500);
	}
}
