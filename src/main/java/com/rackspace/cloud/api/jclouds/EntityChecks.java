package com.rackspace.cloud.api.jclouds;

public class EntityChecks {
	
	private String entityId;
	private String checkId;
	private String checkLabel;
	private boolean disabled;
	
	public EntityChecks(String entityId, String checkId, String checkLabel, boolean disabled){
		this.entityId=entityId;
		this.checkId=checkId;
		this.checkLabel=checkLabel;
		this.disabled=disabled;
	}
	
	
	
	public boolean isDisabled() {
		return disabled;
	}



	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}



	public String getEntityId() {
		return entityId;
	}
	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}
	public String getCheckId() {
		return checkId;
	}
	public void setCheckId(String checkId) {
		this.checkId = checkId;
	}
	public String getCheckLabel() {
		return checkLabel;
	}
	public void setCheckLabel(String checkLabel) {
		this.checkLabel = checkLabel;
	}
	
	public String toString(){
		StringBuffer retVal=new StringBuffer("");
		retVal.append("[");
		retVal.append("entityId=");
		retVal.append(this.entityId);
		retVal.append(", checkId=");
		retVal.append(this.checkId);
		retVal.append(", checkLabel=");
		retVal.append(this.checkLabel);
		retVal.append(", disabled=");
		retVal.append(this.disabled);
		return retVal.toString();
	}

}
