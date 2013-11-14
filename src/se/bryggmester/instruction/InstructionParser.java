package se.bryggmester.instruction;

/**
 * @author jorgen.smas@entercash.com
 */
public abstract class InstructionParser<I extends Instruction> {

	abstract I parse(String string);

	public String export(Instruction i) {
		return myParse(getInstructionType().cast(i));
	}

	abstract protected String myParse(I i);

	abstract Class<I> getInstructionType();

}
