package se.trollbrook.bryggmester;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import se.trollbrook.util.Time;

/**
 * @author jorgen.smas@entercash.com
 */
public class RecipeDatabaseTest {

	private RecipeDatabase db;
	private Recipe r;

	@Before
	public void createDir() throws IOException {
		File dir = new File("/tmp/" + Math.random()).getAbsoluteFile();
		dir.mkdir();

		db = new RecipeDatabase();
		db.setDir(dir);
	}

	@After
	public void removeDir() {

	}

	@Test
	public void testAddGetAll() throws IOException {
		r = new Recipe();
		r.setBoilDuration(Time.parse("80m"));
		List<Hop> hops = new ArrayList<>();
		r.setHops(hops);
		r.setName("TestCase1");
		List<Rast> rasts = new ArrayList<>();
		r.setRasts(rasts);
		r.setStartTemperature(new Temperature(50));

		db.add(r);
		Assert.assertEquals(1, db.getAllRecipes().size());
		Recipe dbr = db.getAllRecipes().iterator().next();
		Assert.assertEquals(r.getBoilDuration(), dbr.getBoilDuration());
		Assert.assertEquals(r.getName(), dbr.getName());
		Assert.assertEquals(r.getStartTemperature(), dbr.getStartTemperature());
		Assert.assertEquals(r.getHops(), dbr.getHops());
		Assert.assertEquals(r.getRasts(), dbr.getRasts());
	}
}
