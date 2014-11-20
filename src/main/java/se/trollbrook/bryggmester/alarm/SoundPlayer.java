package se.trollbrook.bryggmester.alarm;

import java.util.Iterator;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import se.trollbrook.bryggmester.Relay;
import se.trollbrook.bryggmester.Relay.RelayState;

/**
 * @author jorgen.smas@entercash.com
 */
@Service
public class SoundPlayer {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Resource(name = "beeperpin")
	private Relay pin;

	public synchronized void play(final Sound sound) {
		Thread t = new Thread() {
			@Override
			public void run() {
				Iterator<Beep> iter = sound.iterator();
				try {
					while (iter.hasNext()) {
						Beep s = iter.next();
						pin.setState(RelayState.ON);
						if (s.getDuration() > 0)
							Thread.sleep(s.getDuration());
						pin.setState(RelayState.OFF);
						if (s.getSilence() > 0)
							Thread.sleep(s.getSilence());
					}
				} catch (InterruptedException e) {
					logger.info(e.toString());
				} finally {
					pin.setState(RelayState.OFF);
				}
			}
		};
		t.start();
	}

	public Relay getPin() {
		return pin;
	}

	public void setPin(Relay pin) {
		this.pin = pin;
	}
}
