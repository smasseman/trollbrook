package se.trollbrook.bryggmester.web.message;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jorgen.smas@entercash.com
 */
public class Messages {

	private static ThreadLocal<List<Message>> messages = new ThreadLocal<>();
	private static Logger logger = LoggerFactory.getLogger(Messages.class);

	public static void add(Message m) {
		List<Message> list = messages.get();
		if (list == null) {
			list = new LinkedList<Message>();
			messages.set(list);
		}
		logger.debug("Added to messages: " + m);
		list.add(m);
	}

	public static void addInfoMessage(String string) {
		Message m = new Message(MessageType.INFO, string);
		add(m);
	}

	public static List<Message> removeAll() {
		List<Message> list = messages.get();
		messages.remove();
		logger.debug("Messages are cleared for thread.");
		if (list == null)
			list = new LinkedList<>();
		return list;
	}
}
