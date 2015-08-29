package com.dportenis.wtm.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import org.apache.commons.io.IOUtils;

import com.dportenis.wtm.bean.Batch;
import com.dportenis.wtm.bean.Upload;
import com.dportenis.wtm.endpoint.CreateBatch;
import com.dportenis.wtm.endpoint.UploadFile;
import com.dportenis.wtm.endpoint.ReleaseBatch;
import com.dportenis.wtm.endpoint.GetBatchAttributes;
import com.dportenis.wtm.endpoint.SaveBatchAttribute;

public class TestwTM {
	
	private static final String URL_CONTEXT = "http://192.168.185.159:82";
	private static final String APP_NAME = "Dportenis";
	private static final String JOB_NAME = "Mobile Job";
	private int QUEUEID = 968;
	
	private Log log = LogFactory.getLog(TestwTM.class);

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCreateBatch() {
		
		String service = URL_CONTEXT + "/service/Queue/CreateBatch";
		StringBuffer payload = new StringBuffer();
		payload.append("<createBatchAttributes>");
		payload.append("<application>" + APP_NAME + "</application>");
		payload.append("<job>" + JOB_NAME + "</job>");
		payload.append("</createBatchAttributes>");
		
		try {	
			
			Batch batch = CreateBatch.sendRequest(service, payload.toString());
			
			if (batch.getQueueId() == -1)
				throw new RuntimeException("El batch no pudo ser creado");
			
			log.debug("Batch Creado");
			log.debug("BatchId: " + batch.getBatchId());
			log.debug("BatchDir: " + batch.getBatchdir());
			log.debug("QueueId: " + batch.getQueueId());
			
		} catch (Exception e) {
			log.error(e);
		}		
	}
	
	@Test
	public void testGetBatchAttributes() {
		
		String service = URL_CONTEXT + "/service/Queue/GetBatchAttributes/" + APP_NAME + "/" + QUEUEID;
		
		try {
			
			Batch batch = GetBatchAttributes.sendRequest(service);

			if (batch.getQueueId() == -1)
				throw new RuntimeException("Error al leer los atributos del batch");
			
			log.debug("Batch Obtenido");
			log.debug("BatchId: " + batch.getBatchId());
			log.debug("BatchDir: " + batch.getBatchdir());
			log.debug("QueueId: " + batch.getQueueId());
			log.debug("Job: " + batch.getJob());
			log.debug("Task: " + batch.getTask());
			log.debug("Status: " + batch.getStatus());
			log.debug("Priority: " + batch.getPriority());
			log.debug("Operator: " + batch.getOperatorName());
			log.debug("Station: " + batch.getStation());
			log.debug("XtraBatchFields: " + batch.getXtraBatchFields().toString());
			
		} catch (Exception e) {
			log.error(e);
		}
	}	
	
	@Test
	public void testSaveBatchAttribute() {
		
		try {

			String service = URL_CONTEXT + "/service/Queue/GetBatchAttributes/" + APP_NAME + "/" + QUEUEID;
			Batch batch = GetBatchAttributes.sendRequest(service);

			if (batch.getQueueId() == -1)
				throw new RuntimeException("Error al leer los atributos del batch");
			
			StringBuffer payload = new StringBuffer();
			payload.append("<BatchAttrSave>");
			payload.append("<batchDir>" + batch.getBatchdir() + "</batchDir>");
			payload.append("<operationUser>admin</operationUser>"); 
			payload.append("<pageFile>MobileScan.xml</pageFile>");
			payload.append("<queueID>" + batch.getQueueId() + "</queueID>");
			payload.append("<station>1</station>"); 
			payload.append("<xtraBatchFields>"); 
			payload.append("<Count>1</Count>"); 
			payload.append("<Fields>");
			payload.append("<XtraBatchField><field>pb_IFEDocID</field><value>ABCD</value></XtraBatchField>"); 
			payload.append("</Fields>");
			payload.append("</xtraBatchFields>"); 
			payload.append("</BatchAttrSave>");		
		
			service = URL_CONTEXT + "/service/Queue/SaveBatchAttribute/" + APP_NAME;
			int returnCode = SaveBatchAttribute.sendRequest(service, payload.toString());

			if (returnCode != 0)
				throw new RuntimeException("Error al salvar los atributos del batch");
			
			log.debug("Batch Salvado");
			
		} catch (Exception e) {
			log.error(e);
		}
	}	
	
