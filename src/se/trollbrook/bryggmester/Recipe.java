package se.trollbrook.bryggmester;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import se.trollbrook.util.Time;

/**
 * @author jorgen.smas@entercash.com
 */
public class Recipe {

	public static final Comparator<Recipe> NAME_COMPARATOR = new Comparator<Recipe>() {

		@Override
		public int compare(Recipe o1, Recipe o2) {
			return o1.name.compareTo(o2.name);
		}
	};
	private Long id;
	private String name;
	private Temperature startTemperature;
	private List<Rast> rasts = new LinkedList<>();
	private Time boilDuration;
	private List<Hop> hops = new LinkedList<>();

	@Override
	public String toString() {
		return id + ", " + name + ", " + startTemperature + ", " + rasts + ", " + boilDuration + ", " + hops;
	}

	public String toExtendedString() {
		StringBuilder s = new StringBuilder();
		s.append("Id: ").append(id).append("\n");
		s.append("Name: ").append(name).append("\n");
		s.append("Start temp: ").append(startTemperature.toString()).append("\n");
		for (Rast r : rasts) {
			s.append("Rast: ");
			s.append(r.getTemperature());
			s.append(" ");
			s.append(r.getDuration().toMinutes());
			s.append(" min \n");
		}
		for (Hop h : hops) {
			s.append("Hop: ");
			s.append(h.getText());
			s.append(" ");
			s.append(h.getTime().toMinutes());
			s.append(" min \n");
		}
		s.append("Boil duration: ").append(boilDuration.toMinutes()).append(" min");

		return s.toString();
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
