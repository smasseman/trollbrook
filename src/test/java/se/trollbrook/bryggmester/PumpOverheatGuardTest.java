package se.trollbrook.bryggmester;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;


/**
 * @author jorgen.smas@entercash.com
 */
public class PumpOverheatGuardTest {

	private PumpOverheatGuard g;
	private Pump pump;
	private TemperatureSensorMock tempSensor = new TemperatureSensorMock();

	@Before
	public void init() {
		LoggerFactory.getLogger(getClass()).debug("Init test.");
		pump = Mockito.mock(Pump.class);
		g = new PumpOverheatGuard();
		g.pump = pump;
		g.tempSensor = tempSensor;
		g.init();
	}

	@After
	public void destroy() {
	}

	@Test
	public void test() throws InterruptedException {
		tempSensor.setCurrentTemp(new Temperature(50));
		Thread.sleep(500);
		Mockito.verifyNoMoreInteractions(pump);

		tempSensor.setCurrentTemp(new Temperature(100));
		Thread.sleep(500);
		Mockito.verify(pump, Mockito.times(1)).setOverheated();

		tempSensor.setCurrentTemp(new Temperature(70));
		Thread.sleep(500);
		Mockito.verify(pump, Mockito.times(1)).setTempOk();

		tempSensor.setCurrentTemp(new Temperature(100));
		Thread.sleep(500);
		Mockito.verify(pump, Mockito.times(2)).setOverheated();

		tempSensor.setCurrentTemp(new Temperature(70));
		Thread.sleep(500);
		Mockito.verify(pump, Mockito.times(2)).setTempOk();

	}
}