	@Test
	public void testUploadFile() {
		
		String service = URL_CONTEXT + "/service/Queue/UploadFile/" + APP_NAME + "/" + QUEUEID;
		String fileToUpload = "/Users/juansaad/Downloads/ife10.jpg";
		FileOutputStream out = null;
		
		try {
			
			File tmpFile = File.createTempFile("tmp", ".jpg");
			out = new FileOutputStream(tmpFile);
			IOUtils.copy(new FileInputStream(new File(fileToUpload)), out);
			Upload upload = UploadFile.sendRequest(service, tmpFile);
			tmpFile.deleteOnExit();
			
			if (!tmpFile.getName().equals(upload.getOriginalFileName()))
				throw new RuntimeException("El archivo " + fileToUpload + " no puedo ser enviado");
			
			log.debug("Imagen Enviada");
			log.debug("OriginalFileName: " + upload.getOriginalFileName());
			log.debug("PageId: " + upload.getPageId());
			
		} catch (Exception e) {
			log.error(e);
		}
		finally {
			IOUtils.closeQuietly(out);
		}
	}
	
	@Test
	public void testReleaseBatch() {
		
		String service = URL_CONTEXT + "/service/Queue/ReleaseBatch/" + APP_NAME + "/" + QUEUEID + "/finished";
		
		try {
			
			ReleaseBatch.sendRequest(service);
			log.debug("Batch Liberado");		
			
		} catch (Exception e) {
			log.error(e);
		}
	}
	
	@Test
	public void testFullTransaction() {
		
		String service;
		Batch batch;
		StringBuffer payload;
		int queueId;
		FileOutputStream out = null;
		
		try {	

			service = URL_CONTEXT + "/service/Queue/CreateBatch";
			payload = new StringBuffer();
			payload.append("<createBatchAttributes>");
			payload.append("<application>" + APP_NAME + "</application>");
			payload.append("<job>" + JOB_NAME + "</job>");
			payload.append("</createBatchAttributes>");
			batch = CreateBatch.sendRequest(service, payload.toString());
			
			if (batch.getQueueId() == -1)
				throw new RuntimeException("El batch no pudo ser creado");
			
			queueId = batch.getQueueId();

			payload = new StringBuffer();
			payload.append("<BatchAttrSave>");
			payload.append("<batchDir>" + batch.getBatchdir() + "</batchDir>");
			payload.append("<operationUser>admin</operationUser>"); 
			payload.append("<pageFile>MobileScan.xml</pageFile>");
			payload.append("<queueID>" + queueId + "</queueID>");
			payload.append("<station>1</station>"); 
			payload.append("<xtraBatchFields>"); 
			payload.append("<Count>1</Count>"); 
			payload.append("<Fields>");
			payload.append("<XtraBatchField><field>pb_IFEDocID</field><value>ABCD</value></XtraBatchField>"); 
			payload.append("</Fields>");
			payload.append("</xtraBatchFields>"); 
			payload.append("</BatchAttrSave>");		
		
			service = URL_CONTEXT + "/service/Queue/SaveBatchAttribute/" + APP_NAME;
			int returnCode = SaveBatchAttribute.sendRequest(service, payload.toString());

			if (returnCode != 0)
				throw new RuntimeException("Error al salvar los atributos del batch");	
			
			String fileToUpload = "/Users/juansaad/Downloads/ife0.jpg";
			File scratch = new File("/Users/juansaad/Downloads");
			File tmpFile = File.createTempFile("tmp", ".jpg", (scratch.exists() && scratch.isDirectory() ? scratch : null));
			out = new FileOutputStream(tmpFile);
			IOUtils.copy(new FileInputStream(new File(fileToUpload)), out);
			service = URL_CONTEXT + "/service/Queue/UploadFile/" + APP_NAME + "/" + queueId;
			Upload upload = UploadFile.sendRequest(service, tmpFile);
			tmpFile.deleteOnExit();
			
			if (!tmpFile.getName().equals(upload.getOriginalFileName()))
				throw new RuntimeException("El archivo " + fileToUpload + " no puedo ser enviado");		
			
			service = URL_CONTEXT + "/service/Queue/ReleaseBatch/" + APP_NAME + "/" + queueId + "/finished";
			ReleaseBatch.sendRequest(service);

			log.debug("Batch Procesado");			
			
		} catch (Exception e) {
			log.error(e);
		
		} finally {
			IOUtils.closeQuietly(out);
		}
		
	}

}
