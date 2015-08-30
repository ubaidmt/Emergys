package com.dportenis.wtm.bean;

public class Page {
	
	String id;
	int status = -1;
	String type; 
	String imageFile;	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}	
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getImageFile() {
		return imageFile;
	}
	public void setImageFile(String imageFile) {
		this.imageFile = imageFile;
	}

}
