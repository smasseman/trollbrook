package se.trollbrook.util.event;

/**
 * @author jorgen.smas@entercash.com
 */
public interface Listener<E> {

	void eventNotification(E event);
}
