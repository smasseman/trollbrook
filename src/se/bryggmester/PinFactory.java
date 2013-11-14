package se.bryggmester;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;

import se.bryggmester.pi4j.GpioProviderSimulator;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

/**
 * @author jorgen.smas@entercash.com
 */
public class PinFactory implements FactoryBean<GpioPinDigitalOutput> {

	private Logger logger = LoggerFactory.getLogger(getClass());
	private String pin;
	@Resource
	private boolean simulatePins;
	private GpioController gpio;
	private GpioPinDigitalOutput gpioPin;

	@Override
	public GpioPinDigitalOutput getObject() throws Exception {
		if (simulatePins)
			GpioFactory.setDefaultProvider(new GpioProviderSimulator());
		this.gpio = GpioFactory.getInstance();
		Pin p = (Pin) RaspiPin.class.getField(pin).get(null);
		this.gpioPin = gpio.provisionDigitalOutputPin(p, null, PinState.LOW);
		logger.debug("Created pin " + pin);
		return gpioPin;
	}

	@PreDestroy
	public void destroy() {
		try {
			gpioPin.low();
			gpio.unexport(gpioPin);
			logger.debug("Destroyed pin " + pin);
		} catch (Exception e) {
			logger.info("Failed to destroy pin " + pin, e);
		}
	}

	@Override
	public Class<?> getObjectType() {
		return GpioPinDigitalOutput.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	public String getPin() {
		return pin;
	}

	public void setPin(String pin) {
		this.pin = pin;
	}
}
