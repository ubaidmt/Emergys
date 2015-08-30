package com.dportenis.wtm.endpoint;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public class ReleaseBatch {
	
	public static void sendRequest(String url, boolean debug) throws Exception {
	
		// create HTTP Client
		HttpClient httpClient = HttpClientBuilder.create().build();				
		
		// Create HTTP Request
		HttpPut request = new HttpPut(url);
	
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
		
	}

}
