package se.trollbrook.bryggmester.history;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import se.trollbrook.bryggmester.Recipe;
import se.trollbrook.bryggmester.Relay;
import se.trollbrook.bryggmester.Relay.RelayListener;
import se.trollbrook.bryggmester.Relay.RelayState;
import se.trollbrook.bryggmester.Temperature;
import se.trollbrook.bryggmester.TemperatureController;
import se.trollbrook.bryggmester.TemperatureController.WantedTemperatureListener;
import se.trollbrook.bryggmester.TemperatureListener;
import se.trollbrook.bryggmester.TemperatureSensor;

/**
 * @author jorgen.smas@entercash.com
 */
@Service
public class HistoryLogger {

	protected static final String SUFFIX = ".brygglog";

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Resource(name = "pumprelay")
	private Relay pump;

	@Resource(name = "heatrelay")
	private Relay heat;

	@Resource
	private TemperatureController tempCtrl;

	@Resource
	private TemperatureSensor tempSensor;

	private Long currentId;

	@Resource(name = "historydirectory")
	private File directory;

	private PrintWriter out;

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	private TemperatureListener tempListener = new TemperatureListener() {

		@Override
		public void eventNotification(Temperature temp) {
			add(HistoryData.Type.TEMP, temp.getValue().toString());
		}
	};

	private WantedTemperatureListener tempCtrlListener = new WantedTemperatureListener() {

		private Temperature lastWantedState;

		@Override
		public void eventNotification(Temperature temp) {
			if (lastWantedState != null) {
				add(System.currentTimeMillis() - 1, HistoryData.Type.WANTED, temp.getValue().toString());
			}
			add(HistoryData.Type.WANTED, temp.getValue().toString());
			lastWantedState = temp;
		}
	};

	private RelayListener heatListener = new Relay.RelayListener() {

		private RelayState lastHeatState;

		@Override
		public void eventNotification(RelayState event) {
			if (lastHeatState != null) {
				add(System.currentTimeMillis() - 1, HistoryData.Type.HEAT, lastHeatState.name());
			}
			add(HistoryData.Type.HEAT, event.name());
			lastHeatState = event;
		}
	};

	private RelayListener pumpListener = new Relay.RelayListener() {

		private RelayState lastPumpState;

		@Override
		public void eventNotification(RelayState event) {
			if (lastPumpState != null) {
				add(System.currentTimeMillis() - 1, HistoryData.Type.PUMP, lastPumpState.name());
			}
			add(HistoryData.Type.PUMP, event.name());
			lastPumpState = event;
		}

	};

	@PostConstruct
	public void init() {
		tempSensor.addListener(tempListener);
		tempCtrl.addListener(tempCtrlListener);
		heat.addListener(heatListener);
		pump.addListener(pumpListener);
	}

	private void add(HistoryData.Type type, String value) {
		add(System.currentTimeMillis(), type, value);
	}

	private void add(long time, HistoryData.Type type, String value) {
		if (currentId != null) {
			HistoryData data = new HistoryData(new Date(time), type, value);
			out.println(data.toExternal());
			out.flush();
		}
	}

	public void start(Recipe recipe) {
		File file = new File(directory, System.currentTimeMillis() + SUFFIX).getAbsoluteFile();
		try {
			out = new PrintWriter(file);
			out.println("version=1");
			out.println("name=" + recipe.getName());
			out.println("date=" + sdf.format(new Date()));
			out.println("---");
			logger.info("Started to log into " + file);
			currentId = getEntryIdFromFile(file);
			new Thread() {
				@Override
				public void run() {
					heatListener.eventNotification(heat.getCurrentState());
					pumpListener.eventNotification(pump.getCurrentState());
					tempCtrlListener.eventNotification(tempCtrl.getCurrentWantedTemperature());
					tempListener.eventNotification(tempSensor.getCurrentTemperature());
				}
			}.start();
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Failed to create outputp file " + file);
		}
	}

	public void stop() {
		out.close();
		currentId = null;
	}

	public List<HistoryEntry> getHistoryEntries() {
		File[] files = getAllHistoryFiles();
		List<HistoryEntry> result = parseFiles(files);
		sortByDate(result);
		return result;
	}

	private List<HistoryEntry> parseFiles(File[] files) {
		List<HistoryEntry> result = new ArrayList<>();
		for (File f : files) {
			try {
				result.add(parse(f));
			} catch (Exception e) {
				throw new RuntimeException("Failed to parse " + f, e);
			}
		}
		return result;
	}

	private File[] getAllHistoryFiles() {
		File[] files = directory.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(SUFFIX);
			}
		});
		return files;
	}

	private void sortByDate(List<HistoryEntry> result) {
		Comparator<HistoryEntry> comp = new Comparator<HistoryEntry>() {

			@Override
			public int compare(HistoryEntry o1, HistoryEntry o2) {
				return o2.getDate().compareTo(o1.getDate());
			}
		};
		Collections.sort(result, comp);
	}

	private HistoryEntry parse(File f) throws IOException, ParseException {
		HistoryEntry e = new HistoryEntry();
		e.setId(getEntryIdFromFile(f));
		BufferedReader reader = new BufferedReader(new FileReader(f));
		try {
			String line;
			int rowCount = 0;
			while ((line = reader.readLine()) != null) {
				if (rowCount == 0) {
					// Ignore version
				} else if (rowCount == 1) {
					e.setProgramName(getValue(line));
				} else if (rowCount == 2) {
					e.setDate(sdf.parse(getValue(line)));
				} else if (rowCount == 3) {
					// Ignore ---
				} else {
					HistoryData data = HistoryData.parse(line);
					e.getData().add(data);
				}
				rowCount++;
			}
		} finally {
			close(reader);
		}
		return e;
	}

	private File getFilenameFromEntryId(Long id) {
		String filename = id + SUFFIX;
		return new File(directory, filename);
	}

	private Long getEntryIdFromFile(File f) {
		return new Long(f.getName().substring(0, f.getName().length() - SUFFIX.length()));
	}

	private String getValue(String line) {
		int index = line.indexOf('=');
		return line.substring(index + 1);
	}

	private void close(BufferedReader reader) {
		try {
			reader.close();
		} catch (Exception ignored) {
		}
	}

	public HistoryEntry getEntryById(Long id) {
		List<HistoryEntry> entries = getHistoryEntries();
		for (HistoryEntry e : entries) {
			if (e.getId().equals(id))
				return e;
		}
		return null;
	}

	public void getDelete(Long id) {
		HistoryEntry e = getEntryById(id);
		if (e == null)
			return;
		File file = getFilenameFromEntryId(e.getId());
		file.delete();

	}

	public HistoryEntry getCurrent() {
		if (currentId == null)
			return null;
		return getEntryById(currentId);
	}
}
