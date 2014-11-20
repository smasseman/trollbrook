package se.trollbrook.bryggmester.alarm;

import java.util.Iterator;

/**
 * @author jorgen.smas@entercash.com
 */
public interface Sound {

	public abstract Iterator<Beep> iterator();

	public abstract void stop();

}
