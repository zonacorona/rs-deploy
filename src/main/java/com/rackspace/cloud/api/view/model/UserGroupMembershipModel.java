package com.rackspace.cloud.api.view.model;

import java.io.Serializable;


public class UserGroupMembershipModel implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3893455323949412338L;
	
	private String groupName;
	private boolean checked;
	private String html;

	public UserGroupMembershipModel(String groupName, boolean checked){
		this.groupName=groupName;
		this.checked=checked;
	}
	
	public UserGroupMembershipModel(){
		this.groupName="";
		this.checked=false;
		this.html="";
	}
	
	public String getHtml() {
		return html;
	}

	public void setHtml(String html) {
		this.html = html;
	}
	
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public boolean getChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}
	
	
	public boolean equals(Object obj){
		boolean retVal=false;
		if(this==obj){
			retVal=true;
		}
		else{
			if(null==obj){
				retVal=true;
			}
			else{
				if(super.getClass()!=obj.getClass()){
					retVal=false;
				}
				else{
					UserGroupMembershipModel member=(UserGroupMembershipModel)obj;
					if(member.getGroupName().equals(this.getGroupName())){
						retVal=true;
					}
				}
			}
		}
		return retVal;
	}
	
	public int hashCode(){
		return this.groupName.hashCode();
	}
	

}
