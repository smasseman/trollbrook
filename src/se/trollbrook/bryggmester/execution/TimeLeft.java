package se.trollbrook.bryggmester.execution;

import se.trollbrook.util.Time;

/**
 * @author jorgen.smas@entercash.com
 */
public class TimeLeft {

	private Time start;
	private Time end;

	public TimeLeft(Time start, Time end) {
		super();
		this.start = start;
		this.end = end;
	}

	public Time getStart() {
		return start;
	}

	public Time getEnd() {
		return end;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getClass().getSimpleName());
		builder.append("[start=");
		builder.append(start);
		builder.append(", end=");
		builder.append(end);
		builder.append("]");
		return builder.toString();
	}

}
