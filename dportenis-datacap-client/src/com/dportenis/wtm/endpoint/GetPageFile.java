package com.dportenis.wtm.endpoint;

import org.apache.http.HttpResponse;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.HttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.http.impl.client.HttpClientBuilder;

import com.dportenis.wtm.xml.DCOResponse;
import com.dportenis.wtm.bean.BatchDCO;

public class GetPageFile {
	
	public static BatchDCO sendRequest(String url, boolean debug) throws Exception {
		
		// Response Page File
		BatchDCO batchDCO = new BatchDCO();

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
        DCOResponse responseXML = new DCOResponse(input);    
        batchDCO.setId(responseXML.getBatchId());
        batchDCO.setStatus(responseXML.getBatchStatus());
        batchDCO.setType(responseXML.getBatchType());
        batchDCO.setPages(responseXML.getPages());
        EntityUtils.consume(resEntity);

		return batchDCO;
		
	}

}
