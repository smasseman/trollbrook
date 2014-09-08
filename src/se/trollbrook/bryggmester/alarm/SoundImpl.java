package se.trollbrook.bryggmester.alarm;

import java.util.Iterator;
import java.util.List;

/**
 * @author jorgen.smas@entercash.com
 */
public class SoundImpl implements Sound {

	private List<Beep> beeps;
	private boolean stop;

	public SoundImpl(List<Beep> beeps) {
		this.beeps = beeps;
	}

	@Override
	public Iterator<Beep> iterator() {
		final Iterator<Beep> iter = beeps.iterator();
		return new Iterator<Beep>() {

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

			@Override
			public Beep next() {
				return iter.next();
			}

			@Override
			public boolean hasNext() {
				return iter.hasNext() && !stop;
			}
		};
	}

	@Override
	public void stop() {
		this.stop = true;
	}
}
