package se.bryggmester;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import se.bryggmester.TemperatureSensor.Listener;
import se.bryggmester.instruction.PumpInstruction;
import se.bryggmester.util.TimeUnit;

/**
 * @author jorgen.smas@entercash.com
 */
@Service
public class PumpController implements Listener {

	@Resource
	private TemperatureSensor tempSensor;
	@Resource
	private Pump pump;
	private Logger logger = LoggerFactory.getLogger(getClass());
	private InstructionWorker currentWorker;
	private AtomicBoolean running = new AtomicBoolean(true);
	private PumpSwitch pumpSwitch;

	public Pump getPump() {
		return pump;
	}

	public void setPump(Pump pump) {
		this.pump = pump;
	}

	public void reset() {
		running.set(true);
	}

	@PostConstruct
	public void init() {
		pumpSwitch = new PumpSwitch();
		tempSensor.addListener(this);
	}

	@PreDestroy
	public void stop() throws InterruptedException {
		synchronized (running) {
			running.set(false);
			stopCurrentWorker();
			pumpSwitch.setWorkerState(PumpState.OFF);
		}
	}

	private void stopCurrentWorker() throws InterruptedException {
		if (currentWorker != null) {
			currentWorker.interrupt();
			currentWorker.join();
		}
	}

	public void execute(PumpInstruction i) throws InterruptedException {
		synchronized (running) {
			stopCurrentWorker();
			if (!running.get())
				return;
			currentWorker = new InstructionWorker(i);
			currentWorker.start();
		}
	}

	private class InstructionWorker extends Thread {

		private PumpInstruction i;

		public InstructionWorker(PumpInstruction i2) {
			this.i = i2;
		}

		@Override
		public void run() {
			logger.debug("Instruction worker {} will execute {}",
					System.identityHashCode(InstructionWorker.this), i);

			if (i.getState() == PumpState.OFF) {
				logger.debug("Execute " + i);
				pumpSwitch.setWorkerState(PumpState.OFF);
			} else if (i.getPausInterval() <= 0) {
				pumpSwitch.setWorkerState(PumpState.ON);
				logger.debug("Execute " + i);
			} else {
				try {
					while (!interrupted()) {
						pumpSwitch.setWorkerState(PumpState.ON);
						logger.debug("Started pump and will now sleep "
								+ TimeUnit.format(i.getRunInterval()));
						sleep(i.getRunInterval());

						pumpSwitch.setWorkerState(PumpState.OFF);
						logger.debug("Stoped pump and will now sleep "
								+ TimeUnit.format(i.getPausInterval()));
						sleep(i.getPausInterval());
					}
				} catch (InterruptedException e) {
				} finally {
					logger.debug("Instruction worker {} is done.",
							System.identityHashCode(InstructionWorker.this));
				}
			}
		}
	}

	@Override
	public void notifyTemperatureChanged(Temperature temp) {
		PumpState s;
		if (temp.getValue() > 95) {
			s = PumpState.OFF;
		} else {
			s = PumpState.ON;
		}
		logger.trace("Temp is " + temp + " setting state to " + s);
		pumpSwitch.heatLimit(s);
	}

	public TemperatureSensor getTempSensor() {
		return tempSensor;
	}

	public void setTempSensor(TemperatureSensor tempSensor) {
		this.tempSensor = tempSensor;
	}

	private class PumpSwitch {
		PumpState heat = PumpState.OFF;
		PumpState worker = PumpState.OFF;

		public synchronized void heatLimit(PumpState s) {
			heat = s;
			update();
		}

		public synchronized void setWorkerState(PumpState s) {
			worker = s;
			update();
		}

		private void update() {
			logger.trace("heat=" + heat + ", worker=" + worker);
			if (heat == PumpState.ON && worker == PumpState.ON) {
				pump.setState(PumpState.ON);
			} else {
				pump.setState(PumpState.OFF);
			}
		}
	}
}
