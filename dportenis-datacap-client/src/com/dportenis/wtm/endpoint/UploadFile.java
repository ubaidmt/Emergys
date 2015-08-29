package com.dportenis.wtm.endpoint;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import java.io.File;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.xml.sax.SAXException;

import com.dportenis.wtm.xml.Response;
import com.dportenis.wtm.bean.Upload;

public class UploadFile {
	
	public static Upload sendRequest(String url, File file)  throws ClientProtocolException, IOException, SAXException, ParserConfigurationException {
		
		// Response File Upload Attributes
		Upload bean = new Upload();		
		
		// create HTTP Client
		CloseableHttpClient httpClient = HttpClients.createDefault();
		
		try {
			
			// Create HTTP Request
			HttpPost request = new HttpPost(url);
			
			// Set Multipart Body
			FileBody bin = new FileBody(file);

            HttpEntity reqEntity = MultipartEntityBuilder.create()
                    .addPart("bin", bin)
                    .build();

            request.setEntity(reqEntity);	
	
			// Execute your request and catch response
	        CloseableHttpResponse response = httpClient.execute(request);
	        
			// Check for HTTP response code: 201 = success
			if (response.getStatusLine().getStatusCode() != 201)
				throw new RuntimeException("HTTP error. " + response.getStatusLine().getStatusCode() + ". " + response.getStatusLine().getReasonPhrase());	        
	        
			// Get-Capture Complete text/xml body response
            try {
            	
                HttpEntity resEntity = response.getEntity();
                Response responseXML = new Response(resEntity.getContent());    
                bean.setOriginalFileName(responseXML.getOriginalFileName());
                bean.setPageId(responseXML.getPageId());               
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
