package com.dportenis.wtm.endpoint;

import org.apache.http.HttpResponse;
import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.HttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.http.impl.client.HttpClientBuilder;

import com.dportenis.wtm.xml.BatchResponse;
import com.dportenis.wtm.bean.BatchAttributes;

import org.apache.commons.io.IOUtils;

public class CreateBatch {
	
	protected int timeout = 10000; // default to 10 secs
	
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	
	public BatchAttributes sendRequest(String url, String xmlString, boolean debug) throws Exception {
		
		// Response Batch Attributes
		BatchAttributes bean = new BatchAttributes();
		
		// Create HTTP Client
		RequestConfig config = RequestConfig.custom()
				.setConnectTimeout(timeout)
				.setSocketTimeout(timeout).build();		
		HttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
		
		// Create HTTP Request
		HttpPost request = new HttpPost(url);

		// Add additional headers
		request.addHeader("Content-Type", "text/xml");
		
		// Set XML Body
        StringEntity xmlEntity = new StringEntity(xmlString);
        request.setEntity(xmlEntity);

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
        EntityUtils.consume(resEntity);
        
		return bean;
		
	}

}
