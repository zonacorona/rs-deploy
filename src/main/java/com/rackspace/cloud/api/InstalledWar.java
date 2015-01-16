package com.rackspace.cloud.api;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;


import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;
import org.json.JSONObject;

public class InstalledWar {
	
	private static int              updateCount=1;
	//The folderNameList, groupId, artifactId, docName, and lastModifiedList are passed into InstallWar Constructor
	private List<DocNameNFolder>    docNameNFolderNamesList;
	private String                  groupId;
	private String                  artifactId;
	private String                  pomName;
	private List<Long>              lastModifiedList;
	//lastModifiedListDateAsStr takes its value from formatting of lastModifiedList
	private List<String>            lastModifiedListDatesAsStr;	
	
	//This value is extracted from the <clouddocs-docbook> that is in the bookinfo.xml for a given book
	private String                  clouddocsDocbook;    

	
	private String                  absolutePathName;
	private String                  warFileName;
	
	//This url is built from the props.properties file
	private String                   lastJenkinsBuildUrl;

	//These values are queried from the Jenkin REST API
	private String                   theBulidUrl;
	private String                   buildNumber;
	private boolean                  isBuilding;
	private String                   result;
	
	private static Properties        propsFile;
	private static Logger            log=Logger.getLogger(InstalledWar.class);
	
	public String toString(){
		StringBuilder retVal=new StringBuilder("");
		retVal.append("{");
		retVal.append("groupId=");
		retVal.append(this.groupId);
		retVal.append(",artifactId=");		
		retVal.append(this.artifactId);
		retVal.append(",pomName=");
		retVal.append(this.pomName);
		retVal.append(",abosolutePathName=");
		retVal.append(this.absolutePathName);
		retVal.append(",clouddocs-docbook");
		retVal.append(this.clouddocsDocbook);
		retVal.append("}");
		
		return retVal.toString();
	}
	
	public InstalledWar(){
		this.docNameNFolderNamesList=null;
		this.absolutePathName=null;	
		this.pomName=null;
		this.lastModifiedList=null;
		this.groupId=null;
		this.artifactId=null;
		this.isBuilding=false;
		this.clouddocsDocbook="false";
		
		if(null==InstalledWar.propsFile){
			synchronized(this){
				if(null==InstalledWar.propsFile){
			        InstalledWar.propsFile=new Properties();
					try {
						InputStream inny=InstalledWar.class.getClassLoader().getResourceAsStream("/props.properties");
						InstalledWar.propsFile.load(inny);
					} 
					catch (FileNotFoundException e) {
						e.printStackTrace();
						//do nothing, if we cant get the props.properties, then we will use the respective default values
						if(log.isDebugEnabled()){
							log.debug("InstalledWar() Constructor: FileNotFoundException failed to load /props.properties ");
						}
					}
					catch(IOException e){
						e.printStackTrace();
						//do nothing, if we cant get the props.properties, then we will use the respective default values
						if(log.isDebugEnabled()){
							log.debug("InstalledWar() Constructor: IOException failed to load /props.properties ");
						}						
					}
					catch(Throwable e){
						e.printStackTrace();
						//do nothing, if we cant get the props.properties, then we will use the respective default values
						if(log.isDebugEnabled()){
							log.debug("InstalledWar() Constructor: Throwable failed to load /props.properties ");
						}						
					}
				}
			}
		}	
	}


