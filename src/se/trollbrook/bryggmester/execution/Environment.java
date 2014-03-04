package se.trollbrook.bryggmester.execution;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import se.trollbrook.bryggmester.PumpPausController;
import se.trollbrook.bryggmester.TemperatureController;
import se.trollbrook.bryggmester.TemperatureSensor;
import se.trollbrook.bryggmester.alarm.Alarms;

/**
 * @author jorgen.smas@entercash.com
 */
@Component
public class Environment {

	@Resource
	private TemperatureController temperatureController;
	@Resource
	private TemperatureSensor temperatureSensor;
	@Resource
	private Alarms alarms;
	@Resource
	private PumpPausController pumpController;

	public TemperatureController getTemperatureController() {
		return temperatureController;
	}

	public void setTemperatureController(TemperatureController temperatureController) {
		this.temperatureController = temperatureController;
	}

	public TemperatureSensor getTemperatureSensor() {
		return temperatureSensor;
	}

	public void setTemperatureSensor(TemperatureSensor temperatureSensor) {
		this.temperatureSensor = temperatureSensor;
	}

	public Alarms getAlarms() {
		return alarms;
	}

	public void setAlarms(Alarms alarms) {
		this.alarms = alarms;
	}

	public PumpPausController getPumpController() {
		return pumpController;
	}

	public void setPumpController(PumpPausController pumpController) {
		this.pumpController = pumpController;
	}

}
