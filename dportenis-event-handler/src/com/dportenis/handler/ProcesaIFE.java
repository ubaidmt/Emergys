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
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import com.dportenis.handler.constants.DPSettings;
import com.dportenis.wtm.bean.BatchAttributes;
import com.dportenis.wtm.bean.BatchDCO;
import com.dportenis.wtm.bean.Page;
import com.dportenis.wtm.bean.Upload;
import com.dportenis.wtm.endpoint.CreateBatch;
import com.dportenis.wtm.endpoint.GetBatchAttributes;
import com.dportenis.wtm.endpoint.UploadFile;
import com.dportenis.wtm.endpoint.GetPageFile;
import com.dportenis.wtm.endpoint.ReleaseBatch;
import com.dportenis.wtm.endpoint.SaveBatchAttribute;

public class ProcesaIFE implements EventActionHandler {
	
	private java.util.Properties props;

    public ProcesaIFE() {
    }
    
    /**
     * Event action para crear batch en Datacap y procesar IFE
     */
    public void onEvent(ObjectChangeEvent event, Id subId) {
    	
        File tmpFile = null;
        OutputStream out = null;
        
        try 
        {
	        // Get object store
	        ObjectStore os = event.getObjectStore();
	        
	        // Load settings
	        Document settings = Factory.Document.fetchInstance(os, DPSettings.DOC_PATH, null);
	        ContentTransfer ct = getContentTransfer(settings);
	        java.util.Properties props = new java.util.Properties();
	        props.load(ct.accessContentStream());
	        setProps(props);
			if (isDebug())
				System.out.println("Archivo de settings cargado");
   
	        // Get source document
			if (isDebug())	        			
				System.out.println("Obteniendo documento origen...");
	        Document doc = (Document) event.get_SourceObject();
	        if (isDebug())	        
	        	System.out.println("Documento a procesar con Id " + doc.get_Id().toString());
	        
	        // Create temporary file to be uploaded
	        if (isDebug())	        	        
	        	System.out.println("Generando archivo temporal...");
			ct = getContentTransfer(doc);
			tmpFile = new File(props.getProperty(DPSettings.PROPS_DC_SCRATCH) + File.separator + ProcesaIFE.getTimeStamp() + "." + FilenameUtils.getExtension(ct.get_RetrievalName()));
			out = new FileOutputStream(tmpFile);
			IOUtils.copy(ct.accessContentStream(), out);
			IOUtils.closeQuietly(out);
			if (!tmpFile.exists())
				throw new RuntimeException("El archivo temporal no pudo ser generado: " + tmpFile.getAbsoluteFile());
			if (isDebug())	        
				System.out.println("Archivo temporal generado: " + tmpFile.getAbsoluteFile());
			
	        // Process document
	        doProcesaIFE(doc.get_Id().toString(), tmpFile);
        
        } 
        catch (Exception e) 
        {
        	System.out.println(e.getMessage());
		} 
        finally 
        {
			FileUtils.deleteQuietly(tmpFile);
		}	        
    }
    
	public ContentTransfer getContentTransfer(Document doc) throws Exception {
    	ContentElementList cel = doc.get_ContentElements();
    	ContentTransfer ct = (ContentTransfer)cel.get(0);
    	return ct;
	} 
	
