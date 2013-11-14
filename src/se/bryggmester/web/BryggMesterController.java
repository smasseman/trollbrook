package se.bryggmester.web;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import se.bryggmester.Database;
import se.bryggmester.Heat;
import se.bryggmester.HeatController;
import se.bryggmester.HeatState;
import se.bryggmester.HistoryEntry;
import se.bryggmester.HistoryLogger;
import se.bryggmester.MorseCodePlayer;
import se.bryggmester.Program;
import se.bryggmester.ProgramExecutor;
import se.bryggmester.Pump;
import se.bryggmester.Pump.Listener;
import se.bryggmester.PumpState;
import se.bryggmester.Temperature;
import se.bryggmester.Temperature.Scale;
import se.bryggmester.TemperatureSensor;
import se.bryggmester.TimeLeftCalculator;
import se.bryggmester.instruction.AlarmInstruction;
import se.bryggmester.instruction.AlarmInstruction.Type;
import se.bryggmester.instruction.HeatInstruction;
import se.bryggmester.instruction.Instruction;
import se.bryggmester.instruction.InstructionType;
import se.bryggmester.instruction.WaitInstruction;
import se.bryggmester.util.TimeUnit;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

/**
 * @author jorgen.smas@entercash.com
 */
@Controller
public class BryggMesterController implements Listener,
		se.bryggmester.Heat.Listener,
		se.bryggmester.TemperatureSensor.Listener,
		se.bryggmester.ProgramExecutor.Listener {

	private Logger logger = LoggerFactory.getLogger(getClass());
	@Resource
	private HistoryLogger historyLogger;
	@Resource
	private ProgramExecutor programExecutor;
	@Resource
	private Pump pump;
	@Resource
	private Heat heat;
	@Resource
	private HeatController heatController;
	@Resource
	private Database database;
	private long maxWaitSeconds = 20;
	@Autowired
	private ServletContext ctx;
	@Resource
	private MorseCodePlayer morsePlayer;
	@Resource
	private TemperatureSensor tempSensor;
	private Object lock = new Object();
	private long notificationTimestamp = -1;

	public BryggMesterController() {
	}

	@PostConstruct
	public void init() {
		pump.addListener(this);
		heat.addListener(this);
		tempSensor.addListener(this);
		programExecutor.addListener(this);
	}

	@RequestMapping("/controlpanel.html")
	public void controlPanel(Model model,
			@RequestParam(required = false) String pump,
			@RequestParam(required = false) String heat,
			@RequestParam(required = false) String morse) throws IOException,
			InterruptedException {
		if (pump != null) {
			PumpState pumpState = PumpState.valueOf(pump);
			this.pump.setState(pumpState);
		}
		model.addAttribute("pump", this.pump.getCurrentState());
		if (heat != null) {
			HeatState heatState = HeatState.valueOf(heat);
			this.heat.setState(heatState);
		}
		model.addAttribute("heat", this.heat.getCurrentState());
		model.addAttribute("temperature", tempSensor.getCurrentTemperature());
		if (morse != null) {
			morsePlayer.setText(morse);
		}
		model.addAttribute("morse", (morsePlayer.getText() == null ? ""
				: morsePlayer.getText()));
		model.addAttribute("cputemp", getCpuTemp());
	}

	private Object getCpuTemp() {
		try {
			Process p = Runtime.getRuntime().exec(
					"/opt/vc/bin/vcgencmd measure_temp");
			p.waitFor();
			byte[] bytes = new byte[512];
			int len = p.getInputStream().read(bytes);
			return new String(bytes, 0, len);
		} catch (Exception e) {
			logger.debug("Failed to mesure cpu temp: " + e);
			return null;
		}
	}

	@RequestMapping("/index.html")
	public void index(Model model) throws IOException {
		logger.debug("Index called.");
		List<Program> programs = database.listPrograms();
		model.addAttribute("programs", programs);
		model.addAttribute("runningprogram",
				programExecutor.getCurrentRunningProgram());
	}

	@RequestMapping("/historylist.html")
	public void historyList(Model model) throws IOException {
		List<HistoryEntry> entries = historyLogger.getHistoryEntries();
		model.addAttribute("entries", entries);
	}

	@RequestMapping("/deletehistory.html")
	public String deleteHistory(Model model, @RequestParam Long id)
			throws IOException {
		historyLogger.getDelete(id);
		List<HistoryEntry> entries = historyLogger.getHistoryEntries();
		model.addAttribute("entries", entries);
		return "historylist";
	}

	@RequestMapping("/showhistory.html")
	public void showHistory(Model model, @RequestParam Long id)
			throws IOException {
		HistoryEntry e = historyLogger.getEntryById(id);
		if (e == null)
			throw new IllegalArgumentException("No history with id " + id);
		model.addAttribute("entry", e);
	}

	@RequestMapping("/saveprogram.html")
	public String saveProgram(HttpServletRequest request, Model model,
			@RequestParam(required = false) Long id,
			@RequestParam(required = false) Long deleteInstruction,
			@RequestParam(required = false) Integer upInstruction,
			@RequestParam(required = false) Integer downInstruction,
			@RequestParam(required = false) Integer removeInstruction,
			@RequestParam(required = false) String name) throws IOException {
		Program p;
		if (id == null) {
			if ("".equals(name)) {
				addError(model, "Du måste ange ett namn på ditt program.");
				return "index";
			}
			p = new Program();
			database.add(p);
		} else {
			p = database.getProgramById(id);
		}
		if (deleteInstruction != null) {
			p.getInstructions().remove(deleteInstruction.intValue());
		}
		if (upInstruction != null) {
			Instruction i = p.getInstructions()
					.remove(upInstruction.intValue());
			p.getInstructions().add(upInstruction - 1, i);
		}
		if (downInstruction != null) {
			Instruction i = p.getInstructions().remove(
					downInstruction.intValue());
			p.getInstructions().add(downInstruction + 1, i);
		}
		if (name != null)
			p.setName(name);

		logger.debug("Update program " + p);
		database.update(p);
		return showProgram(model, p);
	}

	private void addAvailableAlarmNamesToModel(Model model) {
		Set<String> available = new TreeSet<String>();
		Set<String> list = ctx.getResourcePaths("/audio");
		for (String name : list) {
			if (name.contains(".")) {
				available.add(name.substring("/audio".length() + 1,
						name.indexOf('.')));
			}
		}
		model.addAttribute("alarms", available);
	}

	@RequestMapping("/edit.html")
	public void program(Model model, @RequestParam Long id) throws IOException {
		Program p = database.getProgramById(id);
		model.addAttribute("prog", p);
		model.addAttribute(
				"timeleft",
				new TimeLeftCalculator().calculateTimeLeft(p.getInstructions(),
						Temperature.createCelcius(15))
						/ TimeUnit.MINUTE.asMillis(1));

		addAvailableAlarmNamesToModel(model);
	}

	@RequestMapping("/addpump.html")
	public String addPump(Model model, @RequestParam Long id,
			@RequestParam String value) throws IOException {
		Program p = database.getProgramById(id);
		Instruction i = InstructionType.PUMP.parse(value);
		return addInstruction(model, p, i);
	}

	@RequestMapping("/addheat.html")
	public String addHeat(Model model, @RequestParam Long id,
			@RequestParam String value) throws IOException {
		Program p = database.getProgramById(id);
		float t;
		try {
			t = new Float(value);
		} catch (Exception e) {
			logger.debug(e.toString());
			addError(model, "Felaktigt värde på temperaturen.");
			model.addAttribute("prog", p);
			return "edit";
		}
		Temperature temp = new Temperature(t, Scale.CELCIUS);
		Instruction i = new HeatInstruction(temp);
		return addInstruction(model, p, i);
	}

	@RequestMapping("/addwait.html")
	public String addWait(Model model, @RequestParam Long id,
			@RequestParam String value) throws IOException {
		Program p = database.getProgramById(id);
		long t;
		try {
			if (value.endsWith("s"))
				t = new Long(value.substring(0, value.length() - 1)) * 1000;
			else
				t = new Long(value) * 1000 * 60;
		} catch (Exception e) {
			logger.debug(e.toString());
			addError(model, "Felaktigt värde på minuterna.");
			model.addAttribute("prog", p);
			return "edit";
		}
		Instruction i = new WaitInstruction(t);
		return addInstruction(model, p, i);
	}

	@RequestMapping("/addalarm.html")
	public String addAlarm(Model model, @RequestParam Long id,
			@RequestParam String message, @RequestParam String alarmName)
			throws IOException {
		Program p = database.getProgramById(id);
		Type alarmType = Type.WAIT;
		Instruction i = new AlarmInstruction(alarmName, alarmType, message);
		return addInstruction(model, p, i);
	}

	@SuppressWarnings("unchecked")
	private void addError(Model model, String string) {
		List<Message> list = (List<Message>) model.asMap().get("errors");
		if (list == null) {
			list = new LinkedList<Message>();
			model.addAttribute("errors", list);
		}
		list.add(new Message(string));
	}

	private String addInstruction(Model model, Program p, Instruction i)
			throws IOException {
		p.getInstructions().add(i);
		database.update(p);
		return showProgram(model, p);
	}

	private String showProgram(Model model, Program p) {
		return "redirect:edit.html?id=" + p.getId();
	}

	@RequestMapping("/delete.html")
	public String delete(HttpServletRequest request, Model model,
			@RequestParam(required = true) Long id) throws IOException {
		Program p = database.getProgramById(id);
		database.delete(p);
		index(model);
		return "redirect:index.html";
	}

	@RequestMapping("/run.html")
	public void runProgram(@RequestParam Long programid, Model model)
			throws IOException {
		Program p = database.getProgramById(programid);
		if (p == null)
			throw new IllegalArgumentException("No program with id "
					+ programid);
		programExecutor.startProgram(p);
		index(model);
	}

	@RequestMapping("/stop.html")
	public String stopProgram(Model model) throws IOException,
			InterruptedException {
		programExecutor.stopProgram();
		index(model);
		return "index";
	}

	@RequestMapping("/view.html")
	public void viewRunning(Model model) throws IOException {
		model.addAttribute("program", programExecutor.getCurrentProgram());
	}

	@RequestMapping("/ackalarm.html")
	public void ackAlarm(HttpServletResponse response,
			@RequestParam(required = true, value = "alarmid") Integer alarmId)
			throws IOException, InterruptedException {
		programExecutor.acknowledgeAlarm(alarmId);
		JsonObject obj = new JsonObject();
		obj.addProperty("status", "OK");
		writeJsonToResponse(obj, response);
	}

	@RequestMapping("/status.html")
	public void getStatus(
			HttpServletResponse response,
			@RequestParam(required = false, value = "clientstatetimestamp", defaultValue = "0") Long clientStateTimestamp)
			throws IOException, InterruptedException {
		long now = System.currentTimeMillis();
		long maxTime = now + TimeUnit.SECOND.asMillis(maxWaitSeconds);
		logger.debug("now=" + now + ", maxtime=" + maxTime);
		synchronized (lock) {
			while (!stateChanged(clientStateTimestamp)
					&& System.currentTimeMillis() < maxTime)
				lock.wait(calculateWaitTime(maxTime));
			writeStatus(response);
			logger.debug("Status written after "
					+ TimeUnit.format(System.currentTimeMillis() - now));
		}
	}

	private long calculateWaitTime(long maxTime) {
		long x = maxTime - System.currentTimeMillis();
		return Math.max(x, 1);
	}

	private boolean stateChanged(Long clientStateTimestamp) {
		if (clientStateTimestamp == null)
			return true;
		synchronized (lock) {
			boolean result = clientStateTimestamp < notificationTimestamp;
			logger.debug("State changed = " + result + ", client="
					+ clientStateTimestamp + ", server="
					+ notificationTimestamp);
			return result;
		}
	}

	private void writeStatus(HttpServletResponse response) throws IOException {
		synchronized (lock) {
			JsonObject json = new JsonObject();
			json.addProperty("status", "OK");
			JsonObject state = new JsonObject();
			addProperty(state, "timestamp", System.currentTimeMillis());
			addProperty(state, "currenttemp",
					tempSensor.getCurrentTemperature());
			addProperty(state, "desiredtemp", heatController.getWantedTemp());
			addProperty(state, "instructionpointer",
					programExecutor.getInstructionPointer());
			addProperty(state, "pumpstate", pump.getCurrentState());
			addProperty(state, "heatstate", heat.getCurrentState());
			state.addProperty("running", programExecutor.isRunning());
			json.add("state", state);

			Long totalTimeLeft = programExecutor.getTotalTimeLeft();
			if (totalTimeLeft != null)
				addProperty(json, "totalminutesleft", totalTimeLeft
						/ (TimeUnit.MINUTE.asMillis(1)));
			Long timeLeftToWait = programExecutor.getTimeLeftToWait();
			if (timeLeftToWait != null)
				addProperty(json, "secondsleft", timeLeftToWait / 1000);
			writeJsonToResponse(json, response);
		}
	}

	private void addProperty(JsonObject state, String string, Object obj) {
		if (obj == null)
			state.addProperty(string, (String) null);
		else
			state.addProperty(string, obj.toString());
	}

	private void writeJsonToResponse(JsonObject json,
			HttpServletResponse response) throws IOException {
		GsonBuilder b = new GsonBuilder();
		b.setPrettyPrinting();
		String jsonString = b.create().toJson(json);
		response.setContentType("application/json");
		response.getWriter().write(jsonString);
		logger.debug("Written " + jsonString);
	}

	@Override
	public void notifyProgramChanged(Integer i) {
		logger.debug("Notified with instr pointer " + i);
		handleNotification();
	}

	@Override
	public void notifyTemperatureChanged(Temperature temp) {
		logger.debug("Notified with " + temp);
		handleNotification();
	}

	@Override
	public void heatStateNotify(HeatState state) {
		logger.debug("Notified with heat " + state);
		handleNotification();
	}

	@Override
	public void pumpStataNotify(PumpState state) {
		logger.debug("Notified with pump " + state);
		handleNotification();
	}

	private void handleNotification() {
		synchronized (lock) {
			notificationTimestamp = System.currentTimeMillis();
			lock.notifyAll();
		}
	}
}
