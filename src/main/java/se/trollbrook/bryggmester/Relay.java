package se.trollbrook.bryggmester;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.trollbrook.util.ObjectUtil;
import se.trollbrook.util.event.Listener;
import se.trollbrook.util.event.ListenerManager;

/**
 * @author jorgen.smas@entercash.com
 */
public class Relay {

	public static enum RelayState {
		ON, OFF;
	}

	public interface RelayListener extends Listener<RelayState> {
	}

	private ListenerManager<RelayState> lm = new ListenerManager<>();
	private RelayState currentState = RelayState.OFF;
	private String name;
	protected Logger logger = LoggerFactory.getLogger(getClass());

	public Relay(String name) {
		this.name = name;
	}

	public RelayState getCurrentState() {
		return currentState;
	}

	public void addListener(RelayListener listener) {
		lm.addListener(listener);
	}

	public void setState(RelayState state) {
		if (state == null)
			throw new IllegalArgumentException("Can not set state to null.");
		if (!ObjectUtil.equals(currentState, state)) {
			changeState(state);
			logger.debug(name + " change from {} to {}", currentState, state);
			currentState = state;
			logger.debug("Notify " + lm.getListeners() + " with " + state);
			lm.notifyListeners(state);
		}
	}

	// Override this.
	protected void changeState(RelayState state) {
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(name);
		builder.append("[");
		builder.append(currentState);
		builder.append("]");
		return builder.toString();
	}
}
