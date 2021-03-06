package se.trollbrook.bryggmester.execution;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import se.trollbrook.bryggmester.Hop;
import se.trollbrook.bryggmester.Rast;
import se.trollbrook.bryggmester.Recipe;
import se.trollbrook.bryggmester.Temperature;
import se.trollbrook.util.Time;

/**
 * @author jorgen.smas@entercash.com
 */
@Component
public class ActionFactory {

	@Resource
	private Environment env;

	public List<Action> createActions(Recipe recipe) {
		List<Action> result = new ArrayList<>(25);
		result.add(new AddWaterAction(env.getAlarms()));
		result.add(createAntiAirAction());
		result.add(new StartTempAction(env, recipe.getStartTemperature()));
		result.add(new MountMaltPipe(env));
		addRasts(recipe, result);
		result.add(new RemoveMaltPipe(env));
		result.add(new StartBoiling(env, recipe));
		addHops(recipe, result);
		result.add(new CoolDown(env));
		result.add(new DoneAction());
		return result;
	}

	private void addRasts(Recipe recipe, List<Action> result) {
		Temperature prev = recipe.getStartTemperature();
		for (Rast r : recipe.getRasts()) {
			result.add(new RastAction(prev, r, env));
			prev = r.getTemperature();
		}
	}

	private void addHops(Recipe recipe, List<Action> result) {
		long millisBeforeEndToAddKylslingan = TimeUnit.MINUTES.toMillis(10);
		boolean kylslinganAlarmAdded = false;
		Time sum = Time.ZERO;
		for (Hop hop : recipe.getHops()) {
			if (!kylslinganAlarmAdded && hop.getTime().toMillis() < millisBeforeEndToAddKylslingan) {
				Time timeToWait = addKylslinganAction(recipe, result, millisBeforeEndToAddKylslingan, sum);
				sum = sum.add(timeToWait);
				kylslinganAlarmAdded = true;
			}
			Time timeFromEnd = hop.getTime();
			Time timeFromStart = recipe.getBoilDuration().subtract(timeFromEnd);
			Time timeToWait = timeFromStart.subtract(sum);
			sum = sum.add(timeToWait);
			String msg = hop.getText() + " " + hop.getWeight() + " " + hop.getTime().toMinutes() + " m";
			result.add(new AlertAction(msg, timeToWait, env.getAlarms()));
		}
		if (!kylslinganAlarmAdded) {
			addKylslinganAction(recipe, result, millisBeforeEndToAddKylslingan, sum);
		}
	}

	private Time addKylslinganAction(Recipe recipe, List<Action> result, long millisBeforeEndToAddKylslingan, Time sum) {
		Time timeFromEnd = Time.fromMillis(millisBeforeEndToAddKylslingan);
		Time timeFromStart = recipe.getBoilDuration().subtract(timeFromEnd);
		Time timeToWait = timeFromStart.subtract(sum);
		result.add(new AlertAction("Lägg i kylslingan.", timeToWait, env.getAlarms()));
		return timeToWait;
	}

	private Action createAntiAirAction() {
		return new AntiAirAction(env);
	}

	public Environment getEnv() {
		return env;
	}

	public void setEnv(Environment env) {
		this.env = env;
	}
}
