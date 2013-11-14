package se.trollbrook.bryggmester;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jorgen.smas@entercash.com
 */
public class AbstractTemperatureSensor implements TemperatureSensor {

	protected Logger logger = LoggerFactory.getLogger(getClass());
	private Temperature currentTemp;
	private List<TemperatureListener> listeners = new LinkedList<>();

	@Override
	public void addListener(TemperatureListener listener) {
		if (listener == null)
			throw new IllegalArgumentException("Listener may not be null.");
		listeners.add(listener);
	}

	protected void setCurrentTemp(Temperature temp) {
		if (!temp.equals(currentTemp)) {
			currentTemp = temp;
			logger.info("Temp " + temp.toString());
			notifyListeners(temp);
		}
	}

	private void notifyListeners(Temperature temp) {
		for (TemperatureListener l : listeners) {
			l.temperateureChanged(temp);
		}
	}

	@Override
	public Temperature getCurrentTemperature() {
		return currentTemp;
	}

}
