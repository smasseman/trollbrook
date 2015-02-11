package se.trollbrook.springboot;

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
	public Relay pumppin() throws Exception {
		return new RpiRelay("Pump", "01");
	}

	@Bean
	public ConfigurableEnvironment apa() {
		return new StandardEnvironment();
	}
}
