package se.trollbrook.bryggmester;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import se.trollbrook.bryggmester.AbstractTemperatureSensor;
import se.trollbrook.bryggmester.Temperature;

/**
 * @author jorgen.smas@entercash.com
 */
@Profile("default")
@Service
public class TemperatureSensorMock extends AbstractTemperatureSensor {

	@Override
	public void setCurrentTemp(Temperature temp) {
		super.setCurrentTemp(temp);
	}
}
