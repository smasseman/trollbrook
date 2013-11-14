package se.bryggmester.morse;

/**
 * @author jorgen.smas@entercash.com
 */
public enum Signal {

	LONG('-'), SHORT('.');

	private char character;

	Signal(char c) {
		character = c;
	}

	@Override
	public String toString() {
		return String.valueOf(character);
	}
}
