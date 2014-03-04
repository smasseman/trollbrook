package se.trollbrook.bryggmester.web;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import se.trollbrook.bryggmester.Relay;
import se.trollbrook.bryggmester.Relay.RelayListener;
import se.trollbrook.bryggmester.Relay.RelayState;
import se.trollbrook.bryggmester.Temperature;
import se.trollbrook.bryggmester.TemperatureController;
import se.trollbrook.bryggmester.TemperatureController.WantedTemperatureListener;
import se.trollbrook.bryggmester.TemperatureListener;
import se.trollbrook.bryggmester.TemperatureSensor;
import se.trollbrook.bryggmester.alarm.ActiveAlarm;
import se.trollbrook.bryggmester.alarm.Alarms;
import se.trollbrook.bryggmester.execution.ExecutionState;
import se.trollbrook.bryggmester.execution.Executor;
import se.trollbrook.bryggmester.execution.Executor.ExecutionListener;
import se.trollbrook.bryggmester.execution.TimeLeft;
import se.trollbrook.util.Time;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * @author jorgen.smas@entercash.com
 */
@Controller
public class StatusController {

	private Logger logger = LoggerFactory.getLogger(getClass());
	private JsonObject json = new JsonObject();
	@Resource
	private TemperatureSensor tempSensor;
	@Resource(name = "pumprelay")
	private Relay pump;
	@Resource(name = "heatrelay")
	private Relay heat;
	@Resource
	private TemperatureController tempCtrl;
	@Resource
	private Alarms alarms;
	@Resource
	private Executor executor;
	private Object lock = new Object();
	private long timestamp = System.currentTimeMillis();
	private Gson gson;
	private boolean initedJson;

	public StatusController() {
		logger.debug("Created.");
	}

	@PostConstruct
	public void init() {
		GsonBuilder b = new GsonBuilder();
		b.setPrettyPrinting();
		this.gson = b.create();

		addWantedTempListener();
		addTempListener();
		addPumpListener();
		addHeatListener();
		addExecutorListener();
		logger.debug("Inited.");
	}

	private void addExecutorListener() {
		executor.addListener(new ExecutionListener() {

			@Override
			public void eventNotification(ExecutionState event) {
				synchronized (lock) {
					setExecutionState(event);
					updateTimestamp();
				}
			}

		});
	}

	private void setExecutionState(ExecutionState event) {
		json.addProperty("actionindex", event.getCurrentActionIndex());
		json.addProperty("currentstate", event.getStatus().name());
	}

	private void addHeatListener() {
		heat.addListener(new RelayListener() {

			@Override
			public void eventNotification(RelayState event) {
				synchronized (lock) {
					setHeatState(event);
					updateTimestamp();
				}
			}

		});
	}

	private void setHeatState(RelayState event) {
		json.addProperty("heat", event.name());
	}

	private void addPumpListener() {
		pump.addListener(new RelayListener() {

			@Override
			public void eventNotification(RelayState event) {
				synchronized (lock) {
					setPumpState(event);
					updateTimestamp();
				}
			}

		});
	}

	private void setPumpState(RelayState event) {
		json.addProperty("pump", event.name());
	}

	private void addWantedTempListener() {
		tempCtrl.addListener(new WantedTemperatureListener() {

			@Override
			public void eventNotification(Temperature wantedTemp) {
				synchronized (lock) {
					setWantedState(wantedTemp);
					updateTimestamp();
				}
			}

		});
	}

	private void setWantedState(Temperature wantedTemp) {
		if (wantedTemp != null)
			json.addProperty("wantedtemp", wantedTemp.getValue().toPlainString());
	}

	private void addTempListener() {
		TemperatureListener tempListener = new TemperatureListener() {

			@Override
			public void eventNotification(Temperature temp) {
				synchronized (lock) {
					setTemperature(temp);
					updateTimestamp();
				}
			}

		};
		tempSensor.addListener(tempListener);
	}

	private void setTemperature(Temperature temp) {
		if (temp != null)
			json.addProperty("temp", temp.getValue().toPlainString());
	}

	protected void updateTimestamp() {
		timestamp = System.currentTimeMillis();
		json.addProperty("ts", timestamp);
		lock.notifyAll();
	}

