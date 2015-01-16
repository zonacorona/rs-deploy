package com.rackspace.cloud.api.jclouds;

public class DocToolsEntity {
	
	
	
	private String id;
	private String publicIP;
	private String prjsonivate0_v4;
	private String label;
	private String uri;
	
	public DocToolsEntity(){
		this.id=null;
		this.publicIP=null;
		this.label=null;
		this.uri=null;
		this.prjsonivate0_v4=null;
	}
	
	public DocToolsEntity(String id, String prjsonivate0_v4, String publicIP, String label, String uri){
		this.id=id;
		this.prjsonivate0_v4=prjsonivate0_v4;
		this.publicIP=publicIP;
		this.label=label;
		this.uri=uri;
	}
	
	public String getPrjsonivate0_v4(){
		return this.prjsonivate0_v4;
	}
	
	public void setPrjsonivate0_v4(String prjsonivate0_v4){
		this.prjsonivate0_v4=prjsonivate0_v4;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPublicIP() {
		return publicIP;
	}

	public void setPublicIP(String publicIP) {
		this.publicIP = publicIP;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

    
	public String toString(){
		StringBuffer retVal=new StringBuffer("");
		
		retVal.append("{id=");
		retVal.append(this.getId());
		retVal.append(", label=");
		retVal.append(this.getLabel());
		retVal.append(", privateJSONIPAddr=");
		retVal.append(this.getPrjsonivate0_v4());
		retVal.append(", publicIPAddr=");
		retVal.append(this.getPublicIP());
		retVal.append(" uri=");
		retVal.append(this.getUri());
		retVal.append("}");
		
		return retVal.toString();
	}
	
//	public static void main(String[] args){
//		String str="<Check: id=ch7ZsrP8mz label=docs-prod-5...><Check: id=chO09MjwxF label=docs-prod-5b...>Total: 2";
//		
//		String[]checks=str.split("<Check:");
//		System.out.println(checks.length);
//		for(String aCheck:checks){
//			if(!aCheck.isEmpty()){
//				int checkIdIndex=aCheck.indexOf("id=");
//				int labelIndex=aCheck.indexOf("label=");
//				int endIndex=aCheck.indexOf("...>");
//				String checkId=aCheck.substring((checkIdIndex+3),(labelIndex-1));
//				String checkLabel=aCheck.substring((labelIndex+6),(endIndex));
//
//				System.out.println("checkId="+checkId+" checkLabel="+checkLabel);
//			}
//		}
//	}
}
