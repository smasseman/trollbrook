package se.trollbrook.bryggmester.alarm;

/**
 * @author jorgen.smas@entercash.com
 */
public class Beep {

	private long duration;
	private long silence;

	public Beep(long i, long l) {
		this.duration = i;
		this.silence = l;
	}

	public long getDuration() {
		return duration;
	}

	public long getSilence() {
		return silence;
	}
}
