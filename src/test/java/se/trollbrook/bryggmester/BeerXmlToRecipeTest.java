package se.trollbrook.bryggmester;

import java.io.FileInputStream;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author jorgen.smas@entercash.com
 */
public class BeerXmlToRecipeTest {

	@Test
	public void test() throws Exception {
		BeerXmlToRecipe importer = new BeerXmlToRecipe();
		Recipe r = importer.parse(new FileInputStream("src/test/files/brooklyn.xml"));
		// System.out.println("Imported: " + r.toExtendedString());
		Assert.assertEquals("TB Brooklyn Lager", r.getName());
	}

	@Test
	public void test2() throws Exception {
		BeerXmlToRecipe importer = new BeerXmlToRecipe();
		Recipe r = importer.parse(new FileInputStream("src/test/files/spring_ipa_2014.xml"));
		// System.out.println("Imported: " + r.toExtendedString());
		Assert.assertEquals(100, r.getHops().get(r.getHops().size() - 1).getWeight().getValue().intValueExact());
	}
}
