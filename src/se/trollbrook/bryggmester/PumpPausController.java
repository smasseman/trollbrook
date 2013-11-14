package se.trollbrook.bryggmester;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import se.trollbrook.util.TimeUnit;

/**
 * @author jorgen.smas@entercash.com
 */
@Service
public class PumpPausController {

	@Resource(name = "pumpoverheatcontroller")
	private Pump pump;
	private Logger logger = LoggerFactory.getLogger(getClass());
	private Thread thread;
	private boolean die = false;
	private boolean off = true;
	private final Object lock = new Object();
	private long runDuration;
	private long pausDuration;

	@PostConstruct
	public void init() {
		thread = new Thread() {
			@Override
			public void run() {
				try {
					synchronized (lock) {
						while (!die) {
							try {
								while (off)
									lock.wait();

								pump.setState(PumpState.ON);
								logger.debug("State is ON. Lets sleep for "
										+ TimeUnit.format(runDuration));
								sleep(runDuration);

								pump.setState(PumpState.OFF);
								logger.debug("State is OFF. Lets sleep for "
										+ TimeUnit.format(pausDuration));
								sleep(pausDuration);

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
			off = true;
			lock.notifyAll();
		}
	}

	public void start(long runDuration, long pausDuration) {
		synchronized (lock) {
			off = false;
			this.runDuration = runDuration;
			this.pausDuration = pausDuration;
			lock.notifyAll();
		}
	}
}
