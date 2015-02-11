package se.trollbrook.bryggmester;

import org.junit.Assert;
import org.junit.Test;

import se.trollbrook.util.event.SimpleListener;

public class PumpTest {

	@Test
	public void test() {
		Pump pump = new Pump();
		pump.relay = new Relay(getClass().getSimpleName());
		pump.listeners.setAsynchronous(false);
		SimpleListener<PumpState> listener = new SimpleListener<>();
		pump.addListener(listener);

		pump.setOn();
		Assert.assertEquals(PumpState.ON, listener.getLast());

		pump.setPaused();
		Assert.assertEquals(PumpState.PAUSED, listener.getLast());

		pump.setOff();
		Assert.assertEquals(PumpState.OFF, listener.getLast());

		pump.setOverheated();
		Assert.assertEquals(PumpState.OFF, listener.getLast());

		pump.setOn();
		Assert.assertEquals(PumpState.HOT, listener.getLast());

		pump.setTempOk();
		Assert.assertEquals(PumpState.ON, listener.getLast());

		pump.setOff();
		Assert.assertEquals(PumpState.OFF, listener.getLast());

		pump.setOn();
		Assert.assertEquals(PumpState.ON, listener.getLast());

		pump.setPaused();
		Assert.assertEquals(PumpState.PAUSED, listener.getLast());

		pump.setOn();
		Assert.assertEquals(PumpState.ON, listener.getLast());
	}
}
