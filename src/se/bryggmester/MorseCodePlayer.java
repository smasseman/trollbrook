package se.bryggmester;

import java.util.List;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import se.bryggmester.morse.MorseUtil;
import se.bryggmester.morse.Output;
import se.bryggmester.morse.Output.Sound;

import com.pi4j.io.gpio.GpioPinDigitalOutput;

/**
 * @author jorgen.smas@entercash.com
 */
@Service
public class MorseCodePlayer {

	private Logger logger = LoggerFactory.getLogger(getClass());
	@Resource(name = "morsepin")
	private GpioPinDigitalOutput pin;
	private String text;
	private long unitDuration = 600;
	private Thread thread;

	public MorseCodePlayer() {
		logger.debug("Created.");
	}

	private void start() {
		final List<Output> output = MorseUtil.toSignals(text, unitDuration);
		thread = new Thread(new Runnable() {

			@Override
			public void run() {
				logger.debug("Player thread started.");
				try {
					while (!Thread.interrupted()) {
						for (Output o : output) {
							logger.trace("Play " + o);
							if (o.getSound() == Sound.BEEP) {
								pin.high();
							} else if (o.getSound() == Sound.SILENCE) {
								pin.low();
							}
							Thread.sleep(o.getDuration());
						}
						pin.low();
						Thread.sleep(unitDuration * 10);
					}
				} catch (InterruptedException e) {
				} catch (Throwable t) {
					logger.error("Error in player.", t);
				}
				logger.info("Player is down...");
			}
		});
		thread.start();
	}

	@PreDestroy
	public void stop() {
		logger.debug("Stop morse player thread.");
		if (thread != null)
			thread.interrupt();
	}

	public String getText() {
		return text;
	}

	public void setText(String text) throws InterruptedException {
		if (equals(this.text, text))
			return;
		if (thread != null) {
			stop();
			thread.join();
		}
		this.text = text;
		if (text != null && text.length() > 0) {
			start();
		}
	}

	private boolean equals(String a, String b) {
		if (a == null)
			return b == null;
		return a.equals(b);
	}
}
