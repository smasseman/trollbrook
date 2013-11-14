package se.bryggmester;

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

import se.bryggmester.HistoryData.Type;
import se.bryggmester.Temperature.Scale;

/**
 * @author jorgen.smas@entercash.com
 */
@Service
public class HistoryLogger implements
		se.bryggmester.TemperatureSensor.Listener,
		se.bryggmester.HeatController.Listener, se.bryggmester.Heat.Listener,
		se.bryggmester.Pump.Listener, se.bryggmester.ProgramExecutor.Listener {

	protected static final String SUFFIX = ".brygglog";

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Resource
	private Pump pump;

	@Resource
	private Heat heat;

	@Resource
	private HeatController heatController;

	@Resource
	private TemperatureSensor tempSensor;

	@Resource
	private ProgramExecutor programExecutor;

	private boolean active;

	@Resource(name = "logdirectory")
	private File directory;

	private PrintWriter out;

	private SimpleDateFormat sdf = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss.SSS");

	private HeatState lastHeatState;

	private Temperature lastWanted;

	private PumpState lastPump;

	@PostConstruct
	public void init() {
		tempSensor.addListener(this);
		heatController.addListener(this);
		heat.addListener(this);
		pump.addListener(this);
		programExecutor.addListener(this);
	}

	@Override
	public void pumpStataNotify(PumpState state) {
		if (lastPump != null)
			addCurrent(Type.PUMP, lastPump.name());
		add(Type.PUMP, state.name());
		this.lastPump = state;
	}

	private void addCurrent(HistoryData.Type type, String value) {
		add(System.currentTimeMillis() - 1, type, value);
	}

	private void add(HistoryData.Type type, String value) {
		add(System.currentTimeMillis(), type, value);
	}

	private void add(long time, HistoryData.Type type, String value) {
		if (active) {
			HistoryData data = new HistoryData(new Date(time), type, value);
			out.println(data.toExternal());
			out.flush();
		}
	}

	@Override
	public void heatStateNotify(HeatState state) {
		if (this.lastHeatState != null)
			addCurrent(Type.HEAT, lastHeatState.name());
		add(Type.HEAT, state.name());
		this.lastHeatState = state;
	}

	@Override
	public void notifyWantedTempChanged(Temperature temp) {
		if (this.lastWanted != null)
			addCurrent(Type.WANTED, "" + lastWanted.getValue());
		add(Type.WANTED, (temp == null ? "0" : "" + temp.getValue()));
		this.lastWanted = temp;
	}

	@Override
	public void notifyTemperatureChanged(Temperature temp) {
		add(Type.TEMP, "" + temp.getValue());
	}

	@Override
	public void notifyProgramChanged(Integer i) {
		if (i > -1) {
			if (!active) {
				startLogging();
			}
		} else {
			if (active) {
				stopLogging();
			}
		}
	}

	private void startLogging() {
		File file = new File(directory, System.currentTimeMillis() + SUFFIX)
				.getAbsoluteFile();
		try {
			out = new PrintWriter(file);
			out.println("version=1");
			out.println("name=" + programExecutor.getCurrentProgram().getName());
			out.println("date=" + sdf.format(new Date()));
			out.println("---");
			logger.info("Started to log into " + file);
			active = true;
			notifyWantedTempChanged(heatController.getWantedTemp());
			Temperature temp = tempSensor.getCurrentTemperature();
			if (temp == null)
				temp = new Temperature(0, Scale.CELCIUS);
			notifyTemperatureChanged(temp);
		} catch (FileNotFoundException e) {
			logger.warn("Failed to create outputp file " + file);
		}
	}

	private void stopLogging() {
		out.close();
		active = false;
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
		return new Long(f.getName().substring(0,
				f.getName().length() - SUFFIX.length()));
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

	public File getDirectory() {
		return directory;
	}

	public void setDirectory(File directory) {
		this.directory = directory;
	}

	public Pump getPump() {
		return pump;
	}

	public void setPump(Pump pump) {
		this.pump = pump;
	}

	public Heat getHeat() {
		return heat;
	}

	public void setHeat(Heat heat) {
		this.heat = heat;
	}

	public HeatController getHeatController() {
		return heatController;
	}

	public void setHeatController(HeatController heatController) {
		this.heatController = heatController;
	}

	public TemperatureSensor getTempSensor() {
		return tempSensor;
	}

	public void setTempSensor(TemperatureSensor tempSensor) {
		this.tempSensor = tempSensor;
	}

	public ProgramExecutor getProgramExecutor() {
		return programExecutor;
	}

	public void setProgramExecutor(ProgramExecutor programExecutor) {
		this.programExecutor = programExecutor;
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

}