	public InstalledWar(List<DocNameNFolder> docNameNFolderNamesList, String pomName, List<Long> modifiedLast, String groupId, String artifactId){
		String METHOD_NAME="InstalledWar 5 args Constructor";
		//By default we set isBuilding to false 
		this.isBuilding=false;
		this.clouddocsDocbook="false";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START: pomName="+pomName+" groupId="+groupId+" artifactId="+artifactId+" START:");
		}
		if(null==InstalledWar.propsFile){
			synchronized(this){
				if(null==InstalledWar.propsFile){
					log.debug(METHOD_NAME+": ~~~~~~~~~instatiating propsFile");
					InstalledWar.propsFile=new Properties();
					InputStream anInnyStream=InstalledWar.class.getClassLoader().getResourceAsStream("/props.properties");
					if(null==anInnyStream){
						log.debug(METHOD_NAME+": ~~~~~~~~~~~~anInnyStream is null, try to get it with the system class loader");
						InstalledWar.class.getClassLoader();
						anInnyStream=ClassLoader.getSystemClassLoader().getResourceAsStream("/props.proerties");
					}
					try{
					    InstalledWar.propsFile.load(anInnyStream);
					}
					catch (FileNotFoundException e) {
						//do nothing, if we cant get the props.properties, then we will use the respective default values
						if(log.isDebugEnabled()){
							log.debug("InstalledWar() Constructor: FileNotFoundException, failed to load /props.properties");
						}
					}	
					catch(IOException e){
						//do nothing, if we cant get the props.properties, then we will use the respective default values
						if(log.isDebugEnabled()){
							log.debug(METHOD_NAME+": IOException, failed to load the /props.properties ");
						}			
					}
				}			
			}
		}

		this.docNameNFolderNamesList=docNameNFolderNamesList;
		this.pomName=pomName;
		
		this.lastModifiedList=modifiedLast;
		//Make sure that lastModifiedList can never be null
	    if(null==this.lastModifiedList){
			this.lastModifiedList=new ArrayList<Long>();
		}
		this.lastModifiedListDatesAsStr=new ArrayList<String>();
		for(Long aLong:this.lastModifiedList){
			Date lmDate=new Date(aLong);
			SimpleDateFormat sdf = new SimpleDateFormat("E MMM dd yyyy hh:mm:ss"); 
			//SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy, hh:mm:ss"); 
			this.lastModifiedListDatesAsStr.add(sdf.format(lmDate));
		}

		this.groupId=groupId;
		this.artifactId=artifactId;

