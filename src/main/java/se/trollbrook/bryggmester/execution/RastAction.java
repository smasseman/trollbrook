package se.trollbrook.bryggmester.execution;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.trollbrook.bryggmester.Rast;
import se.trollbrook.bryggmester.Temperature;
import se.trollbrook.bryggmester.TimeLeftCalculator;
import se.trollbrook.util.Time;

/**
 * @author jorgen.smas@entercash.com
 */
public class RastAction implements Action {

	private Logger logger = LoggerFactory.getLogger(getClass());
	private Environment env;
	private Rast rast;
	private Time runDuration = new Time(5, TimeUnit.MINUTES);
	private Time pausDuration = new Time(30, TimeUnit.SECONDS);
	private Date doneTime;
	private boolean running;
	private Temperature previousTemp;

	public RastAction(Temperature previousTemp, Rast r, Environment env) {
		this.env = env;
		this.rast = r;
		this.previousTemp = previousTemp;
		toString();
	}

	@Override
	public void execute() {
		doneTime = null;
		running = true;
		try {
			env.getPumpController().start(runDuration, pausDuration);
			env.getTemperatureController().setWantedTemperature(rast.getTemperature());
			try {
				WaitForTemperature.waitFor(env.getTemperatureSensor(), rast.getTemperature());
			} catch (InterruptedException e) {
				env.getPumpController().off();
				env.getTemperatureController().setWantedTemperature(Temperature.OFF);
				return;
			}

			this.doneTime = new Date(System.currentTimeMillis() + rast.getDuration().toMillis());
			try {
				sleepUntilDoneTime();
			} catch (InterruptedException e) {
				return;
			} finally {
				env.getPumpController().off();
			}
		} finally {
			running = false;
		}
	}

	private void sleepUntilDoneTime() throws InterruptedException {
		long oneMinute = TimeUnit.SECONDS.toMillis(60);
		long t;
		while ((t = doneTime.getTime() - System.currentTimeMillis()) > 0) {
			logger.debug("This rast should last for " + this.rast.getDuration() + " so we will sleep here "
					+ Time.fromMillis(t).removeMillisSeconds() + " until "
					+ new SimpleDateFormat("HH:mm").format(doneTime));
			if (t < oneMinute)
				Thread.sleep(t);
			else
				Thread.sleep(oneMinute);
		}
	}

	@Override
	public String displayString() {
		return "Rast " + rast.getTemperature() + " " + rast.getDuration().toMinutes() + " m";
	}

	@Override
	public Time calculateTimeLeft() {
		Time t;
		if (running) {
			t = TimeLeftCalculator.calculateTimeToWarm(env.getTemperatureSensor().getCurrentTemperature(),
					rast.getTemperature());
		} else {
			t = TimeLeftCalculator.calculateTimeToWarm(previousTemp, rast.getTemperature());
		}
		if (doneTime == null) {
			t = t.add(rast.getDuration());
		} else {
			t = t.add(Time.fromMillis(doneTime.getTime() - System.currentTimeMillis()));
		}
		return t;
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
		return getClass().getSimpleName() + "[temp=" + rast.getTemperature() + ", duration=" + rast.getDuration() + "]";
	}
}
