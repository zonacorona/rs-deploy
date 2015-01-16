package com.rackspace.cloud.api.util.sax;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.rackspace.cloud.api.entity.Groups;


public class BookInfoXmlSaxHandler extends DefaultHandler {
	
	private String groupId;
	private boolean hasGroupId; 
	
	private String artifactId;
	private boolean hasArtifactId;
	
	private Groups groupAndArtifact;
	
	private String pomName;
	private boolean hasPomName;
	
	private boolean hasEndPominfo;
	
	private String displayName;
	private boolean hasDisplayName;
	
	private String docName;
	private boolean hasDocName;
	
	private String jenkinsJobName;
	private boolean hasJenkinsJobName;
	
	private String clouddocsDocBook;
	private boolean hasClouddocsDocBook;
	
	public void startElement(String uri, String localName,String qName, 
			Attributes attributes) throws SAXException{
		if(qName.equalsIgnoreCase("groupid")){
			this.hasGroupId=true;			
		}
		if(qName.equalsIgnoreCase("artifactid")){
			this.hasArtifactId=true;			
		}
		if(qName.equalsIgnoreCase("pomname")){
			this.hasPomName=true;
		}
		if(qName.equalsIgnoreCase("displayname")){
			this.hasDisplayName=true;
		}
		if(qName.equalsIgnoreCase("docname")){
			this.hasDocName=true;
		}
		if(qName.equalsIgnoreCase("jenkinsjobname")){
			this.hasJenkinsJobName=true;
		}
		if(qName.equalsIgnoreCase("clouddocs-docbook")){
			this.hasClouddocsDocBook=true;
		}
	}
	public void endElement(String uri, String localName,
			String qName) throws SAXException {
		if(qName.equalsIgnoreCase("pominfo")){
			this.hasEndPominfo=true;
		}
		if(qName.equalsIgnoreCase("clouddocs-docbook")){
			this.hasClouddocsDocBook=false;
		}
	}
	
	public String getClouddocsDocBook(){
		return this.clouddocsDocBook;
	}
	
	public void setClouddocsDocBook(String clouddocsDocBook){
		this.clouddocsDocBook=clouddocsDocBook;
	}
	
	public String getDisplayName(){
		return this.displayName;
	}
	
	public void setDisplayName(String displayName){
		this.displayName=displayName;
	}
	
	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}

	public Groups getGroupAndArtifact(){
		return this.groupAndArtifact;
	}
	
	public String getPomName(){
		return this.pomName;
	}
	
	public void setPomName(String pomName){
		this.pomName=pomName;
	}
	
	public String getDocName(){
		return this.docName;
	}
	
	public void setDocName(String docName){
		this.docName=docName;
	}
		
	public String getJenkinsJobName() {
		return jenkinsJobName;
	}
	
	public void setJenkinsJobName(String jenkinsJobName) {
		this.jenkinsJobName = jenkinsJobName;
	}
	
	//All the Strings do not have any unique special characters so just create the String once, we
	//don't have to concatenate it
	public void characters(char ch[], int start, int length)
			throws SAXException {

		if(this.hasGroupId){
			this.groupId=new String(ch, start, length);			
			this.hasGroupId=false;
			
		}
		if(this.hasArtifactId){
			this.artifactId=new String(ch,start,length);
			this.hasArtifactId=false;
		}
		if(this.hasEndPominfo){
			if(null!=this.artifactId && null!=this.groupId){
				this.groupAndArtifact=new Groups();
				this.groupAndArtifact.setName(this.groupId+"---"+this.artifactId);
			}
			this.hasEndPominfo=false;
		}
		if(this.hasPomName){
			this.pomName=new String(ch,start,length);
			this.hasPomName=false;
		}
		if(this.hasDisplayName){
			this.displayName=new String(ch,start,length);
			this.hasDisplayName=false;
		}
		if(this.hasDocName){
			this.docName=new String(ch,start,length);
			this.hasDocName=false;			
		}
		if(this.hasJenkinsJobName){
			this.jenkinsJobName=new String(ch,start,length);
			this.hasJenkinsJobName=false;
		}
		if(this.hasClouddocsDocBook){
			this.clouddocsDocBook=new String(ch,start,length);
			this.hasClouddocsDocBook=false;
		}
	}

}
