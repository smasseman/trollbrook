package se.trollbrook.bryggmester;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import se.trollbrook.util.event.Listener;
import se.trollbrook.util.event.ListenerManager;

/**
 * @author jorgen.smas@entercash.com
 */
@Component
public class TemperatureController {

	public static interface WantedTemperatureListener extends Listener<Temperature> {
	}

	@Resource
	private Heat heat;
	@Resource
	private TemperatureSensor tempSensor;

	private ListenerManager<Temperature> lm = new ListenerManager<>();
	private Temperature wanted;
	protected Temperature current;

	@PostConstruct
	public void init() {
		tempSensor.addListener(new TemperatureListener() {

			@Override
			public void eventNotification(Temperature temp) {
				synchronized (TemperatureController.this) {
					current = temp;
					updateHeat();
				}
			}
		});
	}

	public synchronized void addListener(WantedTemperatureListener listener) {
		lm.addListener(listener);
	}

	public synchronized void setWantedTemperature(Temperature temp) {
		if (!temp.equals(wanted)) {
			wanted = temp;
			updateHeat();
			lm.notifyListeners(wanted);
		}
	}

	private void updateHeat() {
		if (current == null)
			return;
		if (wanted == null)
			return;
		if (wanted.greaterThen(current) || wanted.equals(Temperature.MAX))
			heat.on();
		else
			heat.off();
	}

	public TemperatureSensor getTempSensor() {
		return tempSensor;
	}

	public void setTempSensor(TemperatureSensor tempSensor) {
		this.tempSensor = tempSensor;
	}

	public Heat getHeat() {
		return heat;
	}

	public void setHeat(Heat heat) {
		this.heat = heat;
	}

	public synchronized Temperature getCurrentWantedTemperature() {
		return wanted;
	}
}
