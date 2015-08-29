package com.dportenis.wtm.endpoint;

import java.io.IOException;
 
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.dportenis.wtm.xml.Response;
import com.dportenis.wtm.bean.Batch;

public class GetBatchAttributes {
	
	public static Batch sendRequest(String url) throws ClientProtocolException, IOException, SAXException, ParserConfigurationException {
		
		// Response Batch Attributes
		Batch bean = new Batch();
		
		// create HTTP Client
		CloseableHttpClient httpClient = HttpClients.createDefault();		
		
		try {
			
			// Create HTTP Request
			HttpGet request = new HttpGet(url);
	
			// Execute your request and catch response
	        CloseableHttpResponse response = httpClient.execute(request);
	
			// Check for HTTP response code: 200 = success
			if (response.getStatusLine().getStatusCode() != 200)
				throw new RuntimeException("HTTP error. " + response.getStatusLine().getStatusCode() + ". " + response.getStatusLine().getReasonPhrase());
			
			// Get-Capture Complete text/xml body response
            try {
            	
                HttpEntity resEntity = response.getEntity();
                Response responseXML = new Response(resEntity.getContent());    
                bean.setBatchId(responseXML.getBatchId());
                bean.setBatchdir(responseXML.getBatchDir());
                bean.setQueueId(responseXML.getQueueId());
                bean.setJob(responseXML.getJob());
                bean.setTask(responseXML.getTask());
                bean.setStatus(responseXML.getBatchStatus());
                bean.setPriority(responseXML.getPriority());
                bean.setOperatorName(responseXML.getOperatorName());
                bean.setStation(responseXML.getStation());
                bean.setXtraBatchFields(responseXML.getXtraBatchFields());
                EntityUtils.consume(resEntity);
            	
            } finally {
                response.close();
            }				
		
        } finally {
        	httpClient.close();
        }
		
		return bean;
		
	}

}
