package se.trollbrook.bryggmester;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import se.trollbrook.util.ObjectUtil;

/**
 * @author jorgen.smas@entercash.com
 */
@Service
public class PumpOverheatGuard implements TemperatureListener, Pump {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Resource(name = "pumprelay")
	private Pump pump;

	@Resource
	private TemperatureSensor tempSensor;

	private Temperature limit = new Temperature(90);

	private PumpState wantedState = PumpState.OFF;
	private PumpState currentState;

	private Temperature currentTemp;

	@PostConstruct
	public void init() {
		tempSensor.addListener(this);
	}

	private void update() {
		if (currentTemp == null) {
			logger.debug("Do not update state since temp is unknown.");
			return;
		}
		if (currentTemp.greaterThen(limit)) {
			setCurrentState(PumpState.OFF);
		} else {
			setCurrentState(wantedState);
		}
	}

	private void setCurrentState(PumpState state) {
		if (!ObjectUtil.equals(currentState, state)) {
			currentState = state;
			pump.setState(currentState);
			logger.info("Setting current state to " + state + ". Wanted="
					+ wantedState + " Temperature=" + currentTemp + " Limit="
					+ limit);
		}
	}

	@Override
	public void temperateureChanged(Temperature temp) {
		currentTemp = temp;
		update();
	}

	@Override
	public void setState(PumpState state) {
		wantedState = state;
		update();
	}

	public Pump getPump() {
		return pump;
	}

	public void setPump(Pump pump) {
		this.pump = pump;
	}

	public TemperatureSensor getTempSensor() {
		return tempSensor;
	}

	public void setTempSensor(TemperatureSensor tempSensor) {
		this.tempSensor = tempSensor;
	}
}
