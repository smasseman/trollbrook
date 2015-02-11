package se.trollbrook.bryggmester.execution;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Assert;
import org.junit.Test;

import se.trollbrook.bryggmester.Temperature;
import se.trollbrook.bryggmester.TemperatureSensorMock;

/**
 * @author jorgen.smas@entercash.com
 */
public class WaitForTemperatureTest {

	private AtomicBoolean b;

	@Test
	public void test() throws InterruptedException {
		final TemperatureSensorMock s = new TemperatureSensorMock();
		b = new AtomicBoolean();
		new Thread() {
			@Override
			public void run() {
				try {
					WaitForTemperature.waitFor(s, new Temperature(50));
				} catch (InterruptedException e) {
					System.out.println("Interrupted.");
				}
				b.set(true);
			}
		}.start();

		Thread.sleep(500);
		assertNotDone();

		s.setCurrentTemp(new Temperature(40));
		Thread.sleep(500);
		assertNotDone();

		s.setCurrentTemp(new Temperature(60));
		Thread.sleep(500);
		assertDone();
	}

	private void assertDone() {
		Assert.assertTrue(b.get());
	}

	private void assertNotDone() {
		Assert.assertFalse(b.get());
	}
}
