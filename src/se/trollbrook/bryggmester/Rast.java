package se.trollbrook.bryggmester;

import se.trollbrook.util.Time;

/**
 * @author jorgen.smas@entercash.com
 */
public class Rast {

	private Temperature temperature;
	private Time duration;

	public Rast(Temperature tempereature, Time duration) {
		super();
		this.temperature = tempereature;
		this.duration = duration;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getClass().getSimpleName());
		builder.append("[tempereature=");
		builder.append(temperature);
		builder.append(", duration=");
		builder.append(getDuration());
		builder.append("]");
		return builder.toString();
	}

	public Time getDuration() {
		return duration;
	}

	public void setDuration(Time duration) {
		this.duration = duration;
	}

	public Temperature getTemperature() {
		return temperature;
	}

	public void setTemperature(Temperature temperature) {
		this.temperature = temperature;
	}

}
