package se.trollbrook.bryggmester.execution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.trollbrook.bryggmester.Temperature;
import se.trollbrook.bryggmester.TimeLeftCalculator;
import se.trollbrook.util.Time;

/**
 * @author jorgen.smas@entercash.com
 */
public class StartTempAction implements Action {

	private Logger logger = LoggerFactory.getLogger(getClass());
	private Environment env;
	private Temperature temperature;

	public StartTempAction(Environment env, Temperature temperature) {
		this.env = env;
		this.temperature = temperature;
	}

	@Override
	public void execute() {
		env.getPumpController().startMax();
		env.getTemperatureController().setWantedTemperature(temperature);
		try {
			logStartWait();
			WaitForTemperature.waitFor(env.getTemperatureSensor(), temperature);
			logDoneWaiting();
		} catch (InterruptedException e) {
			return;
		}
	}

	private void logDoneWaiting() {
		logger.debug("Done waiting for " + temperature);
	}

	private void logStartWait() {
		logger.debug("Current temp is {} and we will wait here for {}.", env.getTemperatureSensor()
				.getCurrentTemperature(), temperature);
	}

	@Override
	public String displayString() {
		return "Värm till " + temperature;
	}

	@Override
	public Time calculateTimeLeft() {
		return TimeLeftCalculator.calculateTimeToWarm(env.getTemperatureSensor().getCurrentTemperature(), temperature);
	}

	@Override
	public boolean requireUserActionAtStart() {
		return false;
	}

	@Override
	public boolean requireUserActionAtEnd() {
		return false;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
