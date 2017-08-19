package com.deportenis.rest.test;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ibm.json.java.JSONObject;

public class TestREST {
	
	private String url = "http://localhost:9080/dportenis-rest-broker/jaxrs/po/datos";
	private int timeout = 10000; // default to 10 secs

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSendData() {
		JSONObject request = new JSONObject();
		request.put("batchId", "XXXXXX");
		try {
			setTimeout(5000);
			JSONObject response = doPost(request);
			System.out.println(response.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void setTimeout(int timeout) {
		this.timeout = timeout;
	}		
	
	private JSONObject doPost(JSONObject data) throws Exception {
		// Create HTTP Client
		RequestConfig config = RequestConfig.custom()
				.setConnectTimeout(timeout)
				.setSocketTimeout(timeout).build();		
		HttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();	
		
		// Create HTTP Request
		HttpPost request = new HttpPost(url);

		// Add additional headers
		request.addHeader("Content-Type", "application/json");
		
		// Set Body
        StringEntity entity = new StringEntity(data.serialize());
        request.setEntity(entity);

		// Execute your request and catch response
        HttpResponse response = httpClient.execute(request);

        // Get string response
        HttpEntity resEntity = response.getEntity();
		return JSONObject.parse(IOUtils.toString(resEntity.getContent()));
	}

}
