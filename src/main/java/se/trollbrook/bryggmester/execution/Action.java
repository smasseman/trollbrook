package se.trollbrook.bryggmester.execution;

import se.trollbrook.util.Time;

/**
 * @author jorgen.smas@entercash.com
 */
public interface Action {

	public void execute();

	public String displayString();

	public Time calculateTimeLeft();

	public boolean requireUserActionAtStart();

	public boolean requireUserActionAtEnd();
}
