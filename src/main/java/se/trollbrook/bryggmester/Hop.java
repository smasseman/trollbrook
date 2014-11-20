package se.trollbrook.bryggmester;

import se.trollbrook.util.Time;
import se.trollbrook.util.Weight;

/**
 * @author jorgen.smas@entercash.com
 */
public class Hop {

	private String text;
	private Time time;
	private Weight weight;

	public Hop(Time time, String text, Weight weight) {
		super();
		this.text = text;
		this.time = time;
		this.setWeight(weight);
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

	public Weight getWeight() {
		return weight;
	}

	public void setWeight(Weight weight) {
		this.weight = weight;
	}

}
