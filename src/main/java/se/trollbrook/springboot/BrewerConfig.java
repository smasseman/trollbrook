package se.trollbrook.springboot;

import java.io.File;

import se.trollbrook.bryggmester.Relay;

/**
 * @author jorgen.smas@entercash.com
 */
public interface BrewerConfig {

	public Relay heatrelay() throws Exception;

	public Relay extrarelay() throws Exception;

	public Relay beeperpin() throws Exception;

	public Relay pumprelay() throws Exception;

	public File temperatureFile();

	public File databasedirectory();

	public File historydirectory();

	public String ctxRoot();
}
