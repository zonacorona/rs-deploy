package com.rackspace.cloud.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.rackspace.cloud.api.dao.IMembersDao;
import com.rackspace.cloud.api.dao.IUsersDao;
import com.rackspace.cloud.api.entity.Members;
import com.rackspace.cloud.api.entity.Users;
import com.rackspace.cloud.api.jclouds.DocToolsEntity;
import com.rackspace.cloud.api.jclouds.JCloudsException;

public class DeployUtility {
	
	private static Logger log = Logger.getLogger(DeployUtility.class);  
	private static Properties propsFile;	
	
	public static final String STATUS_ABORTED="aborted";
	public static final String STATUS_DONE="done";
	public static final String STATUS_FAILED="failed";
	public static final String STATUS_NODE1_STARTED="node1 started";
	public static final String STATUS_OTHER_NODES_STARTED="other nodes started";
	public static final String STATUS_TO_INTERNAL_FROM_EXTERNAL="deploy to internal from external";
	public static final String STATUS_STARTED="started";
	public static final String JOB_TYPE_DEPLOY="deploy";
	public static final String JOB_TYPE_REVERT="revert";
	public static int          WAIT_FOR_JENKINS=5000;
	public static int          MAX_NUMBER_OF_JENKINS_TRIES=10;
	public static final String PUBLIC_0_STR="public0_v4";
	public static final String PUBLIC_1_STR="public1_v4";
	public static final String IPADDRESSES="ip_addresses";
	public static final String PRIVATE_JSON_IP_ADDRESS="private0_v4";

	

	static{
		try {
			//ServletContext ctx=super.getServletContext().getContext("/rax-autodeploy");
			//InputStream inny=ctx.getResourceAsStream("WEB-INF/props.properties");
			InputStream inny=DeployUtility.class.getClassLoader().getResourceAsStream("props.properties");
			propsFile=new Properties();
			propsFile.load(inny);
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch(Throwable e){
			e.printStackTrace();
		}
	}

	public static void loadUsers(IMembersDao membersDao, IUsersDao users,Map<String, Set<String>>userAccessToGroupsMap){
		String METHOD_NAME="loadUsers()";
		
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START:");
		}
		if(null!=userAccessToGroupsMap){
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": userAccessToGroupsMap.size()="+userAccessToGroupsMap.size());
			}
			//HibernateUtil util=new HibernateUtil();
			//SessionFactory sessionFactory=util.getSessionFactory();
			
			//Session session=sessFactory.openSession();
			//UsersDaoImpl users=new UsersDaoImpl();
			
