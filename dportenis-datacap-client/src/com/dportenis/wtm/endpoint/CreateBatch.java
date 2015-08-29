package com.dportenis.wtm.endpoint;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.xml.sax.SAXException;

import com.dportenis.wtm.xml.Response;
import com.dportenis.wtm.bean.Batch;

public class CreateBatch {
	
	public static Batch sendRequest(String url, String xmlString) throws ClientProtocolException, IOException, SAXException, ParserConfigurationException {
		
		// Response Batch Attributes
		Batch bean = new Batch();
		
		// create HTTP Client
		CloseableHttpClient httpClient = HttpClients.createDefault();		
		
		try {
			
			// Create HTTP Request
			HttpPost request = new HttpPost(url);
	
			// Add additional headers
			request.addHeader("Content-Type", "text/xml");
			
			// Set XML Body
	        HttpEntity xmlEntity = new ByteArrayEntity(xmlString.getBytes("UTF-8"));
	        request.setEntity(xmlEntity);
	
			// Execute your request and catch response
	        CloseableHttpResponse response = httpClient.execute(request);
	
			// Check for HTTP response code: 201 = success
			if (response.getStatusLine().getStatusCode() != 201)
				throw new RuntimeException("HTTP error. " + response.getStatusLine().getStatusCode() + ". " + response.getStatusLine().getReasonPhrase());
			
			// Get-Capture Complete text/xml body response
            try {
            	
                HttpEntity resEntity = response.getEntity();
                Response responseXML = new Response(resEntity.getContent());    
                bean.setBatchId(responseXML.getBatchId());
                bean.setBatchdir(responseXML.getBatchDir());
                bean.setQueueId(responseXML.getQueueId());
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
