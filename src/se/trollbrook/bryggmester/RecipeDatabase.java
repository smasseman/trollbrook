package se.trollbrook.bryggmester;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import se.trollbrook.util.Time;
import se.trollbrook.util.Weight;
import se.trollbrook.util.Weight.Unit;
import se.trollbrook.util.xml.XmlHandler;

/**
 * @author jorgen.smas@entercash.com
 */
@Component
public class RecipeDatabase {

	private static final String SUFFIX = ".brw.xml";
	@Resource(name = "databasedirectory")
	private File dir;
	private Logger logger = LoggerFactory.getLogger(getClass());

	private static class RecipeItem {

		private File file;
		private Recipe recipe;

		private RecipeItem(File file, Recipe recipe) {
			super();
			this.file = file;
			this.recipe = recipe;
		}
	}

	public void delete(Recipe r) {
		File file = findFile(r);
		if (file != null)
			file.delete();
	}

	public void update(Recipe r) {
		RecipeItem item = getRecipeMap().get(r.getId());
		if (item == null)
			throw new IllegalStateException("Can not update receipt since is doest not exist.");
		File file = createFilename(r);
		write(r, file);
		if (!item.file.getName().equals(file.getName())) {
			item.file.delete();
		}
	}

	public void add(Recipe r) {
		r.setId(generateId());
		File file = createFilename(r);
		write(r, file);
	}

	private Long generateId() {
		TreeSet<Long> currentIds = new TreeSet<>(getRecipeMap().keySet());
		currentIds.add(0L);
		return 1 + currentIds.descendingIterator().next();
	}

	private void write(Recipe r, File file) {
		StringBuilder s = new StringBuilder();
		s.append("<recipe id='" + r.getId() + "'>\n");
		s.append("  <name>" + r.getName() + "</name>\n");
		s.append("  <starttemperature temperature='" + r.getStartTemperature().getValue() + "'/>\n");
		for (Rast rast : r.getRasts()) {
			s.append("  <rast temperature='" + rast.getTemperature().getValue() + "' duration='"
					+ rast.getDuration().toString() + "'/>\n");
		}
		s.append(" <boilduration duration='" + r.getBoilDuration() + "'/>\n");
		for (Hop hop : r.getHops()) {
			s.append("  <hop");
			s.append(" weightvalue='" + hop.getWeight().getValue().toString() + "'");
			s.append(" weightunit='" + hop.getWeight().getUnit().name() + "'");
			s.append(" fromend='" + hop.getTime());
			s.append("'>");
			s.append(hop.getText());
			s.append("</hop>\n");
		}
		s.append("</recipe>\n");
		try {
			FileWriter w = new FileWriter(file);
			w.write(s.toString());
			w.close();
			logger.debug("Written " + file.getAbsolutePath() + " with recipe id=" + r.getId() + " and name="
					+ r.getName());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private File createFilename(Recipe r) {
		String name = r.getName() + "-" + r.getId() + SUFFIX;
		return new File(dir, name);
	}

	private Map<Long, RecipeItem> getRecipeMap() {
		final Map<Long, RecipeItem> result = new HashMap<>();
		dir.listFiles(new FileFilter() {

			@Override
			public boolean accept(File f) {
				if (f.getName().endsWith(SUFFIX)) {
					Recipe r = parseRecipe(f);
					result.put(r.getId(), new RecipeItem(f, r));
					logger.debug("Found recipe with id " + r.getId() + " and name " + r.getName() + " in "
							+ f.getAbsolutePath());
				}
				return false;
			}

		});
		return result;
	}

	private File findFile(Recipe r) {
		RecipeItem i = getRecipeMap().get(r.getId());
		return i == null ? null : i.file;
	}

	public Collection<Recipe> getAllRecipes() {
		Map<Long, RecipeItem> map = getRecipeMap();
		List<Recipe> r = new ArrayList<>(map.size());
		for (RecipeItem i : map.values()) {
			r.add(i.recipe);
		}
		Collections.sort(r, Recipe.NAME_COMPARATOR);
		return r;
	}

	private Recipe parseRecipe(File f) {
		final Recipe r = new Recipe();
		try {
			XMLReader parser = XMLReaderFactory.createXMLReader();
			parser.setContentHandler(new XmlHandler(new se.trollbrook.util.xml.XmlHandlerCallback() {

				@Override
				public void startElement(String path, Attributes attrs) throws SAXException {
				}

				@Override
				public void endElement(String path, String tagText, Attributes attrs) throws SAXException {
					if ("/recipe".equals(path)) {
						r.setId(XmlHandler.parseLong(path, attrs, "id"));

					} else if ("/recipe/name".equals(path)) {
						r.setName(tagText);

					} else if ("/recipe/starttemperature".equals(path)) {
						r.setStartTemperature(new Temperature(XmlHandler.parseInteger(path, attrs, "temperature")));
					} else if ("/recipe/boilduration".equals(path)) {
						r.setBoilDuration(Time.parse(attrs.getValue("duration")));

					} else if ("/recipe/rast".equals(path)) {
						Temperature tempereature = new Temperature(XmlHandler.parseInteger(path, attrs, "temperature"));
						Time duration = Time.parse(attrs.getValue("duration"));
						r.getRasts().add(new Rast(tempereature, duration));

					} else if ("/recipe/hop".equals(path)) {
						Time fromEnd = Time.parse(attrs.getValue("fromend"));

						String s = attrs.getValue("weightvalue");
						if (s == null)
							s = "0";
						BigDecimal wValue = new BigDecimal(s);
						s = attrs.getValue("weightunit");
						if (s == null)
							s = "GRAM";
						Unit wUnit = Unit.valueOf(s);
						Weight w = new Weight(wValue, wUnit);
						r.getHops().add(new Hop(fromEnd, tagText, w));
					} else {
						logger.warn("Unknown tag " + path);
					}
				}
			}));
			parser.parse(new InputSource(new FileInputStream(f)));
		} catch (Exception e) {
			logger.warn("Failed to parse " + f, e);
			return null;
		}
		Collections.sort(r.getHops(), new HopComparator());
		Collections.sort(r.getRasts(), new RastComparator());
		return r;
	}

	public static void main(String[] args) {
		RecipeDatabase db = new RecipeDatabase();
		db.dir = new File("/Users/jorgen/git/smasseman/trollbrook/recipes");
		for (Recipe r : db.getAllRecipes()) {
			System.out.println("Found: " + r);
		}
	}

	public Recipe getRecipeById(Long id) {
		RecipeItem i = getRecipeMap().get(id);
		return i == null ? null : i.recipe;
	}

	public File getDir() {
		return dir;
	}

	public void setDir(File dir) {
		this.dir = dir;
	}

}
