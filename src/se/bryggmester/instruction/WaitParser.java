package se.bryggmester.instruction;

import se.bryggmester.util.TimeUnit;

/**
 * @author jorgen.smas@entercash.com
 */
public class WaitParser extends InstructionParser<WaitInstruction> {

	@Override
	public WaitInstruction parse(String string) {
		return new WaitInstruction(TimeUnit.parse(string));
	}

	@Override
	public String myParse(WaitInstruction i) {
		return TimeUnit.format(i.getMillis());
	}

	@Override
	Class<WaitInstruction> getInstructionType() {
		return WaitInstruction.class;
	}
}
