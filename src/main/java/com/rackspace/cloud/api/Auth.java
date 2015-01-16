package com.rackspace.cloud.api;

import org.codehaus.jackson.annotate.JsonIgnore;

import java.io.Serializable;

public class Auth implements Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = 4627963842486935943L;

    @JsonIgnore
	PasswordCredentials auth;

	public PasswordCredentials getPassCredentials() {
		return auth;
	}

	public void setPassCredentials(PasswordCredentials auth) {
		this.auth = auth;
	}
    
    
}
