package com.rackspace.cloud.api.view.model;

import java.io.Serializable;

public class UserAccessModel implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5857037750142872291L;
	
	private boolean hasAccess;

	public boolean hasAccess() {
		return hasAccess;
	}

	public void setHasAccess(boolean hasAccess) {
		this.hasAccess = hasAccess;
	}
	
	public String toString(){
		StringBuffer retVal=new StringBuffer("");
		retVal.append("{");
		retVal.append("hasAccess:");
		retVal.append("}");
		return retVal.toString();
	}

}
