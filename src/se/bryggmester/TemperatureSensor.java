package se.bryggmester;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import se.bryggmester.Temperature.Scale;

/**
 * @author jorgen.smas@entercash.com
 */
@Service
public class TemperatureSensor {

	public interface Listener {
		void notifyTemperatureChanged(Temperature temp);
	}

	private Logger logger = LoggerFactory.getLogger(getClass());
	@Resource(name = "temperatureFile")
	private File file;
	private Thread thread;
	private long updateInterval = 10;
	private Temperature currentTemp;
	private List<Listener> listeners = new LinkedList<>();

	public void addListener(Listener l) {
		listeners.add(l);
		if (currentTemp != null)
			l.notifyTemperatureChanged(currentTemp);
	}

	@PostConstruct
	public void start() {
		thread = new Thread() {

			@Override
			public void run() {
				try {
					while (!Thread.interrupted()) {
						try {
							getTemperature();
						} catch (IOException e) {
							logger.debug("Failed to update temperature: " + e);
						}
						Thread.sleep(updateInterval * 1000L);
					}
				} catch (InterruptedException e) {
				} catch (Throwable t) {
					logger.warn("Temp updater terminated.", t);
				}
			}
		};
		thread.start();
	}

	@PreDestroy
	public void stop() {
		if (thread != null) {
			thread.interrupt();
		}
	}

	public Temperature getTemperature() throws IOException {
		FileReader reader = new FileReader(getFile());
		try (BufferedReader br = new BufferedReader(reader)) {
			String firstLine = br.readLine();
			if (firstLine == null)
				throw new IOException("No first line.");
			if (!firstLine.endsWith("YES"))
				throw new IOException("First line ended with " + firstLine);
			String secondLine = br.readLine();
			if (secondLine == null)
				throw new IOException("No second line.");
			Pattern p = Pattern.compile(".*t.([0-9]*)");
			Matcher m = p.matcher(secondLine);
			m.matches();
			String valueString = m.group(1);
			BigDecimal value = new BigDecimal(valueString);
			value = value.divide(new BigDecimal(1000), 1, RoundingMode.HALF_UP);
			Temperature temp = new Temperature(value.floatValue(),
					Scale.CELCIUS);
			setCurrentTemp(temp);
			return temp;
		}
	}

	protected void setCurrentTemp(Temperature temp) {
		if (!temp.equals(currentTemp)) {
			currentTemp = temp;
			logger.info("Temp " + temp.toString());
			notifyListeners(temp);
		}
	}

	private void notifyListeners(Temperature temp) {
		for (Listener l : listeners) {
			l.notifyTemperatureChanged(temp);
		}
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public static void main(String[] args) throws IOException {
		TemperatureSensor s = new TemperatureSensor();
		s.setFile(new File("/sys/bus/w1/devices/28-000003cced16/w1_slave"));
		System.out.println(s.getTemperature());
	}

	public Temperature getCurrentTemperature() {
		return currentTemp;
	}
}