		//updateWar();
		
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": this.isBuilding="+this.isBuilding+" this.jenkinsUr="+this.lastJenkinsBuildUrl+
					  " this.theBuildUrl="+this.theBulidUrl+" this.result="+this.result);
			log.debug(METHOD_NAME+": END:");
		}
	} 
		
	//This updates the jenkins information
	public void updateWar(){
		String METHOD_NAME="updateWar()";
		
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+":~~~~~~~~~~~~~~ START: updateCount="+updateCount);
		}
		++updateCount;
		
		String jenkinsURl=(propsFile.getProperty("jenkinsurl","http://docs-staging.rackspace.com/jenkins")).trim();
		//The jenkins URL canNOT end in a slash
		if(jenkinsURl.endsWith("/")){
			jenkinsURl=jenkinsURl.substring(0,(jenkinsURl.length()-1));
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+":~~~~~~~~~~~~~~ jenkinsURl="+jenkinsURl);
		}
		String userName=(propsFile.getProperty("username","docs")).trim();
		String password=(propsFile.getProperty("password","raXd0cs")).trim();
		
		String jenkinsUrl=propsFile.getProperty("jenkinsurl","http://docs-staging.rackspace.com/jenkins/");
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+":~~~~~~~~~~~~~~ jenkinsUrl="+jenkinsUrl);
		}
		this.lastJenkinsBuildUrl=new String(jenkinsUrl);
		if(!lastJenkinsBuildUrl.endsWith("/")){
			this.lastJenkinsBuildUrl+="/"	;
		}
		this.lastJenkinsBuildUrl+=("job/"+groupId+"---"+artifactId+"/lastBuild/");

		String jenkinsLastBuild=lastJenkinsBuildUrl+"api/json";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+":~~~~~~~~~~~~~~ this.lastJenkinsBuildUrl="+this.lastJenkinsBuildUrl);
			log.debug(METHOD_NAME+":~~~~~~~~~~~~~~ jenkinsLastBuild="+jenkinsLastBuild);
		}		
		
		HttpClient client=null;
		GetMethod getMethod=null;
		
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": calling authenticate, jenkinsUrl="+jenkinsURl+" userName="+
					userName+" password="+password);
		}
		String jsonStr="";
		try {			
			JSONObject jsonObj;
			try{
			    client = DeployUtility.authenticate(jenkinsURl, userName, password);
			    getMethod = new GetMethod(jenkinsLastBuild);
			}
			catch (HttpException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				if(log.isDebugEnabled()){
					log.debug(METHOD_NAME+": HttpException caught, could not authenticate e.getMessage()="+e.getMessage());
				}
				this.result="unknown";
			} 
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				if(log.isDebugEnabled()){
					log.debug(METHOD_NAME+": IOException caught, could not authenticate e.getMessage()="+e.getMessage());
				}
				this.result="unknown";
			}
			catch(Throwable e){
				e.printStackTrace();
				if(log.isDebugEnabled()){
					log.debug(METHOD_NAME+": Throwable caught, could not authenticate e.getMessage()="+e.getMessage());
				}
				this.result="unknown";
			}
			
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": ^^^^^^^^About to execute jenkinsLastBuild: "+jenkinsLastBuild);
			}
			int status=client.executeMethod(getMethod);	
			int numberOfTries=1;
			String contentType="Not what we want";
			Header aHeader=getMethod.getResponseHeader("Content-Type");
			if(null!=aHeader){
				contentType=aHeader.getValue();
			}
			if(null==contentType){
				contentType="";
			}
			jsonStr=getMethod.getResponseBodyAsString();
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": status="+status+"\njsonStr="+jsonStr);
				log.debug(METHOD_NAME+": contentType="+contentType);
			}
			//Jenkins is still idle, we have to wait for jenkins to startup
			//For some reason, the first time we get here we are NOT authenticated. I tried sleeping after authenticating, but
			//this did not seem to work. Perhaps we should check to for a status 403 and sleep and try to authenticate again.
			//When we get here and we are not authenticated, the Content-Type returned is not json but instead html. This only 
			//occurs when the Tomcat server is started and we access rax-autodeploy for the first time. We are unable to
			//authenticate to the jenkins app. The status initially returned may be 503, because jenkins is starting, we have
			// to wait for jenkins to properly initialize
			while(status>=500 && numberOfTries<DeployUtility.MAX_NUMBER_OF_JENKINS_TRIES){
				aHeader=getMethod.getResponseHeader("Content-Type");
				if(null!=aHeader){
					contentType=aHeader.getValue();					
				}
				if(null==contentType){
					contentType="";
				}
				log.debug(METHOD_NAME+":$$$$$$$$$$");
				log.debug(METHOD_NAME+":$$$$$$$$$$ contentType="+contentType);
				log.debug(METHOD_NAME+":$$$$$$$$$$");

				Thread.sleep(DeployUtility.WAIT_FOR_JENKINS);
				++numberOfTries;
				status=client.executeMethod(getMethod);	
			}
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": Had to try "+numberOfTries+
						" to get json return values, aHeader="+aHeader);
			}	
			jsonStr=getMethod.getResponseBodyAsString();

			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": jsonStr="+jsonStr);
				log.debug(METHOD_NAME+": about to serialize json string to JSONObject");
			}

			//jsonObj=(org.json.JSONObject)JSONSerializer.toJSON(jsonStr);
			jsonObj=new JSONObject(jsonStr);
			this.isBuilding=jsonObj.getBoolean("building");
			this.theBulidUrl=jsonObj.getString("url");
			
			this.result=jsonObj.getString("result");
			//When isBuilding is true, result is the String literal "null"
			if(null==this.result||this.result.isEmpty()||this.result.equalsIgnoreCase("null")){
			    	this.result="unknown";
			}
			
			if(null!=this.result && !this.result.isEmpty()){
				this.result=(this.result.trim()).toLowerCase();
			}
			else{
				this.result="unknown";
			}
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": this.isBuilding="+this.isBuilding+" this.theBulidUrl="+
						this.theBulidUrl+ "this.result="+this.result);
			}		    
		}
		catch(HttpException e){
			e.printStackTrace();
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": HttpException, could not execute get with lastJenkinsBuildUrle="+
			              lastJenkinsBuildUrl+" e.getMessage()="+e.getMessage());
			}
			this.result="unknown";
		}
		catch(IOException e){
			e.printStackTrace();
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": IOException, could not execute get with lastJenkinsBuildUrle="+
			              lastJenkinsBuildUrl+" e.getMessage()="+e.getMessage());
			}
			this.result="unknown";
		}
		catch(InterruptedException e){
			e.printStackTrace();
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": InterruptedException caught, message="+e.getMessage()+" jsonStr="+jsonStr);
			}
			this.result="unknown";
		}
		catch(Throwable e){
			e.printStackTrace();
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": Throwable caught, message="+e.getMessage()+" jsonStr="+jsonStr);
			}	
			this.result="unknown";
		}		
		finally{
			getMethod.releaseConnection();
			if(null==this.result || this.result.isEmpty()){
				this.result="unknown";
			}	
		}
		if(null==this.result || this.result.isEmpty()){
			this.result="unknown";
		}	
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END");
		}
	}
	
	public String getDisplayDeployButton(){
		String retVal="block";
		
		if(this.isBuilding){
			retVal="none";
		}
		return retVal;
	}
	
	public String getDisplayCannotDeployMessage(){
		String retVal="none";
		
		if(this.isBuilding){
			retVal="block";
		}		
		return retVal;
	}
	
	public String getReconciledBuildUrl(){
		String retVal=" ";
		if(null!=this.result && !this.result.equalsIgnoreCase("unknown")){
			if(null!=this.theBulidUrl && !this.theBulidUrl.isEmpty()){
				retVal=this.theBulidUrl;
			}
			else{
				retVal=this.lastJenkinsBuildUrl;
			}
		}
		else{
			retVal=this.lastJenkinsBuildUrl;
		}
		return retVal;
	}
	
	public String getPomName() {
		return pomName;
	}


	public void setPomName(String pomName) {
		this.pomName = pomName;
	}


	public String getResult() {
		return this.result;
	}


	public void setResult(String result) {
		this.result = result;
	}


	public String getTheBulidUrl() {
		return theBulidUrl;
	}


	public void setTheBulidUrl(String theBulidUrl) {
		this.theBulidUrl = theBulidUrl;
	}


	public String getBuildNumber() {
		return buildNumber;
	}


	public void setBuildNumber(String buildNumber) {
		this.buildNumber = buildNumber;
	}


	public boolean isBuilding() {
		return isBuilding;
	}


	public void setBuilding(boolean isBuilding) {
		this.isBuilding = isBuilding;
	}


	public String getLastJenkinsBuildUrl() {
		return lastJenkinsBuildUrl;
	}


	public void setLastJenkinsBuildUrl(String jenkinsUrl) {
		this.lastJenkinsBuildUrl = jenkinsUrl;
	}

	public String getId(){
		return this.groupId+"~~~"+this.artifactId;
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


	public List<String> getLastModifiedListDatesAsStr() {
		return lastModifiedListDatesAsStr;
	}


	public void setLastModifiedListDatesAsStr(List<String> lastModifiedListDatesAsStr) {
		this.lastModifiedListDatesAsStr = lastModifiedListDatesAsStr;
	}


	public void setDocNameNFolderNamesList(List<DocNameNFolder> docNameNFolderNamesList) {
		this.docNameNFolderNamesList = docNameNFolderNamesList;
	}


	public List<DocNameNFolder> getDocNameNFolderNamesList() {
		return this.docNameNFolderNamesList;
	}


	public List<Long> getLastModifiedList() {
		return this.lastModifiedList;
	}

	public void setLastModifiedList(List<Long> lastModifiedList) {
		this.lastModifiedList = lastModifiedList;
	}

	public String getAbsolutePathName() {
		return absolutePathName;
	}

	public void setAbsolutePathName(String absolutePathName) {
		this.absolutePathName = absolutePathName;
		if(null!=absolutePathName && absolutePathName.contains("/")){
			int index=absolutePathName.lastIndexOf("/");
			if(index<(absolutePathName.length()-1)){
				this.warFileName=absolutePathName.substring(absolutePathName.lastIndexOf("/"))+".war";
			}
			else{
				this.warFileName="";
			}
		}
		else{
			this.warFileName="";
		}
	}
	
	public String getWarFileName(){
		return this.warFileName;
	}

	public String getClouddocsDocbook() {
		return clouddocsDocbook;
	}

	public void setClouddocsDocbook(String clouddocsDocbook) {
		this.clouddocsDocbook = clouddocsDocbook;
	}
	
	
}
