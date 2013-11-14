package se.bryggmester.util;

/**
 * @author jorgen.smas@entercash.com
 */
public class ObjectUtil {

	public static boolean equals(Object a, Object b) {
		if (a == null)
			return b == null;
		return a.equals(b);
	}
}
