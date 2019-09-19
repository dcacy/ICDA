package us.godby.utilities;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlUtils {

	private XPath xPath = XPathFactory.newInstance().newXPath();
	
	// get a blank XML document
	public Document getNewDocument() {
		Document doc = null;
		try {
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			doc = docBuilder.newDocument();			
		} catch (Exception e) { e.printStackTrace(); }
		return doc;
	}
	
	// load an XML document from a file
	public Document getXmlFromFile(String path, String filename) {
		File file = new File(path, filename);
		return getXmlFromFile(file);
	}
	
	// load an XML document from a file
	public Document getXmlFromFile(File file) {
		Document doc = null;
		try {
			// create a new Document from the returned XML
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			docBuilderFactory.setIgnoringElementContentWhitespace(true);
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			doc = docBuilder.parse(file);
			doc.getDocumentElement().normalize();
				
			docBuilderFactory = null;
			docBuilder = null;
		}
		catch (Exception e) { e.printStackTrace(); }
		return doc;
	}
	
	// write an XML document to a file
	public boolean writeXMLDocToFile(String path, String filename, Document doc) {
		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");
			
			StreamResult result = new StreamResult(new File(path, filename));
			DOMSource source = new DOMSource(doc);
			transformer.transform(source, result);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public Node getNodeByXPath(Node node, String expression) {
		Node n = null;
		try {
			n = (Node) xPath.compile(expression).evaluate(node, XPathConstants.NODE);
		} catch (Exception e) { e.printStackTrace(); }
		return n;
	}
	
	public NodeList getNodeListByXPath(Document xmlDoc, String expression) {
		NodeList nl = null;
		try {
			nl = (NodeList) xPath.compile(expression).evaluate(xmlDoc, XPathConstants.NODESET);
		} catch (Exception e) { e.printStackTrace(); }
		return nl;
	}
	
	public NodeList getNodeListByXPath(Node node, String expression) {
		NodeList nl = null;
		try {
			nl = (NodeList) xPath.compile(expression).evaluate(node, XPathConstants.NODESET);
		} catch (Exception e) { e.printStackTrace(); }
		return nl;
	}
	
	public String getStringByXPath(Node node, String expression) {
		String s = "";
		try {
			s = xPath.compile(expression).evaluate(node);
		} catch (Exception e) { e.printStackTrace(); }
		return s;
	}
	
	// create the specified XML element
	public Element createElement(Document doc, Element parent, String name, String value, Boolean isCdata) {
		// create the element in the source doc
		Element e = doc.createElement(name);
		// if a value was specified
		if (value != null) {
			// create a CDATA section or regular text value
			if (isCdata) {
				CDATASection cd = doc.createCDATASection(value);
				e.appendChild(cd);
			}
			else {
				e.setTextContent(value);
			}
		}
		parent.appendChild(e);
		return e;
	}
	
}
