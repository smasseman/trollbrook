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
public class Heat {

	public interface Listener {
		public void heatStateNotify(HeatState state);
	}

	private Logger logger = LoggerFactory.getLogger(getClass());
	private HeatState currentState;
	private List<Listener> listeners = new ArrayList<Listener>();
	@Resource(name = "heatpin")
	private GpioPinDigitalOutput pin;

	public void addListener(Listener l) {
		listeners.add(l);
	}

	public void setState(HeatState state) {
		if (state != currentState) {
			if (state == HeatState.ON) {
				pin.high();
				logger.info("Start heat.");
			} else {
				pin.low();
				logger.info("Stop heat.");
			}
			currentState = state;
			for (Listener l : listeners) {
				l.heatStateNotify(currentState);
			}
		}
	}

	public HeatState getCurrentState() {
		return currentState;
	}
}
