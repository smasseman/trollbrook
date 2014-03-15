package se.trollbrook.bryggmester.web;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import se.trollbrook.bryggmester.BeerXmlToRecipe;
import se.trollbrook.bryggmester.Hop;
import se.trollbrook.bryggmester.Rast;
import se.trollbrook.bryggmester.Recipe;
import se.trollbrook.bryggmester.RecipeDatabase;
import se.trollbrook.bryggmester.Temperature;
import se.trollbrook.bryggmester.alarm.Alarms;
import se.trollbrook.bryggmester.execution.Action;
import se.trollbrook.bryggmester.execution.AlreadyExecutingException;
import se.trollbrook.bryggmester.execution.Executor;
import se.trollbrook.bryggmester.web.message.Message;
import se.trollbrook.bryggmester.web.message.MessageType;
import se.trollbrook.bryggmester.web.message.Messages;
import se.trollbrook.util.Time;
import se.trollbrook.util.Weight;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

/**
 * @author jorgen.smas@entercash.com
 */
@Controller
public class BryggMesterController {

	@Resource
	private RecipeDatabase db;
	@Resource
	private Executor executor;
	@Resource
	private Alarms alarms;

	private Gson gson;
	private Logger logger = LoggerFactory.getLogger(getClass());

	@PostConstruct
	public void init() {
		GsonBuilder b = new GsonBuilder();
		b.setPrettyPrinting();
		this.gson = b.create();
	}

	@RequestMapping("/edit.html")
	public String edit(Model model, @RequestParam("id") Long id) {
		Recipe r = db.getRecipeById(id);
		if (r == null) {
			Messages.addInfoMessage("Kunde inte hitta något recept med id " + id);
			return redirectToIndex(model);
		}
		model.addAttribute("r", r);
		return "edit";
	}

	@RequestMapping("/editstarttemp.html")
	public String editStartTemp(Model model, @RequestParam Long id) {
		return getEditPage(id, model);
	}

	@RequestMapping("/editboilduration.html")
	public String editBoilDuration(Model model, @RequestParam Long id) {
		return getEditPage(id, model);
	}

	private String getEditPage(Long id, Model model) {
		Recipe r = getRecipe(id);
		model.addAttribute("r", r);
		return null;
	}

	@RequestMapping("/editname.html")
	public String editName(Model model, @RequestParam Long id) {
		return getEditPage(id, model);
	}

	@RequestMapping("/saveboilduration.html")
	public String saveBoilDuration(Model model, @RequestParam Long id, @RequestParam String value) {
		Recipe r = getRecipe(id);
		try {
			r.setBoilDuration(Time.parse(value + "m"));
		} catch (Exception e) {
			Messages.addInfoMessage("Kunde inte spara pga felaktigt värde.");
			logger.debug("Failed to set boild duration.", e);
		}
		db.update(r);
		return "redirect:edit.html?id=" + id;
	}

	@RequestMapping("/savestarttemp.html")
	public String saveStartTemp(Model model, @RequestParam Long id, @RequestParam String value) {
		Recipe r = getRecipe(id);
		try {
			r.setStartTemperature(new Temperature(new BigDecimal(value)));
		} catch (Exception e) {
			logger.debug("Failed to set start temp.", e);
		}
		db.update(r);
		return "redirect:edit.html?id=" + id;
	}

	@RequestMapping("/deleterast.html")
	public String deleteRast(Model model, @RequestParam Long id, @RequestParam Integer index) {
		Recipe r = getRecipe(id);
		r.getRasts().remove(index - 1);
		db.update(r);
		return "redirect:edit.html?id=" + id;
	}

	@RequestMapping("/deletehop.html")
	public String deleteHop(Model model, @RequestParam Long id, @RequestParam Integer hop) {
		Recipe r = getRecipe(id);
		r.getHops().remove(hop - 1);
		db.update(r);
		return "redirect:edit.html?id=" + id;
	}

	@RequestMapping("/import.html")
	public void importRecipe(Model model) {
	}

	@RequestMapping(value = "/doimport.html", method = RequestMethod.POST)
	public String doImportRecipe(Model model, @RequestParam("data") MultipartFile part) throws Exception {
		BeerXmlToRecipe parser = new BeerXmlToRecipe();
		InputStream stream = part.getInputStream();
		Recipe r;
		try {
			r = parser.parse(stream);
		} catch (Exception e) {
			logger.info("Failed to parse file.", e);
			Messages.addInfoMessage("Det funkade inte med filen som du valde. Ogiltigt format kanske?");
			return "import";
		}
		db.add(r);
		return gotoEdit(r);
	}

	@RequestMapping("/newrecipe.html")
	public String newRecipe(Model model) {
		Recipe r = new Recipe();
		r.setName("Nytt recept");
		r.setBoilDuration(Time.parse("60m"));
		r.setStartTemperature(new Temperature(50));
		db.add(r);
		return gotoEdit(r);
	}

	private String gotoEdit(Recipe r) {
		return "redirect:edit.html?id=" + r.getId();
	}

	@RequestMapping("/addrast.html")
	public String addRast(Model model, @RequestParam Long id) {
		Recipe r = getRecipe(id);
		model.addAttribute("r", r);
		return "editrast";
	}

	@RequestMapping("/addhop.html")
	public String addHop(Model model, @RequestParam Long id) {
		Recipe r = getRecipe(id);
		model.addAttribute("r", r);
		return "edithop";
	}