			//Transaction trans=session.getTransaction();
			//trans.begin();
			List<Users> usersList=users.findAll();
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": usersList="+usersList);
			}
			if(null!=usersList){
				if(log.isDebugEnabled()){
					log.debug(METHOD_NAME+": usersList.size()="+usersList.size());
				}
				for(Object aUserObj:usersList){
					Users aUser=(Users)aUserObj;
					//Now we need to find all the groups that the user is in
					String ldapname=aUser.getLdapname();
					if(log.isDebugEnabled()){
						log.debug(METHOD_NAME+": ldapname="+ldapname);
						log.debug(METHOD_NAME+": !userAccessToGroupsMap.containsKey("+ldapname+")="+
								!userAccessToGroupsMap.containsKey(ldapname));
					}
					Set<String>groups=null;
					//This ldap username is not in the Map
					if(!userAccessToGroupsMap.containsKey(ldapname)){
						if(log.isDebugEnabled()){
							log.debug(METHOD_NAME+": creating new groups HashSet and adding it to userAccessToGroupsMap");
						}
						groups=new HashSet<String>();

						//Add the groups to the map keyed by the ldapname
						userAccessToGroupsMap.put(ldapname, groups);
					}
					//The ldap username if in the Map, just get the Set of group names
					else{
						groups=userAccessToGroupsMap.get(ldapname);
						if(log.isDebugEnabled()){
							log.debug(METHOD_NAME+": groups HashSet already exists: groups.size()="+groups.size());
						}
					}
					//Get all the groups that the user belongs to
					List<Members> membersList=membersDao.findByLdapname(ldapname);
					if(log.isDebugEnabled()){
						log.debug(METHOD_NAME+": membersList="+membersList);
					}				
					if(null!=membersList){
						if(log.isDebugEnabled()){
							log.debug(METHOD_NAME+": membersList.size()="+membersList.size());
						}

						for(Object memObj:membersList){
							Members aMember=(Members)memObj;
							String groupName=aMember.getId().getGroupname();
							if(log.isDebugEnabled()){
								log.debug(METHOD_NAME+": Adding groupName="+groupName);
							}
							groups.add(groupName);
						}
					}
				}
			}
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": Ending userAccessToGroupsMap.size()="+userAccessToGroupsMap.size());
				log.debug(METHOD_NAME+": ~~~~~~~~loadedUsers start:");
				Set<String>keys=userAccessToGroupsMap.keySet();
				for(String aKey:keys){
					Set<String>groups=userAccessToGroupsMap.get(aKey);
					log.debug(METHOD_NAME+": groups for user: "+aKey);
					log.debug("{");
					for(Iterator<String>iter=groups.iterator();iter.hasNext();){
						String aGroup=iter.next();
						log.debug(aGroup+" ");
					}
					log.debug("}");
				}
				log.debug(METHOD_NAME+": ~~~~~~~~loadedUsers end:");
			}
			//trans.commit();
		}		
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END: ");
		}
	}	
	
	private static String genericJschCommand(String command, String prodServer){
		String METHOD_NAME="genericJschCommand()";
		String retVal="";
	    if(log.isDebugEnabled()){
	    	log.debug(METHOD_NAME+": START: command="+command+" nprodServer="+prodServer);

	    }			
	    retVal=DeployUtility.jSchExec(command, prodServer, null);
	    if(log.isDebugEnabled()){
	    	log.debug(METHOD_NAME+":commandRetVal="+retVal+"~~~~~~~~~");
	    }
    	   
	    if(log.isDebugEnabled()){
	    	log.debug(METHOD_NAME+": END: retVal="+retVal);
	    }
	    return retVal;
	}
	
	public static boolean doesProdServerContainWarInScpwebapps1(String prodServer, String fullPathToFile){
	    String METHOD_NAME="doesProdServerContainWarInScpwebapps1()";
	    boolean retVal=false;
	    if(log.isDebugEnabled()){
	    	log.debug(METHOD_NAME+": START: fullPathToFile="+fullPathToFile);	    	
	    }	    
	    String command="ls "+fullPathToFile;
	    if(log.isDebugEnabled()){
	    	log.debug(METHOD_NAME+":command="+command);
	    }
	    String commandRetVal=DeployUtility.genericJschCommand(command, prodServer);
	    if(null!=commandRetVal && commandRetVal.toLowerCase().contains("exit-status: 0")){
	    	retVal=true;
	    }
	    if(log.isDebugEnabled()){
	    	log.debug(METHOD_NAME+": END retVal="+retVal);
	    }
	    return retVal;
	}
	
	//Returns the String exit-status: 0 if the command is successful
	public static String copyWarFromScpwebapps1ToScpwebapps2OverScp(String prodServer, String fullPathToFileName, String fullPathToNewFileName){
		String METHOD_NAME="copyWarFromScpwebapps1ToScpwebapps2()";
	    if(log.isDebugEnabled()){
	    	log.debug(METHOD_NAME+": START: prodServer="+prodServer+" fullPathToFileName="+fullPathToFileName+" fullPathToNewFileName="+fullPathToNewFileName);
	    }
		String retVal="";
		String command="cp "+fullPathToFileName+" "+fullPathToNewFileName;
		retVal=DeployUtility.genericJschCommand(command, prodServer);
	    if(log.isDebugEnabled()){
	    	log.debug(METHOD_NAME+": command="+command);
	    }
	    
	    if(log.isDebugEnabled()){
	    	log.debug(METHOD_NAME+": END:");
	    }
		return retVal;
	}
	
	//Returns the String exit-status: 0 if the command is successful
	public static String moveWarFromScpwebapps2ToWebappsOverScp(String prodServer, String fullpathToScp2File, String fullpathToWebappsFile){
		String METHOD_NAME="moveWarFromScpwebapps2ToWebappsOverScp()";
	    if(log.isDebugEnabled()){
	    	log.debug(METHOD_NAME+": START: prodServer="+prodServer+" fullpathToScp2File="+fullpathToScp2File+" fullpathToWebappsFile="+fullpathToWebappsFile);
	    }
		String retVal="";
		String command="mv "+fullpathToScp2File+" "+fullpathToWebappsFile;
		retVal=DeployUtility.genericJschCommand(command, prodServer);
	    if(log.isDebugEnabled()){
	    	log.debug(METHOD_NAME+": command="+command);
	    }
	    
	    if(log.isDebugEnabled()){
	    	log.debug(METHOD_NAME+": END: retVal="+retVal);
	    }
		return retVal;
	}
	
	public static List<TomcatPid>getTomcatPidsOnServer(String serverName){
		String METHOD_NAME="getTomcatPidsOnServer()";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START:");
		}
		List<TomcatPid>retVal=new ArrayList<TomcatPid>();
		String scpUser=(propsFile.getProperty("scpuser","docs")).trim();									
		String passwd=(propsFile.getProperty("docspasswd","Fanatical7")).trim();
		String known_hosts=(propsFile.getProperty("knownhosts","/home/docs/.ssh/known_hosts")).trim();
		String id_rsa=(propsFile.getProperty("idrsa","/home/docs/.ssh/id_rsa")).trim();
		String pids=jSchExec(scpUser,passwd,known_hosts,id_rsa,serverName, null,"ps -ef | grep tomcat");
		if(log.isDebugEnabled()){
		    log.debug(METHOD_NAME+": pids="+pids);
		    log.debug(METHOD_NAME+": scpUser="+scpUser);
		    log.debug(METHOD_NAME+": known_hosts="+known_hosts);
		    log.debug(METHOD_NAME+": id_rsa="+id_rsa);
		}
		if(null!=pids&&!pids.isEmpty()){
			String[] pidsArr=pids.split("\\n");
			for(String aPid:pidsArr){
				if(log.isDebugEnabled()){
				    log.debug(METHOD_NAME+": aPid="+aPid);
				}
				if(!aPid.isEmpty() && aPid.toLowerCase().contains("-dcatalina.base")){
					TomcatPid tomcatPid=new TomcatPid();
					
					String[] aPidStrArr=aPid.split("\\s");
					List<String>aTomcatPidList=new ArrayList<String>();
					if(null!=aPidStrArr){
						for(String aPidStr:aPidStrArr){
							if(null!=aPidStr&&!aPidStr.isEmpty()){
								aTomcatPidList.add(aPidStr);
								if(aPidStr.toLowerCase().contains("-dcatalina.base")){
									int index=aPidStr.indexOf("=");
									if(-1!=index){
									    tomcatPid.setBasePath(aPidStr.substring((index+1)));
									}
								}
							}
						}
					}
					if(aTomcatPidList.size()>0){
						tomcatPid.setUser(aTomcatPidList.get(0));
					}
					if(aTomcatPidList.size()>1){
						tomcatPid.setId(aTomcatPidList.get(1));
					}
					retVal.add(tomcatPid);
				}
			}
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END:");
		}
		return retVal;
	}
	
	public static boolean isTomcatRunning(String prodServer, boolean isAnInternalDeploy){
		String METHOD_NAME="isTomcatRunning()";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START:");
		}
		boolean retVal=false;

		//First check to see if tomcat is up and running
		List<TomcatPid>tomcatPids=DeployUtility.getTomcatPidsOnServer(prodServer);
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": tomcatPids.size()="+tomcatPids.size());
			log.debug(METHOD_NAME+": tomcatPids="+tomcatPids);
		}
		if(tomcatPids.size()>0){
			String theBasePath="/home/docs/Tomcat/latest";
			if(isAnInternalDeploy){
				theBasePath="/home/docs/Tomcat/internal/latest";
			}
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": theBasePath="+theBasePath);
			}
		    for(TomcatPid aPid:tomcatPids){
		    	String basePath=aPid.getBasePath();
		    	if(log.isDebugEnabled()){
		    		log.debug(METHOD_NAME+":basePath="+basePath);
		    	}
		    	if(null!=basePath&&basePath.equals(theBasePath)){
		    		retVal=true;
		    		break;
		    	}
		    }
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END: retVal="+retVal);
		}
		return retVal;
	}
	
	public static String shutDownTomcat(String serverName, Map<String,List<String>>messages, boolean isInternalDeploy){
		String retVal="";
		String scpUser=(propsFile.getProperty("scpuser","docs")).trim();									
		String passwd=(propsFile.getProperty("docspasswd","Fanatical7")).trim();
		String known_hosts=(propsFile.getProperty("knownhosts","/home/docs/.ssh/known_hosts")).trim();
		String id_rsa=(propsFile.getProperty("idrsa","/home/docs/.ssh/id_rsa")).trim();
		if(!isInternalDeploy){
			retVal=jSchExec(scpUser,passwd,known_hosts,id_rsa,serverName,messages,"/home/docs/Tomcat/latest/bin/shutdown.sh");
		}
		else{
			retVal=jSchExec(scpUser,passwd,known_hosts,id_rsa,serverName,messages,"/home/docs/Tomcat/internal/latest/bin/shutdown.sh");
		}
		return retVal;		
	}
	
	public static String startUpTomcat(String serverName, Map<String,List<String>>messages, boolean isInternalDeploy){
		String retVal="";
		String scpUser=(propsFile.getProperty("scpuser","docs")).trim();									
		String passwd=(propsFile.getProperty("docspasswd","Fanatical7")).trim();
		String known_hosts=(propsFile.getProperty("knownhosts","/home/docs/.ssh/known_hosts")).trim();
		String id_rsa=(propsFile.getProperty("idrsa","/home/docs/.ssh/id_rsa")).trim();
		if(!isInternalDeploy){
			retVal=jSchExec(scpUser,passwd,known_hosts,id_rsa,serverName,messages,"/home/docs/Tomcat/latest/bin/startup.sh");
		}
		else{
			retVal=jSchExec(scpUser,passwd,known_hosts,id_rsa,serverName,messages,"/home/docs/Tomcat/internal/latest/bin/startup.sh");
		}
		return retVal;		
	}
	
	public static String backupSelectedWar(String serverName, Map<String,List<String>>messages, String absolutePathToWar,
			String toFolder){
		if(!toFolder.endsWith("/")){
			toFolder+="/";
		}
		String scpUser=(propsFile.getProperty("scpuser","docs")).trim();									
		String passwd=(propsFile.getProperty("docspasswd","Fanatical7")).trim();
		String known_hosts=(propsFile.getProperty("knownhosts","/home/docs/.ssh/known_hosts")).trim();
		String id_rsa=(propsFile.getProperty("idrsa","/home/docs/.ssh/id_rsa")).trim();
		return jSchExec(scpUser,passwd,known_hosts,id_rsa,serverName,messages,"cp "+absolutePathToWar+" "+toFolder+".");	
	}
	
	public static String restoreSelectedWarFromTemp(String serverName, Map<String,List<String>>messages, String fullPathToTempWar,
			String toFolder){
		
		String scpUser=(propsFile.getProperty("scpuser","docs")).trim();									
		String passwd=(propsFile.getProperty("docspasswd","Fanatical7")).trim();
		String known_hosts=(propsFile.getProperty("knownhosts","/home/docs/.ssh/known_hosts")).trim();
		String id_rsa=(propsFile.getProperty("idrsa","/home/docs/.ssh/id_rsa")).trim();
		return jSchExec(scpUser,passwd,known_hosts,id_rsa,serverName,messages,"mv "+fullPathToTempWar+" "+toFolder);
	}
	
	public static String jSchExec(String command, String prodServer, Map<String,List<String>>messages){
		String scpUser=(propsFile.getProperty("scpuser","docs")).trim();							
		String passwd=(propsFile.getProperty("docspasswd","Fanatical7")).trim();
		String known_hosts=(propsFile.getProperty("knownhosts","/home/docs/.ssh/known_hosts")).trim();
		File known_hostsFile=new File(known_hosts);
		if(!known_hostsFile.exists()){
			known_hosts="/Users/thu4404/.ssh/known_hosts";
		}
		String id_rsa=(propsFile.getProperty("idrsa","/home/docs/.ssh/id_rsa")).trim();
		File id_rsaFile=new File(id_rsa);
		if(!id_rsaFile.exists()){
			id_rsa="/Users/thu4404/.ssh/id_rsa";
		}
		return jSchExec(scpUser,passwd,known_hosts,id_rsa,prodServer,messages,command);
	}

	private static String jSchExec(String scpUser, String passwd, String known_hosts, String id_rsa, 
			String prodServer, Map<String,List<String>>messages, String command){
		String retVal="";
		String METHOD_NAME="private static jSchExec()";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START: scpUser="+scpUser+" passwd="+passwd+" known_hosts="+known_hosts+" id_rsa="+id_rsa+
					" prodServer="+prodServer+ " command="+command);
		}
		JSch jsch=new JSch();
		Channel channel=null;
		com.jcraft.jsch.Session session=null;
		try {
			jsch.setKnownHosts(known_hosts);
			jsch.addIdentity(id_rsa);
			//Get a jsch session
			session=jsch.getSession(scpUser, prodServer, 22);
			session.setPassword(passwd);
			//Properties config = new Properties();
			//config.put("StrictHostKeyChecking","yes");
			//session.setConfig(config);
			session.setConfig("PreferredAuthentication", "publickey");
			session.setConfig("StrictHostKeyChecking","no");
			session.connect();

			channel=session.openChannel("exec");
			((ChannelExec)channel).setCommand(command);
			
			channel.setInputStream(null);
			((ChannelExec)channel).setErrStream(System.err);

			InputStream inny=channel.getInputStream();			
			channel.connect();
			
		      byte[] tmp=new byte[1024];
		      StringBuffer strBuff=new StringBuffer("");
		      while(true){
		    	  while(inny.available()>0){
		    		  int i=inny.read(tmp, 0, 1024);
		    		  if(i<0){
		    			  break;
		    		  }
		    		  strBuff.append(new String(tmp, 0, i));
		    	  }
		    	  if(channel.isClosed()){
		    		  strBuff.append("exit-status: "+channel.getExitStatus());
		    		  break;
		    	  }
		    	  try{
		    		  Thread.sleep(1000);
		    	  }
		    	  catch(InterruptedException e){
		    		  strBuff.append("InterruptException caught message: ");
		    		  strBuff.append(e.getMessage());
		    		  if(null!=messages){
		    			  List<String>messagesList=messages.get("error");
		    			  if(null==messagesList){
		    				  messagesList=new ArrayList<String>();
		    				  messages.put("error", messagesList);
		    			  }
		    			  messagesList.add("<span class='failuremessage'>InterruptedException Error sleeping and trying to run jSch command: "+
		    					  command+" with error message:"+ e.getMessage()+"</span>");
		    		  }
		    		  e.printStackTrace();
		    	  }
		    	  catch(Throwable e){
		    		  strBuff.append("Throwable Exception caught message: ");
		    		  strBuff.append(e.getMessage());
		    		  if(null!=messages){
		    			  List<String>messagesList=messages.get("error");
		    			  if(null==messagesList){
		    				  messagesList=new ArrayList<String>();
		    				  messages.put("error", messagesList);
		    			  }
		    			  messagesList.add("<span class='failuremessage'>Throwable Error while sleeping and trying to run jSch command: "+
		    					  command+" with error message:"+ e.getMessage()+"</span>");
		    		  }
		    		  e.printStackTrace();
		    	  }
		      }
		    channel.disconnect();
			session.disconnect();
		    retVal=strBuff.toString();
		}
		catch (JSchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			log.debug(METHOD_NAME+": END: JSchException caught e.getMessage()="+e.getMessage());
			if(null!=messages){
				List<String>messagesList=messages.get("error");
				if(null==messagesList){
					messagesList=new ArrayList<String>();
					messages.put("error", messagesList);
				}
				messagesList.add("<span class='failuremessage'>JSchException Error trying to run jSch command: "+command+
						" with error message:"+ e.getMessage()+"</span>");
			}
		}
		catch(Throwable e){
			log.debug(METHOD_NAME+": END: Throwable caught e.getMessage()="+e.getMessage());
			if(null!=messages){
				List<String>messagesList=messages.get("error");
				if(null==messagesList){
					messagesList=new ArrayList<String>();
					messages.put("error", messagesList);
				}
				messagesList.add("<span class='failuremessage'>Throwable Exception Error trying to run jSch command: "+command+
						" with error message:"+ e.getMessage()+"</span>");
			}
			e.printStackTrace();
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END: retVal="+retVal);
		}
		return retVal;
	}

	public static boolean scpFileToServer(String scpUser, String prodServer, File fileToTransfer, String finalFileName, 
			String prodWebAppsPath, String passwd,String known_hosts, String id_rsa, Map<String,List<String>> messages){
		String METHOD_NAME="scpFileToServer";
		boolean retVal=true;
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START prodServer="+prodServer+" fileToTransfer="+fileToTransfer+
					" finalFileName="+finalFileName+" prodWebAppsPath="+prodWebAppsPath);

		}		
		JSch jsch=new JSch();
		try {
			jsch.setKnownHosts(known_hosts);
			jsch.addIdentity(id_rsa);
			//Get a jsch session
			com.jcraft.jsch.Session session=jsch.getSession(scpUser, prodServer, 22);
			session.setPassword(passwd);
			//Properties config = new Properties();
			//config.put("StrictHostKeyChecking","yes");
			//session.setConfig(config);
			session.setConfig("PreferredAuthentication", "publickey");
			session.setConfig("StrictHostKeyChecking","no");
			session.connect();

			Channel channel=session.openChannel("sftp");
			channel.connect();
			ChannelSftp channelSftp=(ChannelSftp)channel;
			channelSftp.cd(prodWebAppsPath);

			FileInputStream innyStream=new FileInputStream(fileToTransfer);
			channelSftp.put(innyStream,finalFileName);

			channel.disconnect();
			session.disconnect();
			innyStream.close();	    		    
		} 
		catch (JSchException e) {
			e.printStackTrace();
			retVal=false;
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": END: JSchException caught e.getMessage()="+e.getMessage());
			}

			DeployUtility.addABadMessage(finalFileName, messages,"<span class='failuremessage'>Could not upload "+finalFileName+" to "+prodServer+" JSch error: "+e.getMessage()+"</span>");
		}
		catch(SftpException e){
			e.printStackTrace();
			retVal=false;
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": END: SftpException caught e.getMessage()="+e.getMessage());
			}
			DeployUtility.addABadMessage(finalFileName, messages,"<span class='failuremessage'>Could not upload "+finalFileName+" to"+prodServer+" Sftp error: "+e.getMessage()+"</span>");
		}
		catch(FileNotFoundException e){
			e.printStackTrace();
			retVal=false;
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": END: FileNotFoundException caught e.getMessage()="+e.getMessage());
			}
			DeployUtility.addABadMessage(finalFileName, messages,"<span class='failuremessage'>Could not upload "+finalFileName+" to "+prodServer+" File not found error: "+e.getMessage()+"</span>");

		}
		catch(IOException e){
			e.printStackTrace();
			retVal=false;
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": END: IOException caught e.getMessage()="+e.getMessage());
			}
			DeployUtility.addABadMessage(finalFileName, messages,"<span class='failuremessage'>Could not upload "+finalFileName+" to +"+prodServer+" Input/output error: "+e.getMessage()+"</span>");
		}
		catch(Throwable e){
			e.printStackTrace();
			retVal=false;
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": END: Thorwable caught e.getMessage()="+e.getMessage());
			}
			DeployUtility.addABadMessage(finalFileName, messages,"<span class='failuremessage'>Could not upload "+finalFileName+" to "+prodServer+" Unknown Throwable error: "+e.getMessage()+"</span>");
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END retVal="+retVal);
		}
		return retVal;
	}
	
	public static String getBuildDateOfWarOnScpwebapps2(String prodServer, String warFileName) throws JSONException{
		String METHOD_NAME="getBuildDateOfWarOnScpwebapps2()";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START: :prodServer="+prodServer+" warFileName="+warFileName);
		}
		String retVal="";
		//We must make a rest call to respective server in the form of:
		//http://serverName/rax-scpwebapps/rest/raxscp/raxbuildtime?war=cm-devguide.war
		//this has changd to: http://serverName/rax-prod-services/rest/raxscp/raxbuildtime?war=cm-devguide.war
		String url="http://"+prodServer+"/rax-prod-services/rest/raxscp/raxbuildtime?war="+warFileName;
		try{
		    retVal=makeRestCall(url);
		}
		catch(MalformedURLException e){
			e.printStackTrace();
			log.debug(e);
			retVal=null;
		}
		catch(IOException e){
			e.printStackTrace();
			log.debug(e);
			retVal=null;			
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+":deploytimes="+retVal);
		}	
		JSONObject jsonObj=new JSONObject(retVal);	
		retVal=jsonObj.getString("buildtime");
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+":END: retVal="+retVal);
		}
		return retVal;
	}
	
	public static String getDeployTimes(String prodServer, String folders){
		String METHOD_NAME="getDeployTimes()";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START: :prodServer="+prodServer+" folders="+folders);
		}
		String url="http://"+prodServer+"/rax-prod-services/rest/raxscp/getdeploytimes?folders="+folders;
		String retVal="";
		try{
		    retVal=makeRestCall(url);
		}
		catch(MalformedURLException e){
			e.printStackTrace();
			log.debug(e);
			retVal=null;
		}
		catch(IOException e){
			e.printStackTrace();
			log.debug(e);
			retVal=null;			
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+":END: deploytimes="+retVal);
		}
		return retVal;				
	}
	
	private static JSONObject getAuthenticationMetadata(){
		JSONObject retVal=null;
		try {
			 retVal=makeRESTCall("POST","https://identity.api.rackspacecloud.com/v2.0/tokens",null,null,null,null,
					"{\"auth\":{\"passwordCredentials\":{\"username\":\"mossoths\",\"password\":\"M0ss0h0st1ng\"}}}");			 
		} 
		catch (MalformedURLException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		} 
		catch (JSONException e) {
			e.printStackTrace();
		}
		return retVal;
	}
	
	private static JSONObject getAuthenTokenAndAccount()throws JSONException{
		String METHOD_NAME="getAuthenTokenAndAccount()";
		JSONObject retVal=new JSONObject();
		JSONObject authMetadata=getAuthenticationMetadata();
		if(null!=authMetadata && !authMetadata.isNull("access")){
			String token=authMetadata.getJSONObject("access").getJSONObject("token").getString("id");
			String account=authMetadata.getJSONObject("access").getJSONObject("token").getJSONObject("tenant").getString("id");
			retVal.put("token", token);
			retVal.put("account", account);
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": token="+token+" account="+account);
			}
		}
		return retVal;
	}

	public static JSONObject getEntities(){
		String METHOD_NAME="getEntities()";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START");
		}
		JSONObject retVal=null;
		try {
			JSONObject authMetadata=getAuthenTokenAndAccount();

			if(null!=authMetadata){			

				String[] headers={"X-Auth-Token"};
				String[] values={authMetadata.getString("token")};
				String account=authMetadata.getString("account");
				String url="https://monitoring.api.rackspacecloud.com/v1.0/"+account+"/entities";

				if(log.isDebugEnabled()){
					log.debug(METHOD_NAME+": url="+url);
				}
				retVal=makeRESTCall("GET",url, headers, values, null, null, null);
			} 
		}
		catch (MalformedURLException e) {
			log.error(e);
			e.printStackTrace();
		}
		catch (IOException e) {
			log.error(e);
			e.printStackTrace();
		}
		catch (JSONException e) {
			log.error(e);
			e.printStackTrace();
		}
		catch (Throwable e) {
			log.error(e);
			e.printStackTrace();
		}		
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END:");
		}
		return retVal;
	}

	//Gets all entities that have an ip_address JSONObject, note this will retrieve entities that
	//are not a member of the load balancer. The Map returned is keyed by the public (it could be
	//public0_v4 or public1_v4) ip_address of the entity
	public static Map<String,DocToolsEntity>getEntitiesInAMap()throws JSONException{
		String METHOD_NAME="getEntitiesInMap()";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START:");
		}
		Map<String, DocToolsEntity>retVal=new HashMap<String, DocToolsEntity>();
		
		JSONObject entitiesJSONObj=DeployUtility.getEntities();

		if(null!=entitiesJSONObj && !entitiesJSONObj.isNull("values")){

			JSONArray entitiesJSONArr=entitiesJSONObj.getJSONArray("values");
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": entitiesJSONArr.length()="+entitiesJSONArr.length());
			}
			for(int i=0;i<entitiesJSONArr.length();++i){
				JSONObject jsonEntity=entitiesJSONArr.getJSONObject(i);
				
				if(jsonEntity.has(DeployUtility.IPADDRESSES) && !jsonEntity.isNull(DeployUtility.IPADDRESSES)){
					if(log.isDebugEnabled()){
						log.debug(METHOD_NAME+": jsonEntity.toString()="+jsonEntity.toString());
					}

					JSONObject ipAddressesJsonObj=jsonEntity.getJSONObject(DeployUtility.IPADDRESSES);
					if(log.isDebugEnabled()){
						log.debug(METHOD_NAME+": ipAddressesJsonObj.toString()="+ipAddressesJsonObj.toString());
						log.debug(METHOD_NAME+": (ipAddressesJsonObj.has(\"public0_v4\")||ipAddressesJsonObj.has(\"public1_v4\"))="+
								(ipAddressesJsonObj.has("public0_v4")||ipAddressesJsonObj.has("public1_v4")));
					}	
					//We are only interest in the entity if it has either a public0_v4 or public1_v4
					//ip_address
					if(ipAddressesJsonObj.has("public0_v4")||ipAddressesJsonObj.has("public1_v4")){

						String publicIpAddStr=null;
						if(!ipAddressesJsonObj.isNull("public0_v4")){
							publicIpAddStr=ipAddressesJsonObj.getString("public0_v4");
						}
						else{
							if(!ipAddressesJsonObj.isNull("public1_v4")){
								publicIpAddStr=ipAddressesJsonObj.getString("public1_v4");
							}
						}
						String privateIpAddStr=null;
						if(log.isDebugEnabled()){
							log.debug(METHOD_NAME+" (ipAddressesJsonObj.has(DeployUtility."+PRIVATE_JSON_IP_ADDRESS+"))"+ipAddressesJsonObj.has(DeployUtility.PRIVATE_JSON_IP_ADDRESS));
						}
						if(ipAddressesJsonObj.has(DeployUtility.PRIVATE_JSON_IP_ADDRESS)){
							privateIpAddStr=ipAddressesJsonObj.getString(DeployUtility.PRIVATE_JSON_IP_ADDRESS);
						}

						String id=jsonEntity.getString("id");
						String label=jsonEntity.getString("label");
						String uri=jsonEntity.getString("uri");

						if(log.isDebugEnabled()){
							log.debug(METHOD_NAME+": publicIpAddStr="+publicIpAddStr+" privateIpAddStr="+
									privateIpAddStr+" id="+id+" label="+label+" uri="+uri);
						}

						DocToolsEntity anEntity=new DocToolsEntity();
						anEntity.setPrjsonivate0_v4(publicIpAddStr);
						anEntity.setPublicIP(privateIpAddStr);
						anEntity.setId(id);
						anEntity.setLabel(label);
						anEntity.setUri(uri);
						if(log.isDebugEnabled()){
							log.debug("%%%%%^^^^^anEntity="+anEntity);
						}
						retVal.put(anEntity.getPublicIP(), anEntity);
					}
				}
			}
		}		
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END:");
		}
		return retVal;
	}
	
	private static void disableOrEnableAlarm(String entityId, String alarmId, boolean isDisabled){
		String METHOD_NAME="disableOrEnableAlarm()";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START: entityId="+entityId+" alarmId="+alarmId);
		}
		if(null!=entityId && null!=alarmId){
			try{
			    JSONObject authMetadata=getAuthenTokenAndAccount();
			    
			    if(null!=authMetadata && !authMetadata.isNull("token")){	
			    	String token=authMetadata.getString("token");
			    	String account=authMetadata.getString("account");
			    	
					String[] headers={"X-Auth-Token"};
					String[] values={token};	
					String[] paramKeys={"disabled"};
					String[] paramValues=null;
					if(isDisabled){
						paramValues=new String[]{"true"};
					}
					else{
						paramValues=new String[]{"false"};
					}
					String url="https://monitoring.api.rackspacecloud.com/v1.0/"+account+"/entities/"+entityId+"/alarms/"+alarmId;					

					if(log.isDebugEnabled()){
						log.debug(METHOD_NAME+": url="+url);
					}
					makeRESTCall("PUT",url, headers, values, paramKeys, paramValues,null);
			    }	    
			}
			catch (MalformedURLException e) {
				log.error(e);
				e.printStackTrace();
			}
			catch (IOException e) {
				log.error(e);
				e.printStackTrace();
			}
			catch(JSONException e){
				e.printStackTrace();
				log.error(e);
			}
			catch(Throwable e){
				e.printStackTrace();
				log.error(e);
			}
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END:");
		}		
	}
	
	public static void createAlarmsForCheck(String entityId, String checkId){
		String METHOD_NAME="createAlarmsForCheck()";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START: entityId="+entityId+" checkId="+checkId);
		}
		if(null!=entityId && null!=checkId){
			try{
			    JSONObject authMetadata=getAuthenTokenAndAccount();
			    
			    if(null!=authMetadata && !authMetadata.isNull("token")){	
			    	String token=authMetadata.getString("token");
			    	String account=authMetadata.getString("account");
			    	
					String[] headers={"X-Auth-Token"};
					String[] values={token};					
					String url="https://monitoring.api.rackspacecloud.com/v1.0/"+account+"/entities/"+entityId+"/alarms";
					
					String criteria1="if (metric['duration'] > 25000) {\n  return new AlarmStatus(CRITICAL, "+
					"'HTTP request took more than 25000 milliseconds.'); \n}\nif (metric['duration'] > 20000) "+
					"{\n  return new AlarmStatus(WARNING, 'HTTP request took more than 20000 milliseconds.');\n} "+
					"\n\nreturn new AlarmStatus(OK, 'HTTP connection time is normal');";

					String criteria2="if (metric['code'] regex '4[0-9][0-9]') {\n  return new AlarmStatus(CRITICAL, "+
					"'HTTP server responding with 4xx status');\n}\nif (metric['code'] regex '5[0-9][0-9]') {\n  return new AlarmStatus(CRITICAL, "+
					"'HTTP server responding with 5xx status'); \n}\nreturn new AlarmStatus(OK, 'HTTP server is functioning normally');";
					
                    JSONObject body1=new JSONObject();
                    body1.put("check_id", checkId);
                    body1.put("criteria", criteria1);
                    body1.put("notification_plan_id", "npTechnicalContactsEmail");
                    body1.put("label", "Connection time");

                    JSONObject body2=new JSONObject();
                    body2.put("check_id", checkId);
                    body2.put("criteria", criteria2);
                    body2.put("notification_plan_id", "npTechnicalContactsEmail");
                    body2.put("label", "Status code");

					if(log.isDebugEnabled()){
						log.debug(METHOD_NAME+": url="+url);
					}
					makeRESTCall("POST",url, headers, values, null, null, body1.toString());
					makeRESTCall("POST",url, headers, values, null, null, body2.toString());
			    }	    
			}
			catch (MalformedURLException e) {
				
				e.printStackTrace();
			}
			catch (IOException e) {
				
				e.printStackTrace();
			}
			catch(JSONException e){
				e.printStackTrace();
				
			}
			catch(Throwable e){
				e.printStackTrace();
				
			}
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END:");
		}
	}
	
	public static void deleteAlarmsForChecks(List<JSONObject>alarmsJSONObj, String entityId, String checkId){
		String METHOD_NAME="deleteAlarmsForChecks()";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START:");
		}
		for(JSONObject anAlarmJSONObj:alarmsJSONObj){
			String alarmCheckId=null;
			try{
				alarmCheckId=anAlarmJSONObj.getString("check_id");
				if(log.isDebugEnabled()){
					log.debug(METHOD_NAME+": alarmCheckId="+alarmCheckId+" checkId="+checkId);
				}
				if(null!=alarmCheckId && alarmCheckId.equals(checkId)){
					//Have to do this way, because in testing , anAlarmJSONObj.getString("id"); did not work									   
					Object alarmIdObj=anAlarmJSONObj.get("id");
					if(log.isDebugEnabled()){
						log.debug(METHOD_NAME+": alarmIdObj="+alarmIdObj);
					}
					if(null!=alarmIdObj){
						String alarmId=alarmIdObj.toString();
						if(log.isDebugEnabled()){
							log.debug(METHOD_NAME+": alarmId="+alarmId);
						}
						if(null!=alarmId && !alarmId.isEmpty()){
							if(log.isDebugEnabled()){
								log.debug(METHOD_NAME+": deleting the alarm alarmId: "+alarmId+" entityId: "+entityId);
							}
							deleteAlarm(entityId, alarmId);
						}										   
					}								   
				}
			}
			catch (JSONException e) {
				e.printStackTrace();
				log.debug(METHOD_NAME+": Error!!!! caught JSONException, could not disable alarm: entityId="+
						entityId+" checkId="+checkId+" moving on. "+
						" Error message: "+e.getMessage());
				log.debug(e);
			}
			catch (Throwable e) {
				e.printStackTrace();
				log.debug(METHOD_NAME+": Error!!!! caught Throwable, could not disable alarm: entityId="+
						entityId+" checkId="+checkId+" moving on. "+
						" Error message: "+e.getMessage());
				log.debug(e);
			}
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END:");
		}
	}
	
	private static void deleteAlarm(String entityId, String alarmId){
		String METHOD_NAME="deleteAlarm()";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START: entityId="+entityId+" alarmId="+alarmId);
		}
		if(null!=entityId && null!=alarmId){
			try{
			    JSONObject authMetadata=getAuthenTokenAndAccount();
			    
			    if(null!=authMetadata && !authMetadata.isNull("token")){	
			    	String token=authMetadata.getString("token");
			    	String account=authMetadata.getString("account");
			    	
					String[] headers={"X-Auth-Token"};
					String[] values={token};					
					String url="https://monitoring.api.rackspacecloud.com/v1.0/"+account+"/entities/"+entityId+"/alarms/"+alarmId;

					if(log.isDebugEnabled()){
						log.debug(METHOD_NAME+": url="+url);
					}
					makeRESTCall("DELETE",url, headers, values, null, null, null);
			    }
			    
			}
			catch (MalformedURLException e) {
				log.error(e);
				e.printStackTrace();
			}
			catch (IOException e) {
				log.error(e);
				e.printStackTrace();
			}
			catch(JSONException e){
				e.printStackTrace();
				log.error(e);
			}
			catch(Throwable e){
				e.printStackTrace();
				log.error(e);
			}
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END:");
		}
	}
	
	public static List<JSONObject>getAlarmsForAnEntityAndCheck(String anEntityId, String checkId){
		String METHOD_NAME="getAlarmsForAnEntityAndCheck()";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": anEntityId="+anEntityId+" checkId="+checkId);
		}
		List<JSONObject>retList=new ArrayList<JSONObject>();

		JSONObject alarms=getAlarms(anEntityId);
		if(null!=alarms && !alarms.isNull("values")){
			try {
				JSONArray valuesJsonArr=alarms.getJSONArray("values");
				for(int i=0;i<valuesJsonArr.length();++i){
					JSONObject aJsonValue=valuesJsonArr.getJSONObject(i);
					retList.add(aJsonValue);
				}
			} catch (JSONException e) {
				log.error(e);
				e.printStackTrace();
			}
		}
			
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": retList.size()="+retList.size());
		}
		return retList;
	}
	
	public static JSONObject getChecks(String entityId){
		String METHOD_NAME="getChecks()";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START: entityId="+entityId);
		}	
		JSONObject retVal=null;
		
		if(null!=entityId){
			try{
			    JSONObject authMetadata=getAuthenTokenAndAccount();
			    
			    if(null!=authMetadata && !authMetadata.isNull("token")){	
			    	String token=authMetadata.getString("token");
			    	String account=authMetadata.getString("account");
			    	
					String[] headers={"X-Auth-Token"};
					String[] values={token};					
					String url="https://monitoring.api.rackspacecloud.com/v1.0/"+account+"/entities/"+entityId+"/checks";

					if(log.isDebugEnabled()){
						log.debug(METHOD_NAME+": url="+url);
					}
					retVal=makeRESTCall("GET",url, headers, values, null,null,null);
			    }			    
			}
			catch (MalformedURLException e) {
				log.error(e);
				e.printStackTrace();
			}
			catch (IOException e) {
				log.error(e);
				e.printStackTrace();
			}
			catch(JSONException e){
				e.printStackTrace();
				log.error(e);
			}
			catch(Throwable e){
				e.printStackTrace();
				log.error(e);
			}
		}
		if(null==retVal){
			retVal=new JSONObject();
		}		
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END:");
		}
		return retVal;
	}
	
	public static DocToolsEntity disableEntityChecks(Map<String, DocToolsEntity>entities, org.jclouds.rackspace.cloudloadbalancers.v1.domain.Node aNode)throws JCloudsException{
		return changeEntityChecks(entities, aNode, false);
	}
	
	public static DocToolsEntity enableEntityChecks(Map<String, DocToolsEntity>entities, org.jclouds.rackspace.cloudloadbalancers.v1.domain.Node aNode)throws JCloudsException{
		return changeEntityChecks(entities, aNode, true);
	}
	
	private static DocToolsEntity changeEntityChecks(Map<String, DocToolsEntity>entities, org.jclouds.rackspace.cloudloadbalancers.v1.domain.Node aNode, boolean enable){
		String METHOD_NAME="changeEntityChecks()";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START: enable="+enable+" aNode="+aNode);
			log.debug(METHOD_NAME+": aNode.getAddress().trim()="+aNode.getAddress().trim());
			log.debug(METHOD_NAME+": entities="+entities);
		}
		
		DocToolsEntity retVal=entities.get(aNode.getAddress().trim());
		String entityId="";
		if(null!=retVal){
			entityId=retVal.getId();
			JSONObject checks=null;	
			checks = getChecks(entityId);

			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": checks"+checks+" entityId="+entityId);
			}
			//We only procede with the enable/disable if we were able to retrieve the checks successfully
			if(null!=checks){
				String checkId="";
				String checkLabel="";
				try{
					JSONArray checksArr=checks.getJSONArray("values");
					for(int i=0;i<checksArr.length(); ++i){
						JSONObject aCheck=checksArr.getJSONObject(i);

						checkId=aCheck.getString("id");						
						checkLabel=aCheck.getString("label");
						if(log.isDebugEnabled()){
							log.debug(METHOD_NAME+": checkId="+checkId+" checkLabel="+checkLabel+" enable="+enable);
						}
						List<JSONObject>alarmsJSONObj=getAlarmsForAnEntityAndCheck(entityId, checkId);
						if(log.isDebugEnabled()){
							log.debug(METHOD_NAME+": alarmsJSONObj.size()="+alarmsJSONObj.size());
						}
						if(!enable){							
							if(log.isDebugEnabled()){
								log.debug(METHOD_NAME+": About to delete alarms");
							}
							DeployUtility.deleteAlarmsForChecks(alarmsJSONObj,entityId,checkId);							
						}
						else{
							if(log.isDebugEnabled()){
								log.debug(METHOD_NAME+": About to create alarms");
							}
							createAlarmsForCheck(entityId, checkId);	
						}

					}
				}
				catch(JSONException e){
					log.error(METHOD_NAME+": Error!!!! caught JCloudsException, could not enable="+enable+" the monitor: entityId="+
							entityId+" checkId="+checkId+" checkLabel="+checkLabel+" moving on, there maybe notifications problems."+
							" Error message: "+e.getMessage());
					log.error(e);
				} 
				catch(Throwable e){
					log.error(METHOD_NAME+": Error!!!! caught Throwable, could not enable="+enable+" the monitor: entityId="+
							entityId+" checkId="+checkId+" checkLabel="+checkLabel+" moving on, there maybe notifications problems."+
							" Error message: "+e.getMessage());
					log.error(e);						
				}
			}
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END: retVal="+retVal);
		}
		return retVal;
	}	
	
	public static JSONObject getAlarms(String entityId){
		String METHOD_NAME="getAlarms()";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START:");
		}
		JSONObject retVal=null;
		if(null!=entityId){
			try{
			    JSONObject authMetadata=getAuthenTokenAndAccount();
			    
			    if(null!=authMetadata && !authMetadata.isNull("token")){	
			    	String token=authMetadata.getString("token");
			    	String account=authMetadata.getString("account");
			    	
					String[] headers={"X-Auth-Token"};
					String[] values={token};					
					String url="https://monitoring.api.rackspacecloud.com/v1.0/"+account+"/entities/"+entityId+"/alarms";

					if(log.isDebugEnabled()){
						log.debug(METHOD_NAME+": url="+url);
					}
					retVal=makeRESTCall("GET",url, headers, values, null,null,null);
			    }
			    
			}
			catch (MalformedURLException e) {
				log.error(e);
				e.printStackTrace();
			}
			catch (IOException e) {
				log.error(e);
				e.printStackTrace();
			}
			catch(JSONException e){
				e.printStackTrace();
				log.error(e);
			}
			catch(Throwable e){
				e.printStackTrace();
				log.error(e);
			}
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END:");
		}
		return retVal;
	}
	
	private static JSONObject makeRESTCall(String method, String url, String[] headers, 
			String[] headerValues, String[]paramKeys, String[] paramValues, String body)throws MalformedURLException, IOException, JSONException{
		String METHOD_NAME="makeRestCall()";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+"START: url="+url);
		}
		StringBuffer jsonResponse=null;	
		InputStream inny=null;

		URL theURL = new URL(url);
        
        
		//HttpURLConnection.setFollowRedirects(true);
		HttpURLConnection httpConn = (HttpURLConnection)theURL.openConnection();
		httpConn.setDoOutput(true);		
		httpConn.setRequestMethod(method);
		httpConn.setRequestProperty("Content-Type", "application/json");
		
		if(null!=headers && null!=headerValues){
			if(headers.length!=headerValues.length){
				throw new RuntimeException("Failed: headers and headerValues array must be the same length");
			}
			else{
				for(int i=0;i<headers.length;++i){
					String aHeader=headers[i];
					String aHeaderValue=headerValues[i];
					httpConn.addRequestProperty(aHeader, aHeaderValue);
				}				
			}
		}
		if(null!=paramKeys && null!=paramValues){
			if(paramKeys.length!=paramValues.length){
				throw new RuntimeException("Failed: paramKeys and paramValues array must be the same length");
			}
			else{
				for(int i=0;i<headers.length;++i){
					String aParamKey=headers[i];
					String aParamValue=headerValues[i];
					httpConn.setRequestProperty(aParamKey, aParamValue);
				}
			}
		}
		
		httpConn.setConnectTimeout(10000);
		httpConn.setReadTimeout(10000);

		if(null!=body && !body.isEmpty()){
			OutputStream os=httpConn.getOutputStream();
			os.write(body.getBytes());
			os.flush();
			os.close();
		}
        int responseCode=httpConn.getResponseCode();
        
        if(log.isDebugEnabled()){
        	log.debug(METHOD_NAME+": debug log: responseCode="+responseCode);        	
        }
        if(responseCode>299 || responseCode<200){    
        	log.error(METHOD_NAME+": debug log: responseCode="+responseCode);        	            
        	throw new RuntimeException("Failed: HTTP error code: "+responseCode);
        }
        
		inny=httpConn.getInputStream();		
		int readInt=-1;
		char readChar=' ';
		jsonResponse=new StringBuffer("");
		while(-1!=(readInt=inny.read())){
			readChar=(char)readInt;
			jsonResponse.append(readChar);
		}		
		inny.close();
	    
	    if(jsonResponse.toString().isEmpty()){
	    	jsonResponse.append("{}");
	    }
		return new JSONObject(jsonResponse.toString());
	}	
	
	private static String makeRestCall(String url)throws MalformedURLException, IOException{
		String METHOD_NAME="makeRestCall()";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+"START: url="+url);
		}
		StringBuffer jsonResponse=null;
		//We must make a rest call to respective server in the form of:
		//http://serverName/rax-scpwebapps/rest/raxscp/raxbuildtime?war=cm-devguide.war
		//this has changd to: http://serverName/rax-prod-services/rest/raxscp/raxbuildtime?war=cm-devguide.war		
		InputStream inny=null;

		URL theURL = new URL(url);

		HttpURLConnection.setFollowRedirects(true);
		HttpURLConnection httpConn = (HttpURLConnection)theURL.openConnection();
		httpConn.setRequestMethod("GET");
		httpConn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
		httpConn.setConnectTimeout(10000);
		httpConn.setReadTimeout(10000);

		inny=httpConn.getInputStream();		
		int readInt=-1;
		char readChar=' ';
		jsonResponse=new StringBuffer("");
		while(-1!=(readInt=inny.read())){
			readChar=(char)readInt;
			jsonResponse.append(readChar);
		}		


		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+"END: jsonResponse="+jsonResponse);
		}		
		return jsonResponse.toString();
	}

	public static String doesWarHaveInternalFolderOnInternalDocsServer(String aSelectedApp)
			throws MalformedURLException, ProtocolException, IOException, JSONException{
		String METHOD_NAME="doesWarHaveInternalFolderOnInternalDocsServer()";
		String retVal=null;
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START: aSelectedApp="+aSelectedApp);
		}
		if(null!=aSelectedApp && !aSelectedApp.isEmpty()){

			//We must make a rest call to staging internal to see if we should scp the .war to the internal server too
			String url=propsFile.getProperty("prodserverinternalraxinternalfolderquery", 
					"http://docs-internal/rax-doctools-services/rest/doctools/raxinternalfolder?webappfolder=");

			InputStream inny=null;

			url+=aSelectedApp;
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": url="+url);
			}
			URL theURL=new URL(url);

			HttpURLConnection.setFollowRedirects(true);

			HttpURLConnection httpConn = (HttpURLConnection)theURL.openConnection();

			httpConn.setRequestMethod("GET");
			httpConn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");

			httpConn.setConnectTimeout(10000);
			httpConn.setReadTimeout(10000);

			inny=httpConn.getInputStream();		
			int readInt=-1;
			char readChar=' ';
			StringBuffer jsonResponse=new StringBuffer("");
			while(-1!=(readInt=inny.read())){
				readChar=(char)readInt;
				jsonResponse.append(readChar);
			}
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+":~!@~!@~!@~!@~!@~@jsonResponse.toString()="+jsonResponse.toString());
			}
			JSONObject jsonObj=new JSONObject(jsonResponse.toString());
			retVal=jsonObj.getString("hasinternalfolder");					
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+":~!@~!@~!@~!@~!@~@hasinternalfolder="+retVal);
			}
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": END: retVal="+retVal);
			}
		}
		return retVal;
	}

	public static void addABadMessage(String finalFileName,Map<String,List<String>>messages, String message){
		String key=finalFileName+"-bad";
		List<String>badMessages=messages.get(key);
		if(null==badMessages){
			badMessages=new ArrayList<String>();
			messages.put(key, badMessages);
		}
		badMessages.add(message);
	}
	

