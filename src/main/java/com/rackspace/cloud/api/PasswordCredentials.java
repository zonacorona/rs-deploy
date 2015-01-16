package com.rackspace.cloud.api;

import java.io.Serializable;

public class PasswordCredentials implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8283735077310362229L;
	
	String username;
	String password;
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
	
	
}
