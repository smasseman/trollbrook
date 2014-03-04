package se.trollbrook.bryggmester;

import java.util.Comparator;

/**
 * @author jorgen.smas@entercash.com
 */
public class HopComparator implements Comparator<Hop> {

	@Override
	public int compare(Hop o1, Hop o2) {
		return o2.getTime().compareTo(o1.getTime());
	}
}
