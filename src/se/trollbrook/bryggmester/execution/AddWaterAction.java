package se.trollbrook.bryggmester.execution;

import se.trollbrook.bryggmester.alarm.Alarm;
import se.trollbrook.bryggmester.alarm.Alarms;
import se.trollbrook.bryggmester.alarm.OneTimeAlarm;
import se.trollbrook.util.Time;

/**
 * @author jorgen.smas@entercash.com
 */
public class AddWaterAction implements Action {

	private Alarms alarms;

	public AddWaterAction(Alarms alarms) {
		this.alarms = alarms;
	}

	@Override
	public void execute() {
		try {
			alarms.fireAlarm(new Alarm("Fyll på vatten. Klicka här när det är gjort.", Alarm.Type.WAIT_FOR_USER_INPUT,
					new OneTimeAlarm()));
		} catch (InterruptedException e) {
			return;
		}
	}

	@Override
	public String displayString() {
		return "Fyll vatten.";
	}

	@Override
	public Time calculateTimeLeft() {
		return Time.ZERO;
	}

	@Override
	public boolean requireUserActionAtStart() {
		return true;
	}

	@Override
	public boolean requireUserActionAtEnd() {
		return false;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getClass().getSimpleName());
		builder.append("[]");
		return builder.toString();
	}
}
