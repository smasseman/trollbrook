package se.trollbrook.bryggmester;

import java.util.Comparator;

/**
 * @author jorgen.smas@entercash.com
 */
public class RastComparator implements Comparator<Rast> {

	@Override
	public int compare(Rast o1, Rast o2) {
		return o1.getTemperature().getValue().compareTo(o2.getTemperature().getValue());
	}
}
