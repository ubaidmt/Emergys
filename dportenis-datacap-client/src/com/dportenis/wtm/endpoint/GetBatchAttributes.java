package com.dportenis.wtm.endpoint;

import org.apache.http.HttpResponse;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.HttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.http.impl.client.HttpClientBuilder;

import com.dportenis.wtm.xml.BatchResponse;
import com.dportenis.wtm.bean.BatchAttributes;

public class GetBatchAttributes {
	
	public static BatchAttributes sendRequest(String url, boolean debug) throws Exception {
		
		// Response Batch Attributes
		BatchAttributes bean = new BatchAttributes();
		
		// create HTTP Client
		HttpClient httpClient = HttpClientBuilder.create().build();			
		
		// Create HTTP Request
		HttpGet request = new HttpGet(url);

		// Execute your request and catch response
		HttpResponse response = httpClient.execute(request);

        // Get string response
        HttpEntity resEntity = response.getEntity();
        String input = IOUtils.toString(resEntity.getContent());
        
		if (debug)
            System.out.println("Response: " + input);

		// Check for HTTP response code
		if (response.getStatusLine().getStatusCode() != 200 && response.getStatusLine().getStatusCode() != 201)
			throw new RuntimeException("HTTP error " + response.getStatusLine().getStatusCode() + ". " + response.getStatusLine().getReasonPhrase());
		
		// Get-Capture Complete text/xml body response
        BatchResponse responseXML = new BatchResponse(input);    
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
		
		return bean;
		
	}

}
