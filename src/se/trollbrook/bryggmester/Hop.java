package se.trollbrook.bryggmester;

/**
 * @author jorgen.smas@entercash.com
 */
public class Hop {

	private String text;
	private long time;

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getClass().getSimpleName());
		builder.append("[text=");
		builder.append(text);
		builder.append(", time=");
		builder.append(time);
		builder.append("]");
		return builder.toString();
	}

}
