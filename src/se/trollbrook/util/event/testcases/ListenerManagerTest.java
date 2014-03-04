package se.trollbrook.util.event.testcases;

import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import se.trollbrook.util.event.Listener;
import se.trollbrook.util.event.ListenerManager;

/**
 * @author jorgen.smas@entercash.com
 */
public class ListenerManagerTest {

	@Test
	public void test() {
		ListenerManager<String> m = new ListenerManager<String>();
		final List<String> events = new LinkedList<String>();
		TestListener listener = new TestListener() {

			@Override
			public void eventNotification(String event) {
				events.add(event);
			}
		};
		m.addListener(listener);
		m.notifyListeners("1");
		Assert.assertEquals(1, events.size());
	}

	public interface TestListener extends Listener<String> {

	}
}
