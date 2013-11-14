package se.bryggmester.morse;

import java.util.LinkedList;
import java.util.List;

/**
 * @author jorgen.smas@entercash.com
 */
public class MorseWord {

	private List<MorseChar> chars = new LinkedList<MorseChar>();

	public List<MorseChar> getChars() {
		return chars;
	}

	public static MorseWord parse(String wordString) {
		MorseWord w = new MorseWord();
		for (char c : wordString.toCharArray()) {
			w.getChars().add(MorseChar.create(c));
		}
		return w;
	}
}
