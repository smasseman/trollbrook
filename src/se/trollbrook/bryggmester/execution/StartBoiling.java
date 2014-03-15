package se.trollbrook.bryggmester.execution;

import java.math.BigDecimal;
import java.util.List;

import se.trollbrook.bryggmester.Rast;
import se.trollbrook.bryggmester.Recipe;
import se.trollbrook.bryggmester.Temperature;
import se.trollbrook.bryggmester.TemperatureController;
import se.trollbrook.bryggmester.TimeLeftCalculator;
import se.trollbrook.util.Time;

/**
 * @author jorgen.smas@entercash.com
 */
public class StartBoiling implements Action {

	private boolean started = false;
	private Temperature expectedStartTemp;
	private Environment env;
	private Recipe recipe;

	public StartBoiling(Environment env, Recipe recipe) {
		this.recipe = recipe;
		this.env = env;
		List<Rast> r = recipe.getRasts();
		if (r.isEmpty()) {
			if (recipe.getStartTemperature() == null)
				expectedStartTemp = new Temperature(20);
			else
				expectedStartTemp = recipe.getStartTemperature();
		} else {
			Rast lastRast = r.get(r.size() - 1);
			expectedStartTemp = lastRast.getTemperature();
		}
	}

	@Override
	public void execute() {
		started = true;
		turnOnHeat();
		turnOnPump();
		try {
			waitForBoiling();
		} catch (InterruptedException e) {
		}
	}

	private void turnOnPump() {
		env.getPumpController().startMax();
	}

	private void waitForBoiling() throws InterruptedException {
		WaitForTemperature.waitFor(env.getTemperatureSensor(), new Temperature(new BigDecimal(97)));
	}

	private void turnOnHeat() {
		TemperatureController tempCtrl = env.getTemperatureController();
		tempCtrl.setWantedTemperature(Temperature.MAX);
	}

	@Override
	public String displayString() {
		return "Starta kokning (Koka i " + recipe.getBoilDuration().toMinutes() + " m).";
	}

	@Override
	public Time calculateTimeLeft() {
		if (started) {
			return TimeLeftCalculator.calculateTimeToWarm(env.getTemperatureSensor().getCurrentTemperature(),
					Temperature.MAX);
		} else {
			return TimeLeftCalculator.calculateTimeToWarm(expectedStartTemp, Temperature.MAX);
		}
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
