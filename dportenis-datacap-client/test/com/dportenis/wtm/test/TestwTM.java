package com.dportenis.wtm.test;

import java.util.List;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import org.apache.commons.io.IOUtils;

import com.dportenis.wtm.bean.BatchAttributes;
import com.dportenis.wtm.bean.BatchDCO;
import com.dportenis.wtm.bean.Page;
import com.dportenis.wtm.bean.Upload;
import com.dportenis.wtm.endpoint.CreateBatch;
import com.dportenis.wtm.endpoint.UploadFile;
import com.dportenis.wtm.endpoint.ReleaseBatch;
import com.dportenis.wtm.endpoint.GetBatchAttributes;
import com.dportenis.wtm.endpoint.GetPageFile;
import com.dportenis.wtm.endpoint.SaveBatchAttribute;

public class TestwTM {
	
	private static final String URL_CONTEXT = "http://192.168.185.130:90/ServicewTM.svc";
	private static final String APP_NAME = "POC";
	private static final String JOB_NAME = "Demo";
	private int QUEUEID = 47;
	
	private Log log = LogFactory.getLog(TestwTM.class);

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCreateBatch() {
		
		String service = URL_CONTEXT + "/Queue/CreateBatch";
		StringBuffer payload = new StringBuffer();
		payload.append("<createBatchAttributes>");
		payload.append("<application>" + APP_NAME + "</application>");
		payload.append("<job>" + JOB_NAME + "</job>");
		payload.append("</createBatchAttributes>");
		
		try {	
			
			CreateBatch createBatch = new CreateBatch();
			BatchAttributes batch = createBatch.sendRequest(service, payload.toString(), true);
			
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
		
		String service = URL_CONTEXT + "/Queue/GetBatchAttributes/" + APP_NAME + "/" + QUEUEID;
		
		try {
			
			GetBatchAttributes getBatch = new GetBatchAttributes();
			BatchAttributes batch = getBatch.sendRequest(service, true);

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
	public void testGetPageFile() {
		
		String service = URL_CONTEXT + "/Queue/GetPageFile/" + APP_NAME + "/" + QUEUEID;
		
		try {
			
			GetPageFile getPageFile = new GetPageFile();
			BatchDCO batchDCO = getPageFile.sendRequest(service, true);

			if (batchDCO.getId().isEmpty())
				throw new RuntimeException("Error al leer el pagefile del batch");
			
			log.debug("Pagefile Obtenido");
			log.debug("BatchId: " + batchDCO.getId());
			log.debug("BatchStatus: " + batchDCO.getStatus());
			log.debug("BatchType: " + batchDCO.getType());
			List<Page> pages = batchDCO.getPages();
			log.debug("NumPages: " + pages.size());
			for (Page page : pages) {
				log.debug("PageId: " + page.getId());
				log.debug("PageStatus: " + page.getStatus());
				log.debug("PageType: " + page.getType());
				log.debug("PageImageFile: " + page.getImageFile());
			}
			
		} catch (Exception e) {
			log.error(e);
		}
	}		
	
	@Test
	public void testSaveBatchAttribute() {
		
		try {

			String service = URL_CONTEXT + "/Queue/GetBatchAttributes/" + APP_NAME + "/" + QUEUEID;
			GetBatchAttributes getBatch = new GetBatchAttributes();
			BatchAttributes batch = getBatch.sendRequest(service, true);

			if (batch.getQueueId() == -1)
				throw new RuntimeException("Error al leer los atributos del batch");
			
			StringBuffer payload = new StringBuffer();
			payload.append("<BatchAttrSave>");
			payload.append("<batchDir>" + batch.getBatchdir() + "</batchDir>");
			payload.append("<operationUser>admin</operationUser>"); 
			payload.append("<pageFile>VScan.xml</pageFile>");
			payload.append("<queueID>" + batch.getQueueId() + "</queueID>");
			payload.append("<station>1</station>"); 
			payload.append("<xtraBatchFields>"); 
			payload.append("<Count>1</Count>"); 
			payload.append("<Fields>");
			payload.append("<XtraBatchField><field>pb_IFEDocID</field><value>ABCD</value></XtraBatchField>"); 
			payload.append("</Fields>");
			payload.append("</xtraBatchFields>"); 
			payload.append("</BatchAttrSave>");		
		
			service = URL_CONTEXT + "/Queue/SaveBatchAttribute/" + APP_NAME;
			SaveBatchAttribute saveBatch = new SaveBatchAttribute();
			int returnCode = saveBatch.sendRequest(service, payload.toString(), true);

			if (returnCode != 0)
				throw new RuntimeException("Error al salvar los atributos del batch");
			
			log.debug("Batch Salvado");
			
		} catch (Exception e) {
			log.error(e);
		}
	}	
	
	@Test
	public void testUploadFile() {
		
		String service = URL_CONTEXT + "/Queue/UploadFile/" + APP_NAME + "/" + QUEUEID;
		String fileToUpload = "/Users/juansaad/Downloads/ife10.jpg";
		FileOutputStream out = null;
		
		try {
			
			File tmpFile = File.createTempFile("tmp", ".jpg");
			out = new FileOutputStream(tmpFile);
			IOUtils.copy(new FileInputStream(new File(fileToUpload)), out);
			UploadFile uploadFile = new UploadFile();
			Upload upload = uploadFile.sendRequest(service, tmpFile, true);
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
		
		String service = URL_CONTEXT + "/Queue/ReleaseBatch/" + APP_NAME + "/" + QUEUEID + "/finished";
		
		try {
			
			ReleaseBatch releaseBatch = new ReleaseBatch();
			releaseBatch.sendRequest(service, true);
			log.debug("Batch Liberado");		
			
		} catch (Exception e) {
			log.error(e);
		}
	}
	
	@Test
	public void testFullTransaction() {
		
		String service;
		BatchAttributes batch;
		StringBuffer payload;
		int queueId;
		FileOutputStream out = null;
		
		try {	

			// create batch
			payload = new StringBuffer();
			payload.append("<createBatchAttributes>");
			payload.append("<application>" + APP_NAME + "</application>");
			payload.append("<job>" + JOB_NAME + "</job>");
			payload.append("</createBatchAttributes>");
			
			service = URL_CONTEXT + "/Queue/CreateBatch";			
			CreateBatch createBatch = new CreateBatch();
			batch = createBatch.sendRequest(service, payload.toString(), true);
			
			if (batch.getQueueId() == -1)
				throw new RuntimeException("El batch no pudo ser creado");
			
			// get batch identifier
			queueId = batch.getQueueId();

			// save batch
			payload = new StringBuffer();
			payload.append("<BatchAttrSave>");
			payload.append("<batchDir>" + batch.getBatchdir() + "</batchDir>");
			payload.append("<operationUser>admin</operationUser>"); 
			payload.append("<pageFile>VScan.xml</pageFile>");
			payload.append("<queueID>" + queueId + "</queueID>");
			payload.append("<station>1</station>");  
			payload.append("<xtraBatchFields></xtraBatchFields>"); 		
			payload.append("</BatchAttrSave>");		
		
			service = URL_CONTEXT + "/Queue/SaveBatchAttribute/" + APP_NAME;
			SaveBatchAttribute saveBatch = new SaveBatchAttribute();
			int returnCode = saveBatch.sendRequest(service, payload.toString(), true);

			if (returnCode != 0)
				throw new RuntimeException("Error al salvar los atributos del batch");	
			
			// upload file
			String fileToUpload = "/Users/juansaad/Downloads/Orden de Compra 0051.pdf";
			File scratch = new File("/Users/juansaad/Downloads");
			File tmpFile = File.createTempFile("tmp", ".pdf", (scratch.exists() && scratch.isDirectory() ? scratch : null));
			tmpFile.deleteOnExit();
			out = new FileOutputStream(tmpFile);
			IOUtils.copy(new FileInputStream(new File(fileToUpload)), out);
			
			service = URL_CONTEXT + "/Queue/UploadFile/" + APP_NAME + "/" + queueId;
			UploadFile uploadFile = new UploadFile();
			Upload upload = uploadFile.sendRequest(service, tmpFile, true);
			
			if (!tmpFile.getName().equals(upload.getOriginalFileName()))
				throw new RuntimeException("El archivo " + fileToUpload + " no puedo ser enviado");		
			
			// release batch
			service = URL_CONTEXT + "/Queue/ReleaseBatch/" + APP_NAME + "/" + queueId + "/finished";
			ReleaseBatch releaseBatch = new ReleaseBatch();
			releaseBatch.sendRequest(service, true);

			log.debug("Batch Procesado");			
			
		} catch (Exception e) {
			log.error(e);
		
		} finally {
			IOUtils.closeQuietly(out);
		}
		
	}

}
