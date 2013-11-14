package se.bryggmester.instruction;

import se.bryggmester.util.TimeUnit;

/**
 * @author jorgen.smas@entercash.com
 */
public class WaitInstruction extends Instruction {

	private long millis;

	public WaitInstruction(long m) {
		super(InstructionType.WAIT);
		this.setMillis(m);
	}

	public long getMillis() {
		return millis;
	}

	public void setMillis(long millis) {
		this.millis = millis;
	}

	@Override
	public String displayString() {
		return "Vänta " + TimeUnit.format(millis);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + TimeUnit.format(millis) + "]";
	}
}
