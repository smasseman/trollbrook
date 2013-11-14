package se.bryggmester;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.pi4j.io.gpio.GpioPinDigitalOutput;

/**
 * @author jorgen.smas@entercash.com
 */
@Service
public class StartSignal {

	@Resource(name = "morsepin")
	private GpioPinDigitalOutput pin;

	@PostConstruct
	public void init() {
		playSignal();
	}

	@PreDestroy
	public void destroy() {
		playSignal();
	}

	private void playSignal() {
		new Thread() {
			@Override
			public void run() {
				try {
					pin.high();
					Thread.sleep(200);
					pin.low();
					Thread.sleep(500);
					pin.high();
					Thread.sleep(200);
					pin.low();
					Thread.sleep(500);
					pin.high();
					Thread.sleep(200);
					pin.low();
				} catch (InterruptedException e) {
				}
			}
		}.start();
	}

}
