package se.trollbrook.bryggmester;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import se.trollbrook.bryggmester.Relay.RelayState;

/**
 * @author jorgen.smas@entercash.com
 */
public class PumpOverheatGuardTest {

	private PumpOverheatGuard g;
	private Relay pumpRelay = new Relay(getClass().getSimpleName() + " Pump");
	private TemperatureSensorMock tempSensor = new TemperatureSensorMock();

	@Before
	public void init() {
		LoggerFactory.getLogger(getClass()).debug("Init test.");
		g = new PumpOverheatGuard();
		g.setPump(new PumpRelay(pumpRelay));
		g.setTempSensor(tempSensor);
		g.init();
	}

	@After
	public void destroy() {
	}

	@Test
	public void testNoTemperature() {
		g.setState(PumpState.ON);
		Assert.assertNull(pumpRelay.getCurrentState());
	}

	@Test
	public void test() {
		g.setState(PumpState.ON);

		tempSensor.setCurrentTemp(new Temperature(50));
		Assert.assertEquals(RelayState.ON, pumpRelay.getCurrentState());

		tempSensor.setCurrentTemp(new Temperature(100));
		Assert.assertEquals(RelayState.OFF, pumpRelay.getCurrentState());

		tempSensor.setCurrentTemp(new Temperature(70));
		Assert.assertEquals(RelayState.ON, pumpRelay.getCurrentState());

		g.setState(PumpState.OFF);
		Assert.assertEquals(RelayState.OFF, pumpRelay.getCurrentState());

		tempSensor.setCurrentTemp(new Temperature(100));
		Assert.assertEquals(RelayState.OFF, pumpRelay.getCurrentState());

		tempSensor.setCurrentTemp(new Temperature(70));
		Assert.assertEquals(RelayState.OFF, pumpRelay.getCurrentState());
	}
}
