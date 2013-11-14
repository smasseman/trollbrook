package se.bryggmester.instruction;

import java.util.regex.Pattern;

import se.bryggmester.PumpState;
import se.bryggmester.util.TimeUnit;

/**
 * @author jorgen.smas@entercash.com
 */
public class PumpParser extends InstructionParser<PumpInstruction> {

	@Override
	public PumpInstruction parse(String string) {
		if (string.contains("|")) {
			String[] slots = string.split(Pattern.quote("|"));
			String state = slots[0];
			long runInterval = TimeUnit.parse(slots[1]);
			long pausInterval = TimeUnit.parse(slots[2]);
			return new PumpInstruction(PumpState.valueOf(state), runInterval,
					pausInterval);
		} else {
			return new PumpInstruction(PumpState.valueOf(string), 0, 0);
		}
	}

	@Override
	public String myParse(PumpInstruction i) {
		return i.getState().name() + "|" + TimeUnit.format(i.getRunInterval())
				+ "|" + TimeUnit.format(i.getPausInterval());
	}

	@Override
	Class<PumpInstruction> getInstructionType() {
		return PumpInstruction.class;
	}
}
