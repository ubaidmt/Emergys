package com.dportenis.rest.module;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import com.ibm.json.java.JSONObject;

@Path(value="/po")
public class OrdenCompra {
	
	public OrdenCompra() {
	}
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)	
    @Path(value="/datos")	
    public JSONObject doExec(JSONObject requestObj)
    {  		  
    		JSONObject responseObj = new JSONObject();
	    	int code = 0;
	    	String message = null;
	    	
	    	try
	    	{
	    		message = "mensaje recibido: " + requestObj.toString();
	    }
	    	catch (Exception e)
	    	{
	    		code = 1;
	    		message = e.getMessage();
	    		e.printStackTrace();
	    	}
	    	finally
	    	{
	    		responseObj.put("codigo", code);
	    		responseObj.put("mensaje", message);  
	    	}
	    	
	    	return responseObj;
    }
}
