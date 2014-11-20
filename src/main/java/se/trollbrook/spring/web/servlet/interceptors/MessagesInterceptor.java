package se.trollbrook.spring.web.servlet.interceptors;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import se.trollbrook.bryggmester.web.message.Message;
import se.trollbrook.bryggmester.web.message.Messages;

/**
 * @author jorgen.smas@entercash.com
 */
public class MessagesInterceptor extends HandlerInterceptorAdapter {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		List<Message> fromThread = getMessagesFromThread();
		List<Message> fromSession = getMessagesFromSession(request);
		List<Message> messages = new ArrayList<>(fromThread.size() + fromSession.size());
		messages.addAll(fromThread);
		messages.addAll(fromSession);

		logger.debug("{} messages from thread.", fromThread.size());
		logger.debug("{} messages from session.", fromSession.size());
		if (modelAndView != null) {
			modelAndView.addObject("messages", messages);
			if (modelAndView.getViewName() != null && modelAndView.getViewName().startsWith("redirect:")) {
				HttpSession session = request.getSession();
				session.setAttribute("messages", messages);
				logger.debug("Was redirect, putting messages on session.");
			}
		}
	}

	private List<Message> getMessagesFromSession(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session == null)
			return emptyList();
		@SuppressWarnings("unchecked")
		List<Message> list = (List<Message>) session.getAttribute("messages");
		session.removeAttribute("messages");
		if (list == null)
			return emptyList();
		return list;
	}

	private List<Message> emptyList() {
		return new LinkedList<>();
	}

	private List<Message> getMessagesFromThread() {
		List<Message> messages = Messages.removeAll();
		return messages;
	}
}
