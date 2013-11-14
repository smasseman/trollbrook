package se.bryggmester.instruction;

import se.bryggmester.Temperature;

/**
 * @author jorgen.smas@entercash.com
 */
public class HeatInstruction extends Instruction {

	private Temperature temperature;

	public HeatInstruction(Temperature t) {
		super(InstructionType.HEAT);
		this.temperature = t;
	}

	public Temperature getTemperature() {
		return temperature;
	}

	@Override
	public String displayString() {
		return "Värme " + (temperature == null ? "av" : temperature);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getClass().getSimpleName());
		builder.append("[temperature=");
		builder.append(temperature);
		builder.append("]");
		return builder.toString();
	}
}
