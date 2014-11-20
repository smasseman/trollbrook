package se.trollbrook.util;

import java.math.BigDecimal;

/**
 * @author jorgen.smas@entercash.com
 */
public class Weight {

	public enum Unit {
		GRAM("g");

		private String displayUnit;

		Unit(String displayUnit) {
			this.displayUnit = displayUnit;
		}
	}

	private BigDecimal value;
	private Unit unit;

	public Weight(BigDecimal value, Unit unit) {
		super();
		this.value = value;
		this.unit = unit;
		if (value == null)
			throw new NullPointerException();
		if (unit == null)
			throw new NullPointerException();
	}

	public BigDecimal getValue() {
		return value;
	}

	public Unit getUnit() {
		return unit;
	}

	@Override
	public String toString() {
		return value + " " + unit.displayUnit;
	}
}
