package se.bryggmester;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import se.bryggmester.instruction.AlarmInstruction;
import se.bryggmester.instruction.HeatInstruction;
import se.bryggmester.instruction.Instruction;
import se.bryggmester.instruction.InstructionType;
import se.bryggmester.instruction.PumpInstruction;
import se.bryggmester.instruction.WaitInstruction;

/**
 * @author jorgen.smas@entercash.com
 */
@Service
public class ProgramExecutor {

	public interface Listener {
		void notifyProgramChanged(Integer i);
	}

	private TimeLeftCalculator timeLeftCalculator = new TimeLeftCalculator();
	private Logger logger = LoggerFactory.getLogger(getClass());
	private int instrPointer = -1;
	private Program program;
	private Thread thread;
	private List<Listener> listeners = new LinkedList<>();
	private AtomicInteger currentAlarm = new AtomicInteger(-1);
	@Resource
	private PumpController pumpController;
	@Resource
	private HeatController heatController;
	private Long waitUntil;
	private Object waitLock = new Object();
	@Resource
	private TemperatureSensor tempSensor;

	public void addListener(Listener l) {
		listeners.add(l);
	}

	public Program getCurrentRunningProgram() {
		if (instrPointer == -1)
			return null;
		return program;
	}

	public void startProgram(Program p) {
		if (instrPointer != -1)
			throw new IllegalStateException("A program is running.");
		this.program = p;
		this.pumpController.reset();
		thread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					programLoop();
				} catch (InterruptedException e) {
					logger.debug("Program thread interrupted.");
				} catch (Throwable t) {
					logger.warn("Program error.", t);
				} finally {
					logger.info("Program loop exited.");
				}
				programTerminated();
			}
		});
		thread.start();
	}

	private void programTerminated() {
		try {
			pumpController.stop();
		} catch (InterruptedException ignore) {
		}
		heatController.setWantedTemp(null);
		instrPointer = -1;
		notifyInstructionPointerChanged(instrPointer);
	}

	@PreDestroy
	public void stopProgram() throws InterruptedException {
		if (thread != null) {
			thread.interrupt();
			thread.join();
		}
	}

	public Program getCurrentProgram() {
		return this.program;
	}

	private void programLoop() throws InterruptedException {
		instrPointer = 0;
		for (Instruction i : program.getInstructions()) {
			if (Thread.interrupted())
				return;
			notifyInstructionPointerChanged(instrPointer);
			logger.debug("Execute instruction ({}): {}", instrPointer, i);
			executeInstruction(i);
			instrPointer++;
		}
	}

	private void executeInstruction(Instruction i) throws InterruptedException {
		if (i.getType() == InstructionType.PUMP) {
			executePumpInstruction((PumpInstruction) i);

		} else if (i.getType() == InstructionType.WAIT) {
			WaitInstruction w = (WaitInstruction) i;
			executeWaitInstruction(w);

		} else if (i.getType() == InstructionType.ALARM) {
			AlarmInstruction a = (AlarmInstruction) i;
			executeAlarmInstruction(a);

		} else if (i.getType() == InstructionType.HEAT) {
			HeatInstruction h = (HeatInstruction) i;
			executeHeatInstruction(h);

		} else {
			throw new RuntimeException("Illegal instruction " + i);
		}
	}

	private void executeHeatInstruction(HeatInstruction h)
			throws InterruptedException {
		heatController.setWantedTemp(h.getTemperature());
		if (h.getTemperature() != null) {
			heatController.waitForTemperature(h.getTemperature());
		}
	}

	private void executePumpInstruction(PumpInstruction i)
			throws InterruptedException {
		pumpController.execute(i);
	}

	private void notifyInstructionPointerChanged(Integer i) {
		for (Listener l : listeners) {
			l.notifyProgramChanged(i);
		}
	}

	private void executeAlarmInstruction(AlarmInstruction a)
			throws InterruptedException {
		synchronized (listeners) {
			playAlarmSound(a);
			logger.info(a.getMessage());
			int id = instrPointer;
			currentAlarm.set(id);
			if (a.getAlarmType() == AlarmInstruction.Type.WAIT) {
				logger.debug("Wait for ack for alarm " + id);
				while (currentAlarm.get() == id)
					listeners.wait();
			}
		}
	}

	public void acknowledgeAlarm(int i) {
		synchronized (listeners) {
			if (currentAlarm.get() == i) {
				logger.debug("Got ack for alarm " + i);
				currentAlarm.set(-1);
				listeners.notifyAll();
			} else {
				logger.debug("Got ack on alarm id " + i
						+ " but current alarm is " + currentAlarm);
			}
		}
	}

	private void playAlarmSound(AlarmInstruction a) {
		logger.info("Play sound " + a.getAlarmName());
	}

	private void executeWaitInstruction(WaitInstruction w)
			throws InterruptedException {
		logger.info("Sleep for " + w.getMillis());
		synchronized (waitLock) {
			try {
				waitUntil = System.currentTimeMillis() + w.getMillis();
				waitLock.wait(w.getMillis());
			} finally {
				waitUntil = null;
			}
		}
	}

	public Integer getInstructionPointer() {
		return instrPointer;
	}

	public boolean isRunning() {
		return instrPointer >= 0;
	}

	public Long getTimeLeftToWait() {
		synchronized (waitLock) {
			if (waitUntil == null)
				return null;
			return Math.max(0, this.waitUntil - System.currentTimeMillis());
		}
	}

	public Long getTotalTimeLeft() {
		synchronized (waitLock) {
			if (getCurrentRunningProgram() == null)
				return null;
			List<Instruction> instructions = this.program.getInstructions()
					.subList(this.instrPointer,
							this.program.getInstructions().size());
			long time = timeLeftCalculator.calculateTimeLeft(instructions,
					tempSensor.getCurrentTemperature());
			if (instructions.size() > 0
					&& instructions.get(0) instanceof WaitInstruction) {
				WaitInstruction w = (WaitInstruction) (instructions.get(0));
				time -= w.getMillis();
				time += getTimeLeftToWait();
			}
			return time;
		}
	}
}
