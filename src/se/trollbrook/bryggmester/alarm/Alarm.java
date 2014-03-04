package se.trollbrook.bryggmester.alarm;

/**
 * @author jorgen.smas@entercash.com
 */
public class Alarm {

	public enum Type {
		NO_INPUT {
			@Override
			public boolean requireUserAction() {
				return false;
			}
		},

		WAIT_FOR_USER_INPUT {
			@Override
			public boolean requireUserAction() {
				return true;
			}
		};

		public boolean requireUserAction() {
			throw new AbstractMethodError();
		}

	};

	private String message;
	private Type type;

	public Alarm(String text, Type type) {
		this.message = text;
		this.setType(type);
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getClass().getSimpleName());
		builder.append("[type=");
		builder.append(type);
		builder.append(", message=");
		builder.append(message);
		builder.append("]");
		return builder.toString();
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}
}
