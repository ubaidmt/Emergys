package com.dportenis.wtm.endpoint;

import java.io.File;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.util.EntityUtils;
import org.apache.http.impl.client.HttpClientBuilder;

import com.dportenis.wtm.xml.BatchResponse;
import com.dportenis.wtm.bean.Upload;

public class UploadFile {
	
	protected int timeout = 10000; // default to 10 secs
	
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}	
	
	public Upload sendRequest(String url, File file, boolean debug)  throws Exception {
		
		// Response File Upload Attributes
		Upload bean = new Upload();		
		
		// Create HTTP Client
		RequestConfig config = RequestConfig.custom()
				.setConnectTimeout(timeout)
				.setSocketTimeout(timeout).build();		
		HttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();		
		
		// Create HTTP Request
		HttpPost request = new HttpPost(url);
		
		// Set Multipart Body
		FileBody bin = new FileBody(file);

        HttpEntity reqEntity = MultipartEntityBuilder.create()
                .addPart("bin", bin)
                .build();

        request.setEntity(reqEntity);	

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
        bean.setOriginalFileName(responseXML.getOriginalFileName());
        bean.setPageId(responseXML.getPageId());               
        EntityUtils.consume(resEntity);

		return bean;
		
	}

}
