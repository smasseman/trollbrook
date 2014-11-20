package se.trollbrook.bryggmester.execution;

import se.trollbrook.util.Time;

/**
 * @author jorgen.smas@entercash.com
 */
public class DoneAction implements Action {

	@Override
	public void execute() {
	}

	@Override
	public String displayString() {
		return "Klart.";
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

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
