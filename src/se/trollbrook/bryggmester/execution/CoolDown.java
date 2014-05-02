package se.trollbrook.bryggmester.execution;

import se.trollbrook.bryggmester.Temperature;
import se.trollbrook.bryggmester.TimeLeftCalculator;
import se.trollbrook.util.Time;

/**
 * @author jorgen.smas@entercash.com
 */
public class CoolDown implements Action {

	private Environment env;
	private boolean started;

	public CoolDown(Environment env) {
		this.env = env;
	}

	@Override
	public void execute() {
		env.getTemperatureController().setWantedTemperature(Temperature.OFF);
		env.getAlarms().fireAlarmWithoutWait("Värmen är avslagen. Påbörja nedkylningen.");

		this.started = true;
		try {
			WaitForTemperature.waitForBelow(env.getTemperatureSensor(), new Temperature(30));
			env.getAlarms().fireAlarmWithoutWait("Temperaturen är nu under 30 grader.");

			WaitForTemperature.waitForBelow(env.getTemperatureSensor(), new Temperature(20));
			env.getAlarms().fireAlarmAndWait("Temperaturen är nu under 20 grader. Körningen är klar.");
		} catch (InterruptedException e) {
			return;
		} finally {
			started = false;
			env.getPumpController().off();
		}
	}

	@Override
	public String displayString() {
		return "Kyl vörten.";
	}

	@Override
	public Time calculateTimeLeft() {
		Temperature to = new Temperature(20);
		if (started)
			return TimeLeftCalculator.calculateTimeToCool(env.getTemperatureSensor().getCurrentTemperature(), to);
		else
			return TimeLeftCalculator.calculateTimeToCool(Temperature.MAX, to);
	}

	@Override
	public boolean requireUserActionAtStart() {
		if (started)
			return false;
		return true;
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
