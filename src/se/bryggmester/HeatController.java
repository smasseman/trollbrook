package se.bryggmester;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import se.bryggmester.TemperatureSensor.Listener;
import se.bryggmester.util.ObjectUtil;

/**
 * @author jorgen.smas@entercash.com
 */
@Service
public class HeatController implements Listener {

	public interface Listener {
		void notifyWantedTempChanged(Temperature temp);
	}

	@Resource
	private Heat heat;
	@Resource
	private TemperatureSensor tempSensor;
	private Temperature wantedTemp;
	private Temperature currentTemp;
	private List<Listener> listeners = new LinkedList<>();
	private Object waitLock = new Object();

	public void addListener(Listener l) {
		listeners.add(l);
	}

	@PostConstruct
	public void start() {
		tempSensor.addListener(this);
	}

	@Override
	public void notifyTemperatureChanged(Temperature temp) {
		currentTemp = temp;
		updateState();
		synchronized (waitLock) {
			waitLock.notifyAll();
		}
		;
	}

	private void updateState() {
		if (wantedTemp == null) {
			heat.setState(HeatState.OFF);
		} else if (currentTemp == null) {
			heat.setState(HeatState.OFF);
		} else if (wantedTemp.lowerThen(currentTemp)) {
			heat.setState(HeatState.OFF);
		} else {
			heat.setState(HeatState.ON);
		}
	}

	public Temperature getWantedTemp() {
		return wantedTemp;
	}

	public void setWantedTemp(Temperature t) {
		if (ObjectUtil.equals(wantedTemp, t))
			return;
		this.wantedTemp = t;
		updateState();
		notifyListeners(t);
	}

	private void notifyListeners(Temperature t) {
		for (Listener l : listeners) {
			l.notifyWantedTempChanged(t);
		}
	}

	public void waitForTemperature(Temperature temperature)
			throws InterruptedException {
		synchronized (waitLock) {
			while (currentTemp == null
					|| tempDiff(temperature, currentTemp) > 1) {
				waitLock.wait();
			}
		}
	}

	private float tempDiff(Temperature t1, Temperature t2) {
		float abs = Math.abs(t1.getValue() - t2.getValue());
		return abs;
	}
}
