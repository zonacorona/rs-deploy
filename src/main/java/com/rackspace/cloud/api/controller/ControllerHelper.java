package com.rackspace.cloud.api.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.sf.json.JSONException;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.rackspace.cloud.api.DeployUtility;
import com.rackspace.cloud.api.Detail;
import com.rackspace.cloud.api.DocNameNFolder;
import com.rackspace.cloud.api.InstalledWar;
import com.rackspace.cloud.api.dao.IUsersDao;
import com.rackspace.cloud.api.entity.Groups;
import com.rackspace.cloud.api.entity.Users;
import com.rackspace.cloud.api.util.sax.BookInfoXmlSaxHandler;

public class ControllerHelper {

	public static final String ROOT_ARTIFACT_ID="rax-indexwar-landing";
	public static final String ROOT_GROUP_ID="com.rackspace.cloud.api";
	public static final String NO_ACCESS="NoAccess";

	private static Logger log = Logger.getLogger(ControllerHelper.class);

	static Map<String,InstalledWar> getWars(HttpSession session, ServletContext servletCtx, 
			Map<String,InstalledWar>extOrInternalWar, IUsersDao userDao,
			String xldapUserName, Properties propsFile, boolean isExtWar){
		String  METHOD_NAME="getWars()";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START: xldapUserName="+xldapUserName+" isExtWar="+isExtWar);
		}
		if((null==xldapUserName||xldapUserName.isEmpty())&&null!=session){
			xldapUserName=(String)session.getAttribute("x-ldap-username");
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": xldapUserName="+xldapUserName);
			log.debug(METHOD_NAME+": extOrInternalWar.size()="+extOrInternalWar.size());
		}	
		if(null==xldapUserName||xldapUserName.isEmpty()){
			xldapUserName="";
		}
		Map<String, InstalledWar>tempSortedInstalledWars=new TreeMap<String, InstalledWar>();
		//The war is empty, load it
		if(extOrInternalWar.size()==0){
			loadAllWars(servletCtx, extOrInternalWar, isExtWar);
		}
		boolean debug=false;
		String propsDebug=propsFile.getProperty("debug");

		if(null!=propsDebug && propsDebug.equalsIgnoreCase("true")){
			debug=true;
		}
		if(log.isDebugEnabled()){			
			if(null!=extOrInternalWar){
				log.debug(METHOD_NAME+":this.extOrInternalWar.size()="+extOrInternalWar.size());
				Set<String>keys=extOrInternalWar.keySet();
				for(String aKey:keys){
					log.debug(METHOD_NAME+": extOrInternalWar.get(\""+aKey+"\")="+extOrInternalWar.get(aKey));
				}
			}
			log.debug(METHOD_NAME+": debug="+debug);
		}
		Users user=userDao.findById(xldapUserName);
		Set<Groups>groupsMembership=user.getGroupses();
		for(InstalledWar aWar:extOrInternalWar.values()){

			String pomName=aWar.getPomName();
			
			//retrievedLoggedInUserAccessToWar returns null if the user has access to aWar
			String group=retrieveLoggedInUserAccessToWar(aWar, propsFile,groupsMembership);
			String pomNameHashCode=(pomName.hashCode()+"");
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+":pomName="+pomName); 
				log.debug(METHOD_NAME+":pomNameHashCode="+pomNameHashCode); 
				log.debug(METHOD_NAME+":group="+group); 
				log.debug(METHOD_NAME+":debug="+debug); 
				log.debug(METHOD_NAME+"!tempSortedInstalledWars.containsKey(\""+pomNameHashCode+"\")="+
						 (!tempSortedInstalledWars.containsKey(pomNameHashCode))); 
			}
			if(!tempSortedInstalledWars.containsKey(pomNameHashCode)){
				//null indicates that the user has access to the .war, any other value indicates that
				//the user does not have access to the .war
				if((null==group || debug)){

					tempSortedInstalledWars.put(pomNameHashCode, aWar);
					if(log.isDebugEnabled()){
						log.debug(METHOD_NAME+": Added pomName="+pomName+" pomNameHashCode="+pomNameHashCode+" with aWar="+aWar);
						log.debug(METHOD_NAME+":tempSortedInstalledWars.size()="+tempSortedInstalledWars.size());
					}					
				}
			}
			else{
				int count=1;
				String origPomName=pomName;
				pomName+="-"+count;
				//We are iterating through all the keys in map looking for the key with the highest number
				//When we reach a key that has a number suffix that does not exist, then we use it
				while(tempSortedInstalledWars.containsKey(pomName)){
					pomName=origPomName;
					++count;
					pomName+="-"+count;
				}
				if(null==group || debug){
					pomNameHashCode=(pomName.hashCode()+"");
					tempSortedInstalledWars.put(pomNameHashCode, aWar);
					if(log.isDebugEnabled()){
						log.debug(METHOD_NAME+": ~Added pomName="+pomName+" pomNameHashCode="+pomNameHashCode+" with aWar="+aWar);
						log.debug(METHOD_NAME+": ~tempSortedInstalledWars.size()="+tempSortedInstalledWars.size());
					}	
				}
			}			
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END: tempSortedInstalledWars.size()="+tempSortedInstalledWars.size());
		}
		return tempSortedInstalledWars;		
	}

	/**
	 * This method was copied from the orignal ServletController, and was used to load both the external and
	 * internal maps. We load both the external and internal bookinfo.xml into respective temp maps, and then
	 * assign the temp map to the correct external or internal map
	 * @param servletCtx
	 * @param theWars
	 * @param isExt
	 */
	static void loadAllWars(ServletContext servletCtx,Map<String,InstalledWar>theWars, boolean isExt){
		String METHOD_NAME="loadAllWars()";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START:");
		}
		//extWars and internalNReviewerWars cannot be null
		if(null!=theWars){
			if(null!=servletCtx){
				if(log.isDebugEnabled()){
					log.debug(METHOD_NAME+": servletCtx is NOT null servletCtx.getReaLPath(/IndexWar)="+servletCtx.getRealPath("/IndexWar"));
				}

				Map<String, InstalledWar>tempWars=new LinkedHashMap<String, InstalledWar>();
				Map<String, InstalledWar>tempInternalNReviewerWars=new LinkedHashMap<String, InstalledWar>();

				//Get the webapps folder 
				String webappPath=servletCtx.getRealPath("..");
				File theFile=new File(webappPath);
				if(log.isDebugEnabled()){
					log.debug(METHOD_NAME+": webappPath="+webappPath);
					log.debug(METHOD_NAME+": theFile.getAbsolutePath()="+theFile.getAbsolutePath());
				}
				//Make sure that we are dealing with a directory
				if(theFile.isDirectory()){
					//Get all the files in the directory
					File[] allFiles=theFile.listFiles();

					if(allFiles!=null && allFiles.length>0){
						if(log.isDebugEnabled()){
							log.debug(METHOD_NAME+": allFiles.length="+allFiles.length);
						}
						String pomName="";
						String aDocName="";
						String pomInfoAsKey="";
						String clouddocsDocbook="false";

						try {						

							for(File aFile:allFiles){	

								//We are only interested in the subfolders that have a bookinfo.xml
								if(aFile.isDirectory()){
									File bookinfoFile=new File(aFile.getAbsolutePath()+"/bookinfo.xml");

									if(log.isDebugEnabled()){
										log.debug(METHOD_NAME+": aFile is a directory aFile.getName()="+aFile.getName());
										log.debug(METHOD_NAME+": bookinfoFile.getAbsolutePath()="+bookinfoFile.getAbsolutePath());
										log.debug(METHOD_NAME+": bookinfoFile.exists()="+bookinfoFile.exists());
									}
									if(bookinfoFile.exists()){
										
										//We need to read the bookinfo.xml file to extract the <clouddocs-docbook> element
										//value
										SAXParserFactory factory = SAXParserFactory.newInstance();
										SAXParser saxParser = factory.newSAXParser();
										BookInfoXmlSaxHandler bookinfoXmlSaxHandler=new BookInfoXmlSaxHandler();
										
										saxParser.parse(bookinfoFile, bookinfoXmlSaxHandler);
										clouddocsDocbook=bookinfoXmlSaxHandler.getClouddocsDocBook();
										//We never want to return null, if we can't read a value just return false
										if(null==clouddocsDocbook){
											clouddocsDocbook="false";
										}
										//Now we should get all the files in the respective webapps directory
										File[] filesInDir=aFile.listFiles();
										String folderName=aFile.getName();
										if(log.isDebugEnabled()){
											log.debug("folderName folder: "+folderName);
										}
										if(null!=filesInDir && filesInDir.length>0){

											String pomGroupId="";
											String pomArtId="";

											aDocName=ControllerHelper.getFirstSingleTagValueStartingFromParent("products","docname", bookinfoFile);											
											pomName=ControllerHelper.getFirstSingleTagValueStartingFromParent("pominfo","pomname", bookinfoFile);											
											pomGroupId=ControllerHelper.getFirstSingleTagValueStartingFromParent("pominfo","groupid",bookinfoFile);												
											pomArtId=ControllerHelper.getFirstSingleTagValueStartingFromParent("pominfo","artifactid", bookinfoFile);
											//If the pomName is null or empty, we just take the artifact ID
											if(null==pomName || pomName.isEmpty()){
												pomName=pomArtId;
											}
											pomInfoAsKey=pomGroupId+"-"+pomArtId;

											if(log.isDebugEnabled()){
												log.debug(METHOD_NAME+":%%%%%%%%%aDocName= "+aDocName+" pomName="+pomName+" pomGroupId="+pomGroupId+" pomArtId="+pomArtId);							
											}
											//add it to the wars list

											InstalledWar aWar=null;
											List<Long>modifiedLastList=null;
											List<DocNameNFolder>docNameNFolderNamesList=null;
											if(log.isDebugEnabled()){
												log.debug(METHOD_NAME+":folderName= "+folderName);	
												log.debug(METHOD_NAME+": !@#!@#!@#!@#aFile.lastModified()"+aFile.lastModified());
												log.debug(METHOD_NAME+": null!=folderName && (folderName.toLowerCase().endsWith(\"-internal\")||folderName.toLowerCase().endsWith(\"-reviewer\"))="+
														(null!=folderName && (folderName.toLowerCase().endsWith("-internal")||folderName.toLowerCase().endsWith("-reviewer"))));
											}
											//This is an internal folder (we used to have .war's that ended in *-reviewer.war, but not any more)
											if(null!=folderName && (folderName.toLowerCase().endsWith("-internal")||folderName.toLowerCase().endsWith("-reviewer"))){
												if(log.isDebugEnabled()){
													log.debug(METHOD_NAME+": (tempInternalNReviewerWars.containsKey("+pomInfoAsKey+"))="+
															(tempInternalNReviewerWars.containsKey(pomInfoAsKey)));
												}
												if(tempInternalNReviewerWars.containsKey(pomInfoAsKey)){
													aWar=tempInternalNReviewerWars.get(pomInfoAsKey);
													aWar.setAbsolutePathName(aFile.getAbsolutePath());
													
													//we no longer need to call this because we no longer display
													//jenkins build information automatically, users must click 
													//on the details button
													//aWar.updateWar();

													if(log.isDebugEnabled()){
														log.debug(METHOD_NAME+": ~~~~adding aFile.lastModified="+aFile.lastModified());
													}
													modifiedLastList=aWar.getLastModifiedList();
													modifiedLastList.add(aFile.lastModified());
													clouddocsDocbook=aWar.getClouddocsDocbook();

													/*
													List<String>modifiedListDatesAsStr=aWar.getLastModifiedListDatesAsStr();											
													SimpleDateFormat sdf=new SimpleDateFormat("E MMM dd yyyy hh:mm:ss");
													//SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy, hh:mm:ss");
													String aDate=sdf.format(aFile.lastModified());
													modifiedListDatesAsStr.add(aDate);													
													 */

													docNameNFolderNamesList=aWar.getDocNameNFolderNamesList();
													DocNameNFolder aDocNameNFolder=new DocNameNFolder(aDocName, aFile.getName());
													docNameNFolderNamesList.add(aDocNameNFolder);													
												}
												else{
													modifiedLastList=new ArrayList<Long>();
													if(log.isDebugEnabled()){
														log.debug(METHOD_NAME+": ~~~~adding just instantiated modifiedLastList");
														log.debug(METHOD_NAME+": ~~~~adding aFile.lastModified="+aFile.lastModified());
													}
													modifiedLastList.add(aFile.lastModified());
													docNameNFolderNamesList=new ArrayList<DocNameNFolder>();
													DocNameNFolder aDocNameNFolder=new DocNameNFolder(aDocName,aFile.getName());
													docNameNFolderNamesList.add(aDocNameNFolder);


													//We do not need to call aWar.getLastModifiedListDatesAsStr because
													//the constructor will get it from the modifiedLastList List
													aWar=new InstalledWar(docNameNFolderNamesList, pomName,modifiedLastList,pomGroupId,pomArtId);
													aWar.setAbsolutePathName(aFile.getAbsolutePath());
													aWar.setClouddocsDocbook(clouddocsDocbook);
													tempInternalNReviewerWars.put(pomInfoAsKey, aWar);												
												}
												if(log.isDebugEnabled()){
													log.debug(METHOD_NAME+" docNameNFolderNamesList="+docNameNFolderNamesList);
													if(null!=docNameNFolderNamesList){
														log.debug(METHOD_NAME+" folderName="+folderName+" docNameNFolderNamesList.size()="+docNameNFolderNamesList.size());
													}
												}
											}
											//This is an external .war
											else{
												if(log.isDebugEnabled()){
													log.debug(METHOD_NAME+" (tempWars.containsKey(\""+pomInfoAsKey+"\")="
												    +(tempWars.containsKey(pomInfoAsKey)));
												}
												if(tempWars.containsKey(pomInfoAsKey)){
													aWar=tempWars.get(pomInfoAsKey);
													aWar.setAbsolutePathName(aFile.getAbsolutePath());

													modifiedLastList=aWar.getLastModifiedList();
													modifiedLastList.add(aFile.lastModified());
													clouddocsDocbook=aWar.getClouddocsDocbook();

													/*
													List<String>modifiedListDatesAsStr=aWar.getLastModifiedListDatesAsStr();											
													//SimpleDateFormat sdf=new SimpleDateFormat("E, MMM dd yyyy, hh:mm:ss");
													SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy, hh:mm:ss");
													String aDate=sdf.format(aFile.lastModified());
													modifiedListDatesAsStr.add(aDate);
													 */

													docNameNFolderNamesList=aWar.getDocNameNFolderNamesList();
													DocNameNFolder aDocNameNFolder=new DocNameNFolder(aDocName, aFile.getName());
													docNameNFolderNamesList.add(aDocNameNFolder);													
												}
												else{
													modifiedLastList=new ArrayList<Long>();
													modifiedLastList.add(aFile.lastModified());
													docNameNFolderNamesList=new ArrayList<DocNameNFolder>();
													DocNameNFolder aDocNameNFolder=new DocNameNFolder(aDocName,aFile.getName());
													docNameNFolderNamesList.add(aDocNameNFolder);

													//We do not need to call aWar.getLastModifiedListDatesAsStr because
													//the constructor will get it from the modifiedLastList List
													aWar=new InstalledWar(docNameNFolderNamesList, pomName,modifiedLastList,pomGroupId,pomArtId);
													aWar.setAbsolutePathName(aFile.getAbsolutePath());
													aWar.setClouddocsDocbook(clouddocsDocbook);
													tempWars.put(pomInfoAsKey, aWar);
												}
											}
										}
										if(log.isDebugEnabled()){
											log.debug("Inside folder: "+aFile.getName()+" END:");
										}
									}
								}
							}
							if(isExt){
								//If there are tempWars values, then update the wars map
								if(tempWars.size()>0){
									synchronized(theWars){
										Set<String>keys=tempWars.keySet();
										for(Iterator<String>iter=keys.iterator();iter.hasNext();){
											String aKey=iter.next();
											theWars.put(aKey, tempWars.get(aKey));
										}
									}
								}
							}
							else{
								//If there are tempInternalNReviewer values, then update the internal and reviewers map
								if(tempInternalNReviewerWars.size()>0){
									synchronized(theWars){
										Set<String>keys=tempInternalNReviewerWars.keySet();
										for(Iterator<String>iter=keys.iterator();iter.hasNext();){
											String aKey=iter.next();
											theWars.put(aKey, tempInternalNReviewerWars.get(aKey));
										}
									}
								}
							}
						}
						catch (Throwable e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							if(log.isDebugEnabled()){
								log.debug(METHOD_NAME+": Throwable caught, could not authenticate e.getMessage()="+e.getMessage());
							}
						}
					}
				}
			}
			else{
				if(log.isDebugEnabled()){
					log.debug(METHOD_NAME+": servletCtx is NULL");
				}
			}
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+":  theWars.size()="+theWars.size());
			}
		}
		else{
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": ERROR theWar is NULL");
			}	
			throw new NullPointerException(METHOD_NAME+"theWar is NULL, NULLPointerException");
		}
		if(log.isDebugEnabled()){
			if(null!=theWars){
				log.debug(METHOD_NAME+": theWars.size()="+theWars.size());
				Set<String>keys=theWars.keySet();
				for(String aKey:keys){
					log.debug(METHOD_NAME+": this.theWars.get(\""+aKey+"\")="+theWars.get(aKey));
				}
			}
			log.debug(METHOD_NAME+": END: ");
		}
	}


	//Leave method with default access so that only package friendly classes can call the method
	//set debug to true for testing locally and for debugging
	//When this method returns null, the user has access to the war
	static String retrieveLoggedInUserAccessToWar(InstalledWar aWar, Properties propsFile, Set<Groups>groupsMembership){
		String METHOD_NAME="retrieveLoggedInUserAccessToWar()";
		boolean debug=false;
		String propsDebug=propsFile.getProperty("debug");
		if(null!=propsDebug && propsDebug.equalsIgnoreCase("true")){
			debug=true;
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START: aWar.getAbsolutePathName()="+aWar.getAbsolutePathName()+" debug="+debug);
		}
		String groupId=aWar.getGroupId();
		String artifactId=aWar.getArtifactId();
		String group=groupId+"---"+artifactId;
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": group="+group);
		}
		//Set<String>groupsMembership=
		//Users user=userDao.findById(loggedInUser);
		//We only continue if we have a logged in user
		if(null!=groupsMembership){

			Map<String,Groups>groupsMembershipMap=new HashMap<String, Groups>();
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": groupsMembership.size()="+groupsMembership.size());
			}
			for(Iterator<Groups>iter=groupsMembership.iterator();iter.hasNext();){
				Groups aGroup=iter.next();
				String aKey=aGroup.getName();
				if(log.isDebugEnabled()){
					log.debug(METHOD_NAME+": aGroup="+aGroup.toString());			
					log.debug(METHOD_NAME+": Adding to map with key="+aKey);
				}
				groupsMembershipMap.put(aGroup.getName(), aGroup);
			}
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": groupsMembership="+groupsMembership);
				log.debug(METHOD_NAME+": groupsMembershipMap.containsKey(\""+group+"\")="+groupsMembershipMap.containsKey(group));
				log.debug(METHOD_NAME+": groupsMembershipMap.containsKey(\"all\")="+groupsMembershipMap.containsKey("all"));
				log.debug(METHOD_NAME+": groupsMembershipMap.containsKey(\"admin\")="+groupsMembershipMap.containsKey("admin"));
				log.debug(METHOD_NAME+": debug="+debug);
			}
			//The user has access to the war
			if(groupsMembershipMap.containsKey(group)||
					groupsMembershipMap.containsKey("all")||
					groupsMembershipMap.containsKey("admin")||
					debug){
				if(log.isDebugEnabled()){
					log.debug(METHOD_NAME+": setting group to null");
				}
				//set the group to null so we end up allowing the user to view the associated war
				group=null;
			}
			else{
				if(log.isDebugEnabled()){
					log.debug(METHOD_NAME+": Not setting group to null, group="+group);
				}
			}
		}
		else{
			group=ControllerHelper.NO_ACCESS;
		}

		//If debug is enabled just return null
		if(debug){
			group=null;
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END: group="+group);
		}
		return group;

	}

	static void outputRequestHeadersAndSessionAttributes(HttpServletRequest request, HttpSession session){
		String METHOD_NAME="outputRequestHeaders()";
		Enumeration<String>headerNames=request.getHeaderNames();
		log.debug(METHOD_NAME+":"+"^^^^^^^^^^^^^^headerNames Begin:");
		while(headerNames.hasMoreElements()){
			String aHeaderName=headerNames.nextElement();
			log.debug(METHOD_NAME+": request.get("+aHeaderName+")="+request.getHeader(aHeaderName));
			System.out.println(METHOD_NAME+": request.get("+aHeaderName+")="+request.getHeader(aHeaderName));
		}
		log.debug(METHOD_NAME+":"+"^^^^^^^^^^^^^^headerNames END:");
		Cookie[]cookies=request.getCookies();

		log.debug(METHOD_NAME+": ~~~~~~cookies="+cookies);
		System.out.println(METHOD_NAME+": ~~~~~~cookies="+cookies);	
		if(null!=cookies ){
			log.debug(METHOD_NAME+": cookies.length="+cookies.length);
			for(Cookie aCookie:cookies){
				String cookieName=aCookie.getName();
				String cookieValue=aCookie.getValue();
				log.debug(METHOD_NAME+": cookieName="+cookieName+" cookieValue="+cookieValue);
			}
		}
		Enumeration<String>sessAttributes=session.getAttributeNames();
		log.debug(METHOD_NAME+":"+"^^^^^^^^^^^^^^sessNames Begin:");
		while(sessAttributes.hasMoreElements()){
			String anAttribute=sessAttributes.nextElement();
			log.debug(METHOD_NAME+": session.getAttribute("+anAttribute+")="+session.getAttribute(anAttribute));
		}
		log.debug(METHOD_NAME+":"+"^^^^^^^^^^^^^^sessNames END:");	
	}

	static List<Detail>getDetails(HttpServletRequest request, ServletContext servletCtx, Properties propsFile,String folders,String isInternal){
		String METHOD_NAME="getDetails()";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START:");
		}
		//JSONObject retVal=new JSONObject();
		List<Detail>retVal=new ArrayList<Detail>();
		String serverName=request.getServerName();
		if(null!=isInternal){
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": serverName="+serverName);
				log.debug(METHOD_NAME+": folders="+folders);
				log.debug(METHOD_NAME+": isInternal="+isInternal);
				log.debug(METHOD_NAME+": this.servletCtx="+servletCtx);
			}		
			if(null!=folders && !folders.isEmpty() && null!=servletCtx){
				//JSONArray details=new JSONArray();

				try {
					String prodServer=propsFile.getProperty("prodserviceserver","docs-prod-2");
					if(isInternal.equalsIgnoreCase("true")){
						prodServer=propsFile.getProperty("prodserverinternal","docs-internal");
					}
					if(log.isDebugEnabled()){
						log.debug(METHOD_NAME+":prodServer="+prodServer);
					}
					String deployTimes=DeployUtility.getDeployTimes(prodServer, folders);
					//If the deployTimes is null that could mean that the prodServer is not available,
					//try to get the latest deploy times from the backup prodServer. We do this for
					//the internal server too, because the external server has some of the same
					//folders, and it's better to get some information rather than none.
					if(null==deployTimes||deployTimes.isEmpty() && isInternal.equalsIgnoreCase("false")){
						prodServer=propsFile.getProperty("prodserviceserverbackup","docs-prod-3");
						deployTimes=DeployUtility.getDeployTimes(prodServer, folders);
					}				
					List<String>deployTimesArr=new ArrayList<String>();
					if(log.isDebugEnabled()){
						log.debug(METHOD_NAME+": prodServer="+prodServer);
						log.debug(METHOD_NAME+": deployTimes="+deployTimes);
					}
					if(null!=deployTimes){
						JSONObject deployTimesJsonObj=new JSONObject(deployTimes);

						if(log.isDebugEnabled()){
							log.debug(METHOD_NAME+": deployTimesJsonObj.toString()="+deployTimesJsonObj.toString());
						}

						JSONArray deployTimesJSONArray=deployTimesJsonObj.getJSONArray("deploytimes");
						if(log.isDebugEnabled()){
							log.debug(METHOD_NAME+": deployTimesJSONArray.length()="+deployTimesJSONArray.length());
						}
						for(int i=0;i<deployTimesJSONArray.length();++i){
							JSONObject aJSONObj=(JSONObject)deployTimesJSONArray.get(i);
							String aDeployTime=aJSONObj.getString("adeploytime");
							if(log.isDebugEnabled()){
								log.debug(METHOD_NAME+": aDeployTime="+aDeployTime);
							}
							deployTimesArr.add(aDeployTime);
						}
					}

					//retVal.put("details", details);
					//Get all the folder names
					String[]foldersArr=folders.split(":");
					if(null!=foldersArr){
						String webappsDir=servletCtx.getRealPath("..");
						if(!webappsDir.endsWith("/")){
							webappsDir+="/";
						}
						String debug=propsFile.getProperty("debug");
						if(null!=debug && debug.equals("true")){
							webappsDir="/home/docs/testwebapps/";
						}
						if(log.isDebugEnabled()){
							log.debug(METHOD_NAME+": webappsDir="+webappsDir);
						}
						//String  result=null;
						String  latestJenkinsUrl=null;
						boolean isWarBuilding=false;

						int index=0;
						for(String aFolder:foldersArr){
							String aFullPathWebapp=webappsDir+aFolder;
							if(!aFullPathWebapp.endsWith("/")){
								aFullPathWebapp+="/";
							}
							if(log.isDebugEnabled()){
								log.debug(METHOD_NAME+": aFullPathWebapp="+aFullPathWebapp);
								log.debug(METHOD_NAME+": aFolder="+aFolder);
							}
							File aWebappFolder=new File(aFullPathWebapp);
							if(log.isDebugEnabled()){
								log.debug(METHOD_NAME+": aWebappFolder.exists()="+aWebappFolder.exists());
							}

							if(aWebappFolder.exists()){

								String pomName="";
								String displayName="";
								String artifactid="";
								String groupid="";
								String docname="";
								String jenkinsJobName="";
								String clouddocsDocbook="";
								
								Detail aDetail=new Detail();
								if(aFolder.equalsIgnoreCase("root")){
									if(log.isDebugEnabled()){
										log.debug(METHOD_NAME+": This is just ROOT");
									}
									JSONObject deployTimesJsonObj=new JSONObject(deployTimes);
									JSONArray  rootDeployTimesArr=deployTimesJsonObj.getJSONArray("deploytimes");
									JSONObject rootDeployTimesObj=rootDeployTimesArr.getJSONObject(0);

									pomName="ROOT";
									displayName="ROOT";

									docname="ROOT";
									artifactid=ROOT_ARTIFACT_ID;
									groupid=ROOT_GROUP_ID;

									String lastModified=rootDeployTimesObj.getString("adeploytime");
									aDetail.setLastModified(lastModified);
									aDetail.setPomName(pomName);
									aDetail.setDisplayName(displayName);
									aDetail.setArtifactid(artifactid);
									aDetail.setGroupid(groupid);
									aDetail.setFolderName(aFolder);
									aDetail.setDocName(docname);
									aDetail.setJenkinsJobName(jenkinsJobName);
									aDetail.setClouddocDocbook(clouddocsDocbook);
								}
								else{
									SAXParserFactory factory = SAXParserFactory.newInstance();
									SAXParser saxParser = factory.newSAXParser();
									BookInfoXmlSaxHandler bookinfoXmlSaxHandler=new BookInfoXmlSaxHandler();
									String aBookInfoXmlStr=aFullPathWebapp+"bookinfo.xml";
									if(log.isDebugEnabled()){
										log.debug(METHOD_NAME+": aBookInfoXmlStr="+aBookInfoXmlStr);
									}	
									File aBookInfoXml=new File(aBookInfoXmlStr);

									saxParser.parse(aBookInfoXml, bookinfoXmlSaxHandler);

									String lastModified="";
									if(log.isDebugEnabled()){
										log.debug(METHOD_NAME+": index="+index+" deployTimesArr.size()="+deployTimesArr.size());
									}
									if(index<deployTimesArr.size()){
										lastModified=deployTimesArr.get(index);
									}
									++index;

									pomName=bookinfoXmlSaxHandler.getPomName();
									displayName=bookinfoXmlSaxHandler.getDisplayName();
									artifactid=bookinfoXmlSaxHandler.getArtifactId();
									groupid=bookinfoXmlSaxHandler.getGroupId();
									docname=bookinfoXmlSaxHandler.getDocName();
									jenkinsJobName=bookinfoXmlSaxHandler.getJenkinsJobName();
									clouddocsDocbook=bookinfoXmlSaxHandler.getClouddocsDocBook();

									if(log.isDebugEnabled()){
										log.debug(METHOD_NAME+": lastModified="+lastModified);
										log.debug(METHOD_NAME+": pomName="+pomName);
										log.debug(METHOD_NAME+": displayName="+displayName);
										log.debug(METHOD_NAME+": artifactid="+artifactid);
										log.debug(METHOD_NAME+": groupid="+groupid);
										log.debug(METHOD_NAME+": foldername="+aFolder);
										log.debug(METHOD_NAME+": docname="+docname);
										log.debug(METHOD_NAME+": jenkinsJobName="+jenkinsJobName);
										log.debug(METHOD_NAME+": clouddocsDocbook="+clouddocsDocbook);
									}
									aDetail.setLastModified(lastModified);
									aDetail.setPomName(pomName);
									aDetail.setDisplayName(displayName);
									aDetail.setArtifactid(artifactid);
									aDetail.setGroupid(groupid);
									aDetail.setFolderName(aFolder);
									aDetail.setDocName(docname);
									aDetail.setJenkinsJobName(jenkinsJobName);
									aDetail.setClouddocDocbook(clouddocsDocbook);
								}
								//we have to get the jenkins build status
								String jenkisURL=propsFile.getProperty("jenkinsurl","http://docs-staging.rackspace.com/jenkins");
								//The jenkins URL canNOT end in a slash
								if(jenkisURL.endsWith("/")){
									jenkisURL=jenkisURL.substring(0,(jenkisURL.length()-1));
								}
								String userName=(propsFile.getProperty("username","docs")).trim();
								String password=(propsFile.getProperty("password","raXd0cs")).trim();

								//Authenticate
								HttpClient client=DeployUtility.authenticate(jenkisURL, userName, password);

								//Now we have to get the result value
								String jenkinsLastBuildUrl=propsFile.getProperty("jenkinsurl","http://docs-staging.rackspace.com/jenkins/");

								if(!jenkinsLastBuildUrl.endsWith("/")){
									jenkinsLastBuildUrl+="/"	;
								}
								if(null!=groupid&&groupid.equals("headerservice.feedback")){
									if(null!=jenkinsJobName&&!jenkinsJobName.isEmpty()){
										jenkinsLastBuildUrl+=("job/"+jenkinsJobName+"/lastBuild/api/json");
									}
								}
								else{
									jenkinsLastBuildUrl+=("job/"+groupid+"---"+artifactid+"/lastBuild/api/json");
								}
								if(log.isDebugEnabled()){
									log.debug(METHOD_NAME+": jenkinsLastBuildUrl="+jenkinsLastBuildUrl);
								}
								GetMethod getMethod = new GetMethod(jenkinsLastBuildUrl);

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
								String jsonStr=getMethod.getResponseBodyAsString();
								if(log.isDebugEnabled()){
									log.debug(METHOD_NAME+": status="+status+"\njsonStr="+jsonStr);
									log.debug(METHOD_NAME+": contentType="+contentType);
								}
								//Jenkins is still idle, we have to wait for jenkins to startup
								//For some reason, the first time we get here we are NOT authenticated. I tried sleeping after authenticating, but
								//this did not seem to work. Perhaps we should check to for a status 403 and sleep and try to authenticate again.
								//When we get here and we are not authenticated, the Content-Type returned is not json but instead html. This only 
								//occurs when the Tomcat server is started and we access rax-staging-services for the first time. We are unable to
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
									if(log.isDebugEnabled()){
										log.debug(METHOD_NAME+":$$$$$$$$$$");
										log.debug(METHOD_NAME+":$$$$$$$$$$ contentType="+contentType);
										log.debug(METHOD_NAME+":$$$$$$$$$$");
									}
									try{
										Thread.sleep(DeployUtility.WAIT_FOR_JENKINS);
									}
									catch(InterruptedException e){
										e.printStackTrace();
									}
									++numberOfTries;
									status=client.executeMethod(getMethod);	
								}
								if(log.isDebugEnabled()){
									log.debug(METHOD_NAME+": Had to try "+numberOfTries+
											" to get json return values, aHeader="+aHeader);
								}	
								jsonStr=getMethod.getResponseBodyAsString();
								if(log.isDebugEnabled()){
									log.debug(METHOD_NAME+":jsonStr="+jsonStr);
								}
								JSONObject returnedJsonObj=new JSONObject(jsonStr);
								isWarBuilding= returnedJsonObj.getBoolean("building");
								latestJenkinsUrl=returnedJsonObj.getString("url");
								String jenkinsBuildResult=returnedJsonObj.getString("result");
								//String retStr="";
								String href="";
								String imgSrc="";
								String title="";
								href=propsFile.getProperty("jenkinsurl","http://docs-staging.rackspace.com/jenkins/");
								if(!href.endsWith("/")){
									href+="/";
								}
								href+="job/";
								if(null!=groupid && null!=artifactid ){
									href+=groupid;
									href+="---";
									href+=artifactid;
									href+="/lastBuild";
								}
								if(log.isDebugEnabled()){
									log.debug(METHOD_NAME+": jenkinsBuildResult="+jenkinsBuildResult);
								}
								if(null!=jenkinsBuildResult){
									aDetail.setResult(jenkinsBuildResult);
									if(jenkinsBuildResult.equalsIgnoreCase("success")){
										imgSrc="resources/images/lastbuild-success.png"; 
										title="Successful Jenkins Build";						
									}
									else if(jenkinsBuildResult.equals("failure")){
										imgSrc="resources/images/lastbuild-failure.png";
										title="Failed Jenkins Build";       	    		
									}
									else if(jenkinsBuildResult.equalsIgnoreCase("aborted")){
										imgSrc="resources/images/lastbuild-aborted.png";
										title="Aborted Jenkins Build";    	    	
									}
									else{
										imgSrc="resources/images/lastbuild-unknown.png";
										title="Unkown Jenkins Build";	        	    		
									}
								}
								else{
									imgSrc="resources/images/lastbuild-unknown.png";
									title="Unkown Jenkins Build";	
								}									
								aDetail.setHref(href);
								aDetail.setImgSrc(imgSrc);
								aDetail.setTitle(title);
								if(log.isDebugEnabled()){
									log.debug(METHOD_NAME+": href="+href);
									log.debug(METHOD_NAME+": ~~~~imgSrc="+imgSrc);
									log.debug(METHOD_NAME+": title="+title);
								}

								if(log.isDebugEnabled()){
									log.debug(METHOD_NAME+": building="+isWarBuilding);
									log.debug(METHOD_NAME+": latestjenkinsurl="+latestJenkinsUrl);
								}
								aDetail.setBuilding(isWarBuilding);
								retVal.add(aDetail);
							}
						}
					}
				} 
				catch (JSONException e) {
					e.printStackTrace();
					log.debug(e);
				}
				catch(SAXException e){
					e.printStackTrace();
					log.debug(e);
				}
				catch(ParserConfigurationException e){
					e.printStackTrace();
					log.debug(e);
				}
				catch(IOException e){
					e.printStackTrace();
					log.debug(e);
				}
				catch(Throwable e){
					e.printStackTrace();
					log.debug(e);
				}
			}
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END: retVal.size()="+retVal.size());
		}
		return retVal;
	}

	static boolean isUserAnAdmin(HttpServletRequest request, IUsersDao userDao, String xldapUsername){
		String METHOD_NAME="isUserAnAdmin()";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+":START:");
		}
		boolean retVal=false;
		String serverName=request.getServerName();
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+":serverName="+serverName);
		}	
		//If this is coming localhost, we allow them to view the page for testing
		if(null!=serverName && serverName.equalsIgnoreCase("localhost")){
			return true;
		}

		if(null!=xldapUsername && !xldapUsername.isEmpty()){
			Users aUser=userDao.findById(xldapUsername);
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": aUser: "+aUser);
			}
			//Make sure the user exists
			if(null!=aUser && aUser.getLdapname()!=null && !aUser.getLdapname().isEmpty()){

				String userStatus=aUser.getStatus();
				if(log.isDebugEnabled()){
					log.debug(METHOD_NAME+": userStatus: "+userStatus);
				}
				//The user must be active
				if(null!=userStatus && userStatus.equalsIgnoreCase("active")){
					Set<Groups>groupMembership=aUser.getGroupses();
					if(null!=groupMembership){

						if(log.isDebugEnabled()){
							log.debug(METHOD_NAME+": groupMembership.size()="+groupMembership.size());
							log.debug(METHOD_NAME+": Group membership for user: "+aUser.getLdapname()+" START:");
						}
						//As long as the user is in one group then we set the return value to true
						for(Groups aGroup:groupMembership){
							if(log.isDebugEnabled()){
								log.debug(METHOD_NAME+": "+aGroup.getName());
							}
							String group=aGroup.getName();
							if(null!=group && group.equalsIgnoreCase("admin")){
								retVal=true;
								break;
							}
						}
						if(log.isDebugEnabled()){
							log.debug(METHOD_NAME+": Group membership for user: "+aUser.getLdapname()+" END:");
						}
					}
				}
			}

		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+":END: retVal="+retVal);
		}
		return retVal;
	}

	static boolean getShouldDisplay(ServletContext servletCtx, HttpSession session){
		String METHOD_NAME="getShouldDisplay()";
		String url=(String)session.getAttribute("from-url");
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START: url="+url);
		}
		boolean retVal=true;

		if(null!=servletCtx && null!=url){
			String path=servletCtx.getRealPath("..");

			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": path="+path);
			}
			if(null!=path){
				if(path.toLowerCase().contains("internal")){
					//This is from the internal server
					if(!url.contains("InstalledWarsInternal.jsp")){
						retVal=false;
					}
				}
				else{
					//This is from the external server
					if(url.contains("InstalledWarsInternal.jsp")){
						retVal=false;
					}
				}
			}
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END: retVal="+retVal);
		}		
		return retVal;
	}

	public static String getFirstSingleTagValueStartingFromParent(String parentTag, String tagName, File xmlFile){
		String retVal="";
		if(null!=tagName && !tagName.isEmpty() && null!=xmlFile && xmlFile.exists()){
			if(null!=parentTag && !parentTag.isEmpty()){
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				try{
					DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();	
					FileInputStream innyStream=new FileInputStream(xmlFile);

					//Make sure we support unicode characters in the xml file
					//For this to work, the .jsp page must have: <%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" /%>
					InputStreamReader innyStreamReader=new InputStreamReader(innyStream);
					InputSource is=new InputSource();
					is.setEncoding("UTF-8");
					is.setCharacterStream(innyStreamReader);

					Document doc=dBuilder.parse(is);
					doc.getDocumentElement().normalize();

					NodeList parentNodeList=doc.getElementsByTagName(parentTag);
					if(null!=parentNodeList){
						for(int i=0;i<parentNodeList.getLength();++i){
							Node aNode=parentNodeList.item(i);
							if(aNode.getNodeType()==Node.ELEMENT_NODE){
								Element parentElement=(Element)aNode;
								NodeList tagNameList=parentElement.getElementsByTagName(tagName);
								if(null!=tagNameList){
									Node tagNameNode=tagNameList.item(0);
									if(null!=tagNameNode){
										NodeList children=tagNameNode.getChildNodes();
										if(null!=children){
											Node node=children.item(0);
											if(null!=node){
												retVal=node.getNodeValue();
												break;
											}
										}
									}
								}
							}
						}
					}				
				}
				catch(ParserConfigurationException e){
					e.printStackTrace();
				}
				catch(FileNotFoundException e){
					e.printStackTrace();
				}
				catch(IOException e){
					e.printStackTrace();
				}
				catch(SAXException e){
					e.printStackTrace();
				}
			}
			else{
				retVal=getFirstSingleTagValue(tagName, xmlFile);
			}
		}
		return retVal;
	}

	public static String getFirstSingleTagValue(String tagName, File xmlFile){
		String retVal="";
		if(null!=tagName && !tagName.isEmpty() && null!=xmlFile && xmlFile.exists()){
			try{
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();	

				FileInputStream innyStream=new FileInputStream(xmlFile);

				//Make sure we support unicode characters in the xml file
				//For this to work, the .jsp page must have: <%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" /%>
				InputStreamReader innyStreamReader=new InputStreamReader(innyStream);
				InputSource is=new InputSource();
				is.setEncoding("UTF-8");
				is.setCharacterStream(innyStreamReader);

				Document doc=dBuilder.parse(is);
				doc.getDocumentElement().normalize();

				NodeList latestPdfsNodeList=doc.getElementsByTagName(tagName);
				if(null!=latestPdfsNodeList){
					//There should be only one tag with the tagname, but in case there are multiple,
					//get the first respective value
					for(int i=0;i<latestPdfsNodeList.getLength();++i){
						Node aNode=latestPdfsNodeList.item(i);
						String context=aNode.getTextContent();
						//We have found the first instance of a value for tagName
						if(null!=context && !context.trim().isEmpty()){
							retVal=context.trim();
							break;
						}
					}
				}

			}
			catch(ParserConfigurationException e){
				e.printStackTrace();
			}
			catch(FileNotFoundException e){
				e.printStackTrace();
			}
			catch(IOException e){
				e.printStackTrace();
			}
			catch(SAXException e){
				e.printStackTrace();
			}
		}
		return retVal;
	}

	public static void addMessage(JSONObject jsonObj, String messageKey, String aMessage){
		String METHOD_NAME="addMessage()";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START: messageKey="+messageKey+" aMessage="+aMessage+
					" jsonObj.toString()="+jsonObj.toString());
		}
		if(null!=jsonObj){
			try{
				String theMessage=null;
				if(jsonObj.has(messageKey)){
					theMessage=(String)jsonObj.getString(messageKey);
				}
				if(log.isDebugEnabled()){
					log.debug(METHOD_NAME+": theMessage="+theMessage);
				}
				if(null==theMessage||theMessage.isEmpty()){
					theMessage=aMessage;
				}
				else{
					theMessage+="\n";
					theMessage+=aMessage;
				}
				jsonObj.put(messageKey, theMessage);			
			}
			catch(JSONException e){
				log.error(METHOD_NAME+":"+ e);
				e.printStackTrace();
			}
			catch(Throwable e){
				log.error(METHOD_NAME+":"+ e);
				e.printStackTrace();
			}
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END: jsonObj.toString()="+jsonObj.toString());
		}
	}

}
