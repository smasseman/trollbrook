package se.trollbrook.bryggmester;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import se.trollbrook.bryggmester.Relay.RelayListener;
import se.trollbrook.bryggmester.Relay.RelayState;

/**
 * @author jorgen.smas@entercash.com
 */
public class RelayTest {

	@Test
	public void testInitialState() {
		Relay r = new Relay("test");
		Assert.assertNull(r.getCurrentState());
	}

	@Test
	public void testGetCurrentState() {
		Relay r = new Relay("test");
		r.setState(RelayState.ON);
		Assert.assertEquals(RelayState.ON, r.getCurrentState());
		r.setState(RelayState.OFF);
		Assert.assertEquals(RelayState.OFF, r.getCurrentState());
	}

	@Test(expected = java.lang.IllegalArgumentException.class)
	public void testSetNullState() {
		Relay r = new Relay("test");
		r.setState(null);
	}

	@Test(expected = java.lang.IllegalArgumentException.class)
	public void testAddNullListener() {
		Relay r = new Relay("test");
		r.addListener(null);
	}

	@Test
	public void testListenerNotifications() {
		Relay r = new Relay("test");
		RelayListener listener = Mockito.mock(RelayListener.class);
		r.addListener(listener);

		r.setState(RelayState.ON);
		Mockito.verify(listener, Mockito.times(1)).eventNotification(
				RelayState.ON);

		r.setState(RelayState.ON);
		Mockito.verify(listener, Mockito.times(1)).eventNotification(
				RelayState.ON);

		r.setState(RelayState.OFF);
		Mockito.verify(listener, Mockito.times(1)).eventNotification(
				RelayState.OFF);

		r.setState(RelayState.ON);
		Mockito.verify(listener, Mockito.times(2)).eventNotification(
				RelayState.ON);
	}

}
