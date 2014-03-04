package se.trollbrook.bryggmester.execution;

import java.util.concurrent.TimeUnit;

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
			env.getAlarms().fireAlarmAndWait(
					"Starttemperaturen är uppnådd. Klicka här innan du börjar montera maltröret.");
			env.getPumpController().off();
			env.getAlarms().fireAlarmAndWait("Klicka här när allt är monterat och malten är fylld.");
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
