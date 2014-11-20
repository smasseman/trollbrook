package se.trollbrook.bryggmester.execution;

import java.util.concurrent.atomic.AtomicBoolean;

import se.trollbrook.bryggmester.Temperature;
import se.trollbrook.bryggmester.TemperatureListener;
import se.trollbrook.bryggmester.TemperatureSensor;
import se.trollbrook.util.Matcher;

/**
 * @author jorgen.smas@entercash.com
 */
public class WaitForTemperature {

	public static void waitFor(TemperatureSensor s, final Temperature desired) throws InterruptedException {
		final Matcher<Temperature> m = new Matcher<Temperature>() {

			@Override
			public boolean matches(Temperature current) {
				return current.greaterThen(desired);
			}
		};
		waitForMatch(s, m);
	}

	public static void waitForBelow(TemperatureSensor s, final Temperature desired) throws InterruptedException {
		final Matcher<Temperature> m = new Matcher<Temperature>() {

			@Override
			public boolean matches(Temperature current) {
				return !current.greaterThen(desired);
			}
		};
		waitForMatch(s, m);
	}

	private static void waitForMatch(TemperatureSensor s, final Matcher<Temperature> m) throws InterruptedException {
		final AtomicBoolean reached = new AtomicBoolean();
		TemperatureListener l = new TemperatureListener() {

			@Override
			public void eventNotification(Temperature current) {
				if (m.matches(current)) {
					synchronized (reached) {
						reached.set(true);
						reached.notifyAll();
					}
				}
			}
		};
		s.addListener(l);
		try {
			synchronized (reached) {
				while (!reached.get() && !m.matches(s.getCurrentTemperature())) {
					reached.wait();
				}
			}
		} finally {
			s.removeListener(l);
		}
	}
}
