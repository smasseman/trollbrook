package se.trollbrook.util.event;

import java.util.LinkedList;

public class SimpleListener<E> implements Listener<E> {

	private LinkedList<E> events = new LinkedList<>();

	@Override
	public synchronized void eventNotification(E event) {
		events.add(event);
	}

	public E getLast() {
		return events.getLast();
	}
}
