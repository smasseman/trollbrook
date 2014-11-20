package se.trollbrook.bryggmester;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

/**
 * @author jorgen.smas@entercash.com
 */
@Service("pump")
public class PumpRelay implements Pump {

	@Resource(name = "pumprelay")
	private Relay relay;

	public PumpRelay() {
	}

	public PumpRelay(Relay r) {
		relay = r;
	}

	@Override
	public void setState(PumpState state) {
		relay.setState(state.toRelayState());
	}
}
