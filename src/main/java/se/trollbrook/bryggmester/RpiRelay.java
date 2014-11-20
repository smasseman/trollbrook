package se.trollbrook.bryggmester;

import javax.annotation.PreDestroy;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

/**
 * @author jorgen.smas@entercash.com
 */
public class RpiRelay extends Relay {

	private GpioPinDigitalOutput pin;

	public RpiRelay(String name, String pinNumber) throws Exception {
		super(name);
		String pinName = "GPIO_" + pinNumber;
		GpioController gpio = GpioFactory.getInstance();
		Pin p = (Pin) RaspiPin.class.getField(pinName).get(null);
		pin = gpio.provisionDigitalOutputPin(p, null, PinState.LOW);
	}

	@PreDestroy
	public void destroy() {
		pin.low();
		pin.unexport();
	}

	@Override
	protected void changeState(RelayState state) {
		if (state == RelayState.OFF) {
			pin.low();
		} else {
			pin.high();
		}
		super.changeState(state);
	}
}