//	public static void getEmailsOld(File bookinfoXml, Map<String, Email>emails){
//
//		if(null!=bookinfoXml && bookinfoXml.exists() && null!=emails){
//			BufferedReader r = null;
//			InputStreamReader innyReader;
//
//			try{
//				//			innyReader=new InputStreamReader(new FileInputStream("/Users/thu4404/Documents/workspace2/testloadbalancer/src/main/resources/bookinfo.xml"));
//				innyReader=new InputStreamReader(new FileInputStream(bookinfoXml));
//				r=new BufferedReader(innyReader);
//				StringBuffer anXmlStrBuff=new StringBuffer("");
//				String line=null;
//				while(null!=(line=r.readLine())){
//					anXmlStrBuff.append(line);
//					anXmlStrBuff.append("\n");
//				}
//				r.close();
//				innyReader.close();
//				processBookInfo(anXmlStrBuff.toString(),emails);
//			}
//			catch(FileNotFoundException e){
//				e.printStackTrace();
//			}
//			catch(IOException e){
//				e.printStackTrace();
//			}
//		}
//	}

	public static void retrieveDocNames(File bookinfoXml, List<String>docNames){
		String METHOD_NAME="retrieveDocNames()";

		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+" START: docNames.size()="+docNames.size());
		}	
		BufferedReader r = null;
		InputStreamReader innyReader;
		
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		try {
			innyReader=new InputStreamReader(new FileInputStream(bookinfoXml));
			r=new BufferedReader(innyReader);
			StringBuffer anXmlStrBuff=new StringBuffer("");
			String line=null;			
			while(null!=(line=r.readLine())){
				anXmlStrBuff.append(line);
				anXmlStrBuff.append("\n");
			}
			r.close();
			innyReader.close();	
			
			dBuilder = dbFactory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(anXmlStrBuff.toString()));
			Document doc=dBuilder.parse(is);
			doc.getDocumentElement().normalize();
			NodeList docNameNodeList=doc.getElementsByTagName("docname");
			String aDocName="";
			if(null!=docNameNodeList){							
				Node docNameNode=docNameNodeList.item(0);
				aDocName=docNameNode.getTextContent();
			}
			if(null==aDocName){
				aDocName="";
			}
			docNames.add(aDocName);
		} 
		catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch(IOException e){
			e.printStackTrace();
		}
		catch(SAXParseException e){
			e.printStackTrace();
		}
		catch(SAXException e){
			e.printStackTrace();
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+" END: docNames.size()="+docNames.size());
		}
	}

	private static Email getAnEmail(Element anEmail){
		String METHOD_NAME="getAnEmail(Element anEmail)";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START:");
		}
		Email retVal=new Email();
		String name=getNodeVal(anEmail,"name");
		retVal.setName(name);
		String to=getNodeVal(anEmail,"to");
		retVal.setTo(to);
		String from=getNodeVal(anEmail,"from");
		retVal.setFrom(from);
		String subject=getNodeVal(anEmail,"subject");
		if(null==subject){
			subject="DocTools deployment results notification";
		}
		retVal.setSubject(subject);
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END: retVal="+retVal);
		}
		return retVal;
	}
	
	public static List<Email> getEmails(Users loggedInuser, List<String>docNames){
		String METHOD_NAME="getEmails(User loggedInuser)";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START: loggedInuser="+loggedInuser);
		}
		List<Email>retVal=new ArrayList<Email>();
		Email anEmail=new Email();
		String subject="DocTools deployment results notification for";
		if(null!=loggedInuser){
			String fName=loggedInuser.getFname();
			if(null!=fName&&!fName.isEmpty()){
				subject+=(" "+fName);
			}
			String lName=loggedInuser.getLname();
			if(null!=lName&&!lName.isEmpty()){
				subject+=(" "+lName);
			}
			String ldapName=loggedInuser.getLdapname();
			if(null!=ldapName&&!ldapName.isEmpty()){
				subject+=(" ("+ldapName+")");
			}
		}
		if(null!=loggedInuser){
			String name=loggedInuser.getFname();
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": name="+name);
			}
			if(null==name){
				name="";
			}			
			String lname=loggedInuser.getLname();
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": lname="+lname);
			}
			if(null!=lname && !lname.isEmpty()){
				name+=" ";
				name+=lname;
			}
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": name="+name);
			}
			if(null==name||name.isEmpty()){
				name="CDT Publication Events";
			}
			String ldapname=loggedInuser.getLdapname();
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": ldapname="+ldapname);
			}
			anEmail.setName(name);
			String emailAddr=loggedInuser.getEmail();
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": emailAddr="+emailAddr);
			}
			anEmail.setTo(emailAddr);
			anEmail.setFrom("clouddoctoolsteam@lists.rackspace.com");
			anEmail.setLdapname(ldapname);			
			
			anEmail.setSubject(subject);
			anEmail.setDocNames(docNames);
			retVal.add(anEmail);
		}
		//We always want to add the cdt-publication-events@lists.rackspace.com email address
		Email email2=new Email();
		email2.setTo("cdt-publication-events@lists.rackspace.com");
		email2.setFrom("clouddoctoolsteam@lists.rackspace.com");
		email2.setLdapname("clouddoctoolsteam@lists.rackspace.com");
		email2.setSubject(subject);
		email2.setName("CDT Publication Events");
		email2.setDocNames(docNames);
		retVal.add(email2);
		
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END: retVal="+anEmail);
		}
		return retVal;
	}

	public static String getNodeVal(Element ele, String eleName){
		String retVal=null;
		NodeList nameNodes=ele.getElementsByTagName(eleName);
		if(null!=nameNodes){
			Node aNode=nameNodes.item(0);
			if(null!=aNode){
				NodeList children=aNode.getChildNodes();
				if(null!=children){
					Node node=children.item(0);
					if(null!=node){
						retVal=node.getNodeValue();
					}
				}
			}
		}
		return retVal;
	}

	public static String cleanMessage(String message){
		String retVal="";
		if(null!=message){
			String[] splitStr=message.split("<");
			StringBuffer strBuff=new StringBuffer("");
			for(String aStr:splitStr){
				if(!aStr.isEmpty()){
					int greaterThanCharIndex=aStr.indexOf(">");
					if(-1!=greaterThanCharIndex){
						strBuff.append(aStr.substring(greaterThanCharIndex+1));
					}
				}
			}
			retVal=strBuff.toString();			
		}
		return retVal;
	}

	public static void sendEmails( List<Email>emails, Map<String,List<String>>messages, 
			                      String action){
		String METHOD_NAME="sendEmails()";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START: action="+action);
		}
		if(null!=emails){
			String host="localhost";
			String from="";
			String to="";
			String subject="";
			String name="";
			StringBuffer theMessage= new StringBuffer("");
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": messages="+messages);
			}
			if(null!=messages){

				for(List<String> aMessageList:messages.values()){
					for(String aMessage:aMessageList){
						aMessage=cleanMessage(aMessage);
						theMessage.append("    ");
						theMessage.append(aMessage);
						theMessage.append("\n");
					}
				}
			}
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": theMessage="+theMessage);
				log.debug(METHOD_NAME+": emails.size()="+emails.size());
			}
			Properties props=new Properties();
			props.setProperty("mail.smtp.host", host);
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": host="+host);
			}
			javax.mail.Session sess=javax.mail.Session.getDefaultInstance(props);
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": created the session, now interating through emails");
			}			
			for(Email anEmail:emails){

				MimeMessage mimeMessage=new MimeMessage(sess);
				if(log.isDebugEnabled()){
					log.debug(METHOD_NAME+": created mime message");
				}
				to=anEmail.getTo();
				if(log.isDebugEnabled()){
					log.debug(METHOD_NAME+": to="+to);
				}
				if(null!=to && !to.isEmpty()){

					from=anEmail.getFrom();
					if(null==from || from.isEmpty()){
						from="clouddoctoolsteam@lists.rackspace.com";
					}
					subject=anEmail.getSubject();
					if(null==subject){
						subject="";
					}
					name=anEmail.getName();
					if(null==name){
						name="";
					}
					String ldapname=anEmail.getLdapname();
					if(null!=ldapname){
						name+="(";
						name+=ldapname;
						name+=")";
					}
					if(log.isDebugEnabled()){
						log.debug(METHOD_NAME+": from="+from+" name="+name+" subject="+subject);						
					}
					try {
						if(null!=from && !from.isEmpty()){

							mimeMessage.setFrom(new InternetAddress(from));
						}

						mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
						mimeMessage.setSubject(subject);
						StringBuffer messStr=new StringBuffer("Dear ");
						if(null==name||name.isEmpty()){
							name="Esteemed DocWriter,\n\n";
						}
						else{
							name+=",\n\n";
						}
						messStr.append(name);

						if(null==action || action.isEmpty()){
							messStr.append("Deployment processing has finished for:\n\n");
						}
						else{
							messStr.append("Revert processing has finished for:\n\n");
						}
						List<String>docNames=anEmail.getDocNames();
						if(null!=docNames){
							for(String aDocName:docNames){
								messStr.append("    "+aDocName+"\n");
							}
						}
						messStr.append("\n");
						messStr.append("Results:\n\n");
						messStr.append(theMessage);
						mimeMessage.setText(messStr.toString());
						Transport.send(mimeMessage);
						if(log.isDebugEnabled()){
							log.debug(METHOD_NAME+"Sent message to: "+to);
							log.debug(METHOD_NAME+"~~~~~messStr: "+messStr);
						}
					} 
					catch (AddressException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
					catch (MessagingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					catch(Throwable e){
						e.printStackTrace();
					}
				}
			}
		}
		else{
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": emails is null do not send any emails");
			}
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END");
		}
	}

	public static void deleteFolderAndSubFolders(File dir){
		if(dir.isDirectory()){
			File[] files=dir.listFiles();
			for(File aFile:files){
				deleteFolderAndSubFolders(aFile);
			}
			dir.delete();
		}
		else{
			dir.delete();
		}
	}

	public static void addSuccessMessages(HttpServletRequest request, String aSelectedAppWithWarSuffix, 
			Map<String,List<String>>messages, boolean deployment){
		String METHOD_NAME="buildSuccessMessages()";

		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START:");
		}
		String serverName=request.getServerName();       	
		String prodServerurl="localhost:8080";       	

		if(serverName.contains(".rackspace.com")){
			if(serverName.contains("internal")){
				prodServerurl="docs-internal.rackspace.com";
			}
			else{
				prodServerurl="docs.rackspace.com";
			}
		}      	
		String autodeployUrl="http://"+prodServerurl;
		if(!autodeployUrl.endsWith("/")){
			autodeployUrl+="/";
		}
		autodeployUrl+=("index.jsp#"+aSelectedAppWithWarSuffix);
		String key=aSelectedAppWithWarSuffix+"-good";
		List<String>messagesList=messages.get(key);
		if(null==messagesList){
			messagesList=new ArrayList<String>();

			messages.put(key, messagesList);
			if(deployment){
				messagesList.add("<span class='successmessage'>Successfully deployed <a target='_blank' href='"+
						autodeployUrl+"'>"+aSelectedAppWithWarSuffix+"</a></span>");
			}
			else{
				messagesList.add("<span class='successmessage'>Successfully reverted to backup <a target='_blank' href='"+
						autodeployUrl+"'>"+aSelectedAppWithWarSuffix+"</a></span>");
			}
		}
		else{
			//We have already encountered a success message for this selected app, do not add another one.
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+"Added a success message for aSelectedAppWithWarSuffix"+aSelectedAppWithWarSuffix);
			log.debug(METHOD_NAME+": END:");
		}
	}

	public static void createWarFileInBackupWithTimeStamp(File warFileInBackupFolder, String webappsBackupFolderStr){
		String METHOD_NAME="createWarFileInBackupWithTimeStamp()";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START: warFileInBackupFolder.getAbsolutePath()="+warFileInBackupFolder.getAbsolutePath()+
					" webappsBackupFolderStr="+webappsBackupFolderStr);
		}
		String warFileName=warFileInBackupFolder.getName();
		String warFileNameWithoutDotWar=warFileName.substring(0,warFileName.lastIndexOf(".war"));

		try{
			String timestamp=DeployUtility.getTimeStamp(new ZipFile(warFileInBackupFolder));
			String warFileNameWithTimestamp=warFileNameWithoutDotWar+"-"+timestamp+".war";
			String warFileWithTimestampBackupFolder=webappsBackupFolderStr+warFileNameWithTimestamp;
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": warFileName="+warFileName);
				log.debug(METHOD_NAME+": warFileNameWithoutDotWar="+warFileNameWithoutDotWar);
				log.debug(METHOD_NAME+": warFileNameWithTimestamp="+warFileNameWithTimestamp);
				log.debug(METHOD_NAME+": warFileWithTimestampBackupFolder="+warFileWithTimestampBackupFolder);				
			}
			File warFileWithTimestamp=new File(warFileWithTimestampBackupFolder);
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+":warFileWithTimestamp.exists()="+warFileWithTimestamp.exists());
			}
			if(warFileWithTimestamp.exists()){
				warFileWithTimestamp.delete();
			}
			warFileWithTimestamp.createNewFile();
			DeployUtility.copyWarFileToNewFile(warFileInBackupFolder, warFileWithTimestamp);
		}
		catch(ZipException e){
			e.printStackTrace();
		}
		catch(IOException e){
			e.printStackTrace();
		}
		catch(Throwable e){
			e.printStackTrace();
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END:");
		}
	}

	public static String getTimeStampOld(ZipFile currentZipFile){
		String METHOD_NAME="getTimeStampOld()";
		String retVal="";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START: retVal="+retVal);
		}
		Enumeration currentZipEntries=currentZipFile.entries();
		try{
			while(currentZipEntries.hasMoreElements()){
				ZipEntry aZipEntry=(ZipEntry)currentZipEntries.nextElement();
				String aZipEntryPathFileName=aZipEntry.getName();
				if(log.isDebugEnabled()){
					log.debug(METHOD_NAME+": aZipEntryPathFileName="+aZipEntryPathFileName);
					log.debug(METHOD_NAME+": aZipEntryPathFileName.endsWith(\"+warinfo.properties+\")="+
							aZipEntryPathFileName.endsWith("warinfo.properties"));
				}
				//We are only interested in the warinfo.properties 
				if(!aZipEntry.isDirectory() && aZipEntryPathFileName.endsWith("warinfo.properties")){
					Properties warinfoProps=new Properties();
					warinfoProps.load(currentZipFile.getInputStream(aZipEntry));
					String timestamp=warinfoProps.getProperty("timestamp");
					if(log.isDebugEnabled()){
						log.debug(METHOD_NAME+": timestamp=" +timestamp);
					}
					if(null!=timestamp && !timestamp.isEmpty()){
						retVal=timestamp;//((timestamp.replaceAll(":", "-"))).trim().replaceAll(" ", "_");					
					}
					break;
				}
			}
		}
		catch(IOException e){
			e.printStackTrace();
		}
		catch(Throwable e){
			e.printStackTrace();
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END: retVal="+retVal);
		}
		return retVal;		
	}

	public static String getTimeStamp(ZipFile currentZipFile){
		String METHOD_NAME="getTimeStamp()";
		String retVal="";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START: retVal="+retVal);
		}		
		try{
			ZipEntry aZipEntry=currentZipFile.getEntry("WEB-INF/warinfo.properties");
			//Just in case we cant find the warinfo.properties file iterate through the zip entries and try to get
			//it that way
			if(null==aZipEntry){
				retVal=getTimeStampOld(currentZipFile);
			}
			else{
				if(log.isDebugEnabled()){
					log.debug(METHOD_NAME+": aZipEntry.getName()="+aZipEntry.getName());
				}
				Properties warinfoProps=new Properties();
				warinfoProps.load(currentZipFile.getInputStream(aZipEntry));
				String timestamp=warinfoProps.getProperty("timestamp");
				if(log.isDebugEnabled()){
					log.debug(METHOD_NAME+": timestamp=" +timestamp);
				}
				if(null!=timestamp && !timestamp.isEmpty()){
					retVal=(timestamp.trim());					
				}
			}
		}
		catch(IOException e){
			e.printStackTrace();
		}
		catch(Throwable e){
			e.printStackTrace();
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END: retVal="+retVal);
		}		
		return retVal;		
	}

	public static void copyWarFileToNewFile(File selectedWarFile, File newTimeStampFile){
		String METHOD_NAME="copySelectedWarFileToBackupAsCurrent()";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START:");
		}
		if(newTimeStampFile.exists()){
			newTimeStampFile.delete();						
		}
		try{
			newTimeStampFile.createNewFile();
			FileOutputStream newTimeStampWarFile=new FileOutputStream(newTimeStampFile);
			ZipOutputStream zipFile=new ZipOutputStream(newTimeStampWarFile);

			FileInputStream fileInny=new FileInputStream(selectedWarFile);
			ZipInputStream zipInnyStream=new ZipInputStream(fileInny);

			ZipEntry anEntry=null;
			//Add all the entries 
			while(null!=(anEntry=zipInnyStream.getNextEntry())){
				String name=anEntry.getName();
				zipFile.putNextEntry(new ZipEntry(name));
				int len=-1;
				while(-1!=(len=zipInnyStream.read())){
					zipFile.write(len);
				}	
				zipFile.closeEntry();
			}
			zipInnyStream.close();
			fileInny.close();
			zipFile.close();
		}
		catch(FileNotFoundException e){
			e.printStackTrace();
		}
		catch(IOException e){
			e.printStackTrace();
		}
		catch(Throwable e){
			e.printStackTrace();
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END:");
		}
	}

	public static void touchRoot(String scpUser, String passwd, String known_hosts, String id_rsa, 
			String prodServer, String fullPathToRootWar){
		String METHOD_NAME="touchRoot()";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START: scpUser="+scpUser+" passwd="+passwd+" known_hosts="+known_hosts+" id_rsa="+id_rsa+
					" prodServer="+prodServer+" fullPathToRootWar="+fullPathToRootWar);
		}
		String command="touch "+fullPathToRootWar;
		jSchExec(command, prodServer, null);
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END:");
		}
	}
	
	public static HttpClient authenticate(String jenkinsUrl,String username, String password)throws IOException, HttpException{
		String METHOD_NAME="authenticate()";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START: jenkinsUrl="+jenkinsUrl+" username="+username+" password="+password);
		}
		HttpClient client = new HttpClient();
		PostMethod postMethodAuth = new PostMethod(jenkinsUrl + "/j_acegi_security_check");
		NameValuePair[] postData = new NameValuePair[3];
		postData[0] = new NameValuePair("j_username", username);
		postData[1] = new NameValuePair("j_password", password);
		postData[2] = new NameValuePair("Login", "login");
		postMethodAuth.addParameters(postData);
		try {
			int status= client.executeMethod(postMethodAuth);
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": status="+status);
			}
		} 
		finally {
			postMethodAuth.releaseConnection();
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END: jenkinsUrl="+jenkinsUrl+" username="+username+" password="+password);
		}	
		return client;		
	}

}
