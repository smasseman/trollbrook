package se.trollbrook.bryggmester.web;

import java.math.BigDecimal;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import se.trollbrook.bryggmester.SystemTime;
import se.trollbrook.bryggmester.Temperature;
import se.trollbrook.bryggmester.TemperatureSensorMock;
import se.trollbrook.util.Time;

@RestController
public class SimulatorController {

	private Logger logger = LoggerFactory.getLogger(getClass());
	@Resource
	TemperatureSensorMock tempSensor;

	@RequestMapping(value = "/addtime", method = RequestMethod.POST)
	public String addTime(@RequestParam String time) {
		try {
			Time timeToAdd = Time.parse(time);
			SystemTime.addMillisToCurrentTime(timeToAdd.toMillis());
			return "OK";
		} catch (Exception e) {
			logger.info("Failed to add time.", e);
			return e.toString();
		}
	}

	@RequestMapping(value = "/temp", method = RequestMethod.POST)
	public String setTemp(@RequestParam String temp) {
		try {
			tempSensor.setCurrentTemp(new Temperature(new BigDecimal(temp)));
			logger.debug("Temp is set to " + temp);
			return "OK";
		} catch (Exception e) {
			logger.info("Failed to set temperature.", e);
			return e.toString();
		}
	}
}
