package se.trollbrook.bryggmester;

import java.util.List;

/**
 * @author jorgen.smas@entercash.com
 */
public class Recipe {

	private Temperature startTemperature;
	private List<Rast> rasts;
	private long boilDuration;
	private List<Hop> hops;

	public Temperature getStartTemperature() {
		return startTemperature;
	}

	public void setStartTemperature(Temperature startTemperature) {
		this.startTemperature = startTemperature;
	}

	public List<Rast> getRasts() {
		return rasts;
	}

	public void setRasts(List<Rast> rasts) {
		this.rasts = rasts;
	}

	public long getBoilDuration() {
		return boilDuration;
	}

	public void setBoilDuration(long boilDuration) {
		this.boilDuration = boilDuration;
	}

	public List<Hop> getHops() {
		return hops;
	}

	public void setHops(List<Hop> hops) {
		this.hops = hops;
	}

}
