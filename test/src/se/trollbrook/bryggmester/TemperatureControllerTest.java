package se.trollbrook.bryggmester;

import junit.framework.Assert;

import org.junit.Test;
import org.mockito.Mockito;

import se.trollbrook.bryggmester.Relay.RelayState;
import se.trollbrook.bryggmester.TemperatureController.WantedTemperatureListener;

/**
 * @author jorgen.smas@entercash.com
 */
public class TemperatureControllerTest {

	@Test
	public void test() {
		TemperatureController c = new TemperatureController();
		Heat heat = new Heat();
		Relay heatRelay = new Relay("HeatMock");
		heat.setRelay(heatRelay);
		c.setHeat(heat);
		TemperatureSensorMock tempSensor = new TemperatureSensorMock();
		c.setTempSensor(tempSensor);
		c.init();

		WantedTemperatureListener listener = Mockito
				.mock(WantedTemperatureListener.class);
		c.addListener(listener);

		tempSensor.setCurrentTemp(new Temperature(40));
		Assert.assertNull(heatRelay.getCurrentState());
		Mockito.verify(listener, Mockito.times(0)).eventNotification(
				Mockito.any(Temperature.class));

		c.setWantedTemperature(new Temperature(30));
		Assert.assertEquals(RelayState.OFF, heatRelay.getCurrentState());

		c.setWantedTemperature(new Temperature(50));
		Assert.assertEquals(RelayState.ON, heatRelay.getCurrentState());

		tempSensor.setCurrentTemp(new Temperature(60));
		Assert.assertEquals(RelayState.OFF, heatRelay.getCurrentState());

		Mockito.verify(listener, Mockito.times(2)).eventNotification(
				Mockito.any(Temperature.class));
	}
}
