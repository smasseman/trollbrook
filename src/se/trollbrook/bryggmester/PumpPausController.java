package se.trollbrook.bryggmester;

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import se.trollbrook.util.Time;

/**
 * @author jorgen.smas@entercash.com
 */
@Service
public class PumpPausController {

	@Resource(name = "pumpoverheatguard")
	private Pump pump;
	private Logger logger = LoggerFactory.getLogger(getClass());
	private Thread thread;
	private boolean die = false;
	private boolean off = true;
	private final Object lock = new Object();
	private Time runDuration;
	private Time pausDuration;

	@PostConstruct
	public void init() {
		thread = new Thread() {
			@Override
			public void run() {
				try {
					synchronized (lock) {
						while (!die) {
							try {
								while (off) {
									pump.setState(PumpState.OFF);
									lock.wait();
								}

								pump.setState(PumpState.ON);
								logger.debug("State is ON. Lets sleep for " + runDuration);
								lock.wait(runDuration.toMillis());

								if (!pausDuration.equals(Time.ZERO)) {
									pump.setState(PumpState.OFF);
									logger.debug("State is OFF. Lets sleep for " + pausDuration);
									lock.wait(pausDuration.toMillis());
								}

							} catch (InterruptedException e) {
								logger.debug("Interrupted.");
							}
						}
					}
				} catch (Throwable t) {
					logger.error("Thread will die.", t);
				} finally {
					if (die)
						logger.debug("Thread was told to die.");
					else
						logger.warn("Thread is down.");
				}
			}
		};
		thread.start();
	}

	@PreDestroy
	public void destroy() {
		die = true;
		thread.interrupt();
	}

	public void off() {
		synchronized (lock) {
			logger.debug("Turn off.");
			off = true;
			lock.notifyAll();
		}
	}

	public void start(Time runDuration, Time pausDuration) {
		synchronized (lock) {
			logger.debug("Start with run duration {} and paus duration {}.", runDuration, pausDuration);
			off = false;
			this.runDuration = runDuration;
			this.pausDuration = pausDuration;
			lock.notifyAll();
		}
	}

	public void startMax() {
		start(new Time(24, TimeUnit.HOURS), Time.ZERO);
	}
}
