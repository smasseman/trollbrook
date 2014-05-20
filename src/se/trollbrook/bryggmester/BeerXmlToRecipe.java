package se.trollbrook.bryggmester;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import se.trollbrook.util.Time;
import se.trollbrook.util.Weight;
import se.trollbrook.util.Weight.Unit;
import se.trollbrook.xml.XmlHandler;
import se.trollbrook.xml.XmlHandlerCallback;

/**
 * @author jorgen.smas@entercash.com
 */
public class BeerXmlToRecipe {

	protected static final BigDecimal THOUSAND = new BigDecimal(1000);

	public BeerXmlToRecipe() {
	}

	public Recipe parse(InputStream s) throws Exception {
		final Recipe recipe = new Recipe();

		ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
		int i;
		while ((i = s.read()) != -1) {
			if (i > 5)
				outBytes.write(i);
		}
		// new FileOutputStream("epa2.xml").write(outBytes.toByteArray());
		XmlHandlerCallback callback = new XmlHandlerCallback() {

			Time hopTime;
			String hopName;
			Weight hopWeight;
			private Temperature rastTemp;
			private Time rastDuration;

			@Override
			public void startElement(String path, Attributes attributes) throws SAXException {
			}

			@Override
			public void endElement(String path, String tagText, Attributes attrs) throws SAXException {
				if (path.equals("/RECIPES/RECIPE/NAME")) {
					recipe.setName(tagText.trim());
				}
				if (path.equals("/RECIPES/RECIPE/BOIL_TIME")) {
					Time boilDuration = parseTime(tagText);
					recipe.setBoilDuration(boilDuration);
				}

				if (path.equals("/RECIPES/RECIPE/HOPS/HOP/NAME")) {
					hopName = tagText.trim();
				}
				if (path.equals("/RECIPES/RECIPE/HOPS/HOP/DISPLAY_AMOUNT")) {
					if (!tagText.endsWith(" g"))
						throw new RuntimeException("Can not parse weight when unit is not gram. Values was " + tagText);
					BigDecimal value = new BigDecimal(tagText.substring(0, tagText.length() - 2));
					value = new BigDecimal(value.intValue());
					hopWeight = new Weight(value, Unit.GRAM);
				}
				if (path.equals("/RECIPES/RECIPE/HOPS/HOP/TIME")) {
					hopTime = parseTime(tagText);
				}
				if (path.equals("/RECIPES/RECIPE/HOPS/HOP")) {
					recipe.getHops().add(new Hop(hopTime, hopName, hopWeight));
				}

				if (path.equals("/RECIPES/RECIPE/MASH/MASH_STEPS/MASH_STEP/STEP_TEMP")) {
					rastTemp = parseTemp(tagText);
				}
				if (path.equals("/RECIPES/RECIPE/MASH/MASH_STEPS/MASH_STEP/STEP_TIME")) {
					rastDuration = parseTime(tagText);
				}
				if (path.equals("/RECIPES/RECIPE/MASH/MASH_STEPS/MASH_STEP")) {
					Rast rast = new Rast(rastTemp, rastDuration);
					recipe.getRasts().add(rast);
				}
			}

			private Temperature parseTemp(String tagText) {
				return new Temperature(new BigDecimal(tagText.trim()).setScale(0, RoundingMode.HALF_UP));
			}

			private Time parseTime(String tagText) {
				BigDecimal minutes = new BigDecimal(tagText.trim());
				long millis = TimeUnit.MINUTES.toMillis(minutes.longValue());
				return Time.fromMillis(millis);
			}
		};
		XmlHandler h = new XmlHandler(callback);
		InputSource source = new InputSource(new ByteArrayInputStream(outBytes.toByteArray()));
		XMLReader parser = XMLReaderFactory.createXMLReader();
		parser.setContentHandler(h);
		parser.parse(source);

		Collections.sort(recipe.getHops(), new HopComparator());
		Collections.sort(recipe.getRasts(), new RastComparator());
		if (recipe.getRasts().isEmpty())
			recipe.setStartTemperature(new Temperature(60));
		else
			recipe.setStartTemperature(recipe.getRasts().get(0).getTemperature());
		return recipe;
	}

	public static void main(String[] args) throws Exception {
		System.out.println(new BeerXmlToRecipe().parse(new FileInputStream("epa.xml")).toExtendedString());
	}
}
