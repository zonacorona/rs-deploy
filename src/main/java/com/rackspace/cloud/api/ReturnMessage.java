package com.rackspace.cloud.api;

import java.io.Serializable;

public class ReturnMessage implements Serializable{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1382744144831790772L;
	private String message;
	private String status;
	
	

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	
}
