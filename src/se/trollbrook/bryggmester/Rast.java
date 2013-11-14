package se.trollbrook.bryggmester;

/**
 * @author jorgen.smas@entercash.com
 */
public class Rast {

	private Temperature tempereature;
	private long duration;

	public Temperature getTempereature() {
		return tempereature;
	}

	public void setTempereature(Temperature tempereature) {
		this.tempereature = tempereature;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getClass().getSimpleName());
		builder.append("[tempereature=");
		builder.append(tempereature);
		builder.append(", duration=");
		builder.append(duration);
		builder.append("]");
		return builder.toString();
	}

}
