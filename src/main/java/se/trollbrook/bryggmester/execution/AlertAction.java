package se.trollbrook.bryggmester.execution;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.trollbrook.bryggmester.alarm.Alarm;
import se.trollbrook.bryggmester.alarm.Alarm.Type;
import se.trollbrook.bryggmester.alarm.Alarms;
import se.trollbrook.bryggmester.alarm.RepeatAlarmSound;
import se.trollbrook.util.Time;

/**
 * @author jorgen.smas@entercash.com
 */
public class AlertAction implements Action {

	private String message;
	private Alarms alarms;
	private Time waitTime;
	private boolean started;
	private Date doneTime;
	private boolean done;
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + waitTime + ": " + message + "]";
	}

	public AlertAction(String message, Time waitTime, Alarms alarms) {
		this.message = message;
		this.alarms = alarms;
		this.waitTime = waitTime;
	}

	@Override
	public void execute() {
		started = true;
		doneTime = new Date(System.currentTimeMillis() + waitTime.toMillis());
		long timeToWait;
		while ((timeToWait = calculateTimeToWait()) > 0) {
			try {
				LoggerFactory.getLogger(getClass()).debug("Wait " + Time.format(timeToWait) + " before " + message);
				Thread.sleep(Math.min(timeToWait, TimeUnit.SECONDS.toMillis(30)));
			} catch (InterruptedException e) {
				return;
			}
		}
		try {
			alarms.fireAlarm(new Alarm(message, Type.NO_INPUT, new RepeatAlarmSound()));
		} catch (InterruptedException e) {
			logger.debug("Interruped.");
		}
		done = true;
	}

	private long calculateTimeToWait() {
		return doneTime.getTime() - System.currentTimeMillis();
	}

	@Override
	public String displayString() {
		return message;
	}

	@Override
	public Time calculateTimeLeft() {
		if (done)
			return Time.ZERO;
		if (started)
			return Time.fromMillis(calculateTimeToWait());
		return this.waitTime;
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
