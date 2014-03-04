package se.trollbrook.bryggmester.alarm;

/**
 * @author jorgen.smas@entercash.com
 */
public class ActiveAlarm {

	private Long id;
	private Alarm alarm;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Alarm getAlarm() {
		return alarm;
	}

	public void setAlarm(Alarm alarm) {
		this.alarm = alarm;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getClass().getSimpleName());
		builder.append("[id=");
		builder.append(id);
		builder.append(", alarm=");
		builder.append(alarm);
		builder.append("]");
		return builder.toString();
	}
}
