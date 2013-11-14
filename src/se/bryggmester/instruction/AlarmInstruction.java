package se.bryggmester.instruction;

/**
 * @author jorgen.smas@entercash.com
 */
public class AlarmInstruction extends Instruction {

	public enum Type {
		WAIT, GO
	}

	private String alarmName;
	private Type alarmType;
	private String message;;

	public AlarmInstruction(String alarmName, Type alarmType, String message) {
		super(InstructionType.ALARM);
		this.alarmName = alarmName;
		this.alarmType = alarmType;
		this.message = message;
	}

	public String getAlarmName() {
		return alarmName;
	}

	public Type getAlarmType() {
		return alarmType;
	}

	public String getMessage() {
		return message;
	}

	@Override
	public String displayString() {
		return "Alarm " + message;
	}
}
