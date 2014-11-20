package se.trollbrook.bryggmester;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

import se.trollbrook.util.Time;
import se.trollbrook.util.Weight;
import se.trollbrook.util.Weight.Unit;

/**
 * @author jorgen.smas@entercash.com
 */
public class RecipeMockFactory {

	public Recipe create() {
		Recipe r = new Recipe();
		r.setBoilDuration(Time.parse("60m"));
		List<Hop> hops = new LinkedList<>();
		hops.add(new Hop(Time.parse("5m"), "Hop 3", new Weight(new BigDecimal(5), Unit.GRAM)));
		hops.add(new Hop(Time.parse("25m"), "Hop 2", new Weight(new BigDecimal(25), Unit.GRAM)));
		hops.add(new Hop(Time.parse("50m"), "Hop 1", new Weight(new BigDecimal(50), Unit.GRAM)));
		r.setHops(hops);
		r.setId(0L);
		r.setName("Test Recipe");
		List<Rast> rasts = new LinkedList<>();
		rasts.add(new Rast(new Temperature(70), Time.parse("30m")));
		rasts.add(new Rast(new Temperature(75), Time.parse("10m")));
		r.setRasts(rasts);
		r.setStartTemperature(new Temperature(63));
		return r;
	}

}
