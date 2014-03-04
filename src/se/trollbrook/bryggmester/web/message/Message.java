package se.trollbrook.bryggmester.web.message;

/**
 * @author jorgen.smas@entercash.com
 */
public class Message {

	private MessageType type;
	private String text;

	public Message(MessageType type, String text) {
		super();
		this.type = type;
		this.text = text;
	}

	public MessageType getType() {
		return type;
	}

	public String getText() {
		return text;
	}

	@Override
	public String toString() {
		return type + ":" + text;
	}
}
