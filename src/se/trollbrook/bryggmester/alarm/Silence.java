package se.trollbrook.bryggmester.alarm;

import java.util.ArrayList;

/**
 * @author jorgen.smas@entercash.com
 */
public class Silence extends SoundImpl {

	public Silence() {
		super(new ArrayList<Beep>());
	}
}
