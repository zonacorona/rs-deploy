package com.rackspace.cloud.api.view.model;

import org.json.JSONException;
import org.json.JSONObject;

public class NewCredentials extends LoginUser{
	
	private String newpassword;
	private String confirmpassword;

	
	
	public String getNewpassword() {
		return newpassword;
	}

	public void setNewpassword(String newpassword) {
		this.newpassword = newpassword;
	}

	public void setConfirmpassword(String confirmpassword) {
		this.confirmpassword = confirmpassword;
	}

	public String getConfirmpassword() {
		return confirmpassword;
	}

	
	public String toString(){
		String retVal="{}";
		
		if(null!=super.getUsername()||null!=super.getPassword()||
				null!=this.confirmpassword || null!=this.newpassword){
			JSONObject json=new JSONObject();
			try {
				if(null!=super.getUsername()){
				    json.put("username", super.getUsername());
				}
				if(null!=super.getPassword()){
				    json.put("password", super.getPassword());
				}
				if(null!=this.newpassword){
					json.put("newpassword", this.newpassword);
				}
				if(null!=this.confirmpassword){
					json.put("confirmpassword", this.confirmpassword);
				}
				retVal=json.toString();
			} 
			catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		return retVal;
	}
}
