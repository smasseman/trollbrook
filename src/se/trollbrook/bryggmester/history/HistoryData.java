package se.trollbrook.bryggmester.history;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author jorgen.smas@entercash.com
 */
public class HistoryData {

	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	public enum Type {
		PUMP, HEAT, WANTED, TEMP;
	}

	private Date date;
	private Type type;
	private String value;

	public HistoryData(Date date, Type type, String value) {
		super();
		this.date = date;
		this.type = type;
		this.value = value;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getClass().getName());
		builder.append("[date=");
		builder.append(date);
		builder.append(", type=");
		builder.append(type);
		builder.append(", value=");
		builder.append(value);
		builder.append("]");
		return builder.toString();
	}

	public static HistoryData parse(String s) {
		Matcher m = Pattern.compile("(.*) (.*) (.*)").matcher(s);
		m.matches();
		try {
			Date d = sdf.parse(m.group(1));
			Type t = Type.valueOf(m.group(2));
			String v = m.group(3);
			return new HistoryData(d, t, v);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	public String toExternal() {
		StringBuilder s = new StringBuilder();
		s.append(sdf.format(date));
		s.append(" ");
		s.append(type.name());
		s.append(" ");
		s.append(value.toString());
		return s.toString();
	}
}
