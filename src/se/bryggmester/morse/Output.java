package se.bryggmester.morse;

import java.util.List;

/**
 * @author jorgen.smas@entercash.com
 */
public class Output {

	public enum Sound {
		BEEP, SILENCE;
	}

	private Sound sound;
	private long duration;

	public Output(Sound sound, long duration) {
		super();
		this.sound = sound;
		this.duration = duration;
	}

	public Sound getSound() {
		return sound;
	}

	public long getDuration() {
		return duration;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + sound + " " + duration + "]";
	}

	public static String toString(List<Output> output) {
		StringBuilder s = new StringBuilder();
		for (Output o : output) {
			for (int i = 0; i < o.getDuration(); i++) {
				if (o.getSound() == Sound.BEEP)
					s.append('=');
				else
					s.append('.');
			}
		}
		return s.toString();
	}

	public void setDuration(long l) {
		this.duration = l;
	}
}
