package com.dportenis.wtm.endpoint;

import java.io.IOException;
 
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class ReleaseBatch {
	
	public static void sendRequest(String url) throws ClientProtocolException, IOException {
		
		// create HTTP Client
		CloseableHttpClient httpClient = HttpClients.createDefault();		
		
		try {
			
			// Create HTTP Request
			HttpPut request = new HttpPut(url);
	
			// Execute your request and catch response
	        CloseableHttpResponse response = httpClient.execute(request);
	
			// Check for HTTP response code: 200 = success
			if (response.getStatusLine().getStatusCode() != 200)
				throw new RuntimeException("HTTP error. " + response.getStatusLine().getStatusCode() + ". " + response.getStatusLine().getReasonPhrase());
			
			// Response Close
			response.close();			
		
        } finally {
        	httpClient.close();
        }
		
	}

}
