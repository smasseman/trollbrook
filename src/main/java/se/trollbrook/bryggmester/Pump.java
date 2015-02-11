package se.trollbrook.bryggmester;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import se.trollbrook.util.event.Listener;
import se.trollbrook.util.event.ListenerManager;

/**
 * @author jorgen.smas@entercash.com
 */
@Service
public class Pump {

	@Resource(name = "pumppin")
	Relay relay;
	ListenerManager<PumpState> listeners = new ListenerManager<PumpState>();
	private boolean on = false;
	private boolean hot = false;
	private boolean paused = false;
	private Logger logger = LoggerFactory.getLogger(getClass());
	private PumpState state = PumpState.OFF;

	public void addListener(Listener<PumpState> listener) {
		listeners.addListener(listener);
	}

	public synchronized void setOverheated() {
		if (hot)
			return;
		hot = true;
		update();
	}

	public synchronized void setOn() {
		if (on && !paused)
			return;
		on = true;
		paused = false;
		update();
	}

	public synchronized void setOff() {
		if (!(on || paused))
			return;
		on = false;
		paused = false;
		update();
	}

	public synchronized void setPaused() {
		if (paused)
			return;
		paused = true;
		update();
	}

	public synchronized void setTempOk() {
		if (!hot)
			return;
		hot = false;
		update();
	}

	private void update() {
		if (!on) {
			state = PumpState.OFF;
		} else if (hot) {
			state = PumpState.HOT;
		} else if (paused) {
			state = PumpState.PAUSED;
		} else {
			state = PumpState.ON;
		}
		logger.debug("on=" + on + ", hot=" + hot + ", paused=" + paused + " -> " + state);
		if (state == PumpState.ON) {
			relay.on();
		} else {
			relay.off();
		}
		listeners.notifyListeners(state);
	}

	public synchronized PumpState getCurrentState() {
		return state;
	}
}
