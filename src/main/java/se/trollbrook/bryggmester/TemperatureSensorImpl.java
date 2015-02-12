package se.trollbrook.bryggmester;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * @author jorgen.smas@entercash.com
 */
@Service
@Profile("pi")
public class TemperatureSensorImpl extends AbstractTemperatureSensor {

	@Value("${temperatureFile}")
	private File file;
	private Thread thread;
	private long updateInterval = 3;

	@PostConstruct
	public void start() {
		thread = new Thread() {

			@Override
			public void run() {
				try {
					while (!Thread.interrupted()) {
						try {
							update();
						} catch (IOException e) {
							logger.debug("Failed to update temperature: " + e);
						}
						Thread.sleep(updateInterval * 1000L);
					}
				} catch (InterruptedException e) {
				} catch (Throwable t) {
					logger.warn("Failure in temp updater.", t);
				} finally {
					logger.info("Temp upater is dead.");
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

	private void update() throws IOException {
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
			Temperature temp = new Temperature(value);
			setCurrentTemp(temp);
		}
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

}