	private Recipe getRecipe(Long id) {
		Recipe r = db.getRecipeById(id);
		if (r == null) {
			throw new RecipeNotFoundException(id);
		}
		return r;
	}

	@ExceptionHandler(RecipeNotFoundException.class)
	public ModelAndView handleCustomException(RecipeNotFoundException ex) {
		Messages.addInfoMessage("Kunde inte hitta recepted med id " + ex.getId());
		ModelAndView model = new ModelAndView("redirect:index.html");
		Collection<Recipe> recipes = db.getAllRecipes();
		model.getModel().put("recipes", recipes);
		return model;
	}

	@RequestMapping("/savename.html")
	public String saveName(Model model, @RequestParam Long id, @RequestParam String name) {
		Recipe r = db.getRecipeById(id);
		if (r == null) {
			Messages.addInfoMessage("Kunde inte hitta något recept med id " + id);
			return redirectToIndex(model);
		} else {
			r.setName(name);
			db.update(r);
			return "redirect:edit.html?id=" + id;
		}
	}

	@RequestMapping("/saverast.html")
	public String saveRast(Model model, @RequestParam Long id, @RequestParam(required = false) Integer index,
			@RequestParam String temp, @RequestParam String time) {
		Recipe r = getRecipe(id);

		Temperature temperature;
		Time duration;
		try {
			temperature = new Temperature(new BigDecimal(temp));
			duration = Time.parse(time.trim() + "m");
		} catch (Exception e) {
			Messages.addInfoMessage("Kunde inte spara pga felaktigt värde.");
			logger.debug("Failed", e);
			return "redirect:edit.html?id=" + id;
		}
		if (index == null) {
			Rast rast = new Rast(temperature, duration);
			r.getRasts().add(rast);
		} else {
			r.getRasts().get(index - 1).setDuration(duration);
			r.getRasts().get(index - 1).setTemperature(temperature);
		}
		db.update(r);
		return "redirect:edit.html?id=" + id;
	}

	@RequestMapping("/savehop.html")
	public String saveHop(Model model, @RequestParam Long id, @RequestParam(required = false) Integer index,
			@RequestParam String time, @RequestParam String text, @RequestParam String weight) {
		Recipe r = getRecipe(id);

		Time t;
		try {
			t = Time.parse(time.trim() + "m");
		} catch (Exception e) {
			Messages.addInfoMessage("Kunde inte spara pga felaktigt värde på tiden.");
			logger.debug("Failed", e);
			return "redirect:edit.html?id=" + id;
		}

		BigDecimal weightValue;
		try {
			weightValue = new BigDecimal(weight);
		} catch (Exception e) {
			Messages.addInfoMessage("Kunde inte spara pga felaktigt värde på vikten.");
			logger.debug("Failed", e);
			return "redirect:edit.html?id=" + id;
		}
		Weight w = new Weight(weightValue, Weight.Unit.GRAM);

		if (index == null) {
			Hop hop = new Hop(t, text, w);
			r.getHops().add(hop);
		} else {
			r.getHops().get(index - 1).setTime(t);
			r.getHops().get(index - 1).setText(text);
		}
		db.update(r);
		return "redirect:edit.html?id=" + id;
	}

	@RequestMapping("/update.html")
	public String update(Model model, @RequestParam Long id, HttpServletRequest request) {
		Recipe r = db.getRecipeById(id);
		if (r == null) {
			Messages.addInfoMessage("Kunde inte hitta något recept med id " + id);
			return redirectToIndex(model);
		}
		Messages.addInfoMessage("Receptet är uppdaterat.");
		model.addAttribute("r", r);
		return "edit";
	}

	@RequestMapping("/index.html")
	public void index(Model model) {
		Collection<Recipe> recipes = db.getAllRecipes();
		model.addAttribute("recipes", recipes);
	}

	private String redirectToIndex(Model model) {
		index(model);
		return "redirect:index.html";
	}

	@RequestMapping("/delete.html")
	public String delete(Model model, @RequestParam Long id) {
		Recipe recipe = db.getRecipeById(id);
		if (recipe == null) {
			Message m = new Message(MessageType.INFO, "Kunde inte hitta receptet som skulle tas bort.");
			Messages.add(m);
		} else {
			db.delete(recipe);
		}
		return "redirect:index.html";
	}

	@RequestMapping("/view.html")
	public void view(Model model) {
		List<Action> actions = executor.getRunningActions();
		if (actions == null) {
			Messages.addInfoMessage("Inget program har laddats än.");
		} else {
			model.addAttribute("actions", actions);
			model.addAttribute("recipe", executor.getCurrectRecipe());
		}
	}

	@RequestMapping("/start.html")
	public String start(@RequestParam Long id, Model model) {
		Recipe recipe = db.getRecipeById(id);
		try {
			executor.load(recipe);
			executor.execute();
			return "redirect:view.html";
		} catch (AlreadyExecutingException e) {
			Messages.addInfoMessage("Det kör redan ett program.");
			return redirectToIndex(model);
		}
	}

	@RequestMapping("/stop.html")
	public String stop(Model model) throws InterruptedException {
		executor.stop();
		Messages.addInfoMessage("Körningen är stoppad.");
		return redirectToIndex(model);
	}

	@RequestMapping("/ackalarm.json")
	public void start(@RequestParam Long id, HttpServletResponse resp) throws IOException {
		alarms.deactive(id);
		JsonObject j = new JsonObject();
		j.addProperty("status", "OK");
		String jsonString = gson.toJson(j);
		JsonWriter.writeJson(resp, jsonString);
	}
}
