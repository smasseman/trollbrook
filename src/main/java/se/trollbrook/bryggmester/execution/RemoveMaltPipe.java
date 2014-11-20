package se.trollbrook.bryggmester.execution;

import java.util.concurrent.TimeUnit;

import se.trollbrook.bryggmester.Temperature;
import se.trollbrook.bryggmester.alarm.Alarm;
import se.trollbrook.bryggmester.alarm.Alarm.Type;
import se.trollbrook.bryggmester.alarm.OneTimeAlarm;
import se.trollbrook.util.Time;

/**
 * @author jorgen.smas@entercash.com
 */
public class RemoveMaltPipe implements Action {

	private Environment env;

	public RemoveMaltPipe(Environment env) {
		this.env = env;
	}

	@Override
	public void execute() {
		try {
			env.getAlarms().fireAlarm(
					new Alarm("Alla raster är klara. Klicka här innan du demonterar maltröret.",
							Type.WAIT_FOR_USER_INPUT, new OneTimeAlarm()));
			env.getPumpController().off();
			env.getTemperatureController().setWantedTemperature(Temperature.OFF);
			env.getAlarms().fireAlarm(
					new Alarm("Klicka här när allt är demonterat och klart för kokning.", Type.WAIT_FOR_USER_INPUT,
							new OneTimeAlarm()));
		} catch (InterruptedException e) {
			return;
		}
	}

	@Override
	public String displayString() {
		return "Demontera maltpipan.";
	}

	@Override
	public Time calculateTimeLeft() {
		return new Time(5, TimeUnit.MINUTES);
	}

	@Override
	public boolean requireUserActionAtStart() {
		return true;
	}

	@Override
	public boolean requireUserActionAtEnd() {
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
