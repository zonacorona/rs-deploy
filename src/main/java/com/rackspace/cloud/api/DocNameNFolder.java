package com.rackspace.cloud.api;

public class DocNameNFolder {
	private String docName;
	private String folderName;
	
	public DocNameNFolder(){
		this.docName=null;
		this.folderName=null;
	}
	
	public DocNameNFolder(String docName, String folderName){
		this.docName=docName;
		this.folderName=folderName;
	}

	public String getDocName() {
		return docName;
	}

	public void setDocName(String docName) {
		this.docName = docName;
	}

	public String getFolderName() {
		return folderName;
	}

	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}

}
