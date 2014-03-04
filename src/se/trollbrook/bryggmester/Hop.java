package se.trollbrook.bryggmester;

import se.trollbrook.util.Time;

/**
 * @author jorgen.smas@entercash.com
 */
public class Hop {

	private String text;
	private Time time;

	public Hop(Time time, String text) {
		super();
		this.text = text;
		this.time = time;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Time getTime() {
		return time;
	}

	public void setTime(Time time) {
		this.time = time;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getClass().getSimpleName());
		builder.append("[text=");
		builder.append(text);
		builder.append(", time=");
		builder.append(time);
		builder.append("]");
		return builder.toString();
	}

}
