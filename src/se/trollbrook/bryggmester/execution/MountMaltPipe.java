package se.trollbrook.bryggmester.execution;

import java.util.concurrent.TimeUnit;

import se.trollbrook.bryggmester.alarm.Alarm;
import se.trollbrook.bryggmester.alarm.OneTimeAlarm;
import se.trollbrook.util.Time;

/**
 * @author jorgen.smas@entercash.com
 */
public class MountMaltPipe implements Action {

	private Environment env;

	public MountMaltPipe(Environment env) {
		this.env = env;
	}

	@Override
	public void execute() {
		try {
			Alarm alarm = new Alarm("Starttemperaturen är uppnådd. "
					+ "Montera maltröret. Kontrollera att rörtet sitter perfekt genom att prova pumpen. "
					+ "Fyll på malt. Klicka OK när allt är gjort.", Alarm.Type.WAIT_FOR_USER_INPUT, new OneTimeAlarm());
			alarm.setShowPumpControl(true);
			env.getAlarms().fireAlarm(alarm);
		} catch (InterruptedException e) {
			return;
		}
	}

	@Override
	public String displayString() {
		return "Montera maltpipan och fyll.";
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
