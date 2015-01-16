package com.rackspace.cloud.api.view.model;

import java.io.Serializable;

public class HistoryModel implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7811768235576716266L;
	
	private int count;
	private boolean isEven;
	private String progressImg;
	
	public boolean isEven() {
		return isEven;
	}
	public void isEven(boolean isEven) {
		this.isEven = isEven;
	}
	public String getProgressImg() {
		return progressImg;
	}
	public void setProgressImg(String progressImg) {
		this.progressImg = progressImg;
	}
	private String user;
	private String warName;
	private String docName;
	private String type;
	private String startTime;
	private String endTime;
	private String status;
	private String failReason;
	
	
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getWarName() {
		return warName;
	}
	public void setWarName(String warName) {
		this.warName = warName;
	}
	public String getDocName() {
		return docName;
	}
	public void setDocName(String docName) {
		this.docName = docName;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getStartTime() {
		return startTime;
	}
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	public String getEndTime() {
		return endTime;
	}
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getFailReason() {
		return failReason;
	}
	public void setFailReason(String failReason) {
		this.failReason = failReason;
	}
	
	

}
