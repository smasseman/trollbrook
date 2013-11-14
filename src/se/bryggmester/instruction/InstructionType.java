package se.bryggmester.instruction;

/**
 * @author jorgen.smas@entercash.com
 */
public enum InstructionType {

	PUMP(new PumpParser()),

	HEAT(new HeatParser()),

	WAIT(new WaitParser()),

	ALARM(new AlarmParser());

	private InstructionParser<? extends Instruction> parser;

	InstructionType(InstructionParser<? extends Instruction> p) {
		parser = p;
	}

	public Instruction parse(String string) {
		return parser.parse(string);
	}

	public String export(Instruction i) {
		return parser.export(i);
	}
}
