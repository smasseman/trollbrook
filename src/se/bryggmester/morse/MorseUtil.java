package se.bryggmester.morse;

import static se.bryggmester.morse.Output.Sound.BEEP;
import static se.bryggmester.morse.Output.Sound.SILENCE;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author jorgen.smas@entercash.com
 */
public class MorseUtil {

	public static List<MorseWord> parse(String text) {
		String[] wordStrings = text.split(" ");
		List<MorseWord> words = new ArrayList<MorseWord>(wordStrings.length);
		for (String wordString : wordStrings) {
			MorseWord word = MorseWord.parse(wordString);
			words.add(word);
		}
		return words;
	}

	private static long shortUnits = 1L;
	private static long longUnits = 3L;
	private static long interElementGapUnits = 1L;
	private static long betweenLettersGapUnits = 3L;
	private static long betweenWordsGapUnits = 7L;

	public static List<Output> toSignals(List<MorseWord> words,
			long unitDuration) {
		LinkedList<Output> output = new LinkedList<>();
		addOutputForWords(words, output);
		multiplySignalLength(unitDuration, output);
		return output;
	}

	private static void addOutputForWords(List<MorseWord> words,
			LinkedList<Output> output) {
		Iterator<MorseWord> wordIter = words.iterator();
		while (wordIter.hasNext()) {
			MorseWord word = wordIter.next();
			addOutputForWord(output, word);
			if (wordIter.hasNext()) {
				output.add(new Output(SILENCE, betweenWordsGapUnits));
			}
		}
	}

	private static void addOutputForWord(LinkedList<Output> output,
			MorseWord word) {
		Iterator<MorseChar> charIter = word.getChars().iterator();
		while (charIter.hasNext()) {
			MorseChar c = charIter.next();
			addOutputForChar(output, c);
			if (charIter.hasNext()) {
				output.add(new Output(SILENCE, betweenLettersGapUnits));
			}
		}
	}

	private static void addOutputForChar(LinkedList<Output> output, MorseChar c) {
		Iterator<Signal> signalIter = c.getSignals().iterator();
		while (signalIter.hasNext()) {
			Signal signal = signalIter.next();
			if (signal == Signal.SHORT) {
				output.add(new Output(BEEP, shortUnits));
			} else {
				output.add(new Output(BEEP, longUnits));
			}
			if (signalIter.hasNext()) {
				output.add(new Output(SILENCE, interElementGapUnits));
			}
		}
	}

	private static void multiplySignalLength(long requestedUnitDuration,
			LinkedList<Output> output) {
		for (Output o : output) {
			o.setDuration(o.getDuration() * requestedUnitDuration);
		}
	}

	public static void main(String[] args) {
		String text = "paris";
		int sum = 0;
		List<MorseWord> words = parse(text);
		List<Output> signals = toSignals(words, 1);
		for (Output o : signals) {
			sum += o.getDuration();
		}
		System.out.println(sum);
	}

	public static List<Output> toSignals(String text, long unitDuration) {
		return toSignals(parse(text), unitDuration);
	}
}
