package se.trollbrook.bryggmester.execution;

import se.trollbrook.bryggmester.ExecutionStatus;

/**
 * @author jorgen.smas@entercash.com
 */
public class ExecutionState {

	private int currentActionIndex;
	private ExecutionStatus status;

	public ExecutionState(int currentActionIndex, ExecutionStatus status) {
		super();
		this.currentActionIndex = currentActionIndex;
		this.status = status;
	}

	public int getCurrentActionIndex() {
		return currentActionIndex;
	}

	public void setCurrentActionIndex(int currentActionIndex) {
		this.currentActionIndex = currentActionIndex;
	}

	public ExecutionStatus getStatus() {
		return status;
	}

	public void setStatus(ExecutionStatus status) {
		this.status = status;
	}
}
