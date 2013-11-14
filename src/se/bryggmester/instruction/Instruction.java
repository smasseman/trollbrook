package se.bryggmester.instruction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jorgen.smas@entercash.com
 */
public abstract class Instruction {

	private static Logger logger = LoggerFactory.getLogger(Instruction.class);

	private InstructionType type;

	protected Instruction(InstructionType t) {
		this.type = t;
	}

	public InstructionType getType() {
		return type;
	}

	public String displayString() {
		return toString();
	}

	public void setType(InstructionType type) {
		this.type = type;
	}

	public static Instruction parse(String line) {
		try {
			int index = line.indexOf(' ');
			String args = line.substring(0, index);
			InstructionType type = InstructionType.valueOf(args);
			return type.parse(line.substring(1 + index));
		} catch (Throwable t) {
			logger.info("Failed to parse " + line);
			throw t;
		}
	}
}
