package se.bryggmester.web;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

/**
 * @author jorgen.smas@entercash.com
 */
@Controller
public class PinController {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@RequestMapping("/gpio.html")
	public String gpio(Model model) throws Exception {
		List<GpioPin> exportedPins = new LinkedList<>();
		model.addAttribute("exported", exportedPins);
		Map<String, Pin> unexportedPins = getPins();
		model.addAttribute("unexported", unexportedPins);

		Collection<GpioPin> pins = GpioFactory.getInstance()
				.getProvisionedPins();
		for (GpioPin p : pins) {
			if (p.isExported()) {
				exportedPins.add(p);
				unexportedPins.remove(p.getPin().getName());
			}
			logPin(p);
		}
		return "gpio";
	}

	@RequestMapping("/togglegpio.html")
	public String toggle(Model model,
			@RequestParam(value = "pin") String pinName) throws Exception {
		Collection<GpioPin> pins = GpioFactory.getInstance()
				.getProvisionedPins();
		for (GpioPin gp : pins) {
			if (gp.getPin().getName().equals(pinName))
				((GpioPinDigitalOutput) gp).toggle();
		}
		return gpio(model);
	}

	@RequestMapping("/export.html")
	public String export(Model model,
			@RequestParam(value = "pin") String pinName) throws Exception {
		Pin p = getPin(pinName);
		GpioFactory.getInstance().provisionDigitalOutputPin(p, null,
				PinState.LOW);
		return gpio(model);
	}

	private Pin getPin(String pinName) throws Exception {
		return getPins().get(pinName);
	}

	private Map<String, Pin> getPins() throws Exception {
		LinkedHashMap<String, Pin> pins = new LinkedHashMap<>();
		for (Field f : RaspiPin.class.getDeclaredFields()) {
			if (f.getType().isAssignableFrom(Pin.class)) {
				Pin p = (Pin) f.get(null);
				pins.put(p.getName(), p);
			}
		}
		return pins;
	}

	private void logPin(GpioPin p) {
		StringBuilder s = new StringBuilder();
		s.append("name=").append(p.getName());
		s.append(" exported=").append(p.isExported());
		s.append(" mode=").append(p.getMode());
		s.append(" pin=").append(p.getPin());
		logger.debug(s.toString());
	}
}
