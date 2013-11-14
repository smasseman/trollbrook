package se.bryggmester.web;

/**
 * @author jorgen.smas@entercash.com
 */
public class Message {

	private String text;

	public Message(String t) {
		this.text = t;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Override
	public String toString() {
		return text;
	}
}
