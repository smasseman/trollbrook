package se.trollbrook.spring.web.servlet.interceptors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

public class ModelObjectInserterInterceptor extends HandlerInterceptorAdapter {

	private String attributeName;
	private Object attributeValue;

	public ModelObjectInserterInterceptor() {
	}

	public ModelObjectInserterInterceptor(String attributeName, Object attributeValue) {
		setAttributeName(attributeName);
		setAttributeValue(attributeValue);
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {

		if (modelAndView == null) {
			return;
		}
		modelAndView.addObject(getAttributeName(), getAttributeValue());
	}

	public String getAttributeName() {
		return attributeName;
	}

	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

	public Object getAttributeValue() {
		return attributeValue;
	}

	public void setAttributeValue(Object attributeValue) {
		this.attributeValue = attributeValue;
	}
}
