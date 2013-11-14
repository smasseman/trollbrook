package se.trollbrook.bryggmester;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.trollbrook.util.ObjectUtil;

/**
 * @author jorgen.smas@entercash.com
 */
public class Relay {

	public static enum RelayState {
		ON, OFF;
	}

	public interface RelayListener {
		void relayStateChanged(RelayState state);
	}

	private List<RelayListener> listeners = new LinkedList<>();
	private RelayState currentState;
	private String name;
	protected Logger logger = LoggerFactory.getLogger(getClass());

	public Relay(String name) {
		this.name = name;
	}

	public RelayState getCurrentState() {
		return currentState;
	}

	public void addListener(RelayListener listener) {
		if (listener == null)
			throw new IllegalArgumentException("Listener may not be null.");
		listeners.add(listener);
	}

	public void setState(RelayState state) {
		if (state == null)
			throw new IllegalArgumentException("Can not set state to null.");
		if (!ObjectUtil.equals(currentState, state)) {
			changeState(state);
			logger.debug(name + " change from {} to {}", currentState, state);
			currentState = state;
			notifyListeners(state);
		}
	}

	private void notifyListeners(RelayState state) {
		for (RelayListener l : listeners) {
			try {
				l.relayStateChanged(state);
			} catch (Exception e) {
				logger.info("Failed to notify " + l, e);
			}
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
