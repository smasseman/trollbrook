package se.trollbrook.bryggmester;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import se.trollbrook.bryggmester.Relay.RelayState;

/**
 * @author jorgen.smas@entercash.com
 */
@Component
public class Heat {

	@Resource(name = "heatrelay")
	private Relay relay;

	public void on() {
		relay.setState(RelayState.ON);
	}

	public void off() {
		relay.setState(RelayState.OFF);
	}

	public Relay getRelay() {
		return relay;
	}

	public void setRelay(Relay relay) {
		this.relay = relay;
	}
}
