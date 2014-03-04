package se.trollbrook.bryggmester;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.trollbrook.util.event.ListenerManager;

/**
 * @author jorgen.smas@entercash.com
 */
public class AbstractTemperatureSensor implements TemperatureSensor {

	protected Logger logger = LoggerFactory.getLogger(getClass());
	private Temperature currentTemp = new Temperature(new BigDecimal(10));
	private ListenerManager<Temperature> lm = new ListenerManager<>();

	@Override
	public void addListener(TemperatureListener listener) {
		lm.addListener(listener);
	}

	@Override
	public void removeListener(TemperatureListener listener) {
		lm.removeListener(listener);
	}

	protected void setCurrentTemp(Temperature temp) {
		if (!temp.equals(currentTemp)) {
			currentTemp = temp;
			logger.info("Temp " + temp.toString());
			lm.notifyListeners(temp);
		}
	}

	@Override
	public Temperature getCurrentTemperature() {
		return currentTemp;
	}
}
