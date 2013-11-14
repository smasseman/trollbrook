package se.bryggmester;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.pi4j.io.gpio.GpioPinDigitalOutput;

/**
 * @author jorgen.smas@entercash.com
 */
@Service
public class Pump {

	public interface Listener {
		public void pumpStataNotify(PumpState state);
	}

	private Logger logger = LoggerFactory.getLogger(getClass());
	private PumpState currentState;
	private List<Listener> listeners = new ArrayList<Listener>();
	@Resource(name = "pumppin")
	private GpioPinDigitalOutput pin;

	public void addListener(Listener l) {
		listeners.add(l);
	}

	public void setState(PumpState state) {
		if (state != currentState) {
			if (state == PumpState.ON) {
				pin.high();
				logger.info("Start pump.");
			} else {
				pin.low();
				logger.info("Stop pump.");
			}
			currentState = state;
			for (Listener l : listeners) {
				l.pumpStataNotify(currentState);
			}
		}
	}

	public PumpState getCurrentState() {
		return currentState;
	}

	public GpioPinDigitalOutput getPin() {
		return pin;
	}

	public void setPin(GpioPinDigitalOutput pin) {
		this.pin = pin;
	}
}
