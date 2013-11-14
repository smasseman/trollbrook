package se.bryggmester;

/**
 * @author jorgen.smas@entercash.com
 */
public class Temperature {

	public enum Scale {
		CELCIUS('C'), FARENHEIT('F');
		private char c;

		Scale(char c) {
			this.c = c;
		}

		@Override
		public String toString() {
			return String.valueOf(c);
		}
	}

	public static final Temperature ZERO = new Temperature(0, Scale.CELCIUS);

	private float value;
	private Scale scale = Scale.CELCIUS;

	public Temperature(float temp, Scale scale) {
		this.value = temp;
		this.scale = scale;
	}

	public float getValue() {
		return value;
	}

	public void setValue(float value) {
		this.value = value;
	}

	public Scale getScale() {
		return scale;
	}

	public void setScale(Scale scale) {
		this.scale = scale;
	}

	@Override
	public String toString() {
		return value + " " + scale;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((scale == null) ? 0 : scale.hashCode());
		result = prime * result + Float.floatToIntBits(value);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Temperature other = (Temperature) obj;
		if (scale != other.scale)
			return false;
		if (Float.floatToIntBits(value) != Float.floatToIntBits(other.value))
			return false;
		return true;
	}

	public boolean lowerThen(Temperature other) {
		if (this.scale != other.scale)
			throw new UnsupportedOperationException();
		return this.value < other.value;
	}

	public static Temperature parse(String s) {
		Scale scale;
		if (s.endsWith("C"))
			scale = Scale.CELCIUS;
		else if (s.endsWith("F"))
			scale = Scale.FARENHEIT;
		else
			throw new IllegalArgumentException(s);
		String valueString = s.substring(0, s.length() - 1);
		return new Temperature(new Float(valueString), scale);
	}

	public Temperature add(int i) {
		return new Temperature(value + i, scale);
	}

	public static Temperature createCelcius(int i) {
		return new Temperature(i, Scale.CELCIUS);
	}

	public float getCelciusValue() {
		if (scale != Scale.CELCIUS)
			throw new UnsupportedOperationException(scale
					+ " is not supported.");
		return value;
	}
}
