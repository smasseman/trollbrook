package se.trollbrook.bryggmester;

import se.trollbrook.bryggmester.Relay.RelayState;

/**
 * @author jorgen.smas@entercash.com
 */
public enum PumpState {

	ON(RelayState.ON), OFF(RelayState.OFF);

	private RelayState relayState;

	PumpState(RelayState r) {
		relayState = r;
	}

	public RelayState toRelayState() {
		return relayState;
	}
}
