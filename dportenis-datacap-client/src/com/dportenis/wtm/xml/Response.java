package com.dportenis.wtm.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import java.io.InputStream;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

public class Response {
	
	private static final String NODE_EXCEPTION_BODY = "ExceptionBody";
	private static final String NODE_EXCEPTION_MESSAGE = "message";
	private static final String NODE_QUEUEID = "queueId";
	private static final String NODE_BATCHID = "batchId";
	private static final String NODE_BATCHDIR = "batchdir";
	private static final String NODE_BATCHSTATUS = "status";
	private static final String NODE_JOB = "job";
	private static final String NODE_PRIORITY = "priority";
	private static final String NODE_TASK = "task";
	private static final String NODE_OPERATORNAME = "operatorName";
	private static final String NODE_STATION = "station";
	private static final String NODE_RETURNCODE = "returnCode";
	private static final String NODE_XTRABATCHFIELD = "XtraBatchField";
	private static final String NODE_XTRABATCHFIELDNAME = "field";
	private static final String NODE_XTRABATCHFIELDVALUE = "value";
	private static final String NODE_ORIGINAL_FILENAME = "a:originalFileName";
	private static final String NODE_PAGEID = "a:pageId";	
	
	private DocumentBuilderFactory factory;
	private DocumentBuilder builder;
	private Document document;
	//private Element root;
	
	public Response(InputStream input) throws ParserConfigurationException, SAXException, IOException {
		//Get Document Builder
		factory = DocumentBuilderFactory.newInstance();
		builder = factory.newDocumentBuilder();	
		
		//Build Document
		document = builder.parse(input);
		 
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
		return getNodeTextContent(NODE_BATCHID);
	}
	
	public String getBatchDir() {
		return getNodeTextContent(NODE_BATCHDIR);
	}	
	
	public int getQueueId() {
		String value = getNodeTextContent(NODE_QUEUEID);
		return (!value.isEmpty() ? Integer.parseInt(value) : -1);
	}	
	
	public int getPriority() {
		String value = getNodeTextContent(NODE_PRIORITY);
		return (!value.isEmpty() ? Integer.parseInt(value) : -1);
	}
	
	public String getBatchStatus() {
		return getNodeTextContent(NODE_BATCHSTATUS);
	}	
	
	public String getTask() {
		return getNodeTextContent(NODE_TASK);
	}
	
	public String getJob() {
		return getNodeTextContent(NODE_JOB);
	}		
	
	public String getOriginalFileName() {
		return getNodeTextContent(NODE_ORIGINAL_FILENAME);
	}
	
	public String getPageId() {
		return getNodeTextContent(NODE_PAGEID);
	}
	
	public String getOperatorName() {
		return getNodeTextContent(NODE_OPERATORNAME);
	}
	
	public int getStation() {
		String value = getNodeTextContent(NODE_STATION);
		return (!value.isEmpty() ? Integer.parseInt(value) : -1);
	}
	
	public int getReturnCode() {
		String value = getNodeTextContent(NODE_RETURNCODE);
		return (!value.isEmpty() ? Integer.parseInt(value) : -1);
	}		
	
	public Map<String,String> getXtraBatchFields() {
		Map<String,String> xtraBatchFields = new HashMap<String,String>();
		NodeList nList = document.getElementsByTagName(NODE_XTRABATCHFIELD);
		for (int i = 0; i < nList.getLength(); i++) {
			Node node = nList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) node;
				String field = eElement.getElementsByTagName(NODE_XTRABATCHFIELDNAME).item(0).getTextContent();
				String value = eElement.getElementsByTagName(NODE_XTRABATCHFIELDVALUE).item(0).getTextContent();
				xtraBatchFields.put(field, value);
			}
		}
		return xtraBatchFields;
	}
	
	private String getNodeTextContent(String nodename) {
		NodeList nList = document.getElementsByTagName(nodename);
		if (nList.getLength() == 0)
			return null;
		
		Node node = nList.item(0);
		if (node.getNodeType() != Node.ELEMENT_NODE)
			return null;
		
		Element eElement = (Element) node;
		return eElement.getTextContent();		
	}

}
