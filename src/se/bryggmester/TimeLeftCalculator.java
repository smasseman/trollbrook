package se.bryggmester;

import java.util.List;

import se.bryggmester.instruction.AlarmInstruction;
import se.bryggmester.instruction.HeatInstruction;
import se.bryggmester.instruction.Instruction;
import se.bryggmester.instruction.PumpInstruction;
import se.bryggmester.instruction.WaitInstruction;
import se.bryggmester.util.TimeUnit;

/**
 * @author jorgen.smas@entercash.com
 */
public class TimeLeftCalculator {

	public long calculateTimeLeft(List<Instruction> instructions,
			Temperature currentTemp) {
		long sum = 0;
		for (Instruction i : instructions) {
			if (i instanceof PumpInstruction) {
				// Ignore

			} else if (i instanceof WaitInstruction) {
				sum += ((WaitInstruction) i).getMillis();

			} else if (i instanceof AlarmInstruction) {
				// Ignore

			} else if (i instanceof HeatInstruction) {
				HeatInstruction heat = (HeatInstruction) i;
				float tempDiff = getTempDiff(heat.getTemperature(), currentTemp);
				// 1 minute for each degre to change.
				long time = TimeUnit.MINUTE.asMillis((long) tempDiff);
				sum += time;
				currentTemp = heat.getTemperature();

			} else {
				throw new IllegalStateException("Unknown instruction: " + i);
			}
		}
		return sum;
	}

	private float getTempDiff(Temperature temperature, Temperature currentTemp) {
		return Math.abs(temperature.getCelciusValue()
				- currentTemp.getCelciusValue());
	}
}
