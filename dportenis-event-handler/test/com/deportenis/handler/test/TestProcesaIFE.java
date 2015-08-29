package com.deportenis.handler.test;

import javax.security.auth.Subject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dportenis.handler.constants.DPSettings;
import com.filenet.api.core.Connection;
import com.filenet.api.core.ContentTransfer;
import com.filenet.api.core.Document;
import com.filenet.api.core.Factory;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.util.UserContext;

import com.dportenis.handler.ProcesaIFE;
import com.dportenis.wtm.bean.Batch;

public class TestProcesaIFE {

	private static final String server = "http://p8server:9080/wsi/FNCEWS40MTOM/";
	private static final String user = "p8admin";
	private static final String password = "filenet";
	private static final String stanza = "FileNetP8WSI";
	private static final String osName = "ExcelECMOS";
	
	private ObjectStore os;

	@Before
	public void setUp() throws Exception {
		try
		{
    		Connection con = Factory.Connection.getConnection(server);
    	    Subject subject = UserContext.createSubject(con, user, password, stanza);
    	    UserContext.get().pushSubject(subject); 	
    	    os = Factory.ObjectStore.fetchInstance(Factory.Domain.getInstance(con,null), osName, null);	
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		} 		
	}

	@After
	public void tearDown() throws Exception {
		UserContext.get().popSubject();
	}

	@Test
	public void testProcesaIFE() {
		
		String objectId = "{079E8C75-B3F0-408D-A30F-7C16AD5A5E95}";
		
		try
		{
	        // Load Settings
	        Document settings = Factory.Document.fetchInstance(os, DPSettings.DOC_PATH, null);
	        java.util.Properties props = new java.util.Properties();
	        ContentTransfer ct = ProcesaIFE.getContentTransfer(settings);
	        props.load(ct.accessContentStream());
	        
	        // Procesa IFE
	        Document doc = Factory.Document.fetchInstance(os, objectId, null);
	        Batch batch = ProcesaIFE.doProcesaIFE(doc, props);
	        
	        System.out.println("El nuevo batch " + batch.getBatchId() + " fue creado para procesar IFE con Id " + doc.get_Id().toString());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
