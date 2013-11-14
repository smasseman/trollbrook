package se.bryggmester.instruction;

import se.bryggmester.Temperature;
import se.bryggmester.Temperature.Scale;

/**
 * @author jorgen.smas@entercash.com
 */
public class HeatParser extends InstructionParser<HeatInstruction> {

	@Override
	public HeatInstruction parse(String string) {
		if (string.equals("OFF"))
			return new HeatInstruction(null);
		else
			return new HeatInstruction(new Temperature(new Float(string),
					Scale.CELCIUS));
	}

	@Override
	public String myParse(HeatInstruction i) {
		if (i.getTemperature() == null)
			return "OFF";
		else
			return String.valueOf(i.getTemperature().getValue());
	}

	@Override
	Class<HeatInstruction> getInstructionType() {
		return HeatInstruction.class;
	}
}
