package se.trollbrook.bryggmester.alarm;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author jorgen.smas@entercash.com
 */
public class RepeatAlarmSound extends SoundImpl {

	public RepeatAlarmSound() {
		super(beeps());
	}

	private static List<Beep> beeps() {
		List<Beep> beeps = new LinkedList<>();
		beeps.add(new Beep(500, 200));
		beeps.add(new Beep(500, 200));
		beeps.add(new Beep(500, TimeUnit.SECONDS.toMillis(10)));
		for (int i = 0; i < 100; i++) {
			beeps.add(new Beep(1500, TimeUnit.SECONDS.toMillis(20)));
		}
		return beeps;
	}
}
