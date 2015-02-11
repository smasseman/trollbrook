package se.trollbrook.springboot;

import se.trollbrook.bryggmester.Relay;

/**
 * @author jorgen.smas@entercash.com
 */
public interface BrewerConfig {

	public Relay heatrelay() throws Exception;

	public Relay extrarelay() throws Exception;

	public Relay beeperpin() throws Exception;

	public Relay pumppin() throws Exception;
}
