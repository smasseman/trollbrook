package se.trollbrook.bryggmester.web;

import java.io.IOException;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import se.trollbrook.bryggmester.history.HistoryEntry;
import se.trollbrook.bryggmester.history.HistoryLogger;
import se.trollbrook.bryggmester.web.message.Messages;

/**
 * @author jorgen.smas@entercash.com
 */
@Controller
public class HistoryController {

	@Resource
	private HistoryLogger historyLogger;

	@RequestMapping("/historylist.html")
	public void historyList(Model model) throws IOException {
		List<HistoryEntry> entries = historyLogger.getHistoryEntries();
		model.addAttribute("entries", entries);
	}

	@RequestMapping("/deletehistory.html")
	public String deleteHistory(Model model, @RequestParam Long id) throws IOException {
		historyLogger.getDelete(id);
		List<HistoryEntry> entries = historyLogger.getHistoryEntries();
		model.addAttribute("entries", entries);
		return "historylist";
	}

	@RequestMapping("/showcurrenthistory.html")
	public String showCurrent(Model model) throws IOException {
		HistoryEntry current = historyLogger.getCurrent();
		if (current == null) {
			Messages.addInfoMessage("Det finns ingen körning just nu.");
			return "redirect:index.html";
		} else {
			return "redirect:showhistory.html?id=" + current.getId() + "&reload=10";
		}
	}

	@RequestMapping("/showhistory.html")
	public void showHistory(Model model, @RequestParam Long id) throws IOException {
		HistoryEntry e = historyLogger.getEntryById(id);
		if (e == null)
			throw new IllegalArgumentException("No history with id " + id);
		model.addAttribute("entry", e);
	}

}
