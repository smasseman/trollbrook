package se.bryggmester.instruction;

import java.util.regex.Pattern;

import se.bryggmester.instruction.AlarmInstruction.Type;

/**
 * @author jorgen.smas@entercash.com
 */
public class AlarmParser extends InstructionParser<AlarmInstruction> {

	@Override
	public AlarmInstruction parse(String string) {
		String[] slots = string.split(Pattern.quote("|"));
		String alarmName = slots[0];
		Type type = Type.valueOf(slots[1]);
		String message = slots[2];
		return new AlarmInstruction(alarmName, type, message);
	}

	@Override
	public String myParse(AlarmInstruction i) {
		return i.getAlarmName() + "|" + i.getAlarmType().name() + "|"
				+ i.getMessage();
	}

	@Override
	Class<AlarmInstruction> getInstructionType() {
		return AlarmInstruction.class;
	}
}
