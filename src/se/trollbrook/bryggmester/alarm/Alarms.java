package se.trollbrook.bryggmester.alarm;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import se.trollbrook.util.event.Listener;
import se.trollbrook.util.event.ListenerManager;

/**
 * @author jorgen.smas@entercash.com
 */
@Component
public class Alarms {

	private Logger logger = LoggerFactory.getLogger(getClass());
	private Map<Long, ActiveAlarm> activeAlarms = new LinkedHashMap<>();
	private Long alarmId = System.currentTimeMillis();
	private ListenerManager<ActiveAlarm> lm = new ListenerManager<>();
	@Resource
	private SoundPlayer player;

	public interface AlarmListener extends Listener<ActiveAlarm> {
	}

	public synchronized void addListener(AlarmListener l) {
		lm.addListener(l);
	}

	public synchronized void fireAlarm(Alarm a) throws InterruptedException {
		ActiveAlarm ac = new ActiveAlarm();
		ac.setAlarm(a);
		ac.setId(alarmId++);
		activeAlarms.put(ac.getId(), ac);
		player.play(a.getSound());
		try {
			logger.debug("Activeated alarm with id {}: {}", ac.getId(), ac.getAlarm().getMessage());
			lm.notifyListeners(ac);
			logger.debug("Current alarms: " + this.activeAlarms);

			if (Alarm.Type.WAIT_FOR_USER_INPUT == a.getType()) {
				logger.debug("Activated alarm and wait for user action: " + a.getMessage());
				try {
					this.waitForAck(ac, null);
				} catch (InterruptedException e) {
					this.deactive(ac.getId());
					throw e;
				}
			} else {
				logger.debug("Activated alarm without wait for user action: " + a.getMessage());
			}
		} finally {
			a.getSound().stop();
		}
	}

	public synchronized void deactive(Long id) {
		ActiveAlarm d = activeAlarms.remove(id);
		if (d == null)
			logger.debug("No alarm with id {} to deactivate.");
		else
			logger.info("Deactivated {}", d.getAlarm().getMessage());
		this.notifyAll();
	}

	public synchronized void deactiveAll() {
		activeAlarms.clear();
		this.notifyAll();
	}

	public synchronized boolean waitForAck(ActiveAlarm alarm, Long timeout) throws InterruptedException {
		if (timeout == null) {
			while (activeAlarms.containsKey(alarm.getId())) {
				logger.debug("Waiting for ack for alarm " + alarm.getId() + " " + alarm.getAlarm().getMessage());
				this.wait(TimeUnit.SECONDS.toMillis(30));
			}
		} else {
			long maxTime = timeout + System.currentTimeMillis();
			while (System.currentTimeMillis() < maxTime && activeAlarms.containsKey(alarm.getId()))
				this.wait(1000);
		}
		return activeAlarms.containsKey(alarm.getId());
	}

	public synchronized List<ActiveAlarm> getActiveAlams() {
		List<ActiveAlarm> list = new ArrayList<>(activeAlarms.size());
		for (Map.Entry<Long, ActiveAlarm> e : activeAlarms.entrySet()) {
			list.add(e.getValue());
		}
		return list;
	}

	public SoundPlayer getPlayer() {
		return player;
	}

	public void setPlayer(SoundPlayer player) {
		this.player = player;
	}
}
