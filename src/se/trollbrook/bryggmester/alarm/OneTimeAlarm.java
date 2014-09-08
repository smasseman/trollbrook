package se.trollbrook.bryggmester.alarm;

import java.util.LinkedList;
import java.util.List;

/**
 * @author jorgen.smas@entercash.com
 */
public class OneTimeAlarm extends SoundImpl {

	public OneTimeAlarm() {
		super(beeps());
	}

	private static List<Beep> beeps() {
		List<Beep> beeps = new LinkedList<>();
		beeps.add(new Beep(500, 200));
		beeps.add(new Beep(500, 200));
		beeps.add(new Beep(500, 0));
		return beeps;
	}
}
