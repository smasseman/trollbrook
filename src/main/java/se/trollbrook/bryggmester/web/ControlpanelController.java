package se.trollbrook.bryggmester.web;

import java.io.IOException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import se.trollbrook.bryggmester.ExecutionStatus;
import se.trollbrook.bryggmester.PumpPausController;
import se.trollbrook.bryggmester.Temperature;
import se.trollbrook.bryggmester.TemperatureController;
import se.trollbrook.bryggmester.alarm.Alarm;
import se.trollbrook.bryggmester.alarm.Alarm.Type;
import se.trollbrook.bryggmester.alarm.Alarms;
import se.trollbrook.bryggmester.alarm.OneTimeAlarm;
import se.trollbrook.bryggmester.execution.Executor;

import com.google.gson.JsonObject;

/**
 * @author jorgen.smas@entercash.com
 */
@Controller
public class ControlpanelController {

	private Logger logger = LoggerFactory.getLogger(getClass());
	@Resource
	private PumpPausController pumpController;
	@Resource
	private Executor executor;
	@Resource
	private Alarms alarms;
	@Resource
	private TemperatureController tempCtrl;
	@Value("${simulatetemp}")
	private boolean simulateTemp;

	@RequestMapping("/controlpanel.html")
	public void getPanel(Model model) {
		model.addAttribute("simulatetemp", simulateTemp);
	}

	@RequestMapping("/controlpanel/startpump.json")
	public void startPump(HttpServletRequest request, HttpServletResponse resp) throws IOException {
		if (checkExecutorState(request, resp)) {
			logger.debug("Set pump state ON on " + pumpController);
			pumpController.startMax();
			send("Pumpen startas.", resp);
		}
	}

	@RequestMapping("testalarm.html")
	public void testAlarm(Model model) throws IOException, InterruptedException {
		Alarm alarm = new Alarm("Test", Type.NO_INPUT, new OneTimeAlarm());
		alarms.fireAlarm(alarm);
		model.addAttribute("alarm", alarm);
	}

	@RequestMapping("/controlpanel/stoppump.json")
	public void stopPump(HttpServletRequest request, HttpServletResponse resp) throws IOException {
		if (checkExecutorState(request, resp)) {
			pumpController.off();
			send("Pumpen stoppad.", resp);
		}
	}

	@RequestMapping("/controlpanel/setwanted.json")
	public void setWanted(HttpServletRequest request, HttpServletResponse resp) throws IOException {
		if (checkExecutorState(request, resp)) {
			try {
				Integer l = new Integer(request.getParameter("wanted"));
				Temperature temp = new Temperature(l);
				tempCtrl.setWantedTemperature(temp);
				send("Önskad temp är satt till " + temp, resp);
			} catch (Exception e) {
				logger.debug("Fel: " + e);
				send("Det gick inte att ändra temperaturen.", resp);
			}
		}
	}

	private void send(String message, HttpServletResponse resp) throws IOException {
		JsonObject json = new JsonObject();
		json.addProperty("message", message);
		JsonWriter.writeJson(resp, json.toString());
		logger.debug("Sent " + json);
	}

	private boolean checkExecutorState(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		if (req.getParameter("force") != null)
			return true;
		if (executor.getCurrentState() == ExecutionStatus.EXECUTING) {
			send("Kan inte göra det när ett program kör.", resp);
			return false;
		}
		return true;
	}
}
