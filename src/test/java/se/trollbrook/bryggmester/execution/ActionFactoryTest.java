package se.trollbrook.bryggmester.execution;

import java.math.BigDecimal;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import se.trollbrook.bryggmester.Hop;
import se.trollbrook.bryggmester.Recipe;
import se.trollbrook.bryggmester.RecipeMockFactory;
import se.trollbrook.bryggmester.alarm.Alarms;
import se.trollbrook.util.Time;
import se.trollbrook.util.Weight;
import se.trollbrook.util.Weight.Unit;

/**
 * @author jorgen.smas@entercash.com
 */
public class ActionFactoryTest {

	private ActionFactory fac;
	private Environment env;

	@Before
	public void init() {
		this.fac = new ActionFactory();
		this.env = new Environment();
		Alarms alarms = new Alarms();
		env.setAlarms(alarms);
		fac.setEnv(env);
	}

	@Test
	public void testAddKylslinga() {
		Recipe recipe = new RecipeMockFactory().create();
		List<Action> actions = fac.createActions(recipe);
		assertOneKylslingaActionExists(actions);
	}

	private void assertOneKylslingaActionExists(List<Action> actions) {
		int sum = 0;
		for (Action a : actions) {
			if (isKylslinganAction(a))
				sum++;
		}
		Assert.assertEquals("Wrong number of add kylslingan actions.", 1, sum);
	}

	private boolean isKylslinganAction(Action a) {
		return ((a instanceof AlertAction) && ((AlertAction) a).displayString().contains("kylslinga"));
	}

	@Test
	public void testAddKylslinga2() {
		Recipe recipe = new RecipeMockFactory().create();
		recipe.getHops().clear();
		recipe.getHops().add(new Hop(Time.parse("50m"), "Hop 1", new Weight(new BigDecimal(50), Unit.GRAM)));
		List<Action> actions = fac.createActions(recipe);
		assertOneKylslingaActionExists(actions);
	}
}
