package se.bryggmester.morse;

import static se.bryggmester.morse.Signal.LONG;
import static se.bryggmester.morse.Signal.SHORT;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jorgen.smas@entercash.com
 */
public class MorseChar {

	private char character;
	private List<Signal> signals;

	private static Map<Character, MorseChar> chars = new HashMap<>();

	private static void add(Character c, Signal... signals) {
		chars.put(c, new MorseChar(c, signals));
	}

	static {
		add('A', SHORT, LONG);
		add('B', LONG, SHORT, SHORT, SHORT);
		add('C', LONG, SHORT, LONG, SHORT);
		add('D', LONG, SHORT, SHORT);
		add('E', SHORT);
		add('F', SHORT, SHORT, LONG, SHORT);
		add('G', LONG, LONG, SHORT);
		add('H', SHORT, SHORT, SHORT, SHORT);
		add('I', SHORT, SHORT);
		add('J', SHORT, LONG, LONG, LONG);
		add('K', LONG, SHORT, LONG);
		add('L', SHORT, LONG, SHORT, SHORT);
		add('M', LONG, LONG);
		add('N', LONG, SHORT);
		add('O', LONG, LONG, LONG);
		add('P', SHORT, LONG, LONG, SHORT);
		add('Q', LONG, LONG, SHORT, LONG);
		add('R', SHORT, LONG, SHORT);
		add('S', SHORT, SHORT, SHORT);
		add('T', LONG);
		add('U', SHORT, SHORT, LONG);
		add('V', SHORT, SHORT, SHORT, LONG);
		add('W', SHORT, LONG, LONG);
		add('X', LONG, SHORT, SHORT, LONG);
		add('Y', LONG, SHORT, LONG, LONG);
		add('Z', LONG, LONG, SHORT, SHORT);
	}

	private MorseChar(char character, Signal... signals) {
		super();
		this.character = character;
		this.signals = Arrays.asList(signals);
	}

	public char getCharacter() {
		return character;
	}

	public List<Signal> getSignals() {
		return signals;
	}

	@Override
	public String toString() {
		return String.valueOf(character);
	}

	public static MorseChar create(char c) {
		char cu = Character.toUpperCase(c);
		MorseChar mc = chars.get(cu);
		if (mc != null)
			return mc;
		throw new IllegalArgumentException(c
				+ " is not a valid morse character.");
	}

	public String toLongString() {
		StringBuilder s = new StringBuilder();
		s.append(character);
		s.append(' ');
		for (Signal signal : signals) {
			s.append(signal);
		}
		return s.toString();
	}
}
