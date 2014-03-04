package se.trollbrook.bryggmester;

import java.net.SocketException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import se.trollbrook.bryggmester.Relay.RelayState;
import se.trollbrook.util.NetworkUtil;

/**
 * @author jorgen.smas@entercash.com
 */
@Service
public class PlayWelcome {

	private Logger logger = LoggerFactory.getLogger(getClass());
	@Resource(name = "extrarelay")
	private Relay relay;
	private Thread thread;

	@PostConstruct
	public void start() {
		logger.info("Start.");
		this.thread = new Thread() {
			@Override
			public void run() {
				try {
					play(3);
					Thread.sleep(5000);

					String ip;
					while ((ip = NetworkUtil.getIp()) == null) {
						logger.info("Got no IP address yet.");
						Thread.sleep(10 * 1000);
					}
					logger.info("My IP is " + ip);
					play(5);
					relay.setState(RelayState.ON);
				} catch (InterruptedException e) {
				} catch (SocketException e) {
					logger.warn("Failed to get ip address.", e);
				}
			}

			private void play(int count) throws InterruptedException {
				for (int i = 0; i < count; i++) {
					relay.setState(RelayState.ON);
					Thread.sleep(100);
					relay.setState(RelayState.OFF);
					Thread.sleep(500);
				}
			}
		};
		this.thread.start();
	}

	@PreDestroy
	public void stop() {
		this.thread.interrupt();
	}
}
