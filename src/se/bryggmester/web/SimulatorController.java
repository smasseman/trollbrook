package se.bryggmester.web;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import se.bryggmester.HeatController;
import se.bryggmester.Temperature;
import se.bryggmester.Temperature.Scale;
import se.bryggmester.TemperatureSensor;

import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;

/**
 * @author jorgen.smas@entercash.com
 */
@Controller
public class SimulatorController {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Resource(name = "pumppin")
	private GpioPinDigitalOutput pump;
	@Resource(name = "heatpin")
	private GpioPinDigitalOutput heat;
	@Resource
	private TemperatureSensor tempSensor;
	@Resource
	private HeatController heatController;
	private Thread changeTempThread;

	private Map<String, String> currentState;

	@RequestMapping("/simulator.html")
	public void html(Model model, HttpServletRequest request) throws Exception {
		updateState();
		logger.debug("Adding current state " + currentState);
		model.addAttribute("currentState", currentState);
	}

	@PostConstruct
	public void init() throws IOException {
		logger.debug("Init.");
		writeTemp(new Temperature(15, Scale.CELCIUS));
		changeTempThread = new Thread() {
			@Override
			public void run() {

				try {
					while (!interrupted()) {
						Thread.sleep(1000);
						Temperature current;
						try {
							current = tempSensor.getTemperature();
						} catch (RuntimeException e) {
							logger.info("Failed: " + e);
							current = new Temperature(15, Scale.CELCIUS);
						}

						Temperature newTemp = current;
						if (heat.getState() == PinState.HIGH) {
							// What?! Is not 100 C the max value?
							// The temperatur sensor might show more then actual
							// value so we simulate that extreme case to make
							// sure nothing breaks caused of a bad temp sensor.
							if (current.getValue() >= 103) {
								newTemp = current.add(-1);
							} else if (current.getValue() >= 100) {
								newTemp = current
										.add(new Random().nextInt(3) - 1);
							} else {
								newTemp = current.add(1);
							}
						} else if (current.getValue() > 20) {
							newTemp = current.add(-1);
						}
						writeTemp(newTemp);
					}
				} catch (InterruptedException e) {
					logger.debug("temp updater down: " + e);
				} catch (Throwable t) {
					logger.error("Temp updater failed.", t);
				}
			}
		};
		changeTempThread.start();
		updateState();
	}

	private void writeTemp(Temperature temperature) throws IOException {
		File file = new File("/tmp/temp.txt");
		String t = String.valueOf((int) (temperature.getValue() * 1000));
		try (FileWriter w = new FileWriter(file)) {
			w.write("7e 01 4b 46 7f ff 02 10 25 : crc=25 YES\n");
			w.write("7e 01 4b 46 7f ff 02 10 25 t=" + t + "\n");
		}
	}

	@PreDestroy
	public void destroy() {
		changeTempThread.interrupt();
	}

	private void updateState() {
		Map<String, String> map = new LinkedHashMap<>();
		add(map, "Pump", pump.getState());
		add(map, "Heat", heat.getState());
		add(map, "Temp", tempSensor.getCurrentTemperature());
		add(map, "Wanted", heatController.getWantedTemp());
		this.currentState = map;
	}

	private void add(Map<String, String> map, String key, Object obj) {
		map.put(key, String.valueOf(obj));
		logger.debug(key + " " + obj);
	}
}
