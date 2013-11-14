package se.trollbrook.bryggmester;

import java.math.BigDecimal;

/**
 * @author jorgen.smas@entercash.com
 */
public class Temperature {

	private BigDecimal value;

	public Temperature(BigDecimal value) {
		this.value = value;
	}

	public Temperature(int i) {
		this(new BigDecimal(i));
	}

	public BigDecimal getValue() {
		return value;
	}

	public boolean greaterThen(Temperature other) {
		return this.value.compareTo(other.value) > 0;
	}

	@Override
	public String toString() {
		return String.valueOf(value);
	}

	@Override
	public int hashCode() {
		return value.intValue();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (o.getClass() != this.getClass())
			return false;
		return 0 == ((Temperature) o).value.compareTo(this.value);
	}
}
