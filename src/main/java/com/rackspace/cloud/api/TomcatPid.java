package com.rackspace.cloud.api;

public class TomcatPid {
	private String user;
	private String id;
	private String basePath;
	
	public TomcatPid(){
	    this.id="";
	    this.basePath="";
	    this.user="";
	}
	
	public TomcatPid(String user, String id, String basePath){
		this.user=user;
		this.id=id;
		this.basePath=basePath;
	}
	
	public String getUser(){
		return this.user;
	}
	
	public void setUser(String user){
		this.user=user;
	}
	
	public String getId(){
		return this.id;
	}
	
	public void setId(String id){
		this.id=id;
	}
	
	public String getBasePath(){
		return this.basePath;
	}
	
	public void setBasePath(String basePath){
		this.basePath=basePath;
	}
	
	public String toString(){
		return "{user="+this.user+", pid="+this.id+", basePath="+this.basePath+"}";
	}
}
