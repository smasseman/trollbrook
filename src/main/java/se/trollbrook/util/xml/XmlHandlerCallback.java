package se.trollbrook.util.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public interface XmlHandlerCallback {

	void startElement(String path, Attributes attributes) throws SAXException;

	void endElement(String path, String tagText, Attributes attrs)
			throws SAXException;

}