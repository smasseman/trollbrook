package se.trollbrook.spring.web.servlet.interceptors;

import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.view.AbstractView;

/**
 * @author jorgen.smas@entercash.com
 */
public class LogModelAndViewInterceptor extends HandlerInterceptorAdapter {

	public Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		if (logger.isDebugEnabled() && modelAndView != null) {
			logger.debug("Viewname: " + getViewname(modelAndView));
			logger.debug("Model:");
			for (Entry<String, Object> e : modelAndView.getModelMap().entrySet()) {
				logger.debug(e.getKey() + "=" + e.getValue());
			}
		}
	}

	private String getViewname(ModelAndView modelAndView) {
		String s = modelAndView.getViewName();
		if (s != null)
			return s;
		if (modelAndView.getView() instanceof AbstractView) {
			return ((AbstractView) modelAndView.getView()).getBeanName();
		}
		return null;
	}
}
