package se.trollbrook.bryggmester;

public class SystemTime {

	private static volatile long added = 0;

	public static synchronized void sleep(long millis) throws InterruptedException {
		long waitTo = currentTimeMillis() + millis;
		while (currentTimeMillis() < waitTo) {
			SystemTime.class.wait(millis);
		}
	}

	public static synchronized void addMillisToCurrentTime(long millis) {
		added += millis;
		SystemTime.class.notifyAll();
	}

	public static synchronized long currentTimeMillis() {
		return System.currentTimeMillis() + added;
	}
}
