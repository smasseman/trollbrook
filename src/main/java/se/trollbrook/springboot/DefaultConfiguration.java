package se.trollbrook.springboot;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;

import se.trollbrook.bryggmester.Relay;

/**
 * @author jorgen.smas@entercash.com
 */
@Configuration
@Profile("default")
public class DefaultConfiguration implements BrewerConfig {

	@Override
	@Bean
	public Relay heatrelay() {
		return new Relay("Heat");
	}

	@Override
	@Bean
	public Relay extrarelay() {
		return new Relay("Extra");
	}

	@Override
	@Bean
	public Relay beeperpin() {
		return new Relay("Beep");
	}

	@Override
	@Bean
	public Relay pumppin() {
		return new Relay("Pump");
	}

	@Bean
	public ConfigurableEnvironment apa() {
		return new StandardEnvironment();
	}
}
