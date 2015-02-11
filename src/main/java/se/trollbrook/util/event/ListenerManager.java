package se.trollbrook.util.event;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import se.trollbrook.bryggmester.TemperatureListener;

/**
 * @author jorgen.smas@entercash.com
 */
public class ListenerManager<E> {

	private static Timer timer = new Timer();

	private List<Listener<E>> listeners = new LinkedList<>();
	private boolean asynchronous = true;

	public void setAsynchronous(boolean a) {
		asynchronous = a;
	}

	public synchronized void addListener(Listener<E> listener) {
		if (listener == null)
			throw new IllegalArgumentException("Listener may not be null.");
		listeners.add(listener);
	}

	public synchronized void notifyListeners(final E event) {
		final ArrayList<Listener<E>> copy = new ArrayList<>(listeners);

		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				for (Listener<E> l : copy) {
					l.eventNotification(event);
				}
			}
		};
		if (asynchronous)
			timer.schedule(task, 0);
		else
			task.run();
	}

	public synchronized void removeListener(TemperatureListener listener) {
		listeners.remove(listener);
	}

	public synchronized List<Listener<E>> getListeners() {
		return new ArrayList<>(listeners);
	}
}
