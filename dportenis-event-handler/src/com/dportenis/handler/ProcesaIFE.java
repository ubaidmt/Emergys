package com.dportenis.handler;

import com.filenet.api.engine.EventActionHandler;
import com.filenet.api.events.ObjectChangeEvent;
import com.filenet.api.util.Id;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.core.Factory;
import com.filenet.api.core.Document;
import com.filenet.api.core.ContentTransfer;
import com.filenet.api.collection.ContentElementList;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;

import org.apache.commons.io.FilenameUtils;

import com.dportenis.handler.constants.DPSettings;
import com.dportenis.wtm.bean.Batch;
import com.dportenis.wtm.bean.Upload;
import com.dportenis.wtm.endpoint.CreateBatch;
import com.dportenis.wtm.endpoint.UploadFile;
import com.dportenis.wtm.endpoint.ReleaseBatch;
import com.dportenis.wtm.endpoint.SaveBatchAttribute;

public class ProcesaIFE implements EventActionHandler {

    public ProcesaIFE() {
    }
    
    /**
     * Event action para crear batch en Datacap y procesar IFE
     */
    public void onEvent(ObjectChangeEvent event, Id subId) {
    	
        System.out.println("Evento para procesar IFE recibido");
        
        try {
        		
	        // Get Object Store
	        ObjectStore os = event.getObjectStore();
	        
	        // Load Settings
	        Document settings = Factory.Document.fetchInstance(os, DPSettings.DOC_PATH, null);
	        java.util.Properties props = new java.util.Properties();
	        ContentTransfer ct = getContentTransfer(settings);
	        props.load(ct.accessContentStream());
	        
	        // Get Source Document
	        Document doc = (Document) event.get_SourceObject();
	        Batch batch = doProcesaIFE(doc, props);
	        
	        System.out.println("El nuevo batch " + batch.getBatchId() + " fue creado para procesar IFE con Id " + doc.get_Id().toString());
        
        } catch (Exception e) {
        	e.printStackTrace();
        }
    }
    
	public static ContentTransfer getContentTransfer(Document doc) throws Exception {
    	ContentElementList cel = doc.get_ContentElements();
    	ContentTransfer ct = (ContentTransfer)cel.get(0);
    	return ct;
	} 
	
	public static Batch doProcesaIFE(Document doc, java.util.Properties props) throws Exception {

		String service;
		Batch batch;
		StringBuffer payload;
		int queueId;
		FileOutputStream out = null;
		
		try {	

			// Create Datacap Batch
			service = props.getProperty(DPSettings.PROPS_WTM_CONTEXT) + "/service/Queue/CreateBatch";
			payload = new StringBuffer();
			payload.append("<createBatchAttributes>");
			payload.append("<application>" + props.getProperty(DPSettings.PROPS_DC_APP) + "</application>");
			payload.append("<job>" + props.getProperty(DPSettings.PROPS_DC_JOB) + "</job>");
			payload.append("</createBatchAttributes>");
			batch = CreateBatch.sendRequest(service, payload.toString());
			
			if (batch.getQueueId() == -1)
				throw new RuntimeException("El batch no pudo ser creado");
			
			queueId = batch.getQueueId();
			
			// Set IFE Document Id as Correlation
			payload = new StringBuffer();
			payload.append("<BatchAttrSave>");
			payload.append("<batchDir>" + batch.getBatchdir() + "</batchDir>");
			payload.append("<operationUser>"+ props.getProperty(DPSettings.PROPS_DC_OPERATOR) + "</operationUser>"); 
			payload.append("<pageFile>" + props.getProperty(DPSettings.PROPS_DC_PAGEFILE) + "</pageFile>");
			payload.append("<queueID>" + queueId + "</queueID>");
			payload.append("<station>" + props.getProperty(DPSettings.PROPS_DC_STATION) + "</station>"); 
			payload.append("<xtraBatchFields>"); 
			payload.append("<Count>1</Count>"); 
			payload.append("<Fields>");
			payload.append("<XtraBatchField><field>" + props.getProperty(DPSettings.PROPS_DC_XTRAFIELD_IFE) + "</field><value>" + doc.get_Id().toString() + "</value></XtraBatchField>"); 
			payload.append("</Fields>");
			payload.append("</xtraBatchFields>"); 
			payload.append("</BatchAttrSave>");		
		
			service = props.getProperty(DPSettings.PROPS_WTM_CONTEXT) + "/service/Queue/SaveBatchAttribute/" + props.getProperty(DPSettings.PROPS_DC_APP);
			int returnCode = SaveBatchAttribute.sendRequest(service, payload.toString());

			if (returnCode != 0)
				throw new RuntimeException("Error al guardar los atributos del batch");
			
			// Upload File
			ContentTransfer ct = getContentTransfer(doc);	
			File scratch = new File(props.getProperty(DPSettings.PROPS_DC_SCRATCH));
			File tmpFile = File.createTempFile(FilenameUtils.removeExtension(ct.get_RetrievalName()), "." + FilenameUtils.getExtension(ct.get_RetrievalName()), (scratch.exists() && scratch.isDirectory() ? scratch : null));
			out = new FileOutputStream(tmpFile);
			IOUtils.copy(ct.accessContentStream(), out);
			
			service = props.getProperty(DPSettings.PROPS_WTM_CONTEXT) + "/service/Queue/UploadFile/" + props.getProperty(DPSettings.PROPS_DC_APP) + "/" + queueId;
			Upload upload = UploadFile.sendRequest(service, tmpFile);
			
			tmpFile.deleteOnExit();
			
			if (!tmpFile.getName().equals(upload.getOriginalFileName()))
				throw new RuntimeException("No se completo el upload del document en el batch");		
			
			// Release batch for recognition
			service = props.getProperty(DPSettings.PROPS_WTM_CONTEXT) + "/service/Queue/ReleaseBatch/" + props.getProperty(DPSettings.PROPS_DC_APP) + "/" + queueId + "/finished";
			ReleaseBatch.sendRequest(service);			
		
		} finally {
			IOUtils.closeQuietly(out);
		}	
		
		return batch;
		
	}

}