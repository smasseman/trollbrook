package se.trollbrook.bryggmester.execution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import se.trollbrook.bryggmester.ExecutionStatus;
import se.trollbrook.bryggmester.PumpPausController;
import se.trollbrook.bryggmester.Recipe;
import se.trollbrook.bryggmester.Temperature;
import se.trollbrook.bryggmester.TemperatureController;
import se.trollbrook.bryggmester.alarm.Alarms;
import se.trollbrook.bryggmester.history.HistoryLogger;
import se.trollbrook.util.Time;
import se.trollbrook.util.event.Listener;
import se.trollbrook.util.event.ListenerManager;

/**
 * @author jorgen.smas@entercash.com
 */
@Service
public class Executor {

	public interface ExecutionListener extends Listener<ExecutionState> {
	}

	@Resource
	private ActionFactory actionFactory;
	@Resource
	private TemperatureController temperatureController;
	@Resource
	private Alarms alarms;
	@Resource
	private PumpPausController pumpController;
	private Logger logger = LoggerFactory.getLogger(getClass());
	private ListenerManager<ExecutionState> lm = new ListenerManager<>();
	private List<Action> actions;
	private ExecutionStatus currentStatus = ExecutionStatus.NOT_ACTIVE;
	private Thread thread;
	private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private int currentActionIndex = 0;
	private HashMap<Thread, StackTraceElement[]> lockThreads = new HashMap<>();
	private Recipe recipe;
	@Resource
	private HistoryLogger historyLogger;

	public void addListener(ExecutionListener listener) {
		lm.addListener(listener);
	}

	private void startThread() {
		this.thread = new Thread("BryggMesterExecutor") {
			@Override
			public void run() {
				executionLoop();
			}
		};
		this.thread.start();
	}

	protected void executionLoop() {
		lock.writeLock().lock();
		try {
			for (int i = 0; i < actions.size(); i++) {
				if (currentStatus != ExecutionStatus.EXECUTING) {
					logger.info("Quit execution thread since we are no longer executing.");
					return;
				}
				currentActionIndex = i;
				Action a = actions.get(i);
				lm.notifyListeners(createExecutionState());
				logger.info("Execute " + a);

				lock.writeLock().unlock();
				try {
					a.execute();
				} finally {
					lock.writeLock().lock();
				}
			}
			logger.info("Quit execution thread since all actions are done.");
		} catch (Throwable t) {
			logger.error("Failure in executor thread.", t);
		} finally {
			logger.info("Executor thread is down.");
			currentStatus = ExecutionStatus.NOT_ACTIVE;
			this.temperatureController.setWantedTemperature(Temperature.OFF);
			this.pumpController.off();
			this.alarms.deactiveAll();
			lm.notifyListeners(createExecutionState());
			lock.writeLock().unlock();
		}
	}

	public ExecutionState getCurrentExecutionState() {
		aquireReadLock();
		try {
			return createExecutionState();
		} finally {
			unlockReadLock();
		}
	}

	private ExecutionState createExecutionState() {
		return new ExecutionState(currentActionIndex, currentStatus);
	}

	@PreDestroy
	public void stop() throws InterruptedException {
		Thread t;
		lock.writeLock().lock();
		try {
			if (this.thread != null) {
				this.currentStatus = ExecutionStatus.NOT_ACTIVE;
				this.thread.interrupt();
				t = this.thread;
				this.thread = null;
			} else {
				t = null;
			}
		} finally {
			lock.writeLock().unlock();
		}
		if (t != null)
			t.join();
	}

	public List<TimeLeft> calculateTimeLeft() {
		aquireReadLock();
		try {
			if (this.actions == null)
				return Collections.emptyList();
			List<TimeLeft> times = new ArrayList<>(this.actions.size());
			Time start = Time.ZERO;
			Time end = Time.ZERO;
			for (int i = 0; i < actions.size(); i++) {
				Action action = actions.get(i);
				start = end;
				if (i >= currentActionIndex && ExecutionStatus.EXECUTING == currentStatus) {
					end = end.add(action.calculateTimeLeft());
					logger.trace("Time left: Start=" + start + ", End=" + end + " for " + action);
				} else {
					logger.trace("Time left: Ignore " + action);
				}
				TimeLeft tl = new TimeLeft(start, end);
				times.add(tl);
				logger.trace("Added " + tl);
			}
			return times;
		} finally {
			unlockReadLock();
		}
	}

	private void unlockReadLock() {
		lock.readLock().unlock();
		synchronized (lockThreads) {
			lockThreads.remove(Thread.currentThread());
		}
	}

	private void aquireReadLock() {
		try {
			while (!lock.readLock().tryLock(5, TimeUnit.SECONDS)) {
				synchronized (lockThreads) {
					for (StackTraceElement[] t : lockThreads.values()) {
						logger.debug("------------------------------");
						for (StackTraceElement trace : t) {
							logger.debug(trace.toString());
						}
					}
				}
			}
			synchronized (lockThreads) {
				lockThreads.put(Thread.currentThread(), Thread.currentThread().getStackTrace());
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Time calculateTimeToNextUserAction() {
		aquireReadLock();
		try {
			if (currentStatus != ExecutionStatus.EXECUTING)
				return Time.ZERO;
			Time result = Time.ZERO;
			for (int i = currentActionIndex; i < actions.size(); i++) {
				Action a = actions.get(i);
				logger.trace("Time to useraction: Index " + i + ": " + a + " requires user action, at start: "
						+ a.requireUserActionAtStart() + ", at end: " + a.requireUserActionAtEnd());
				if (a.requireUserActionAtStart())
					return result;
				result = result.add(a.calculateTimeLeft());
				if (a.requireUserActionAtEnd())
					return result;
			}
			return result;
		} finally {
			unlockReadLock();
		}
	}

	public int getCurrectActionIndex() {
		aquireReadLock();
		try {
			return currentActionIndex;
		} finally {
			unlockReadLock();
		}
	}

	public Recipe getCurrectRecipe() {
		return recipe;
	}

	public ExecutionStatus getCurrentState() {
		return currentStatus;
	}

	public List<Action> getRunningActions() {
		aquireReadLock();
		try {
			if (this.actions != null)
				return new ArrayList<Action>(this.actions);
			else
				return null;
		} finally {
			unlockReadLock();
		}
	}

	public void load(Recipe recipe) throws AlreadyExecutingException {
		lock.writeLock().lock();
		try {
			if (currentStatus == ExecutionStatus.EXECUTING)
				throw new AlreadyExecutingException();
			this.actions = actionFactory.createActions(recipe);
			this.recipe = recipe;
		} finally {
			lock.writeLock().unlock();
		}
	}

	public void execute() throws AlreadyExecutingException {
		lock.writeLock().lock();
		try {
			if (currentStatus == ExecutionStatus.EXECUTING)
				throw new AlreadyExecutingException();
			if (this.actions == null)
				throw new IllegalStateException("Must load actions before execution.");
			currentStatus = ExecutionStatus.EXECUTING;
			startThread();
		} finally {
			lock.writeLock().unlock();
		}
	}
}
