package se.trollbrook.springboot;

import java.io.File;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;

import se.trollbrook.bryggmester.Relay;
import se.trollbrook.bryggmester.RpiRelay;

/**
 * @author jorgen.smas@entercash.com
 */
@Configuration
@Profile("pi")
public class RpiConfiguration implements BrewerConfig {

	@Override
	@Bean
	public String ctxRoot() {
		return "";
	}

	@Override
	@Bean
	public Relay heatrelay() throws Exception {
		return new RpiRelay("Heat", "00");
	}

	@Override
	@Bean
	public Relay extrarelay() throws Exception {
		return new RpiRelay("Extra", "04");
	}

	@Override
	@Bean
	public Relay beeperpin() throws Exception {
		return new RpiRelay("Beep", "03");
	}

	@Override
	@Bean
	public Relay pumprelay() throws Exception {
		return new RpiRelay("Pump", "01");
	}

	@Override
	@Bean
	public File temperatureFile() {
		return new File("/sys/bus/w1/devices/28-000004e48339/w1_slave").getAbsoluteFile();
	}

	@Override
	@Bean
	public File databasedirectory() {
		return new File("/var/trollbrook/bryggmester/recipes").getAbsoluteFile();
	}

	@Override
	@Bean
	public File historydirectory() {
		return new File("/var/trollbrook/bryggmester/history").getAbsoluteFile();
	}

	@Bean
	public ConfigurableEnvironment apa() {
		return new StandardEnvironment();
	}
}
