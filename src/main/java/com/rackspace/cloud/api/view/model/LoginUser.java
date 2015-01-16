package com.rackspace.cloud.api.view.model;

import org.json.JSONException;
import org.json.JSONObject;


public class LoginUser {
	
	private String username;
	private String password;
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	
	public String toString(){
		String retVal="{}";
		
		if(null!=this.username||null!=this.password){
			JSONObject json=new JSONObject();
			try {
				if(null!=this.username){
				    json.put("username", this.username);
				}
				if(null!=this.password){
				    json.put("password", this.password);
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