	public BatchAttributes doProcesaIFE(String docId, File tmpFile) throws Exception {
		
		boolean retry = true;
		int currentRetry = 1;
		String service;
		BatchAttributes batch = new BatchAttributes();
		
		if (isDebug())	        
			System.out.println("Creando nuevo batch...");
		
		// Create Datacap Batch
		StringBuffer payload = new StringBuffer();
		payload.append("<createBatchAttributes>");
		payload.append("<application>" + props.getProperty(DPSettings.PROPS_DC_APP) + "</application>");
		payload.append("<job>" + props.getProperty(DPSettings.PROPS_DC_JOB) + "</job>");
		payload.append("</createBatchAttributes>");
		
		if (isDebug())	        
			System.out.println("Payload: " + payload.toString());
		
		retry = true;
		currentRetry = 1;
		while (retry)
		{
			retry = false;
			try 
			{
				service = props.getProperty(DPSettings.PROPS_WTM_CONTEXT) + "/Queue/CreateBatch";
				if (isDebug())	        
					System.out.println("Service: " + service);
				CreateBatch createBatch = new CreateBatch();
				batch = createBatch.sendRequest(service, payload.toString(), isDebug());		
				if (batch.getQueueId() == -1)
					throw new RuntimeException("Error al crear el batch, queueId invalido");
				
			} 
			catch (Exception e) 
			{			
				if (currentRetry >= Integer.parseInt(props.getProperty(DPSettings.PROPS_DC_MAXRETRIES, "3")))
					throw new RuntimeException("Error al crear el batch, maximo de reintentos alcanzados");
				retry = true;
				currentRetry++;
				if (isDebug()) {	        
					System.out.println("Error: " + e.getMessage());
					System.out.println("Reintentando...");
				}					
				Thread.sleep(Long.parseLong(props.getProperty(DPSettings.PROPS_DC_WAITTIME, "5000")));
			}	
		}
		
		if (isDebug())
			System.out.println("Nuevo batch creado con batchId " + batch.getBatchId() + " y queueId " + batch.getQueueId());		
		int queueId = batch.getQueueId();
		if (isDebug())
			System.out.println("Asignando valor " + docId + " a xtraBatchField " + props.getProperty(DPSettings.PROPS_DC_XTRAFIELD_IFE) + "...");
		Thread.sleep(Long.parseLong(props.getProperty(DPSettings.PROPS_DC_WAITTIME, "5000")));
						
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
		payload.append("<XtraBatchField><field>" + props.getProperty(DPSettings.PROPS_DC_XTRAFIELD_IFE) + "</field><value>" + docId + "</value></XtraBatchField>"); 
		payload.append("</Fields>");
		payload.append("</xtraBatchFields>"); 
		payload.append("</BatchAttrSave>");
		
		if (isDebug())
			System.out.println("Payload: " + payload.toString());
		
		retry = true;
		currentRetry = 1;
		while (retry)
		{
			retry = false;
			try
			{
				service = props.getProperty(DPSettings.PROPS_WTM_CONTEXT) + "/Queue/SaveBatchAttribute/" + props.getProperty(DPSettings.PROPS_DC_APP);
				if (isDebug())
					System.out.println("Service: " + service);
				SaveBatchAttribute saveBatch = new SaveBatchAttribute();
				int returnCode = saveBatch.sendRequest(service, payload.toString(), isDebug());
		
				if (returnCode != 0)
					throw new RuntimeException("Error al guardar los atributos del batch. Codigo de error " + returnCode);
				
				// Validate IFE Document Id Correlation
				if (isDebug())
					System.out.println("Validando...");
				Thread.sleep(Long.parseLong(props.getProperty(DPSettings.PROPS_DC_WAITTIME, "5000")));
				service = props.getProperty(DPSettings.PROPS_WTM_CONTEXT) + "/Queue/GetBatchAttributes/" + props.getProperty(DPSettings.PROPS_DC_APP) + "/" + queueId;
				if (isDebug())
					System.out.println("Service: " + service);
				GetBatchAttributes getBatch = new GetBatchAttributes();
				batch = getBatch.sendRequest(service, isDebug());
		
				if (batch.getQueueId() == -1)
					throw new RuntimeException("Error al leer los atributos del batch, queueId invalido");
				
				Map<String, String> xtraBatchFields = batch.getXtraBatchFields();
				if (!xtraBatchFields.containsKey(props.getProperty(DPSettings.PROPS_DC_XTRAFIELD_IFE)) || xtraBatchFields.get(props.getProperty(DPSettings.PROPS_DC_XTRAFIELD_IFE)).isEmpty())
					throw new RuntimeException("Error al establecer el docId de correlacion");
				
			} 
			catch (Exception e) 
			{		
				if (currentRetry >= Integer.parseInt(props.getProperty(DPSettings.PROPS_DC_MAXRETRIES, "3")))
					throw new RuntimeException("Error al establecer el docId de correlacion, maximo de reintentos alcanzados");
				retry = true;
				currentRetry++;
				if (isDebug()) {	        
					System.out.println("Error: " + e.getMessage());
					System.out.println("Reintentando...");
				}					
				Thread.sleep(Long.parseLong(props.getProperty(DPSettings.PROPS_DC_WAITTIME, "5000")));
			}
		}

		if (isDebug()) {
			System.out.println("xtraBatchField asignado correctamente");
			System.out.println("Realizando upload de imagen " + tmpFile.getName() + "...");		
		}
		
		// Upload File
		retry = true;
		currentRetry = 1;
		while (retry)
		{		
			retry = false;
			try
			{			
				service = props.getProperty(DPSettings.PROPS_WTM_CONTEXT) + "/Queue/UploadFile/" + props.getProperty(DPSettings.PROPS_DC_APP) + "/" + queueId;
				if (isDebug())
					System.out.println("Service: " + service);
				UploadFile upladFile = new UploadFile();
				Upload upload = upladFile.sendRequest(service, tmpFile, isDebug());
				
				if (!tmpFile.getName().equals(upload.getOriginalFileName()))
					throw new RuntimeException("No se completo el upload de la imagen en el batch");
				
				// Validate File Upload
				if (isDebug())
					System.out.println("Validando...");
				Thread.sleep(Long.parseLong(props.getProperty(DPSettings.PROPS_DC_WAITTIME, "5000")));
				service = props.getProperty(DPSettings.PROPS_WTM_CONTEXT) + "/Queue/GetPageFile/" + props.getProperty(DPSettings.PROPS_DC_APP) + "/" + queueId;
				if (isDebug())
					System.out.println("Service: " + service);
				GetPageFile getPageFile = new GetPageFile();
				BatchDCO batchDCO = getPageFile.sendRequest(service, isDebug());
				
				if (batchDCO.getId().isEmpty())
					throw new RuntimeException("Error al leer el pagefile del batch");
				
				List<Page> pages = batchDCO.getPages();
				if (pages.size() == 0)
					throw new RuntimeException("No se encontro el objecto de la pagina en el pagefile");

			}
			catch (Exception e)
			{			
				if (currentRetry >= Integer.parseInt(props.getProperty(DPSettings.PROPS_DC_MAXRETRIES, "3")))	
					throw new RuntimeException("Error al realizar el upload de la imagen, maximo de reintentos alcanzados");
				retry = true;
				currentRetry++;
				if (isDebug()) {	        
					System.out.println("Error: " + e.getMessage());
					System.out.println("Reintentando...");
				}					
				Thread.sleep(Long.parseLong(props.getProperty(DPSettings.PROPS_DC_WAITTIME, "5000")));
			}
		}

		if (isDebug()) {
			System.out.println("Upload de imagen completado");
			System.out.println("Liberando batch...");
		}
		
		retry = true;
		currentRetry = 1;
		while (retry)
		{						
			retry = false;
			try
			{				
				// Release batch for recognition
				service = props.getProperty(DPSettings.PROPS_WTM_CONTEXT) + "/Queue/ReleaseBatch/" + props.getProperty(DPSettings.PROPS_DC_APP) + "/" + queueId + "/finished";
				if (isDebug())
					System.out.println("Service: " + service);
				ReleaseBatch releaseBatch = new ReleaseBatch();
				releaseBatch.sendRequest(service, isDebug());	
				
				// Validate Batch Release
				if (isDebug())
					System.out.println("Validando...");
				Thread.sleep(Long.parseLong(props.getProperty(DPSettings.PROPS_DC_WAITTIME, "5000")));
				service = props.getProperty(DPSettings.PROPS_WTM_CONTEXT) + "/Queue/GetBatchAttributes/" + props.getProperty(DPSettings.PROPS_DC_APP) + "/" + queueId;
				if (isDebug())
					System.out.println("Service: " + service);
				GetBatchAttributes getBatch = new GetBatchAttributes();
				batch = getBatch.sendRequest(service, isDebug());
		
				if (batch.getQueueId() == -1)
					throw new RuntimeException("Error al leer los atributos del batch, queueId invalido");
				
				if (!batch.getTask().equals(props.getProperty(DPSettings.PROPS_DC_RELEASEPROFILE))) 
					throw new RuntimeException("El batch no se libero en el trask profile esperado");
			
			}
			catch (Exception e)
			{			
				if (currentRetry >= Integer.parseInt(props.getProperty(DPSettings.PROPS_DC_MAXRETRIES, "3")))	
					throw new RuntimeException("Error al liberar el batch, maximo de reintentos alcanzados");
				retry = true;
				currentRetry++;
				if (isDebug()) {	        
					System.out.println("Error: " + e.getMessage());
					System.out.println("Reintentando...");
				}					
				Thread.sleep(Long.parseLong(props.getProperty(DPSettings.PROPS_DC_WAITTIME, "5000")));
			}
		}

		if (isDebug())
			System.out.println("Batch liberado");		
		
		return batch;
		
	}
	
	public java.util.Properties getProps() {
		return props;
	}

	public void setProps(java.util.Properties props) {
		this.props = props;
	}	
	
	public synchronized static String getTimeStamp() throws Exception {
		return String.valueOf(Calendar.getInstance().getTimeInMillis());
	}
	
	public boolean isDebug() {
		return Boolean.parseBoolean(props.getProperty(DPSettings.PROPS_WTM_DEBUG, "false"));			
	}

}