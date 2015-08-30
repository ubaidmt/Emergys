package com.deportenis.handler.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.InputStream;

import javax.security.auth.Subject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
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
		
		String objectId = "{CE4C1EFB-16E1-4C01-AD0A-2D5317EEFEC4}";
		String settingsFile = "/Users/juansaad/git/Emergys/dportenis-event-handler/settings/dpsettings.properties";
        File tmpFile = null;
        OutputStream out = null;
        InputStream in = null;
		
		try
		{
			ProcesaIFE procesaIFE = new ProcesaIFE();
			
	        // Load settings
	        in = new FileInputStream(new File(settingsFile)); 
	        java.util.Properties props = new java.util.Properties();
	        props.load(in);
	        procesaIFE.setProps(props);
			IOUtils.closeQuietly(in);
			if (procesaIFE.isDebug())
				System.out.println("Archivo de settings cargado");			
			
			// Get document
			if (procesaIFE.isDebug())		
				System.out.println("Obteniendo documento origen...");
	        Document doc = Factory.Document.fetchInstance(os, objectId, null);
			if (procesaIFE.isDebug())        
				System.out.println("Documento a procesar con Id " + doc.get_Id().toString());			
	        
	        // Create temporary file to be uploaded
			if (procesaIFE.isDebug())		
				System.out.println("Generando archivo temporal...");
	        ContentTransfer ct = procesaIFE.getContentTransfer(doc);
			tmpFile = new File(props.getProperty(DPSettings.PROPS_DC_SCRATCH) + File.separator + ProcesaIFE.getTimeStamp() + "." + FilenameUtils.getExtension(ct.get_RetrievalName()));
			out = new FileOutputStream(tmpFile);
			IOUtils.copy(ct.accessContentStream(), out);
			IOUtils.closeQuietly(out);
			if (!tmpFile.exists())
				throw new RuntimeException("El archivo temporal no pudo ser generado: " + tmpFile.getAbsoluteFile());
			if (procesaIFE.isDebug())
				System.out.println("Archivo temporal generado: " + tmpFile.getAbsoluteFile());        
				      
	        // Procesa IFE
			procesaIFE.doProcesaIFE(doc.get_Id().toString(), tmpFile);
	        
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally {    
			FileUtils.deleteQuietly(tmpFile);
		}	
	}

}
