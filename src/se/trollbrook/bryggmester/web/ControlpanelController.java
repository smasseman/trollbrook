package se.trollbrook.bryggmester.web;

import java.io.IOException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import se.trollbrook.bryggmester.ExecutionStatus;
import se.trollbrook.bryggmester.Pump;
import se.trollbrook.bryggmester.PumpState;
import se.trollbrook.bryggmester.Temperature;
import se.trollbrook.bryggmester.TemperatureController;
import se.trollbrook.bryggmester.execution.Executor;

import com.google.gson.JsonObject;

/**
 * @author jorgen.smas@entercash.com
 */
@Controller
public class ControlpanelController {

	private Logger logger = LoggerFactory.getLogger(getClass());
	@Resource(name = "pumpoverheatguard")
	private Pump pump;
	@Resource
	private Executor executor;
	@Resource
	private TemperatureController tempCtrl;

	@RequestMapping("/controlpanel.html")
	public void getPanel(Model model) {
	}

	@RequestMapping("/controlpanel/startpump.json")
	public void startPump(HttpServletRequest request, HttpServletResponse resp)
			throws IOException {
		if (checkExecutorState(resp)) {
			logger.debug("Set pump state ON on " + pump);
			pump.setState(PumpState.ON);
			send("Pumpen startas.", resp);
		}
	}

	@RequestMapping("/controlpanel/stoppump.json")
	public void stopPump(HttpServletRequest request, HttpServletResponse resp)
			throws IOException {
		if (checkExecutorState(resp)) {
			pump.setState(PumpState.OFF);
			send("Pumpen stoppad.", resp);
		}
	}

	@RequestMapping("/controlpanel/setwanted.json")
	public void setWanted(HttpServletRequest request, HttpServletResponse resp)
			throws IOException {
		if (checkExecutorState(resp)) {
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

	private void send(String message, HttpServletResponse resp)
			throws IOException {
		JsonObject json = new JsonObject();
		json.addProperty("message", message);
		JsonWriter.writeJson(resp, json.toString());
		logger.debug("Sent " + json);
	}

	private boolean checkExecutorState(HttpServletResponse resp)
			throws IOException {
		if (executor.getCurrentState() == ExecutionStatus.EXECUTING) {
			send("Kan inte göra det när ett program kör.", resp);
			return false;
		}
		return true;
	}
}
