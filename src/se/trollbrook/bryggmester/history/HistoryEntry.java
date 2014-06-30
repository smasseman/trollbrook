package se.trollbrook.bryggmester.history;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import se.trollbrook.bryggmester.PumpState;
import se.trollbrook.bryggmester.Relay;
import se.trollbrook.bryggmester.history.HistoryData.Type;

/**
 * @author jorgen.smas@entercash.com
 */
public class HistoryEntry {

	private long id;
	private Date date;
	private String programName;
	private List<HistoryData> data = new LinkedList<>();

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getProgramName() {
		return programName;
	}

	public void setProgramName(String programName) {
		this.programName = programName;
	}

	public List<HistoryData> getData() {
		return data;
	}

	public void setData(List<HistoryData> data) {
		this.data = data;
	}

	public Collection<Type> getDataTypes() {
		return Arrays.asList(Type.values());
	}

	public String getDataString(Type type) {
		StringBuilder s = new StringBuilder();
		String lastValue = null;
		s.append("[");
		for (HistoryData d : data) {
			if (d.getType() == type) {
				Date ts = d.getDate();
				if (s.length() > 1)
					s.append(",");
				s.append("[");
				appendTime(s, ts);
				s.append(",");
				lastValue = appendValue(s, d);
				s.append("]");
			}
		}
		if ((type == Type.HEAT || type == Type.PUMP || type == Type.WANTED) && lastValue != null) {
			s.append(",");
			s.append("[");
			appendTime(s, maxTimeStamp());
			s.append(",");
			s.append(lastValue);
			s.append("]");
		}
		s.append("]");
		return s.toString();
	}

	private void appendTime(StringBuilder s, Date d) {
		long time;
		if (TimeZone.getTimeZone("CET").inDaylightTime(d)) {
			time = d.getTime() + TimeUnit.HOURS.toMillis(2);
		} else {
			time = d.getTime() + TimeUnit.HOURS.toMillis(1);
		}
		s.append(time);
	}

	private Date maxTimeStamp() {
		HistoryData lastData = data.get(data.size() - 1);
		return lastData.getDate();
	}

	private String appendValue(StringBuilder s, HistoryData d) {
		String value;
		if (d.getType() == Type.PUMP) {
			if (PumpState.valueOf(d.getValue()) == PumpState.ON) {
				value = "10";
			} else {
				value = "0";
			}
		} else if (d.getType() == Type.HEAT) {
			if (Relay.RelayState.valueOf(d.getValue()) == Relay.RelayState.ON) {
				value = "20";
			} else {
				value = "0";
			}
		} else {
			value = String.valueOf(d.getValue());
		}
		s.append(value);
		return value;
	}

	public Long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
}
