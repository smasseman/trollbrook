package se.bryggmester;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

/**
 * @author jorgen.smas@entercash.com
 */
public class Gpio {

	public static void main(String[] args) throws Exception {
		String pinNumber = args[0];
		String pinName = "GPIO_" + pinNumber;
		GpioController gpio = GpioFactory.getInstance();
		try {
			Pin p = (Pin) RaspiPin.class.getField(pinName).get(null);
			GpioPinDigitalOutput pin = gpio.provisionDigitalOutputPin(p, null,
					PinState.LOW);
			pin.high();
			Thread.sleep(1000);
			pin.low();
		} finally {
			try {
				gpio.shutdown();
			} catch (Exception ignored) {
			}
		}
	}
}
