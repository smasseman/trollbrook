package se.bryggmester.instruction;

import se.bryggmester.PumpState;
import se.bryggmester.util.TimeUnit;

/**
 * @author jorgen.smas@entercash.com
 */
public class PumpInstruction extends Instruction {

	private PumpState state;
	private long runInterval;
	private long pausInterval;

	public PumpInstruction(PumpState s, long runInterval, long pausInterval) {
		super(InstructionType.PUMP);
		this.state = s;
		this.runInterval = runInterval;
		this.pausInterval = pausInterval;
	}

	public PumpState getState() {
		return state;
	}

	public long getRunInterval() {
		return runInterval;
	}

	public long getPausInterval() {
		return pausInterval;
	}

	public void setState(PumpState state) {
		this.state = state;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getClass().getSimpleName());
		builder.append("[state=");
		builder.append(state);
		builder.append(" (");
		builder.append(TimeUnit.format(runInterval));
		builder.append("/");
		builder.append(TimeUnit.format(pausInterval));
		builder.append(")");
		builder.append("]");
		return builder.toString();
	}

	@Override
	public String displayString() {
		StringBuilder s = new StringBuilder();
		s.append("Pump ");
		if (state == PumpState.ON) {
			s.append("på");
			if (pausInterval > 0) {
				s.append(" (");
				s.append(TimeUnit.format(runInterval));
				s.append("/");
				s.append(TimeUnit.format(pausInterval));
				s.append(")");
			}
		} else {
			s.append("av");
		}
		return s.toString();
	}
}
