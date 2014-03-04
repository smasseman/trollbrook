package se.trollbrook.bryggmester.execution;

import java.util.concurrent.TimeUnit;

import se.trollbrook.bryggmester.PumpPausController;
import se.trollbrook.bryggmester.alarm.Alarms;
import se.trollbrook.util.Time;

/**
 * @author jorgen.smas@entercash.com
 */
public class AntiAirAction implements Action {

	private PumpPausController pumpCtrl;
	private Alarms alarms;

	public AntiAirAction(Environment env) {
		this.pumpCtrl = env.getPumpController();
		this.alarms = env.getAlarms();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	@Override
	public void execute() {
		startPump();
		try {
			alarms.fireAlarmAndWait("Klicka när pumpen är luftad.");
		} catch (InterruptedException e) {
			// Do clean up in finally block.
		} finally {
			pumpCtrl.off();
		}
	}

	private void startPump() {
		Time runDuration = new Time(5L, TimeUnit.SECONDS);
		Time pausDuration = new Time(2L, TimeUnit.SECONDS);
		pumpCtrl.start(runDuration, pausDuration);
	}

	@Override
	public String displayString() {
		return "Luftning av pumpen.";
	}

	@Override
	public Time calculateTimeLeft() {
		return Time.ZERO;
	}

	@Override
	public boolean requireUserActionAtStart() {
		return false;
	}

	@Override
	public boolean requireUserActionAtEnd() {
		return true;
	}

}
