package se.trollbrook.bryggmester;

import java.util.LinkedList;
import java.util.List;

import se.trollbrook.util.Time;

/**
 * @author jorgen.smas@entercash.com
 */
public class Recipe {

	private Long id;
	private String name;
	private Temperature startTemperature;
	private List<Rast> rasts = new LinkedList<>();
	private Time boilDuration;
	private List<Hop> hops = new LinkedList<>();

	@Override
	public String toString() {
		return id + ", " + name + ", " + startTemperature + ", " + rasts + ", "
				+ boilDuration + ", " + hops;
	}

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

	public List<Hop> getHops() {
		return hops;
	}

	public void setHops(List<Hop> hops) {
		this.hops = hops;
	}

	public Time getBoilDuration() {
		return boilDuration;
	}

	public void setBoilDuration(Time boilDuration) {
		this.boilDuration = boilDuration;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

}
