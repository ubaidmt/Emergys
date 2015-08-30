package com.dportenis.wtm.bean;

import java.util.Map;
import java.util.HashMap;

public class BatchAttributes {
	
	private String batchId;
	private String batchdir;
	private int queueId = -1;
	private int priority = -1;
	private String job;
	private String status;
	private String task;
	private String operatorName;
	private String station;
	private Map<String,String> xtraBatchFields = new HashMap<String,String>();
	
	public String getOperatorName() {
		return operatorName;
	}
	public void setOperatorName(String operatorName) {
		this.operatorName = operatorName;
	}
	public String getStation() {
		return station;
	}
	public void setStation(String station) {
		this.station = station;
	}	
	public Map<String, String> getXtraBatchFields() {
		return xtraBatchFields;
	}
	public void setXtraBatchFields(Map<String, String> xtraBatchFields) {
		this.xtraBatchFields = xtraBatchFields;
	} 
	public int getPriority() {
		return priority;
	}
	public void setPriority(int priority) {
		this.priority = priority;
	}
	public String getJob() {
		return job;
	}
	public void setJob(String job) {
		this.job = job;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getTask() {
		return task;
	}
	public void setTask(String task) {
		this.task = task;
	}
	public String getBatchId() {
		return batchId;
	}
	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}
	public String getBatchdir() {
		return batchdir;
	}
	public void setBatchdir(String batchdir) {
		this.batchdir = batchdir;
	}
	public int getQueueId() {
		return queueId;
	}
	public void setQueueId(int queueId) {
		this.queueId = queueId;
	}

}
