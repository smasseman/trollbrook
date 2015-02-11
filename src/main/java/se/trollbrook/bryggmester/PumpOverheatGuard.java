package se.trollbrook.bryggmester;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author jorgen.smas@entercash.com
 */
@Service("pumpoverheatguard")
public class PumpOverheatGuard {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Resource(name = "pump")
	Pump pump;

	@Resource
	TemperatureSensor tempSensor;

	Temperature limit = new Temperature(90);
	private boolean overheated = false;

	@PostConstruct
	public void init() {
		tempSensor.addListener(new TemperatureListener() {

			@Override
			public void eventNotification(Temperature event) {
				if (event.greaterThen(limit)) {
					if (!overheated) {
						logger.info("Temperature is to hot. Current {}. Limit {}.", event, limit);
						overheated = true;
						pump.setOverheated();
					}
				} else {
					if (overheated) {
						logger.info("Temperature is low. Current {}. Limit {}.", event, limit);
						overheated = false;
						pump.setTempOk();
					}
				}
			}
		});
	}
}
