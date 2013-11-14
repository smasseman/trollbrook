package se.trollbrook.bryggmester;

/**
 * @author jorgen.smas@entercash.com
 */
public interface TemperatureSensor {

	public abstract void addListener(TemperatureListener listener);

	public abstract Temperature getCurrentTemperature();

}