package se.trollbrook.util.xml;

import java.lang.reflect.Constructor;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

public class XmlHandler extends DefaultHandler {

	private XmlHandlerCallback callback;
	private Stack<String> pathStack = new Stack<String>();
	private Stack<Attributes> attrStack = new Stack<Attributes>();
	private Stack<StringBuffer> textStack = new Stack<StringBuffer>();
	private ErrorHandler errorHandler;

	public XmlHandler(XmlHandlerCallback callback) {
		this.callback = callback;
	}

	public static Date parseDate(String path, Attributes attrs,
			String attrName, SimpleDateFormat sdf) throws SAXException {
		String s = getStringAttr(path, attrs, attrName);
		return parseDateValue(path, attrName, s, sdf);
	}

	public static Date parseDate(String path, Attributes attrs,
			String attrName, Date defVal, SimpleDateFormat sdf)
			throws SAXException {
		String attrValue = getStringAttr(path, attrs, attrName, null);
		if (attrValue == null)
			return defVal;
		return parseDateValue(path, attrName, attrValue, sdf);
	}

	public static Long parseLong(String path, Attributes attrs, String attrName)
			throws SAXException {
		String attrValue = getStringAttr(path, attrs, attrName);
		return parseNumberValue(path, attrName, attrValue, Long.class);
	}

	public static Long parseLong(String path, Attributes attrs,
			String attrName, Long defVal) throws SAXException {
		String attrValue = getStringAttr(path, attrs, attrName, null);
		if (attrValue == null)
			return defVal;
		return parseNumberValue(path, attrName, attrValue, Long.class);
	}

	public static Float parseFloat(String path, Attributes attrs,
			String attrName, Float defVal) throws SAXException {
		String attrValue = getStringAttr(path, attrs, attrName, null);
		if (attrValue == null)
			return defVal;
		return parseNumberValue(path, attrName, attrValue, Float.class);
	}

	public static Boolean parseBoolean(String path, Attributes attrs,
			String attrName, Boolean defVal) throws SAXException {
		String attrValue = getStringAttr(path, attrs, attrName, null);
		if (attrValue == null)
			return defVal;
		if ("true".equals(attrValue)) {
			return Boolean.TRUE;
		} else if ("false".equals(attrValue)) {
			return Boolean.FALSE;
		} else {
			throw new SAXException("Attribute '" + attrName + "' in " + path
					+ " with value '" + attrValue
					+ "' could not be parsed to a boolean.");
		}
	}

	public static Float parseFloat(String path, Attributes attrs,
			String attrName) throws SAXException {
		String attrValue = getStringAttr(path, attrs, attrName);
		return parseNumberValue(path, attrName, attrValue, Float.class);
	}

	private static <N extends Number> N parseNumberValue(String path,
			String attrName, String attrValue, Class<N> clazz)
			throws SAXException {
		try {
			Constructor<N> c = clazz.getConstructor(String.class);
			return c.newInstance(attrValue);
		} catch (Exception e) {
			throw new SAXException("Attribute '" + attrName + "' in " + path
					+ " with value '" + attrValue
					+ "' could not be parsed to a " + clazz.getName());
		}
	}

	private static Date parseDateValue(String path, String attrName,
			String attrValue, SimpleDateFormat sdf) throws SAXException {
		try {
			return sdf.parse(attrValue);
		} catch (ParseException e) {
			throw new SAXException("Attribute '" + attrName + "' in " + path
					+ " with value '" + attrValue
					+ "' could not be parsed to a date with pattern '"
					+ sdf.toPattern() + "'");
		}
	}

	public static String getStringAttr(String path, Attributes attrs,
			String attrName) throws SAXException {
		String s = attrs.getValue(attrName);
		if (s != null)
			return s;
		throw new SAXException("Missing attribute '" + attrName + "' in tag "
				+ path);
	}

	public static String getStringAttr(String path, Attributes attrs,
			String attrName, String defVal) throws SAXException {
		String s = attrs.getValue(attrName);
		return s == null ? defVal : s;
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		this.pathStack.push(qName);
		this.textStack.push(new StringBuffer());
		this.attrStack.push(new AttributesImpl(attributes));
		this.callback.startElement(getCurrentPath(), attributes);
	}

	private String getCurrentPath() {
		StringBuffer buf = new StringBuffer();
		for (String s : this.pathStack) {
			buf.append("/").append(s);
		}
		return buf.toString();
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		String path = getCurrentPath();
		this.pathStack.pop();
		String tagText = this.textStack.pop().toString();
		Attributes attrs = this.attrStack.pop();
		this.callback.endElement(path, tagText, attrs);
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		StringBuffer buf = this.textStack.peek();
		buf.append(new String(ch, start, length));
	}

	@Override
	public void error(SAXParseException e) throws SAXException {
		if (this.errorHandler != null)
			this.errorHandler.error(e);
	}

	@Override
	public void fatalError(SAXParseException e) throws SAXException {
		if (this.errorHandler != null)
			this.errorHandler.fatalError(e);
	}

	@Override
	public void warning(SAXParseException e) throws SAXException {
		if (this.errorHandler != null)
			this.errorHandler.warning(e);
	}

	public void setErrorHandler(ErrorHandler e) {
		this.errorHandler = e;
	}

	public static Integer parseInteger(String path, Attributes attrs,
			String attrName) throws SAXException {
		String attrValue = getStringAttr(path, attrs, attrName);
		return parseNumberValue(path, attrName, attrValue, Integer.class);
	}
}
