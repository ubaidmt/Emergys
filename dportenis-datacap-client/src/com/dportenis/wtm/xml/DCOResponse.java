package com.dportenis.wtm.xml;

import java.util.List;
import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.io.IOException;
import java.io.StringReader;

import com.dportenis.wtm.bean.Page;

public class DCOResponse {
	
	private static final String NODE_EXCEPTION_BODY = "ExceptionBody";
	private static final String NODE_EXCEPTION_MESSAGE = "message";
	private static final String NODE_VAR_ID = "V";
	private static final String NODE_ATTR_ID = "n";
	private static final String NODE_BATCH = "B";
	private static final String NODE_PAGE = "P";
	private static final String NODE_ID = "id";
	private static final String NODE_STATUS = "STATUS";
	private static final String NODE_TYPE = "TYPE";
	private static final String NODE_IMAGEFILE = "IMAGEFILE";
	
	private DocumentBuilderFactory factory;
	private DocumentBuilder builder;
	private Document document;
	//private Element root;
	
	public DCOResponse(String input) throws ParserConfigurationException, SAXException, IOException {
		//Get Document Builder
		factory = DocumentBuilderFactory.newInstance();
		builder = factory.newDocumentBuilder();	
		
		//Build Document
	    InputSource is = new InputSource();
	    is.setCharacterStream(new StringReader(input));
		document = builder.parse(is);
		 
		//Normalize the XML Structure; It's just too important !!
		document.getDocumentElement().normalize();
		 
		//Here comes the root node
		//root = document.getDocumentElement();
		
		NodeList nList = document.getElementsByTagName(NODE_EXCEPTION_BODY);
		if (nList.getLength() > 0) {
			Node node = nList.item(0);
			 if (node.getNodeType() == Node.ELEMENT_NODE)
			 {
			    Element eElement = (Element) node;
			    throw new RuntimeException(eElement.getElementsByTagName(NODE_EXCEPTION_MESSAGE).item(0).getTextContent());
			 }
		}
	}
	
	public String getBatchId() {
		return getNodeAttributeValuet(null, NODE_BATCH, NODE_ID);
	}
	
	public int getBatchStatus() {
		Element eElement = getFirstElementNode(null, NODE_BATCH);
		String value = getNodeTextContent(eElement, NODE_VAR_ID, NODE_STATUS);
		return (!value.isEmpty() ? Integer.parseInt(value) : -1);
	}
	
	public String getBatchType() {
		Element eElement = getFirstElementNode(null, NODE_BATCH);
		return getNodeTextContent(eElement, NODE_VAR_ID, NODE_TYPE);
	}	
	
	public List<Page> getPages() {
		List<Page> pages = new ArrayList<Page>();
		NodeList nList = document.getElementsByTagName(NODE_PAGE);
		for (int i = 0; i < nList.getLength(); i++) {
			Node node = nList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Page page = new Page();
				Element eElement = (Element) node;
				Node parentNode = eElement.getParentNode();
				if (parentNode.getNodeType() == Node.ELEMENT_NODE) {
					page.setId(getNodeAttributeValuet((Element)parentNode, NODE_PAGE, NODE_ID));
					String value = getNodeTextContent(eElement, NODE_VAR_ID, NODE_STATUS);
					page.setStatus((!value.isEmpty() ? Integer.parseInt(value) : -1));
					page.setType(getNodeTextContent(eElement, NODE_VAR_ID, NODE_TYPE));
					page.setImageFile(getNodeTextContent(eElement, NODE_VAR_ID, NODE_IMAGEFILE));
					pages.add(page);
				}
			}
		}
		return pages;
	}
	
	private String getNodeAttributeValuet(Element element, String nodename, String attname) {
		Element eElement = getFirstElementNode(element, nodename);
		return eElement.getAttribute(attname);		
	}
	
	private String getNodeTextContent(Element element, String nodename, String attname) {
		NodeList nList = element.getElementsByTagName(nodename);
		for (int i = 0; i < nList.getLength(); i++) {
			Node node = nList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) node;
				String value = eElement.getAttribute(NODE_ATTR_ID);
				if (value.equals(attname))
					return eElement.getTextContent();
			}
		}
		return "";
	}
	
	private Element getFirstElementNode(Element element, String nodename) {
		NodeList nList;
		if (element == null)
			nList = document.getElementsByTagName(nodename);
		else
			nList = element.getElementsByTagName(nodename);
		if (nList.getLength() == 0)
			return null;
		
		Node node = nList.item(0);
		if (node.getNodeType() != Node.ELEMENT_NODE)
			return null;
		
		Element eElement = (Element) node;	
		return eElement;
	}		
	
	public String toString() {
		String output = "";
		try
		{
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			StringWriter writer = new StringWriter();
			transformer.transform(new DOMSource(document), new StreamResult(writer));
			output = writer.getBuffer().toString().replaceAll("\n|\r", "");
			
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return output;
	}	

}
