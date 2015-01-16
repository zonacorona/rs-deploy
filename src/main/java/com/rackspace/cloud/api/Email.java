package com.rackspace.cloud.api;

import java.util.ArrayList;
import java.util.List;

public class Email {

	private String name;
	private String to;
	private String from;
	private String subject;
	private List<String> docNames;
	private String ldapname;
	
	
	public Email(){
		this.name=null;
		this.to=null;
		this.from=null;
		this.subject=null;
	}
	
	public Email(String name, String to, String from, String subject, List<String>docNames, String ldapname){
		this.name=name;
		this.to=to;
		this.from=from;
		this.subject=subject;
		this.docNames=docNames;
		this.ldapname=ldapname;
	}
	
	
	
	public List<String> getDocNames() {
		return this.docNames;
	}
	
	public String getLdapname(){
		return this.ldapname;
	}
	
	public void setLdapname(String ldapname){
		this.ldapname=ldapname;
	}

	public void setDocNames(List<String> docNames) {
		this.docNames = docNames;
	}
	
	public void addToDocNames(String aDocName){
		if(null==this.docNames){
			this.docNames=new ArrayList<String>();
		}
		this.docNames.add(aDocName);
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	public String toString(){
		StringBuffer retVal=new StringBuffer("");
		retVal.append("[to: "+this.to);
		retVal.append(", from: " );
		retVal.append(this.from);
		retVal.append(", name: ");
		retVal.append(this.name);
		retVal.append(", subject: ");
		retVal.append(this.subject);
		retVal.append(", docNames: {");
		if(null!=docNames){
			for(String docName:docNames){
				retVal.append(docName);
				retVal.append(", ");
			}
		}
		retVal.append("}]");
		
		return retVal.toString();
	}
	
}
