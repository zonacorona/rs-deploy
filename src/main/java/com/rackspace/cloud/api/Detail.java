package com.rackspace.cloud.api;

import java.io.Serializable;

public class Detail implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3297148075262498159L;
	
	private String href;
	private String imgSrc;
	private String title;
	private boolean building;
	private String pomName;
	private String folderName;
	private String lastModified;
	private String latestJenkinsUrl;
	private String groupid;
	private String docName;
	private String artifactid;
	private String displayName;
	private String result;
	private String jenkinsJobName;
	private String clouddocDocbook;
	
	public Detail(){
	}
	
	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}

	public String getImgSrc() {
		return imgSrc;
	}

	public void setImgSrc(String imgSrc) {
		this.imgSrc = imgSrc;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public boolean isBuilding() {
		return building;
	}

	public void setBuilding(boolean building) {
		this.building = building;
	}

	public String getPomName() {
		return pomName;
	}

	public void setPomName(String pomName) {
		this.pomName = pomName;
	}

	public String getFolderName() {
		return folderName;
	}

	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}

	public String getLastModified() {
		return lastModified;
	}

	public void setLastModified(String lastModified) {
		this.lastModified = lastModified;
	}

	public String getLatestJenkinsUrl() {
		return latestJenkinsUrl;
	}

	public void setLatestJenkinsUrl(String latestJenkinsUrl) {
		this.latestJenkinsUrl = latestJenkinsUrl;
	}

	public String getGroupid() {
		return groupid;
	}

	public void setGroupid(String groupid) {
		this.groupid = groupid;
	}

	public String getDocName() {
		return docName;
	}

	public void setDocName(String docName) {
		this.docName = docName;
	}

	public String getArtifactid() {
		return artifactid;
	}

	public void setArtifactid(String artifactid) {
		this.artifactid = artifactid;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getJenkinsJobName(){
		return this.jenkinsJobName;
	}
	
	public void setJenkinsJobName(String jenkinsJobName){
		this.jenkinsJobName=jenkinsJobName;
	}

	public String getClouddocDocbook() {
		return clouddocDocbook;
	}

	public void setClouddocDocbook(String clouddocDocbook) {
		this.clouddocDocbook = clouddocDocbook;
	}
	
	
}