	@RequestMapping("/status.json")
	public void getStatus(HttpServletRequest request, HttpServletResponse response) throws IOException,
			InterruptedException {
		String clientTsStr = request.getParameter("ts");
		if (clientTsStr == null)
			clientTsStr = "0";
		Long clientTs = new Long(clientTsStr);
		long start = System.currentTimeMillis();
		synchronized (lock) {
			if (!initedJson)
				initJson();
			long maxTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(20);
			while (timestamp <= clientTs && System.currentTimeMillis() < maxTime) {
				logger.debug("My timestamp: " + format(timestamp));
				logger.debug("Client time : " + format(clientTs));

				long diff = maxTime - System.currentTimeMillis();
				if (diff > 0) {
					logger.debug("Sleep for " + Time.fromMillis(diff));
					lock.wait(diff);
				}
			}
			long stop = System.currentTimeMillis();
			logger.debug("Done waiting after " + Time.fromMillis(stop - start));
			Thread.sleep(1000);// Updates often comes in klasar.
			writeStatus(response);
		}
	}

	private void initJson() {
		setTemperature(tempSensor.getCurrentTemperature());
		setPumpState(pump.getCurrentState());
		setExecutionState(executor.getCurrentExecutionState());
		setHeatState(heat.getCurrentState());
		setWantedState(tempCtrl.getCurrentWantedTemperature());
		initedJson = true;
	}

	private String format(long l) {
		return new SimpleDateFormat("HH:mm:ss").format(new Date(l));
	}

	private void writeStatus(HttpServletResponse resp) throws IOException {
		JsonObject j = gson.fromJson(this.json.toString(), JsonElement.class).getAsJsonObject();
		j.addProperty("ts", System.currentTimeMillis());
		JsonArray timeLeftStartArray = new JsonArray();
		JsonArray timeLeftEndArray = new JsonArray();
		Time totalTime = Time.ZERO;
		for (TimeLeft tl : executor.calculateTimeLeft()) {
			addTimeLeft(timeLeftStartArray, tl.getStart());
			addTimeLeft(timeLeftEndArray, tl.getEnd());
			totalTime = tl.getEnd();
		}
		j.add("timeleftstart", timeLeftStartArray);
		j.add("timeleftend", timeLeftEndArray);
		addTime(j, "totaltimeleft", totalTime);
		addTime(j, "timelefttouseraction", executor.calculateTimeToNextUserAction());
		JsonArray alarmsArray = new JsonArray();
		for (ActiveAlarm a : alarms.getActiveAlams()) {
			JsonObject alarmJson = new JsonObject();
			alarmJson.addProperty("id", a.getId());
			alarmJson.addProperty("text", a.getAlarm().getMessage());
			alarmsArray.add(alarmJson);
		}
		j.add("alarms", alarmsArray);
		writeCpuTemp(j);
		String jsonString = gson.toJson(j);
		JsonWriter.writeJson(resp, jsonString);
		logger.debug("Written:\n" + jsonString);
	}

	private void addTimeLeft(JsonArray jsonArray, Time time) {
		if (Time.ZERO.equals(time))
			jsonArray.add(new JsonPrimitive(""));
		else if (Time.ZERO.equals(time.removeSeconds()))
			jsonArray.add(new JsonPrimitive("<1m"));
		else
			jsonArray.add(new JsonPrimitive(time.removeSeconds().toString()));
	}

	private void writeCpuTemp(JsonObject j) {
		try {
			Process p = Runtime.getRuntime().exec("/opt/vc/bin/vcgencmd measure_temp");
			p.waitFor();
			byte[] bytes = new byte[512];
			int len = p.getInputStream().read(bytes);
			String s = new String(bytes, 0, len);
			if (s.startsWith("temp="))
				s = s.substring("temp=".length());
			j.addProperty("cputemp", s);
		} catch (Exception e) {
			logger.debug("Failed to mesure cpu temp: " + e);
		}
	}

	private void addTime(JsonObject json, String propertyname, Time time) {
		if (time.equals(Time.ZERO)) {
			json.addProperty(propertyname, "0");
		}
		time = time.removeSeconds();
		if (time.equals(Time.ZERO)) {
			json.addProperty(propertyname, "<1m");
			return;
		}
		json.addProperty(propertyname, time.toString());
	}
}
