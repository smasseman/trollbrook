package se.trollbrook.bryggmester.alarm;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jorgen.smas@entercash.com
 */
public class SoundImpl implements Sound {

	private Logger logger = LoggerFactory.getLogger(getClass());
	private List<Beep> beeps;
	private boolean stop;

	public SoundImpl() {
		this(new LinkedList<Beep>());
	}

	public SoundImpl(List<Beep> beeps) {
		this.beeps = beeps;
	}

	@Override
	public Iterator<Beep> iterator() {
		logger.debug("Returning beeps for " + getClass().getSimpleName());
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
				boolean hasMore = iter.hasNext() && !stop;
				if (!hasMore)
					logger.debug("Returning no more beeps for " + getClass().getSimpleName());
				return hasMore;
			}
		};
	}

	@Override
	public void stop() {
		logger.debug("Sound stopped.");
		this.stop = true;
	}

	public void add(Beep beep) {
		this.beeps.add(beep);
	}
}
