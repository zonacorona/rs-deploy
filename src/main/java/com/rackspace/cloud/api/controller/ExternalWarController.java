package com.rackspace.cloud.api.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.TimeoutException;
import java.util.zip.ZipFile;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.json.JSONException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.log4j.Logger;
import org.jclouds.rackspace.cloudloadbalancers.v1.CloudLoadBalancersApi;
import org.jclouds.rackspace.cloudloadbalancers.v1.domain.LoadBalancer;
import org.jclouds.rackspace.cloudloadbalancers.v1.domain.Node;
import org.json.JSONObject;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.rackspace.cloud.api.DeployUtility;
import com.rackspace.cloud.api.Detail;
import com.rackspace.cloud.api.Email;
import com.rackspace.cloud.api.InstalledWar;
import com.rackspace.cloud.api.SendMail;
import com.rackspace.cloud.api.dao.IDeployjobDao;
import com.rackspace.cloud.api.dao.IFreezeDao;
import com.rackspace.cloud.api.dao.IGroupsDao;
import com.rackspace.cloud.api.dao.IMembersDao;
import com.rackspace.cloud.api.dao.IUsersDao;
import com.rackspace.cloud.api.entity.Deployjob;
import com.rackspace.cloud.api.entity.Freeze;
import com.rackspace.cloud.api.entity.Groups;
import com.rackspace.cloud.api.entity.Status;
import com.rackspace.cloud.api.entity.Users;
import com.rackspace.cloud.api.jclouds.DocToolsEntity;
import com.rackspace.cloud.api.jclouds.JCloudsException;
import com.rackspace.cloud.api.jclouds.JCloudsUtility;


@Controller
@RequestMapping(value="/")
@Configuration
public class ExternalWarController implements InitializingBean{

	private static final Object lock = new Object();

	private static Logger log = Logger.getLogger(ExternalWarController.class);
	private static Properties propsFile;

	//First map is keyed by users.ldapname, the inner Set contains all the groups the user belongs to 
	//private Map<String,Set<String>>userAccessToGroupsMap;

	private Map<String, InstalledWar>wars;
	private Map<String, InstalledWar>internalNReviewerWars;
	private  List<String>externalFilterList;
	//private  List<String>internalNReviewerFilterList;

	@Autowired
	private ServletContext servletCtx;

	@Autowired
	private IUsersDao userDao;

	@Autowired
	private IFreezeDao freezeDao;
	
	@Autowired
	private IDeployjobDao deployjobDao;
	
	@Autowired
	private IGroupsDao groupsDao;
	
	@Autowired
	private IMembersDao membersDao;
	

	//First map is keyed by users.ldapname, the inner Set contains all the groups the user belongs to 
	private Map<String,Set<String>>userAccessToGroupsMap;
	
	//Wait for a maximum of 15 minutes there are 60,000 ms in 1 minute
	//900000ms = 15minutes
	public static long MAX_WAIT_TIME=(900000L);
	public static long SLEEP_INTERVAL=30000L;
	public static int READ_TIMEOUT=4000;
	public static int CONNECT_TIMEOUT=5000;
	public static int SLEEP_AFTER_STARTUP=45000;

	

	@RequestMapping(value="/DeployWars", method=RequestMethod.GET)
	public String addModelObjects(Model model, HttpSession session, HttpServletRequest request){
		String METHOD_NAME="addModelObjects()";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START:");
		}
		String retVal="DeployWars";
		if(log.isDebugEnabled()){
			ControllerHelper.outputRequestHeadersAndSessionAttributes(request,session);
		}
		String loggedInUser=(String)request.getHeader("x-ldap-username");
		//Try to look for the x-ldap-username in session scope if it is not in request scope
		if(null==loggedInUser || loggedInUser.trim().isEmpty()){
			loggedInUser=(String)session.getAttribute("x-ldap-username");
		}		
		String propsDebug=propsFile.getProperty("debug");
		request.setAttribute("debug", propsDebug);
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": loggedInUser="+loggedInUser+" propsDebug="+propsDebug);
		}
		//We only procede if we have the x-ldap-username value
		if(null!=loggedInUser || (null!=propsDebug&&propsDebug.equalsIgnoreCase("true"))){
			model.addAttribute("x-ldap-username",loggedInUser);

			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+":"+"x-ldap-username="+loggedInUser);			
			}


			model.addAttribute("loggedInUser",loggedInUser);

			model.addAttribute("message","This is a message from ServletController");
			List<Users>usersList=userDao.findAll();
			model.addAttribute("users",usersList);

			model.addAttribute("shouldDisplay",ControllerHelper.getShouldDisplay(this.servletCtx,session));
			model.addAttribute("shouldFreeze",freezeDao.findById(((short)1)).getShouldfreeze());

			model.addAttribute("isUserAnAdmin",ControllerHelper.isUserAnAdmin(request, this.userDao, loggedInUser));

			List<String>externalFilters=this.getExternalFilterList(loggedInUser);
			model.addAttribute("extFilters",externalFilters);


			Map<String,InstalledWar>extWars=this.getExtWars(session, loggedInUser);
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": extWars.size()="+extWars.size());
				for(InstalledWar aWar:extWars.values()){
					log.debug(METHOD_NAME+": aWar="+aWar);
				}
			}
			if(extWars.size()==0){
				return "NoAccess";
			}
			model.addAttribute("extWars",extWars);

		}
		else{
			retVal="redirect:/Login";
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END: retVal="+retVal);
		}
		return retVal;
	}
	
	
	@RequestMapping(value="/extFilters", method=RequestMethod.GET)
	@ResponseBody
	public List<String>getExternalFilterList(@RequestBody String loggedInUser){
		if(null==this.externalFilterList||this.externalFilterList.size()==0){
			if(null==this.externalFilterList){
				this.externalFilterList=new ArrayList<String>();
			}
			this.loadExternalFilters(loggedInUser);
		}
		return this.externalFilterList;
	}
	
	@RequestMapping(value="/getShouldFreeze", method=RequestMethod.GET)
	@ResponseBody
	public Frozen getShouldFreeze(){
		Frozen retVal=new Frozen();
		retVal.setFrozenFalse();
		//String retVal="{\"frozen\":";
		Boolean frozen=this.freezeDao.findById(((short)1)).getShouldfreeze();		
		if(null!=frozen && frozen){
			retVal.setFrozenTrue();
		}
		return retVal;
	}


	@RequestMapping(value="/allUsers", method=RequestMethod.GET)
	@ResponseBody
	public List<Users>getAllUsers(){
		return userDao.findAll();
	}
	
	public List<Deployjob>getAllDeployJobs(){
		return this.deployjobDao.findAll();
	}

	@RequestMapping(value="/allGroups", method=RequestMethod.GET)
	@ResponseBody
	public List<Groups>getAllGroups(){
		return this.groupsDao.findAll();
	}
	
	@RequestMapping(value="/authenticate",method=RequestMethod.POST)
	@ResponseBody
	public String authenticate(@RequestBody String auth){
		String METHOD_NAME="authenticate()";
		if(log.isDebugEnabled()){
			log.debug("~!@~!@~!@~!@~!@~!@~!@"+METHOD_NAME+": START auth="+auth);
		}
		String retVal="";

		PostMethod postMethodAuth =null;
		try {
			JSONObject authJsonObj=new JSONObject(auth);
			JSONObject authObj=authJsonObj.getJSONObject("auth");

			JSONObject credObj=authObj.getJSONObject("passwordCredentials");
			String userName=credObj.getString("username");
			String password=credObj.getString("password");

			//AuthenResults athenResults=new AuthenResults();
			String JSON_STRING="{\"auth\":{\"passwordCredentials\":{\"username\":\""+userName+
					"\", \"password\":\""+password+"\"}}}";
			log.debug(METHOD_NAME+"~~~~~~~~~JSON_STRING="+JSON_STRING);
			System.out.println("~~~~~~~~~JSON_STRING="+JSON_STRING);
			HttpClient client = new HttpClient();
			postMethodAuth= new PostMethod("https://identity.api.rackspacecloud.com/v2.0/tokens");//+ "/j_acegi_security_check");
			StringRequestEntity requestEntity = new StringRequestEntity(JSON_STRING,"application/json", "UTF-8");
			postMethodAuth.setRequestEntity(requestEntity);
			int status= client.executeMethod(postMethodAuth);
			retVal=postMethodAuth.getResponseBodyAsString();

		}
		catch(JSONException e){
			e.printStackTrace();
		}
		catch(UnsupportedEncodingException e){
			e.printStackTrace();
		}
		catch(HttpException e){
			e.printStackTrace();
		}
		catch(IOException e){
			e.printStackTrace();
		}
		catch(Throwable e){
			e.printStackTrace();
		}
		finally {
			if(null!=postMethodAuth){
				postMethodAuth.releaseConnection();
			}
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END: retVal="+retVal);
		}
		return retVal;

	}


	@Override
	//This gets called upon Bean construction
	public void afterPropertiesSet() throws Exception {
		String METHOD_NAME="afterPropertiesSet()";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START:");
		}
		try{
			File file=new File("/home/docs/DeployWars/props.properties");			
			InputStream inny=null;
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": file.exists()="+file.exists());
			}
			if(file.exists()){
				if(log.isDebugEnabled()){
					log.debug(METHOD_NAME+": file.getAbsolutePath()="+file.getAbsolutePath());
				}
				inny=new FileInputStream(file);
			}
			else{
				inny=ExternalWarController.class.getClassLoader().getResourceAsStream("/props.properties");	
			}
			
			if(log.isDebugEnabled()){
			    log.debug(METHOD_NAME+": opening props.properties");
			}
			propsFile=new Properties();
			propsFile.load(inny);
			if(log.isDebugEnabled()){
			    log.debug(METHOD_NAME+":!!!!!!!!propsFile.get(\"jenkinsurl\")="+propsFile.getProperty("jenkinsurl"));
			}			
			this.userAccessToGroupsMap=new HashMap<String, Set<String>>();
			//HibernateUtil util=new HibernateUtil();
			//SessionFactory sessionFactory=util.getSessionFactory();
			DeployUtility.loadUsers(this.membersDao,this.userDao,this.userAccessToGroupsMap);
		} 
		catch (IOException e) {
			e.printStackTrace();
			log.debug(e);
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": caught IOException message="+e.getMessage());
			}
			log.debug(METHOD_NAME+": END:");
		}
		catch(Throwable e){
			e.printStackTrace();
			log.error(e);
			log.error(METHOD_NAME+": caught Throwable message="+e.getMessage());
			log.debug(METHOD_NAME+": END:");
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END:");
		}
	}
	
	private void loadExternalFilters(String xldapUserName){
		String METHOD_NAME="loadExternalFilters()";	
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+":START");
		}
		List<String>tempFilters=new ArrayList<String>();

		if(null==this.wars||this.wars.size()==0){
			if(null==this.wars){
				this.wars=new HashMap<String, InstalledWar>();
			}
			ControllerHelper.loadAllWars(this.servletCtx,this.wars,true);
		}
		boolean debug=false;
		String propsDebug=propsFile.getProperty("debug");
		if(null!=propsDebug && propsDebug.equalsIgnoreCase("true")){
			debug=true;
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": debug="+debug);
			log.debug(METHOD_NAME+": this.wars.size()="+this.wars.size());
		}
		Set<String>tempSortedInstalledWars=new TreeSet<String>();
		Users user=userDao.findById(xldapUserName);
		Set<Groups>groupsMembership=user.getGroupses();
		for(InstalledWar aWar:wars.values()){
			
			String pomName=aWar.getPomName();
			String group=ControllerHelper.retrieveLoggedInUserAccessToWar(aWar,propsFile,groupsMembership);
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+":pomName="+pomName); 
				log.debug(METHOD_NAME+":group="+group); 
			}
			if(!tempSortedInstalledWars.contains(pomName)){
				if(null==group || debug){
					tempSortedInstalledWars.add(pomName);
				}
			}			
		}		
		for(Iterator<String>iter=tempSortedInstalledWars.iterator();iter.hasNext();){
			String pomName=iter.next();				
			tempFilters.add(pomName);			
		}		
		if(tempFilters.size()>0){
			if(null==this.externalFilterList){
				this.externalFilterList=tempFilters;
			}
			else{
				synchronized(this.externalFilterList){
					this.externalFilterList=tempFilters;
				}
			}
		}
		//Make sure at the very least, it's an empty list
		if(null==this.externalFilterList){
			this.externalFilterList=new ArrayList<String>();
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+":END this.externalFilterList.size()="+this.externalFilterList.size());
		}
		//this.externalFilterList= new ArrayList<String>();
		//this.externalFilterList.addAll(tempSortedInstalledWars);
	}
	
	@RequestMapping(value="/getDetails", method=RequestMethod.GET)
	@ResponseBody
	public List<Detail> getExtDetails(HttpServletRequest request, @RequestParam(value="folders")String folders, 
			@RequestParam(value="internaldeploy") String isInternal){
		String METHOD_NAME="getExtDetails()";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START folders="+folders+" isInternal="+isInternal);
		}
		List<Detail>retVal=ControllerHelper.getDetails(request,this.servletCtx, propsFile, folders, isInternal);

		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END");
		}
		return retVal;		
	}
	
	@RequestMapping(value="/getExtWars", method=RequestMethod.GET)
	@ResponseBody
	public Map<String,InstalledWar> getExtWars(HttpSession session, @RequestParam(value="x-ldap-username") String xldapUserName) {
		String METHOD_NAME="getExtWars()";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START");
		}
		if(null==this.wars){
			this.wars=new HashMap<String, InstalledWar>();
		}
		if(null==this.internalNReviewerWars){
			this.internalNReviewerWars=new TreeMap<String, InstalledWar>();
		}
		Map<String, InstalledWar>retVal=ControllerHelper.getWars(session,this.servletCtx,this.wars,this.userDao,
				xldapUserName,propsFile,true);
		if(log.isDebugEnabled()){
			if(null!=retVal){
				log.debug(METHOD_NAME+": retVal.size()="+retVal.size());
				Set<String>keys=retVal.keySet();
				for(String aKey:keys){
					log.debug(METHOD_NAME+": retVal.get(\""+aKey+"\")="+retVal.get(aKey));
				}
			}
			log.debug(METHOD_NAME+": END");
		}		
		return retVal;
	}
	
	
	@RequestMapping(value="/deployTheWars", method=RequestMethod.POST)
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String METHOD_NAME="doPost()";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START");
		}
		HttpSession session=request.getSession();
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": allowing all cross domain scripting");
		}
		this.allowOrigin(request, response);
		response.setContentType("application/json");
		if(log.isDebugEnabled()){
			Enumeration<String> headerNames=request.getHeaderNames();
			log.debug("~~~~~~~~~~~~~Header Values START:~~~~~~~~~~");
			while(headerNames.hasMoreElements()){
				String aHeaderKey=(String)headerNames.nextElement();
				String aHeaderValue=(String)request.getHeader(aHeaderKey);
				log.debug(METHOD_NAME+": request.getHeader("+aHeaderKey+")="+aHeaderValue);

			}
			log.debug("~~~~~~~~~~~~~Header Values END:~~~~~~~~~~");
			Enumeration<String> sessionNames=session.getAttributeNames();
			log.debug("~~~~~~~~~~~~~Session Attribute Values START:~~~~~~~~~~");
			while(sessionNames.hasMoreElements()){
				String sessAttrKey=(String)sessionNames.nextElement();
				String aSessAttrValue=(String)session.getAttribute(sessAttrKey);
				log.debug(METHOD_NAME+": request.getHeader("+sessAttrKey+")="+aSessAttrValue);

			}
			log.debug("~~~~~~~~~~~~~Session Attribute Values END:~~~~~~~~~~");
		}	
		String debug=ExternalWarController.propsFile.getProperty("debug", "false");
		if(log.isDebugEnabled()){
			log.debug("~~~~~~~~~~~~~debug="+debug);
		}
		if(debug.equalsIgnoreCase("true")){
			session.setAttribute("x-ldap-username", "thu4404");
		}
		//The x-ldap-username is injected in the header by NAM, if it isn't in the session, we should
		//add it for later use
		String xLDAPUsername=(String)session.getAttribute("x-ldap-username");
		String username=null;
		String xLDAPGroupname=null;		
		
		if(null==xLDAPUsername || xLDAPUsername.isEmpty()){
			xLDAPUsername=request.getHeader("x-ldap-username");
			username=request.getHeader("username");
			xLDAPGroupname=request.getHeader("x-ldap-groupname");			
		}
		else{
			username=(String)session.getAttribute("username");
			xLDAPGroupname=(String)session.getAttribute("x-ldap-groupname");
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": xLDAPUsername="+xLDAPUsername+" username="+username+" xLDAPGroupname="+xLDAPGroupname);
		}
		//SessionFactory sessionFactory=null;		
		JSONObject retVal=new JSONObject();
		//If there is no x-ldap-username value, then do not display anything, instead show an error message
		if(null==xLDAPUsername || xLDAPUsername.isEmpty()){
			ControllerHelper.addMessage(retVal, "noldapuser", 
					"CreateNewUserServet.doPost(): Error, header/session parameter: x-ldap-username/username "+
					"is null or empty, delete browser cache and log back into page.");
		}
		//We have a x-ldap-username value, we can continue with the deployment
		else{
			if(log.isDebugEnabled()){			
				log.debug(METHOD_NAME+":xLDAPUsername="+xLDAPUsername+" is not null nor empty");
			}
			Users loggedInuser=this.userDao.findById(xLDAPUsername);	
			//trans.commit();
			if(log.isDebugEnabled()){			
				log.debug(METHOD_NAME+":loggedInuser="+loggedInuser);
			}
			//The next thing we need to do is check the request and see if it's a freeze UI request
			String freezeUI=request.getParameter("freezeui");
			if(log.isDebugEnabled()){			
				log.debug(METHOD_NAME+":freezeui="+freezeUI);
			}
			//This is not  freeze request, but a deploy or revert request
			if(null==freezeUI || freezeUI.isEmpty()){

				//Even though we now know that this is NOT a freeze UI request, we need to double check
				//to make sure that the UI is currently frozen				
				boolean frozen=this.freezeDao.findById((short)1).getShouldfreeze();
				if(log.isDebugEnabled()){			
					log.debug(METHOD_NAME+":frozen="+frozen);
				}
				//The UI is frozen, we should refresh the page so the bean can display to the user that the UI is frozen
				if(frozen){
					String isInternal=request.getParameter("internaldeploy");
					if(null!=isInternal && !isInternal.isEmpty()){
						if(isInternal.equalsIgnoreCase("true")){
							String serverName=request.getServerName();
							if(null!=serverName && serverName.contains("content-services")){
								String redirect=propsFile.getProperty("contentstagingredirect","https://docs-staging.fedsso.rackspace.com/rax-autodeploy/DeployWarsInternal");
								if(log.isDebugEnabled()){			
									log.debug(METHOD_NAME+":redirect="+redirect);
								}
								response.sendRedirect(redirect);
							}
							else{
								String redirect=propsFile.getProperty("docsstagingredirect", "https://docs-staging.fedsso.rackspace.com/rax-autodeploy/DeployWarsInternal");
								if(log.isDebugEnabled()){			
									log.debug(METHOD_NAME+":redirect="+redirect);
								}
								response.sendRedirect(redirect);
							}
						}
						else{
							String redirect=propsFile.getProperty("docsstaginginternalredirect","https://docs-internal-staging.fedsso.rackspace.com/rax-autodeploy/DeployWarsInternal");
							if(log.isDebugEnabled()){			
								log.debug(METHOD_NAME+":redirect="+redirect);
							}
							response.sendRedirect(redirect);
						}
					}
				}
				//The UI is currently enabled, continue with the request
				else{
					boolean success=true;
					try {
						//sleep for a litle to try to ensure the date is unique
						Thread.sleep(2500);
					} 
					catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Date startD=new Date();
					
					Long startTime=startD.getTime();
					String[] warfolderNamesArr=request.getParameterValues("warfoldernames");
					if(log.isDebugEnabled()){	
						log.debug(METHOD_NAME+": warfolderNamesArr="+warfolderNamesArr);
					}
					//there must be a war selected
					if(null!=warfolderNamesArr && warfolderNamesArr.length>0){
						String groupid=request.getParameter("groupid");
						String artifactid=request.getParameter("artifactid");
						if(log.isDebugEnabled()){	
							log.debug("groupid="+groupid);
							log.debug("artifactid="+artifactid);							
							Enumeration<String> enums=session.getAttributeNames();
							while(enums.hasMoreElements()){
								String aName=enums.nextElement();
								log.debug("session.getAttribute("+aName+")="+session.getAttribute(aName));
							}
							log.debug(METHOD_NAME+":Done with printing out session attributes");
						}

						Map<String,List<String>> messages=new HashMap<String,List<String>>();
						if(log.isDebugEnabled()){
							log.debug(METHOD_NAME+":from propsFile file: request.getRequestURL()="+request.getRequestURL());
						}
						List<Deployjob>jobs=new ArrayList<Deployjob>();

						//org.json.JSONArray warFolderNamesJSONArr=new org.json.JSONArray();
						String webappsFolder=this.servletCtx.getRealPath("..");
						if(!webappsFolder.endsWith("/")){
							webappsFolder+="/";
						}		
						List<String>docNames=new ArrayList<String>();
						this.populateDocNames(webappsFolder, warfolderNamesArr, docNames);						
						String action=request.getParameter("action");
						if(log.isDebugEnabled()){
							log.debug(METHOD_NAME+": action="+action);
						}
						//This is a revert request
						if(null!=action && action.equals("revert")){
							this.createJobs(warfolderNamesArr, docNames, jobs, xLDAPUsername, groupid, artifactid, 
									startTime, DeployUtility.JOB_TYPE_REVERT);

							//Only one deploy request should be processed at a time
							//This results in a lot of waiting by a thread that is trying to run this same code, 
							//but we don't want two different reuqests to be processed at the same time.
							synchronized(lock){
								success=processWebAppsFolder(request, warfolderNamesArr, messages, loggedInuser, jobs, false);
							}
						}
						//This is a deploy request
						else{
							this.createJobs(warfolderNamesArr, docNames, jobs, xLDAPUsername, groupid, artifactid, 
									startTime, DeployUtility.JOB_TYPE_DEPLOY);

							//Only one deploy request should be processed at a time
							//This results in a lot of waiting by a thread that is trying to run this same code, 
							//but we don't want two different reuqests to be processed at the same time.
							synchronized(lock){
								success=processWebAppsFolder(request, warfolderNamesArr, messages, loggedInuser, jobs,true);
							}
						}
						this.updateDeployStatusToDone(startTime);

						if(!success){
							DeployUtility.addABadMessage("error", messages,"<span class='failuremessage'>There is at least one error in the deployment. View the details of the email for additional information. ");
							reportSuccess(request,response,success,messages, loggedInuser);	
						}
						else{
							//We now have jobs that we need to update 				
							reportSuccess(request,response,success,messages, loggedInuser);	
						}
					}
					//we need to notify the user that there are no wars selected, clear cache and log back in
					else{
						ControllerHelper.addMessage(retVal, "errormessage", "No wars were selected, deployment halted");
					}
				}
			}
			//This is a freeze ui request check to see if we want to change the freeze value to true or false
			else{
				if(log.isDebugEnabled()){
					log.debug(METHOD_NAME+":~~~~~~~about to update freeze table and set shouldfreeze="+freezeUI);
				}
				Freeze freeze=this.freezeDao.findById((short)1);
				if(freezeUI.equalsIgnoreCase("true")){
					freeze.setShouldfreeze(true);
					this.freezeDao.update(freeze);					
				}
				else{
					freeze.setShouldfreeze(false);
					this.freezeDao.update(freeze);
				}
				String isExternal=request.getParameter("isexternal");
				if(log.isDebugEnabled()){
					log.debug(METHOD_NAME+":~~~~~~~isExternal="+isExternal);
				}
				if(null!=isExternal && !isExternal.isEmpty()){
					try {
						retVal.put("status", "success");
					} 
					catch (org.json.JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(isExternal.equalsIgnoreCase("true")){
						response.sendRedirect("https://docs-staging.fedsso.rackspace.com/rax-autodeploy/DeployWars");
					}
					else{
						response.sendRedirect("https://docs-internal-staging.fedsso.rackspace.com/rax-autodeploy/DeployWarsInternal");
					}
				}
			}
		}
		PrintWriter out=response.getWriter();
		out.print(retVal.toString());
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END: retVal.toString()="+retVal.toString());
		}
	}
	
	//We must get the war display name from each respective bookinfo.xml
	private void populateDocNames(String webappsFolder, String[] warFolderNames, List<String>docNames){
		String METHOD_NAME="populateDocNames()";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+":START: docNames.size()="+docNames.size());
			log.debug(METHOD_NAME+": webappsFolder="+webappsFolder);
			log.debug(METHOD_NAME+": warFolderNames="+warFolderNames);
			log.debug(METHOD_NAME+": warFolderNames="+warFolderNames.length);
		}
		for(String astr:warFolderNames){	
			String pathToBookInfo=webappsFolder+astr+"/bookinfo.xml";
			File aBookInfoFile=new File(pathToBookInfo);

			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": pathToBookInfo="+pathToBookInfo);	
				log.debug(METHOD_NAME+": aBookInfoFile.exists()="+aBookInfoFile.exists());
			}
			if(astr.equalsIgnoreCase("root")){
				docNames.add("ROOT");
			}
			else if(aBookInfoFile.exists()){
				DeployUtility.retrieveDocNames(aBookInfoFile,docNames);
			}			
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+":END: docNames.size()="+docNames.size());
		}
	}
	
	private void reportSuccess(HttpServletRequest request, HttpServletResponse response, 
			boolean success, Map<String,List<String>> messages, Users loggedInuser){
		String METHOD_NAME="reportSuccess";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": Start: success="+success+" loggedInuser="+loggedInuser);
		}		
		response.setContentType("text/json");
		String action=request.getParameter("action");

		String[] warFolderNames=request.getParameterValues("warfoldernames");
		String   groupId=request.getParameter("groupid");
		String   artifactId=request.getParameter("artifactid");
		String   rowNumber=request.getParameter("rownumber");

		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": groupId="+groupId+" artifactId="+artifactId+ " rowNumber="+rowNumber+" action="+action);
		}	
		if(null==warFolderNames){
			warFolderNames= new String[0];
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": warFolderNames.length()="+warFolderNames.length);
		}	
		if(null==groupId){
			groupId="";
		}
		if(null==artifactId){
			artifactId="";
		}
		if(null==rowNumber){
			rowNumber="";
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": groupId="+groupId+" artifactId="+artifactId+ " rowNumber="+rowNumber);
		}	
		//org.json.JSONArray warFolderNamesJSONArr=new org.json.JSONArray();
		String webappsFolder=this.servletCtx.getRealPath("..");
		if(!webappsFolder.endsWith("/")){
			webappsFolder+="/";
		}		
		List<String>docNames=new ArrayList<String>();
		this.populateDocNames(webappsFolder, warFolderNames, docNames);
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": emailsMap.size()="+docNames.size());
		}
		List<Email>emails=DeployUtility.getEmails(loggedInuser,docNames);
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": Creating new Thread to process sending of email");
			log.debug(METHOD_NAME+": Sending emailsMap.size()="+emails.size());
			for(Email anEmail:emails){
				log.debug(METHOD_NAME+" anEmail="+anEmail);
			}
		}
		Thread mailThread = new Thread(new SendMail(emails, messages, action, loggedInuser));
		mailThread.start();	

		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END");
		}		
	}
	
	private void createJobs(String[] warfolderNamesArr, List<String>docNames, List<Deployjob>jobs, String loggedInUserStr,
			String groupid, String artifactid, Long starttime, String jobtype){
		String METHOD_NAME="createJobs()";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START: loggedInUserStr="+loggedInUserStr+" groupid="+groupid+" artifactid="+artifactid
					+" docsNames="+docNames+" jobs="+jobs);
		}
		if(null!=docNames && jobs!=null){
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": jobs.size()="+jobs.size());
			}
			for(int i=0;i<warfolderNamesArr.length;++i){
				Deployjob aJob=new Deployjob();
				String aWarFolder=warfolderNamesArr[i];
				if(log.isDebugEnabled()){
					log.debug(METHOD_NAME+": aWarFolder="+aWarFolder);
				}
				if(null==loggedInUserStr || loggedInUserStr.isEmpty()){
					loggedInUserStr="unknown";
				}
				if(aWarFolder!=null&&aWarFolder.equalsIgnoreCase("root")){
					groupid=ControllerHelper.ROOT_GROUP_ID;
					artifactid=ControllerHelper.ROOT_ARTIFACT_ID;
				}
				if(log.isDebugEnabled()){
					log.debug(METHOD_NAME+": loggedInUserStr="+loggedInUserStr);
					log.debug(METHOD_NAME+": groupid="+groupid);
					log.debug(METHOD_NAME+": artifactid="+artifactid);
					log.debug(METHOD_NAME+": aWarFolder="+aWarFolder);
					log.debug(METHOD_NAME+": starttime="+starttime);
				}
				aJob.setLdapname(loggedInUserStr);
				aJob.setGroupid(groupid);
				aJob.setArtifactid(artifactid);
				aJob.setWarname((aWarFolder+=".war"));
				aJob.setStarttime(starttime);
				aJob.setStatus(new Status(DeployUtility.STATUS_STARTED));
				aJob.setType(jobtype);
				jobs.add(aJob);
			}
			//Session session=sessionFactory.openSession();
			//Transaction tx=session.beginTransaction();
			for(int i=0;i<docNames.size();++i){
				if(i<jobs.size()){
					String aDocName=docNames.get(i);
					Deployjob aDeployJob=jobs.get(i);
					aDeployJob.setPomname(aDocName);
					if(log.isDebugEnabled()){
						log.debug(METHOD_NAME+": aDeployJob:"+aDeployJob+" pomname="+aDocName);
					}									
				}			
			}
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": Saving jobs");
			}
			try{
				this.deployjobDao.save(jobs);
				if(log.isDebugEnabled()){
					log.debug(METHOD_NAME+":~~~~After saving jobs:");
					
					for(Deployjob aJob:jobs){
						log.debug(METHOD_NAME+": aJob:"+aJob);
					}
				}
			}
			catch(Throwable e){
				e.printStackTrace();
			}
		}
		else{			
			if(null==docNames && log.isDebugEnabled()){
				log.debug(METHOD_NAME+": docNames is NULL");
			}
			if(null==jobs && log.isDebugEnabled()){
				log.debug(METHOD_NAME+": jobs is NULL");
			}
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END:");
		}
	}
	
	private boolean processWebAppsFolder(HttpServletRequest request, String[] warfolderNamesArr, Map<String,List<String>>messages, 
			Users loggedInuser, List<Deployjob>jobs, boolean isADeployment){
		boolean retVal=true;
		String METHOD_NAME="processWebAppsFolder()";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START: loggedInuser="+loggedInuser+" isADeployment="+isADeployment);
		}
		if(null!=warfolderNamesArr&&warfolderNamesArr.length>0){			

			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": warfolderNamesArr.length="+warfolderNamesArr.length);
			}
			//We just have to get the webapps folder and get the corresponding .war file and upload it to the production server 
			//the sch API
			String webAppPathOnStagingServer=this.servletCtx.getRealPath("..");
			if(!webAppPathOnStagingServer.endsWith("/")){
				webAppPathOnStagingServer+="/";
			}
			File webAppsFolderOnStagingServer=new File(webAppPathOnStagingServer);
			try{
				if(webAppsFolderOnStagingServer.exists()){

					File[] files=webAppsFolderOnStagingServer.listFiles();
					String webappsFolder=propsFile.getProperty("webappfolder","/home/docs/Tomcat/latest/webapps/");
					String internalDeploy=request.getParameter("internaldeploy");
					String action=request.getParameter("action");
					boolean isAnInternalDeploy=false;

					if(null!=internalDeploy && internalDeploy.equalsIgnoreCase("true")){
						isAnInternalDeploy=true;					
					}
					if(log.isDebugEnabled()){
						log.debug(METHOD_NAME+": action="+action);
						log.debug(METHOD_NAME+": internalDeploy="+internalDeploy);
						log.debug(METHOD_NAME+": isAnInternalDeploy="+isAnInternalDeploy);
					}

					if(isAnInternalDeploy){
						webappsFolder=propsFile.getProperty("webappfolderinternal","/home/docs/Tomcat/internal/latest/webapps/");
					}

					if(!webappsFolder.endsWith("/")){
						webappsFolder+="/";
					}
					if(log.isDebugEnabled()){
						log.debug(METHOD_NAME+": webappsFolder="+webappsFolder);
						log.debug(METHOD_NAME+": warfolderNamesArr.length="+warfolderNamesArr.length);								
					}												
					List<String>docNames=new ArrayList<String>();
					this.populateDocNames(webappsFolder, warfolderNamesArr, docNames);

					Map<String, File>filesMap=new HashMap<String, File>();
					//Iterate through all the webapps files and folders adding only the *.war files into the map keyed by the .war file name
					for(File aFile:files){
						if(aFile.isFile()){
							String aFileName=aFile.getName();
							if(aFileName.endsWith(".war")){
								if(log.isDebugEnabled()){
									log.debug(METHOD_NAME+":adding aFileName="+aFileName+" to the map");
								}
								filesMap.put(aFile.getName(), aFile);
							}
						}
					}
					if(log.isDebugEnabled()){
						log.debug(METHOD_NAME+":webAppPathOnStagingServer="+webAppPathOnStagingServer);
						log.debug(METHOD_NAME+":Selected web apps Starting:");
					}			
					//We should figure out whether this is a deploy or revert 				
					String scpUser=(propsFile.getProperty("scpuser","docs")).trim();							
					String prodWebAppsPath=(propsFile.getProperty("prodwebappspath","/home/docs/Tomcat/latest/webapps/")).trim();
					String passwd=(propsFile.getProperty("docspasswd","Fanatical7")).trim();
					String known_hosts=(propsFile.getProperty("knownhosts","/home/docs/.ssh/known_hosts")).trim();
					String id_rsa=(propsFile.getProperty("idrsa","/home/docs/.ssh/id_rsa")).trim();
					String scpwebapps1Dir=propsFile.getProperty("scpwebapps1","/home/docs/Tomcat/latest/scpwebapps1/");
					String scpwebapps2Dir=propsFile.getProperty("scpwebapps2","/home/docs/Tomcat/latest/scpwebapps2/");
					if(!scpwebapps1Dir.endsWith("/")){
						scpwebapps1Dir+="/";
					}				
					if(!scpwebapps2Dir.endsWith("/")){
						scpwebapps2Dir+="/";
					}
					if(log.isDebugEnabled()){
						log.debug(METHOD_NAME+":scpUser="+scpUser);
						log.debug(METHOD_NAME+":prodWebAppsPath="+prodWebAppsPath);
						log.debug(METHOD_NAME+":known_hosts="+known_hosts);
						log.debug(METHOD_NAME+":id_rsa="+id_rsa);
						log.debug(METHOD_NAME+":scpUser="+scpUser);
						log.debug(METHOD_NAME+":scpwebapps1Dir="+scpwebapps1Dir);
						log.debug(METHOD_NAME+":scpwebapps2Dir="+scpwebapps2Dir);
					}
					//This is an internal deployment, there is no load balancing needed, we just have to get the production 
					//internal server
					if(isAnInternalDeploy){
						String prodInternal=propsFile.getProperty("prodserverinternal","docs-internal.rackspace.com");											
						String pathToTempFolder=propsFile.getProperty("webappsTemp","/home/docs/Tomcat/latest/webappsBackup/Temp/");
						if(!pathToTempFolder.endsWith("/")){
							pathToTempFolder+="/";
						}

						//Even though this is an internal deploy, since the jSch command runs on the production server
						//the path needs to match the production server path
						String webappsBackupFolder=propsFile.getProperty("webappsBackup","/home/docs/Tomcat/latest/webappsBackup/");
						if(!webappsBackupFolder.endsWith("/")){
							webappsBackupFolder+="/";
						}

						if(log.isDebugEnabled()){
							log.debug(METHOD_NAME+": pathToTempFolder="+pathToTempFolder);
							log.debug(METHOD_NAME+": webappsBackupFolder="+webappsBackupFolder);
						}
						for(String aSelectedApp:warfolderNamesArr){
							if(log.isDebugEnabled()){
								log.debug(METHOD_NAME+": aSelectedWebApp="+aSelectedApp);
							}
							String aSelectedAppWithWarSuffix=aSelectedApp+".war";
							String pathToTempWar=pathToTempFolder+aSelectedAppWithWarSuffix;
							String backupSelectedWebappsFile=(webappsBackupFolder+aSelectedApp+".war");
							String pathToSelectedWarFile=((propsFile.getProperty("webappfolder","/home/docs/Tomcat/latest/webapps/"))+aSelectedAppWithWarSuffix);
							if(log.isDebugEnabled()){
								log.debug(METHOD_NAME+": aSelectedAppwithWarSuffix="+aSelectedAppWithWarSuffix);
								log.debug(METHOD_NAME+": pathToTempWar="+pathToTempWar);
								log.debug(METHOD_NAME+": backupSelectedWebappsFile="+backupSelectedWebappsFile);
								log.debug(METHOD_NAME+": pathToSelectedWarFile="+pathToSelectedWarFile);
							}
							File selectedWarFile=null;

							//The .war file should exist in the map
							if(filesMap.containsKey(aSelectedAppWithWarSuffix)){
								selectedWarFile=filesMap.get(aSelectedAppWithWarSuffix);
							}
							else{
								//The .war file does not exist in the webapps folder, instantiate the file and put it in the map
								selectedWarFile=new File(webAppPathOnStagingServer+aSelectedAppWithWarSuffix);
								filesMap.put(aSelectedAppWithWarSuffix, selectedWarFile);				    	
							}
							if(log.isDebugEnabled()){
								log.debug(METHOD_NAME+": selectedWarFile="+selectedWarFile);
								if(null!=selectedWarFile){
									log.debug(METHOD_NAME+": selectedWarFile.getAbsoloutePath()="+selectedWarFile.getAbsolutePath());
								}
							}
							String fullPathWarScpwebapps2=scpwebapps2Dir+aSelectedAppWithWarSuffix;
							String fullPathWarScpwebapps1=scpwebapps1Dir+aSelectedAppWithWarSuffix;
							if(log.isDebugEnabled()){
								log.debug(METHOD_NAME+": fullPathWarScpwebapps1="+fullPathWarScpwebapps1+
										" fullPathWarScpwebapps2="+fullPathWarScpwebapps1);
							}						
							//This is a deploy, we have to back it up
							if(isADeployment){
								//even though this is an internal deploy, since the copy command is ran on a production server
								//the webappsfolder path will always be /home/docs/Tomcat/latest/webapps/

								//first we backup the current .war on production to the Temp folder in case we need to back out
								//If this fails for the time being just ignore the failure
								DeployUtility.backupSelectedWar(prodInternal, messages, pathToSelectedWarFile, pathToTempFolder);								

								if(selectedWarFile.exists()){
									boolean continueWithScp=true;
									try{
										continueWithScp=mvWarFromScp2ToWebapps(prodInternal, fullPathWarScpwebapps1, fullPathWarScpwebapps2, 
												selectedWarFile, aSelectedAppWithWarSuffix);

										//We could not find the backup folder on the production server or this is a revert we need to scp it
										if(continueWithScp){
											Date dateNow=new Date();
											Long endtime=dateNow.getTime();											

											if(DeployUtility.scpFileToServer(scpUser, prodInternal, selectedWarFile, aSelectedAppWithWarSuffix, prodWebAppsPath, 
													passwd, known_hosts, id_rsa, messages)){								
												DeployUtility.addSuccessMessages(request, aSelectedAppWithWarSuffix, messages,isADeployment);
												this.markJobsEndTime(jobs,endtime);
												this.updateJobForSelectedWar(jobs, aSelectedAppWithWarSuffix, DeployUtility.STATUS_DONE,null);
												//Do not touch the ROOT.war within the loop, do it when the loop ends so we touch it only once per request

												//Now backup the .war from the Temp folder to the webappsBackup folder
												if(log.isDebugEnabled()){
													log.debug(METHOD_NAME+": continueWithScp="+continueWithScp);
													log.debug(METHOD_NAME+": pathToTempWar="+pathToTempWar);
													log.debug(METHOD_NAME+": webappsBackupFolder="+webappsBackupFolder);
													log.debug(METHOD_NAME+": webappsFolder="+webappsFolder);
												}
												//copy the .war from the webapps/Temp folder to the webappsBackup folder
												DeployUtility.backupSelectedWar(prodInternal, messages, pathToTempWar, prodWebAppsPath);
											}
											//The scp failed, add an error message and try to backout
											else{
												this.markJobsEndTime(jobs, endtime);
												this.updateJobForSelectedWar(jobs, aSelectedAppWithWarSuffix, DeployUtility.STATUS_FAILED,"Scp to internal prod server: "+prodInternal+" failed");

												if(log.isDebugEnabled()){
													log.debug(METHOD_NAME+": pathToTempWar="+pathToTempWar);
													log.debug(METHOD_NAME+": prodWebAppsPath="+prodWebAppsPath);
												}
												//Try to back out of the change copy the .war in the webapps/Temp folder to the webapps folder
												DeployUtility.backupSelectedWar(prodInternal, messages, pathToTempWar, prodWebAppsPath);
												retVal=false;
												//we should halt the deploy process
												break;
											}
										}
										else{
											DeployUtility.addSuccessMessages(request, aSelectedAppWithWarSuffix, messages,isADeployment);
											Date dateNow=new Date();
											Long endtime=dateNow.getTime();
											this.markJobsEndTime(jobs,endtime);
											this.updateJobForSelectedWar(jobs, aSelectedAppWithWarSuffix, DeployUtility.STATUS_DONE,null);
											//Do not touch the ROOT.war within the loop, do it when the loop ends so we touch it only once per request

											//Now backup the .war from the Temp folder to the webappsBackup folder
											if(log.isDebugEnabled()){
												log.debug(METHOD_NAME+": continueWithScp="+continueWithScp);
												log.debug(METHOD_NAME+": pathToTempWar="+pathToTempWar);
												log.debug(METHOD_NAME+": webappsBackupFolder="+webappsBackupFolder);
											}
											//copy the .war from the webapps/Temp folder to the webappsBackup folder
											DeployUtility.backupSelectedWar(prodInternal, messages, pathToTempWar, webappsBackupFolder);
										}
									}
									catch(IOException e){
										log.error(METHOD_NAME+": IOException Internal mvWarFromScp2ToWebapps failed for prodInternal="+prodInternal+
												" fullPathWarScpwebapps1="+fullPathWarScpwebapps1+" fullPathWarScpwebapps2="+fullPathWarScpwebapps2+
												" selectedWarFile="+selectedWarFile+" aSelectedAppWithWarSuffix="+aSelectedAppWithWarSuffix);
										log.error(e);

										Date dateNow=new Date();
										Long endtime=dateNow.getTime();
										this.markJobsEndTime(jobs, endtime);
										this.updateJobForSelectedWar(jobs, aSelectedAppWithWarSuffix, DeployUtility.STATUS_FAILED, "Move failed for internal server: "+
												prodInternal+" for .war: "+aSelectedAppWithWarSuffix);

										DeployUtility.addABadMessage("error", messages,"<span class='failuremessage'>Error: "+
												" IOException when trying internal move method: mvWarFromScp2ToWebapps</span>");

										log.error(METHOD_NAME+": END retVal=false~");										
										return false;
									}
									catch(Throwable e){
										log.error(METHOD_NAME+": Throwable Internal mvWarFromScp2ToWebapps failed for prodInternal="+prodInternal+
												" fullPathWarScpwebapps1="+fullPathWarScpwebapps1+" fullPathWarScpwebapps2="+fullPathWarScpwebapps2+
												" selectedWarFile="+selectedWarFile+" aSelectedAppWithWarSuffix="+aSelectedAppWithWarSuffix);
										log.error(e);

										Date dateNow=new Date();
										Long endtime=dateNow.getTime();
										this.markJobsEndTime(jobs, endtime);
										this.updateJobForSelectedWar(jobs, aSelectedAppWithWarSuffix, DeployUtility.STATUS_FAILED, "Move failed for internal server: "+
												prodInternal+" for .war: "+aSelectedAppWithWarSuffix);

										DeployUtility.addABadMessage("error", messages,"<span class='failuremessage'>Error: "+
												" Throwable when trying internal move method: mvWarFromScp2ToWebapps</span>");											
										log.error(METHOD_NAME+": END retVal=false~");										
										return false;
									}
								}	
								if(retVal){
									if(log.isDebugEnabled()){
										log.debug(METHOD_NAME+": Backing the temp war file to webappsBackup and new internal war file as the *-current.war in the webappsBackup folder");
										log.debug(METHOD_NAME+": pathToTempWar="+pathToTempWar);
										log.debug(METHOD_NAME+": webappsBackupFolder="+webappsBackupFolder);
									}
									
									DeployUtility.backupSelectedWar(prodInternal, messages, pathToTempWar, webappsBackupFolder);
									//String selectedFileOnStagingServerStr=webAppPathOnStagingServer+aSelectedAppWithWarSuffix;
									//File selectedFileOnStaging=new File(selectedFileOnStagingServerStr);
									//Now we have to copy the selected file to the webappsBackup folder as *-current.war
									
								}
							}
							//This is a revert, we need to scp the file from back up
							else{
								String webappsBackupFile=webappsBackupFolder+aSelectedAppWithWarSuffix;
								if(log.isDebugEnabled()){
									log.debug(METHOD_NAME+": Reverting!!!");
									log.debug(METHOD_NAME+": webappsBackupFile="+webappsBackupFile);
									log.debug(METHOD_NAME+": prodWebAppsPath="+prodWebAppsPath);
								}
								String jSchResult=DeployUtility.backupSelectedWar(prodInternal, messages, webappsBackupFile, prodWebAppsPath);
								//the revert failed when trying to use the copy command on the 
								//production server from webappsBackup/*.war to webapps/*.war
								//Try scping from the staging server to production internal server
								if(log.isDebugEnabled()){
									log.debug(METHOD_NAME+": jSchResult="+jSchResult);
								}
							}
						}

						//touch the ROOT war only once
						String rootFullPath=propsFile.getProperty("webappfolder","/home/docs/Tomcat/latest/webapps/");
						rootFullPath+="ROOT.war";
						if(log.isDebugEnabled()){
							log.debug(METHOD_NAME+": touching ROOT.war rootFullPath="+rootFullPath);
						}
						DeployUtility.touchRoot(scpUser, passwd, known_hosts, id_rsa, prodInternal, rootFullPath);				
					}
					/***********
					 * This was clicked from the External Deploy server, we have to make sure that we load balance the udpdate
					 */
					else{
						if(log.isDebugEnabled()){
							log.debug(METHOD_NAME+": This is an external deploy");
						}
						LoadBalancer lb=null;
						CloudLoadBalancersApi clb=null;
						Node disabledNode=null;				
						Map<String,Node>enabledNodes=null;
						Map<String,DocToolsEntity>entities=null;

						//First we have to get all the enabled nodes:
						clb=JCloudsUtility.getCloudLoadBalancer(request);
						lb=JCloudsUtility.getLoadBalancer(request,clb);

						enabledNodes=JCloudsUtility.getEnabledLoadBalancerNodes(lb, clb,request);
						if(log.isDebugEnabled()){
							log.debug(METHOD_NAME+": enabledNodes.size()="+enabledNodes.size());
						}
						//If there is only one node enabled, we should not continue
						if(enabledNodes.size()<=1){
							if(log.isDebugEnabled()){
								log.debug(METHOD_NAME+"send email and halt, only one node enabled");
							}
							Date dateNow=new Date();
							Long endtime=dateNow.getTime();
							this.markJobsEndTime(jobs, endtime);
							//update the status to failure with an explanation of failure
							this.updateJobs(jobs, DeployUtility.STATUS_FAILED, "Only 1 available prod server available");
							//There is only 1 node enabled, we just send an email stating that we can not load balance because there
							//is only one node available 

							String warsTriedToDeploy="";							

							List<String>messagesList=messages.get("error");
							if(null==messagesList){
								messagesList=new ArrayList<String>();
								messages.put("error", messagesList);
							}
							Collection<Node>nodeCollection=enabledNodes.values();							
							Iterator<Node>iter=null;
							Node aNode=null;
							if(null!=nodeCollection){
								iter=nodeCollection.iterator();
							}
							if(null!=iter){
								aNode=iter.next();
							}							
							String address="";
							if(null!=aNode){
								address=aNode.getAddress();
							}
							if(log.isDebugEnabled()){
								log.debug(METHOD_NAME+": address="+address);
							}
							messagesList.add("<span class='failuremessage'>Deploy failed for wars: "+warsTriedToDeploy+
									", only one node with address: "+address+" is available for deploy. Aborting deploy,"+
									" contact administrators.</span>");
							List<Email>emailsList=DeployUtility.getEmails(loggedInuser, docNames);
							if(log.isDebugEnabled()){
								log.debug(METHOD_NAME+": emailsList.size()="+emailsList.size());
							}
							retVal=false;
							DeployUtility.sendEmails(emailsList, messages, action);														
						}
						//More than 1 load balanced node is enabled
						else{
							//First we should disable only one of the nodes so that the other nodes are available. 
							try{
								this.updateJobs(jobs, DeployUtility.STATUS_NODE1_STARTED, null);									
								entities=DeployUtility.getEntitiesInAMap();
								if(log.isDebugEnabled()){
									log.debug(METHOD_NAME+": action="+action+" entities.size()="+entities.size());
								}
								//This not only disables the node, but it also disables all check monitors of that entity node
								disabledNode=JCloudsUtility.disableAnEnabledNode(clb, lb, entities, enabledNodes);
							}
							catch(JSONException e){
								e.printStackTrace();
								log.error(e);
								log.error(METHOD_NAME+": action="+action+ "  !!!!!JSONException caught message: "+e.getMessage());
								retVal=false;
								disabledNode=null;
								if(null==action || (action.trim().equals(""))){
									DeployUtility.addABadMessage("error", messages,"<span class='failuremessage'>Revert of docs failed, "+
											"JSONException, illegal field name, error message: "+e.getMessage()+".</span>");
								}
								else{
									DeployUtility.addABadMessage("error",messages,"<span class='failuremessage'>Deployment of docs failed, "+
											"JSONException, illegal field name, error message: "+e.getMessage()+"</span>");						
								}
								Date dateNow=new Date();
								Long endtime=dateNow.getTime();
								this.markJobsEndTime(jobs, endtime);
								this.updateJobs(jobs, DeployUtility.STATUS_FAILED, "Could not disable nodes from load balancer");									
							}
							catch(JCloudsException e){
								e.printStackTrace();
								log.error(e);
								log.error(METHOD_NAME+": action="+action+ "  !!!!!JCloudsException caught message: "+e.getMessage());
								retVal=false;
								disabledNode=null;
								if(null==action || (action.trim().equals(""))){
									DeployUtility.addABadMessage("error", messages,"<span class='failuremessage'>Revert of docs failed, "+
											"could not disable node from load balancer, error message: "+e.getMessage()+".</span>");
								}
								else{
									DeployUtility.addABadMessage("error",messages,"<span class='failuremessage'>Deployment of docs failed, "+
											"could not disable node from load balancer, error message: "+e.getMessage()+"</span>");						
								}
								Date dateNow=new Date();
								Long endtime=dateNow.getTime();
								this.markJobsEndTime(jobs, endtime);
								this.updateJobs(jobs, DeployUtility.STATUS_FAILED, "Could not disable nodes from load balancer");
							}
							catch(Throwable e){
								e.printStackTrace();
								log.error(e);
								log.error(METHOD_NAME+": action="+action+ "  !!!!!Throwable caught message: "+e.getMessage());
								retVal=false;
								disabledNode=null;
								if(null==action || (action.trim().equals(""))){
									DeployUtility.addABadMessage("error", messages,"<span class='failuremessage'>Revert of docs failed, "+
											"could not disable node from load balancer, error message: "+e.getMessage()+".</span>");
								}
								else{
									DeployUtility.addABadMessage("error",messages,"<span class='failuremessage'>Deployment of docs failed, "+
											"could not disable node from load balancer, error message: "+e.getMessage()+"</span>");						
								}
								Date dateNow=new Date();
								Long endtime=dateNow.getTime();
								this.markJobsEndTime(jobs, endtime);
								this.updateJobs(jobs, DeployUtility.STATUS_FAILED, "ThrowableException could not disable nodes from load balancer");
							}							
							if(log.isDebugEnabled()){
								log.debug(METHOD_NAME+": action="+action+" disabledNode="+disabledNode);
							}
							//The action parameter does not exist, this is a deploy request
							//We have to do a load balanced deploy
							if(null==action || (action.trim()).equals("")){	
								/*****************
								 * This is a deployment request
								 ******************/	
								if(log.isDebugEnabled()){
									log.debug(METHOD_NAME+": This is a deployment action="+action);
								}
								retVal=updateNodes(request, clb, lb, entities, filesMap, disabledNode, enabledNodes,warfolderNamesArr, 
										webAppPathOnStagingServer, messages, true, isAnInternalDeploy, jobs);
							}
							/*****************
							 * This is a revert request
							 ******************/	
							else{
								if(log.isDebugEnabled()){
									log.debug(METHOD_NAME+": This is a revert action="+action);
								}
								retVal=updateNodes(request, clb, lb, entities, filesMap, disabledNode, enabledNodes, warfolderNamesArr, 
										webAppPathOnStagingServer, messages, false, isAnInternalDeploy, jobs);
							}
						}
						try {
							//Wait at least 3 seconds to allow .war to be expanded
							Thread.sleep(3000);
						} 
						catch (InterruptedException e) {
							e.printStackTrace();
							log.error(e);
						}
						catch(Throwable e){
							e.printStackTrace();
							log.error(e);								
						}						
						if(null!=clb){
							try {
								clb.close();
							} 
							catch (IOException e) {								
								e.printStackTrace();
								log.error(e);
							}
							catch(Throwable e){
								e.printStackTrace();
								log.error(e);								
							}
						}												
					}
				}
				else{
					if(log.isDebugEnabled()){
						log.debug(METHOD_NAME+": webAppsFolder does not exist, webAppFolder="+webAppsFolderOnStagingServer);
					}
					retVal=false;
					DeployUtility.addABadMessage("error", messages,"<span class='failuremessage'>Error: "+
							webAppPathOnStagingServer+" does not exist, cannot process request.</span>");
				}
			}
			catch(Throwable e){
				e.printStackTrace();
				log.error(METHOD_NAME+": Throwable Error caught");
				log.error(e);
				retVal=false;
				DeployUtility.addABadMessage("error", messages, "<span class='failuremessage'>Error: "+
						"Throwable exception caught error: "+e.getMessage()+"</span>");
			}
		}

		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END: retVal="+retVal);
		}
		return retVal;
	}
	
	private void updateDeployStatusToDone(Long starttime){
		String METHOD_NAME="updateDeployStatusToDone()";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START: starttime="+starttime);
		}
		//Now we need to get all the status related to the start time, if the status value is not failed, then
		//we should mark it as done
		List<Deployjob>jobsProcessed=this.deployjobDao.findDeployJobByStartTime(starttime);

		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": jobsProcessed="+jobsProcessed);
		}				
		if(null!=jobsProcessed){
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": jobsProcessed.size()="+jobsProcessed.size());
			}
			Date dateNow=new Date();
			Long endtime=dateNow.getTime();
			for(Deployjob aDeployJob:jobsProcessed){
				Status status=aDeployJob.getStatus();
				if(log.isDebugEnabled()){
					log.debug(METHOD_NAME+": status.getValue()="+status.getValue());
					log.debug(METHOD_NAME+":(null!=status && (status.getValue().equals(DeployUtility.STATUS_STARTED)||"+
                        "status.getValue().equals(DeployUtility.STATUS_NODE1_STARTED)||"+
						"status.getValue().equals(DeployUtility.STATUS_OTHER_NODES_STARTED))="+
						(null!=status && (status.getValue().equals(DeployUtility.STATUS_STARTED)||
								status.getValue().equals(DeployUtility.STATUS_NODE1_STARTED)||
								status.getValue().equals(DeployUtility.STATUS_OTHER_NODES_STARTED))));
				}
				if(null!=status && (status.getValue().equals(DeployUtility.STATUS_STARTED)||
						status.getValue().equals(DeployUtility.STATUS_NODE1_STARTED)||
						status.getValue().equals(DeployUtility.STATUS_OTHER_NODES_STARTED))){
					if(log.isDebugEnabled()){
						log.debug(METHOD_NAME+": setting status to done");
						log.debug(METHOD_NAME+": setting end time to: "+endtime.toString());
					}
					aDeployJob.setStatus(new Status(DeployUtility.STATUS_DONE));
					aDeployJob.setEndtime(endtime);
				}
			}
			this.deployjobDao.save(jobsProcessed);
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END:");
		}
	}
	
	//Returns false if the move from scpwebapps2 to webapps folder is successful
	private boolean mvWarFromScp2ToWebapps(String prodServer,String fullPathWarScpwebapps1, String fullPathWarScpwebapps2, 
			File selectedWarFile, String aSelectedAppWithWarSuffix)
					throws IOException, JSONException{
		String METHOD_NAME="mvWarFromScp2ToWebapps()";		
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START: prodServer="+prodServer+" fullPathWarScpwebapps1="+fullPathWarScpwebapps1+" fullPathWarScpwebapps2="+
					fullPathWarScpwebapps2+" selectedWarFile="+selectedWarFile+" aSelectedAppWithWarSuffix="+aSelectedAppWithWarSuffix);
		}
		//At this point we have a selected file, if this is a revert just scp it, otherwise if the file exists
		//in the scpwebapps1 folder
		boolean warExistsInScpwebapps1=DeployUtility.doesProdServerContainWarInScpwebapps1(prodServer,fullPathWarScpwebapps1);
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": warExistsInScpwebapps1="+warExistsInScpwebapps1);
		}
		String commandResult=null;		
		boolean retVal=true;
		if(warExistsInScpwebapps1){
			commandResult=DeployUtility.copyWarFromScpwebapps1ToScpwebapps2OverScp(prodServer, fullPathWarScpwebapps1, fullPathWarScpwebapps2);
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": After copy commandResult="+commandResult);
			}
			//The copy command is successful
			if(null!=commandResult && commandResult.toLowerCase().contains("exit-status: 0")){
				//Now we have to test to see if the build time matches the current 
				try{
					String buildTimeOnScp2=DeployUtility.getBuildDateOfWarOnScpwebapps2(prodServer, aSelectedAppWithWarSuffix);
					ZipFile warZipFile=new ZipFile(selectedWarFile);
					String buildTimeFromSelectedWar=DeployUtility.getTimeStamp(warZipFile);
					if(log.isDebugEnabled()){
						log.debug(METHOD_NAME+":buildTimeFromSelectedWar="+buildTimeFromSelectedWar+
								" buildTimeOnScp2="+buildTimeOnScp2);
						if(null!=buildTimeOnScp2 && null!=buildTimeFromSelectedWar){
							log.debug(METHOD_NAME+": buildTimeOnScp2.trim().equalsIgnoreCase(buildTimeFromSelectedWar.trim())="+
									buildTimeOnScp2.trim().equalsIgnoreCase(buildTimeFromSelectedWar.trim()));
						}
					}
					if(null!=buildTimeOnScp2 && null!=buildTimeFromSelectedWar && 
							buildTimeOnScp2.trim().equalsIgnoreCase(buildTimeFromSelectedWar.trim())){
						//Now we have to run the scp command to move the selected War file to the webapps folder
						//The webapps folder should always be /home/docs/Tomcat/latest/ since this on a production machine
						String mvRetVal=DeployUtility.moveWarFromScpwebapps2ToWebappsOverScp(prodServer, fullPathWarScpwebapps2, "/home/docs/Tomcat/latest/webapps/"+aSelectedAppWithWarSuffix);
						if(log.isDebugEnabled()){
							log.debug(METHOD_NAME+":mvRetVal="+mvRetVal);
						}
						if(null!=mvRetVal && mvRetVal.contains("exit-status: 0")){
							retVal=false;
						}
					}
				}
				catch(org.json.JSONException e){
					e.printStackTrace();
				}
				catch(Throwable e){
					e.printStackTrace();
				}
			}
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END: retVal="+retVal);	
		}			
		return retVal;
	}

	private boolean updateNodes(HttpServletRequest request, CloudLoadBalancersApi clb, LoadBalancer lb, Map<String,DocToolsEntity>entities, 
			Map<String, File>filesMap,Node disabledNode,Map<String,Node>enabledNodes,String[]warfolderNamesArr, 
			String webAppPath,Map<String,List<String>>messages, boolean isADeploy, boolean isAnInternalDeploy,
			List<Deployjob>jobs){
		String METHOD_NAME="updateNodes()";
		if(null!=webAppPath && !webAppPath.endsWith("/")){
			webAppPath+="/";
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+":START: webAppPath="+webAppPath+" isADeploy="+isADeploy+" disabledNode="+disabledNode);
		}		
		String prodServer=null;	
		boolean retVal=true;
		String scpUser=(propsFile.getProperty("scpuser","docs")).trim();							
		String prodWebAppsPath=(propsFile.getProperty("prodwebappspath","/home/docs/Tomcat/latest/webapps/")).trim();
		if(!prodWebAppsPath.endsWith("/")){
			prodWebAppsPath+="/";
		}
		String tempWebapps=propsFile.getProperty("webappsTemp","/home/docs/Tomcat/latest/webapps/Temp/");
		if(!tempWebapps.endsWith("/")){
			tempWebapps+="/";
		}
		String passwd=(propsFile.getProperty("docspasswd","Fanatical7")).trim();
		String known_hosts=(propsFile.getProperty("knownhosts","/home/docs/.ssh/known_hosts")).trim();
		String id_rsa=(propsFile.getProperty("idrsa","/home/docs/.ssh/id_rsa")).trim();

		//If there is a disabled node then we should update all the selected .war's for the node that is 
		//disabled we don't have to check to see if retVal is true because if retVal is false then
		//disabledNoded is also null
		if(null!=disabledNode){						
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": entities.size()="+entities.size());
				log.debug(METHOD_NAME+": entities.containsKey(disabledNode.getAddress()="+entities.containsKey(disabledNode.getAddress()));
				log.debug(METHOD_NAME+": entities.size()="+entities.size());
				Set<String>keys=entities.keySet();
				for(String aKey:keys){
					log.debug(METHOD_NAME+": ~~~~~~~aKey="+aKey);
				}
			}
			//Get the prodServer label for the disabled node
			if(entities.size()>0){
				if(entities.containsKey(disabledNode.getAddress())){
					prodServer=entities.get(disabledNode.getAddress()).getLabel();	
					if(log.isDebugEnabled()){
						log.debug(METHOD_NAME+": prodServer="+prodServer);
						log.debug(METHOD_NAME+": retVal="+retVal);
					}
				}
			}														
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": scpUser="+scpUser+" prodServer="+prodServer+" prodWebAppsPath="+
						prodWebAppsPath+" passwd="+passwd+" known_hosts="+ known_hosts+" id_rsa="+id_rsa);
			}
			if(null!=prodServer && !prodServer.isEmpty()){

				//we have a production server to scp, now we should scp all the selected files
				//We should deploy to the InternalProd server (Set shouldDeployToInternal to true) because we should
				//only update the InternalProd server once, and that should have been done before calling updateAllNodes
				retVal=scpAllSelectedFilesToAProdServerNode(prodServer,request, warfolderNamesArr, filesMap, messages, isADeploy,jobs);	
				//Deployments require backups while reverts do not
				if(log.isDebugEnabled()){
					log.debug(METHOD_NAME+": retVal="+retVal);
				}			
				//if scping All selected files fail, we have to back out now
				if(!retVal){
					Date dateNow=new Date();
					Long endtime=dateNow.getTime();
					this.markJobsEndTime(jobs, endtime);
					this.updateJobs(jobs, DeployUtility.STATUS_FAILED, "Scp failed for first prod node: "+prodServer);

					//Before we enable the node, we have to stop and start tomcat
					if(log.isDebugEnabled()){
						log.debug(METHOD_NAME+": We should try shutting down tomcat no matter what");
					}
					DeployUtility.shutDownTomcat(prodServer, messages, false);
					try {
						//Wait to give the server time to stop
						Thread.sleep((5000));
					} 
					catch (InterruptedException e1) {
						e1.printStackTrace();
						log.debug(e1);
					}						
					boolean isTomcatRunning=DeployUtility.isTomcatRunning(prodServer, isAnInternalDeploy);

					if(log.isDebugEnabled()){
						log.debug(METHOD_NAME+": isTomcatRunning="+isTomcatRunning);
					}
					if(!isTomcatRunning){
						if(log.isDebugEnabled()){
							log.debug(METHOD_NAME+": Starting up tomcat");
						}
						String returncode=DeployUtility.startUpTomcat(prodServer, messages, false);
						if(log.isDebugEnabled()){
							log.debug(METHOD_NAME+": returncode="+returncode);
						}
						if(null!=returncode){
							if(returncode.toLowerCase().contains("exit-status: 0")){
								//We enable the node only if tomcat successfully started								
								enableNodeAndChecksForDisabledNode(disabledNode, clb, lb, entities, enabledNodes, messages);
							}
							else{
								dateNow=new Date();
								endtime=dateNow.getTime();
								this.markJobsEndTime(jobs, endtime);
								this.updateJobs(jobs, DeployUtility.STATUS_FAILED, "Scp failed for first prod node: "+
										prodServer+" and restarting tomcat server failed");
							}
						}
					}
					try {
						//Wait for SLEEP_AFTER_STARTUP secondsd
						Thread.sleep((SLEEP_AFTER_STARTUP));
					} 
					catch (InterruptedException e1) {
						e1.printStackTrace();
						log.debug(e1);
					}
					if(log.isDebugEnabled()){
						log.debug(METHOD_NAME+": finish waiting for "+SLEEP_AFTER_STARTUP+" seconds for tomcat server to start up");
					}																
				}
				//scping all the files to the disabled node was successful
				else{
					//This is a deploy then we have to do two things:
					//1. Backup the selected war's
					//2. Check to see if there is an internal version and scp the internal version to the
					//   internal prod server
					if(isADeploy){						
						String prodServerInternal=(propsFile.getProperty("prodserverinternal","docs-internal.rackspace.com")).trim();
						//Iterate through, back up each .war and deploy it to the internal server if needed
						for(String aSelectedApp:warfolderNamesArr){
							if(log.isDebugEnabled()){
								log.debug(METHOD_NAME+": backing up aSelectedWebApp="+aSelectedApp);
							}
							String aSelectedAppWithWarSuffix=aSelectedApp+".war";
							if(log.isDebugEnabled()){
								log.debug(METHOD_NAME+": aSelectedAppwithWarSuffix="+aSelectedAppWithWarSuffix);
							}
							File selectedWarFile=null;
							//The .war file should exist in the map
							if(filesMap.containsKey(aSelectedAppWithWarSuffix)){
								selectedWarFile=filesMap.get(aSelectedAppWithWarSuffix);
							}
							else{
								//The .war file does not exist in the webapps folder, instantiate the file and put it in the map
								selectedWarFile=new File(webAppPath+aSelectedAppWithWarSuffix);
								filesMap.put(aSelectedAppWithWarSuffix, selectedWarFile);				    	
							}

							try {
								//Check to see if aSelectedApp has an internal folder version, for example the book: cm-v1.0-cm-devguide has
								//internal version: cm-v1.0-cm-devguide-internal on the internal production server
								String hasinternalfolder=DeployUtility.doesWarHaveInternalFolderOnInternalDocsServer(aSelectedApp);
								if(log.isDebugEnabled()){
									log.debug(METHOD_NAME+": hasinteralfolder="+hasinternalfolder);
								}
								//there is no internal folder so we must scp to internal prod too 
								if(null!=hasinternalfolder && hasinternalfolder.equalsIgnoreCase("false")){	
									if(log.isDebugEnabled()){
										log.debug(METHOD_NAME+": scping selectedWarFile="+selectedWarFile+" to internal server: "+
												prodServerInternal);
									}
									this.updateJobForSelectedWar(jobs, aSelectedAppWithWarSuffix, DeployUtility.STATUS_TO_INTERNAL_FROM_EXTERNAL, null);

									String selectedWarOnProdStr=prodWebAppsPath+aSelectedAppWithWarSuffix;

									//First back it up to the temp folder, if it fails just ignore the failure
									DeployUtility.backupSelectedWar(prodServerInternal, messages, selectedWarOnProdStr, tempWebapps);				
									retVal=DeployUtility.scpFileToServer(scpUser, prodServerInternal, selectedWarFile, aSelectedAppWithWarSuffix, prodWebAppsPath, passwd, known_hosts, id_rsa, messages);
									if(log.isDebugEnabled()){
										log.debug(METHOD_NAME+": retVal="+retVal);
									}
									//The scp failed, halt execution
									if(!retVal){
										this.updateJobForSelectedWar(jobs, aSelectedAppWithWarSuffix, DeployUtility.STATUS_FAILED, null);
										DeployUtility.addABadMessage("error", messages,"<span class='failuremessage'>Error: failed while trying to update "+
												"internal production server from an external deploy</span>");
										return false;
									}
									else{
										Thread.sleep(5000);
										String tempSelectedFile=tempWebapps+aSelectedAppWithWarSuffix;
										String webappsBackupFolder=propsFile.getProperty("webappsBackup","/home/docs/Tomcat/latest/webappsBackup/");										
										if(!webappsBackupFolder.endsWith("/")){
											webappsBackupFolder+="/";
										}
										if(log.isDebugEnabled()){
											log.debug(METHOD_NAME+": tempSelectedFile="+tempSelectedFile);
											log.debug(METHOD_NAME+": webappsBackupFolder="+webappsBackupFolder);
										}
										//Now put the temp file into backup folder
										DeployUtility.backupSelectedWar(prodServerInternal, messages, tempSelectedFile, webappsBackupFolder);
									}
								}
							} 
							catch (MalformedURLException e1) {
								e1.printStackTrace();
								log.error(e1);
								retVal=false;
								DeployUtility.addABadMessage("error", messages,"<span class='failuremessage'>Error: MalformedURLException could "+
										"not scp file: "+aSelectedAppWithWarSuffix+" message:"+e1.getMessage()+".</span>");
								Date dateNow=new Date();
								Long endtime=dateNow.getTime();
								this.markJobsEndTime(jobs, endtime);
								this.updateJobForSelectedWar(jobs, aSelectedAppWithWarSuffix, DeployUtility.STATUS_FAILED, 
										"MalformedURLException caught in "+METHOD_NAME+" scp failed with message: "+e1.getMessage());
							} 
							catch (ProtocolException e1) {
								e1.printStackTrace();
								log.error(e1);
								retVal=false;
								DeployUtility.addABadMessage("error", messages,"<span class='failuremessage'>Error: ProtocolException could not "+
										"scp file: "+aSelectedAppWithWarSuffix+" message:"+e1.getMessage()+".</span>");
								Date dateNow=new Date();
								Long endtime=dateNow.getTime();
								this.markJobsEndTime(jobs, endtime);
								this.updateJobForSelectedWar(jobs, aSelectedAppWithWarSuffix, DeployUtility.STATUS_FAILED, 
										"ProtocolException caught in "+METHOD_NAME+" scp failed with message: "+e1.getMessage());
							} 
							catch (IOException e1) {
								e1.printStackTrace();
								log.error(e1);
								retVal=false;
								DeployUtility.addABadMessage("error", messages,"<span class='failuremessage'>Error: IOException could not scp "+
										"file: "+aSelectedAppWithWarSuffix+" message:"+e1.getMessage()+".</span>");
								Date dateNow=new Date();
								Long endtime=dateNow.getTime();
								this.markJobsEndTime(jobs, endtime);
								this.updateJobForSelectedWar(jobs, aSelectedAppWithWarSuffix, DeployUtility.STATUS_FAILED, 
										"IOException caught in "+METHOD_NAME+" scp failed with message: "+e1.getMessage());
							}
							catch(InterruptedException e1){
								//No need for error message, just skip the sleep
								e1.printStackTrace();
								log.error(e1);
								Date dateNow=new Date();
								Long endtime=dateNow.getTime();
								this.markJobsEndTime(jobs, endtime);
								this.updateJobForSelectedWar(jobs, aSelectedAppWithWarSuffix, DeployUtility.STATUS_FAILED, 
										"InterruptedException caught in "+METHOD_NAME+" scp failed with message: "+e1.getMessage());
							}
							catch(JSONException e1){
								e1.printStackTrace();
								log.error(e1);
								Date dateNow=new Date();
								Long endtime=dateNow.getTime();
								this.markJobsEndTime(jobs, endtime);
								this.updateJobForSelectedWar(jobs, aSelectedAppWithWarSuffix, DeployUtility.STATUS_FAILED, 
										"JSONException caught in "+METHOD_NAME+" scp failed with message: "+e1.getMessage());							
							}
							catch(Throwable e1){
								e1.printStackTrace();
								log.error(e1);
								Date dateNow=new Date();
								Long endtime=dateNow.getTime();
								this.markJobsEndTime(jobs, endtime);
								this.updateJobForSelectedWar(jobs, aSelectedAppWithWarSuffix, DeployUtility.STATUS_FAILED, 
										"Throwable caught in "+METHOD_NAME+" scp failed with message: "+e1.getMessage());								
							}	
						}
						//touch the war only once whether the above code failed or succeded, this allows tomcat to refresh the ROOT.war
						String rootFullPath=propsFile.getProperty("webappfolder","/home/docs/Tomcat/latest/webapps/");
						rootFullPath=webAppPath+"ROOT.war";
						if(log.isDebugEnabled()){
							log.debug(METHOD_NAME+": touching ROOT.war rootFullPath="+rootFullPath);
						}
						DeployUtility.touchRoot(scpUser, passwd, known_hosts, id_rsa, prodServerInternal, rootFullPath);
					}
					//Before we enable the node, we have to stop and start tomcat
					if(log.isDebugEnabled()){
						log.debug(METHOD_NAME+": We should try shutting down tomcat no matter what");
					}
					DeployUtility.shutDownTomcat(prodServer, messages, false);
					try {
						//Wait to give the server time to stop
						Thread.sleep((5000));
					} 
					catch (InterruptedException e1) {
						e1.printStackTrace();
						log.error(e1);
					}					
					boolean isTomcatRunning=DeployUtility.isTomcatRunning(prodServer, isAnInternalDeploy);

					if(log.isDebugEnabled()){
						log.debug(METHOD_NAME+": isTomcatRunning="+isTomcatRunning);
					}
					if(!isTomcatRunning){
						if(log.isDebugEnabled()){
							log.debug(METHOD_NAME+": Starting up tomcat");
						}
						DeployUtility.startUpTomcat(prodServer, messages, false);
					}
					try {						
						Thread.sleep(SLEEP_AFTER_STARTUP);
					} 
					catch (InterruptedException e1) {
						e1.printStackTrace();
						log.error(e1);
					}
					if(log.isDebugEnabled()){
						log.debug(METHOD_NAME+": finish waiting for "+SLEEP_AFTER_STARTUP+" seconds for tomcat server to start up");
					}
					retVal=enableNodeAndChecksForDisabledNode(disabledNode, clb, lb, entities, enabledNodes, messages);
					//At this point we have updated one node, now we need to update the remaining nodes
					if(retVal){

						if(log.isDebugEnabled()){
							log.debug(METHOD_NAME+": Before removal enabledNodes.size()="+enabledNodes.size());
						}
						//We first remove the disabledNode from the enabledNode Map
						enabledNodes.remove(disabledNode.getAddress());
						if(log.isDebugEnabled()){
							log.debug(METHOD_NAME+": After removal enabledNodes.size()="+enabledNodes.size());
						}							
						try {
							if(isADeploy){
								retVal=updateAllNodes(clb, lb, entities, enabledNodes, request, warfolderNamesArr, filesMap, 
										messages, true, isAnInternalDeploy, jobs);
							}
							else{
								retVal=updateAllNodes(clb, lb, entities, enabledNodes, request, warfolderNamesArr, filesMap, 
										messages, false, isAnInternalDeploy, jobs);
							}
						} 
						catch (JCloudsException e) {
							e.printStackTrace();
							log.error(e);
							retVal=false;
							//Dont add a failure message, because if we get an exception here, the deploy would have went through, but 
							//we were not able to disable/enable entity checks, this would result in email notifications that the site
							//was done, but deployment could have been successful
							//DeployUtility.addABadMessage("error", messages,"<span class='failuremessage'>Deployment to docs production " +
							//"was successful, but one or more of the balanced nodes was not properly updated error message: "+
							//e.getMessage()+".</span>");
						}									
					}
					else{
						DeployUtility.addABadMessage("error", messages,"<span class='failuremessage'>Deployment to docs production " +
								"failed, although the selected war files were transfered to server node: "+prodServer+", the load balancer"+
								"could NOT enable the node: "+prodServer+". Contact administrator with this message.</span>");
					}

				}
			}
			else{
				DeployUtility.addABadMessage("error", messages,"<span class='failuremessage'>Deployment to docs production "+
						"failed, could not find the production server node to update disabledNode=</span>"+disabledNode);
				retVal=false;
			}
		}
		else{
			DeployUtility.addABadMessage("error", messages,"<span class='failuremessage'>Deployment to docs production "+
					"failed, could not disable the first node:/span>");
			retVal=false;
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+":END:");
		}
		return retVal;
	}

	private void markJobsEndTime(List<Deployjob>jobs,Long endtime){
		if(null!=jobs){
			for(Deployjob aJob:jobs){
				aJob.setEndtime(endtime);				
			}
		}
	}

	private void updateJobForSelectedWar(List<Deployjob>jobs, String selectedWar, String status, String failreason){
		String METHOD_NAME="updateJobForSelectedWar()";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START: status="+status);
		}	

		for(Deployjob aDeployJob:jobs){
			String aWarName=aDeployJob.getWarname();
			if(null!=aWarName && null!=selectedWar && aWarName.equals(selectedWar)){
				if(log.isDebugEnabled()){
					log.debug(METHOD_NAME+": Before resetting status aDeployJob:"+aDeployJob);
				}
				aDeployJob.setStatus(new Status(status));
				if(null!=failreason){
					aDeployJob.setFailreason(failreason);
				}
				if(log.isDebugEnabled()){
					log.debug(METHOD_NAME+": After resetting status aDeployJob:"+aDeployJob);
				}
				//DeployJobDaoImpl jobImp=new DeployJobDaoImpl();
				//jobImp.setSessionFactory(sessionFactory);//setSession(session);
				//List<Deployjobs>aJobToUpdate=new ArrayList<Deployjobs>();
				//aJobToUpdate.add(aDeployJob);
				this.deployjobDao.update(aDeployJob);//jobImp.update(aJobToUpdate);
				break;
			}
		}				
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END:");
		}
	}

	private void updateJobs(List<Deployjob>jobs, String status, String failreason){
		String METHOD_NAME="updateJobs()";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START: status="+status);
			log.debug(METHOD_NAME+": Iterating through jobs:");
		}

		for(Deployjob aDeployJob:jobs){
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": aDeployJob:"+aDeployJob);
				log.debug(METHOD_NAME+": updating status to: "+status);
			}
			
			aDeployJob.setStatus(new Status(status));
			if(null!=failreason){
				aDeployJob.setFailreason(failreason);
			}
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": updating jobs");
		}
		this.deployjobDao.update(jobs);
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END:");
		}
	}
	
	private boolean scpAllSelectedFilesToAProdServerNode(String serverNode, HttpServletRequest request, String[] warfolderNamesArr, 
			Map<String, File>filesMap, Map<String, List<String>>messages, boolean isADeploy,
			List<Deployjob>jobs){
		String METHOD_NAME="scpAllSelectedFilesToAProdServerNode()";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START: serverNode="+serverNode+" isADepoloy="+isADeploy);
		}
		boolean retVal=true;
		String webAppPath=this.servletCtx.getRealPath("..");
		if(!webAppPath.endsWith("/")){
			webAppPath+="/";
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": webAppPath="+webAppPath);
		}
		String scpUser=(propsFile.getProperty("scpuser","docs")).trim();							
		String prodWebAppsPath=(propsFile.getProperty("prodwebappspath","/home/docs/Tomcat/latest/webapps/")).trim();
		if(!prodWebAppsPath.endsWith("/")){
			prodWebAppsPath+="/";
		}
		String internalDeploy=request.getParameter("internaldeploy");
		boolean isAnInternalDeploy=false;
		if(null!=internalDeploy && internalDeploy.equalsIgnoreCase("true")){
			isAnInternalDeploy=true;					
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": internalDeploy="+internalDeploy+" isAnInternalDeploy="+isAnInternalDeploy);
		}
		if(isAnInternalDeploy){
			prodWebAppsPath=(propsFile.getProperty("webappfolderinternal","/home/docs/Tomcat/internal/latest/webapps/")).trim();
		}
		String passwd=(propsFile.getProperty("docspasswd","Fanatical7")).trim();
		String known_hosts=(propsFile.getProperty("knownhosts","/home/docs/.ssh/known_hosts")).trim();
		String id_rsa=(propsFile.getProperty("idrsa","/home/docs/.ssh/id_rsa")).trim();


		if(!prodWebAppsPath.endsWith("/")){
			prodWebAppsPath+="/";	
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": scpUser="+scpUser+" prodWebAppsPath="+
					prodWebAppsPath+" passwd="+passwd+" known_hosts="+ known_hosts+" id_rsa="+id_rsa);
		}
		int count=1;
		String scpwebapps1Dir=propsFile.getProperty("scpwebapps1","/home/docs/Tomcat/latest/scpwebapps1/");
		String scpwebapps2Dir=propsFile.getProperty("scpwebapps2","/home/docs/Tomcat/latest/scpwebapps2/");
		String productionWebAppsFolder=propsFile.getProperty("prodwebappspath","/home/docs/Tomcat/latest/webapps/");					
		if(!productionWebAppsFolder.endsWith("/")){
			productionWebAppsFolder+="/";
		}					
		String webappsBackup=propsFile.getProperty("webappsBackup","/home/docs/Tomcat/latest/webappsBackup/");
		if(!webappsBackup.endsWith("/")){
			webappsBackup+="/";
		}
		String tempWebapps=propsFile.getProperty("webappsTemp","/home/docs/Tomcat/latest/webapps/Temp/");
		if(!tempWebapps.endsWith("/")){
			tempWebapps+="/";
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": productionWebAppsFolder="+productionWebAppsFolder);
			log.debug(METHOD_NAME+": webappsBackup="+webappsBackup);
			log.debug(METHOD_NAME+": tempWebapps="+tempWebapps);
		}
		for(String aSelectedApp:warfolderNamesArr){
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": aSelectedWebApp="+aSelectedApp);
			}
			String aSelectedAppWithWarSuffix=aSelectedApp+".war";

			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": aSelectedAppwithWarSuffix="+aSelectedAppWithWarSuffix);
			}
			File selectedWarFile=null;

			String fullPathWarScpwebapps1=scpwebapps1Dir+aSelectedAppWithWarSuffix;		
			String fullPathWarScpwebapps2=scpwebapps2Dir+aSelectedAppWithWarSuffix;
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": fullPathWarScpwebapps1="+fullPathWarScpwebapps1+
						" fullPathWarScpwebapps2="+fullPathWarScpwebapps2);
				log.debug(METHOD_NAME+": isADeploy="+isADeploy);
			}			
			//This is a deploy request
			if(isADeploy){		
				//The .war file should exist in the map
				if(filesMap.containsKey(aSelectedAppWithWarSuffix)){
					selectedWarFile=filesMap.get(aSelectedAppWithWarSuffix);
				}
				else{
					//The .war file does not exist in the webapps folder, instantiate the file and put it in the map
					selectedWarFile=new File(webAppPath+aSelectedAppWithWarSuffix);
					filesMap.put(aSelectedAppWithWarSuffix, selectedWarFile);				    	
				}
				if(log.isDebugEnabled()){
					log.debug(METHOD_NAME+": selectedWarFile.exists()="+selectedWarFile.exists());
				}	

				String productionWebAppsFile=productionWebAppsFolder+aSelectedAppWithWarSuffix;
				if(log.isDebugEnabled()){
					log.debug(METHOD_NAME+": productionWebAppsFile="+productionWebAppsFile);
				}				
				String tempSelectedFileStr=tempWebapps+aSelectedAppWithWarSuffix;
				
				//first back up the .war in the temp directory
				DeployUtility.backupSelectedWar(serverNode, messages, productionWebAppsFile, tempWebapps);
				if(log.isDebugEnabled()){
					log.debug(METHOD_NAME+": tempSelectedFileStr="+tempSelectedFileStr);
				}
				//now that we have backedup the file in the webapps/Temp folder, we can continue
				try{
					boolean continueWithScp=true;
					//Only try to move from scpwebapps1 on production if this is a deploy
					continueWithScp=mvWarFromScp2ToWebapps(serverNode, fullPathWarScpwebapps1, fullPathWarScpwebapps2, 
							selectedWarFile, aSelectedAppWithWarSuffix);

					if(log.isDebugEnabled()){
						log.debug(METHOD_NAME+": continueWithScp="+continueWithScp);
					}
					//The move was not successful or this is a revert we have to try to scp the file
					if(continueWithScp){
						retVal=DeployUtility.scpFileToServer(scpUser, serverNode, selectedWarFile, aSelectedAppWithWarSuffix, prodWebAppsPath, passwd, known_hosts, id_rsa, messages);
						if(!retVal){
							Date dateNow=new Date();
							Long endtime=dateNow.getTime();
							this.markJobsEndTime(jobs, endtime);
							this.updateJobForSelectedWar(jobs, aSelectedAppWithWarSuffix, DeployUtility.STATUS_FAILED, "Scp failed for node: "+
									serverNode+" for .war: "+aSelectedAppWithWarSuffix);								


							if(log.isDebugEnabled()){
								log.debug(METHOD_NAME+": Error!!!! some of the scp failed, backing out of all scp");
								log.debug(METHOD_NAME+": aSelectedWebApp="+aSelectedApp);
							}

							String tempBackupPath=propsFile.getProperty("webappsTemp","/home/docs/Tomcat/latest/webapps/Temp/");
							if(!tempBackupPath.endsWith("/")){
								tempBackupPath+="/";
							}
							String tempWarFileFullPath=tempBackupPath+aSelectedAppWithWarSuffix;
							if(log.isDebugEnabled()){
								log.debug(METHOD_NAME+": aSelectedAppwithWarSuffix="+aSelectedAppWithWarSuffix);
								log.debug(METHOD_NAME+": tempBackupPath="+tempBackupPath);
								log.debug(METHOD_NAME+": tempWarFileFullPath="+tempWarFileFullPath);
							}
							String key=aSelectedApp+"-bad";						
							List<String>messagesList=messages.get(key);
							if(null==messagesList){
								messagesList=new ArrayList<String>();
								messages.put(key, messagesList);
							}
							messagesList.add("<span class='failuremessage'>Deploy failed, restoring "+
									aSelectedAppWithWarSuffix+" from"+tempWarFileFullPath+"</span>");

							DeployUtility.backupSelectedWar(serverNode, messages, tempWarFileFullPath, webAppPath);

							if(log.isDebugEnabled()){
								log.debug(METHOD_NAME+": END: retVal="+false);
							}
							return false;
						}
						else{
							try{
								//We need to pause SLEEP_INTERVAL seconds in between each deploy to give the tomcat server 
								//time to refresh ROOT.war
								Thread.sleep(CONNECT_TIMEOUT);
							}
							catch(InterruptedException e){
								e.printStackTrace();
								log.error(e);
								retVal=false;
								if(log.isDebugEnabled()){
									log.debug(METHOD_NAME+": END: retVal="+retVal);
								}							
								DeployUtility.addABadMessage("error", messages,"<span class='failuremessage'>Error: InterruptedException could "+
										"not scp file: "+aSelectedAppWithWarSuffix+" message:"+e.getMessage()+".</span>");
							}
						}
					}
				}
				catch(IOException e){
					log.error(METHOD_NAME+": IOException mvWarFromScp2ToWebapps failed for serverNode="+serverNode+
							" fullPathWarScpwebapps1="+fullPathWarScpwebapps1+" fullPathWarScpwebapps2="+fullPathWarScpwebapps2+
							" selectedWarFile="+selectedWarFile+" aSelectedAppWithWarSuffix="+aSelectedAppWithWarSuffix);
					log.error(e);

					DeployUtility.addABadMessage("error", messages,"<span class='failuremessage'>Error: "+
							" IOException when trying move method: mvWarFromScp2ToWebapps</span>");	

					Date dateNow=new Date();
					Long endtime=dateNow.getTime();
					this.markJobsEndTime(jobs, endtime);
					this.updateJobForSelectedWar(jobs, aSelectedAppWithWarSuffix, DeployUtility.STATUS_FAILED, "Scp failed for node: "+
							serverNode+" for .war: "+aSelectedAppWithWarSuffix);

					log.error(METHOD_NAME+": END: retVal=false~");

					return false;
				}
				catch(JSONException e){
					log.error(METHOD_NAME+": JSONException  mvWarFromScp2ToWebapps failed for server: "+serverNode+
							" fullPathWarScpwebapps1="+fullPathWarScpwebapps1+" fullPathWarScpwebapps2="+fullPathWarScpwebapps2+
							" selectedWarFile="+selectedWarFile+" aSelectedAppWithWarSuffix="+aSelectedAppWithWarSuffix);
					log.error(e);

					DeployUtility.addABadMessage("error", messages,"<span class='failuremessage'>Error: "+
							" JSONException when trying get build time on server: "+serverNode+"</span>");	
					Date dateNow=new Date();
					Long endtime=dateNow.getTime();
					this.markJobsEndTime(jobs, endtime);
					this.updateJobForSelectedWar(jobs, aSelectedAppWithWarSuffix, DeployUtility.STATUS_FAILED, "Scp failed for node: "+
							serverNode+" for .war: "+aSelectedAppWithWarSuffix);

					log.error(METHOD_NAME+": END: retVal=false~");

					return false;
				}					
				catch(Throwable e){
					log.error(METHOD_NAME+": ThrowableException  mvWarFromScp2ToWebapps failed for server="+serverNode+
							" fullPathWarScpwebapps1="+fullPathWarScpwebapps1+" fullPathWarScpwebapps2="+fullPathWarScpwebapps2+
							" selectedWarFile="+selectedWarFile+" aSelectedAppWithWarSuffix="+aSelectedAppWithWarSuffix);
					log.error(e);

					DeployUtility.addABadMessage("error", messages,"<span class='failuremessage'>Error: "+
							" Throwable when trying internal move method: mvWarFromScp2ToWebapps</span>");	
					Date dateNow=new Date();
					Long endtime=dateNow.getTime();
					this.markJobsEndTime(jobs, endtime);
					this.updateJobForSelectedWar(jobs, aSelectedAppWithWarSuffix, DeployUtility.STATUS_FAILED, "Scp failed for node: "+
							serverNode+" for .war: "+aSelectedAppWithWarSuffix);

					log.error(METHOD_NAME+": END: retVal=false~");

					return false;
				}	
				//If we reach this point then everything worked, we need to add a success message and
				//also copy the selected war file from the temp folder into the webappsBackup folder
				if(retVal){
					if(log.isDebugEnabled()){
						log.debug(METHOD_NAME+": adding message count="+count +" for aSelectedAppWithWarSuffix="+aSelectedAppWithWarSuffix+ " isADeploy="+isADeploy);
					}
					++count;
					DeployUtility.addSuccessMessages(request, aSelectedAppWithWarSuffix, messages, isADeploy);
					String tempWebappsSelectedFileStr=tempWebapps+aSelectedAppWithWarSuffix;
					if(log.isDebugEnabled()){
						log.debug(METHOD_NAME+": tempWebappsSelectedFileStr="+tempWebappsSelectedFileStr);
						log.debug(METHOD_NAME+": webappsBackup="+webappsBackup);
					}						
					DeployUtility.backupSelectedWar(serverNode, messages, tempWebappsSelectedFileStr, webappsBackup);
				}					

			}
			//This is a revert request
			else{
				String prodSelectedWarStr=prodWebAppsPath+aSelectedAppWithWarSuffix;
				//First back it up in case we have to back out of the change
				DeployUtility.backupSelectedWar(serverNode, messages, prodSelectedWarStr, tempWebapps);

				//First try to revert it from the production Node
				String webappsBackupSelectedFileStr=webappsBackup+aSelectedAppWithWarSuffix;
				if(log.isDebugEnabled()){
					log.debug(METHOD_NAME+": prodSelectedWarStr="+prodSelectedWarStr);
					log.debug(METHOD_NAME+": webappsBackupSelectedFileStr="+webappsBackupSelectedFileStr);
				}
				String revertResult=DeployUtility.backupSelectedWar(serverNode, messages, webappsBackupSelectedFileStr, prodWebAppsPath);
				if(log.isDebugEnabled()){
					log.debug(METHOD_NAME+": revertResult="+revertResult);					
				}
				//The first revert was not successful, try to scp from staging to prod
				if(!revertResult.toLowerCase().contains("exit-status: 0")){

					File selectedFile=filesMap.get(aSelectedAppWithWarSuffix);
					if(log.isDebugEnabled()){
						log.debug(METHOD_NAME+": selectedFile.exists()="+selectedFile.exists());
					}
					//Make sure the file exists
					if(selectedFile.exists()){
						boolean scpResults=DeployUtility.scpFileToServer(scpUser, serverNode, selectedWarFile, 
								aSelectedAppWithWarSuffix, prodWebAppsPath, passwd, known_hosts, id_rsa, messages);
						//The scp did not work back out of the change
						if(!scpResults){
							String tempWebappsSelected=tempWebapps+aSelectedAppWithWarSuffix;
							DeployUtility.backupSelectedWar(serverNode, messages, tempWebappsSelected, prodWebAppsPath);
						}						
					}									
				}
				else{
					//The revert is successful, there is nothing more to do
				}
			}		
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END: retVal="+retVal);
		}
		return retVal;
	}
	
	private boolean enableNodeAndChecksForDisabledNode(Node disabledNode, 
			CloudLoadBalancersApi clb,
			LoadBalancer lb,
			Map<String,
			DocToolsEntity>entities,
			Map<String,Node>enabledNodes,
			Map<String,List<String>>messages)
	{
		String METHOD_NAME="enableNodeAndChecksForDisabledNode()";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START: ");
			log.debug(METHOD_NAME+": disabledNode="+disabledNode);
			for(Node aNode:enabledNodes.values()){
				log.debug("~~~~~aNode="+aNode);
			}
		}
		boolean retVal=true;
		//So disableAnEnabledNode was called, we need to enable the node, then we should disable all the 
		//other nodes and scp the file to them too.
		try{							
			//First enable the node
			JCloudsUtility.enableNode(clb, lb, disabledNode.getId());
			//The enable the checks
			DeployUtility.enableEntityChecks(entities, disabledNode);
		} 
		catch(JCloudsException e){
			//If enable the checks fail we just log the error 
			e.printStackTrace();
			log.error(e);
			log.error(METHOD_NAME+": ERROR!!!! First JCloudsException caught message: "+e.getMessage());
			log.error(METHOD_NAME+"Could not enable the node, trying one more time.");

		}
		catch (TimeoutException e) {
			e.printStackTrace();
			log.error(e);
			log.error(METHOD_NAME+": ERROR!!!! First TimeoutException caught message: "+e.getMessage());
			log.error(METHOD_NAME+"Could not enable the node, trying one more time.");

			//If we time out, try one more time
			try{
				JCloudsUtility.enableNode(clb, lb, disabledNode.getId());
				DeployUtility.enableEntityChecks(entities, disabledNode);
			}
			catch(JCloudsException e3){
				log.error(METHOD_NAME+": second try to enable all checks for an entity failed e3.getMessage()="+e3.getMessage());

				e3.printStackTrace();
				log.error(e3);				
				//We arrived here because we could not successfully enable the entity checks, although this is not optimal
				//we should NOT add an error message to the messages List, because as long as the node was successfully
				//enabled, then we are good

				//				retVal=false;
				//				DeployUtility.addABadMessage("error", messages,"<span class='failuremessage'>Deployment to docs production "+
				//				"failed, with error message: "+e3.getMessage()+" Could not enable disabledNode: "+disabledNode+".</span>");
			}
			catch (TimeoutException e1) {
				e1.printStackTrace();
				log.error(e1);
				log.error(METHOD_NAME+": ERROR!!!! Second TimeoutException caught message: "+e1.getMessage());
				log.error(METHOD_NAME+"Could not enable disabledNode: "+disabledNode+" do not move on");
				retVal=false;
				DeployUtility.addABadMessage("error", messages,"<span class='failuremessage'>Deployment to docs production "+
						"failed, with error message: "+e1.getMessage()+" Could not enable disabledNode: "+disabledNode+".</span>");				
			}
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END: enabledNodes="+enabledNodes+ " retVal="+retVal);
		}
		return retVal;
	}
	
	private boolean updateAllNodes(CloudLoadBalancersApi clb,
			LoadBalancer loadBalancer,Map<String,DocToolsEntity>entities, Map<String, Node>enabledNodes, 
			HttpServletRequest request, String[] warfolderNamesArr, Map<String, File>filesMap, 
			Map<String,List<String>> messages, boolean isADeploy, boolean isAnInternalDeploy,
			List<Deployjob>jobs)throws JCloudsException{	
		String METHOD_NAME="updateAllNodes()";
		boolean retVal=true;
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START:");
		}
		//update all the statuses indicating that the other nodes have started
		this.updateJobs(jobs, DeployUtility.STATUS_OTHER_NODES_STARTED, null);
		if(null!=enabledNodes){
			//String internalProdServer=(propsFile.getProperty("prodserverinternal","docs-internal.rackspace.com")).trim();

			//First iterate through all the nodes and disable them all 
			for(Node aNode:enabledNodes.values()){

				//Just try to disable the entity checks. If for some reason the disable fails, just continue
				DeployUtility.disableEntityChecks(entities, aNode);
				try {
					JCloudsUtility.disableNode(clb, loadBalancer, aNode.getId());
				} 
				catch (TimeoutException e) {
					e.printStackTrace();
					log.error(e);
					log.error(METHOD_NAME+": trying to disable aNode: "+aNode+" one more time");
					//first we try one more time
					try{
						//First wait for a little
						Thread.sleep(READ_TIMEOUT);
						JCloudsUtility.disableNode(clb, loadBalancer, aNode.getId());
					}
					catch(TimeoutException e1){
						DeployUtility.addABadMessage("error", messages,"<span class='failuremessage'>Could not disalbe node: "+aNode+
								" request proceded, but there may be errors. Caught error message:"+e1.getMessage()+
								"Contact administrator with this message.");
						log.error(e1);
						retVal=false;
					}
					catch(InterruptedException e1){
						e1.printStackTrace();
						log.error(e1);
					}

				}
				if(log.isDebugEnabled()){
					log.debug(METHOD_NAME+" Just disabled node: "+aNode);
				}				
			}
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": Now we are trying to update each war to the respective nodes:");
			}
			//Even if we can't disable the active nodes, go ahead and update each node
			String prodServer=null;			
			//Now we need to update each node and bring them up one at a time
			for(Node aNode:enabledNodes.values()){
				DocToolsEntity anEntity=entities.get(aNode.getAddress());
				String disabledUrl=anEntity.getLabel();
				if(log.isDebugEnabled()){
					log.debug(METHOD_NAME+" Scping to disabledUrl: "+disabledUrl);
				}
				prodServer=entities.get(aNode.getAddress()).getLabel();	
				if(log.isDebugEnabled()){
					log.debug(METHOD_NAME+":~~~~~prodServer="+prodServer);
				}
				if(null!=prodServer && !prodServer.isEmpty()){

					retVal=scpAllSelectedFilesToAProdServerNode(prodServer,request, warfolderNamesArr, 
							filesMap, messages, isADeploy, jobs);

					if(log.isDebugEnabled()){
						log.debug(METHOD_NAME+": retVal="+retVal);
					}
					//Even if retVal is false which indicates that scpAllSelectedFilesToAProdServerNode
					//failed, we should restart the server, because even with a failure, we would have
					//backed out of the change and a server refresh would be helpful
					//Before we enable the node, we have to stop and start tomcat
					if(log.isDebugEnabled()){
						log.debug(METHOD_NAME+": We should try shutting down tomcat no matter what");
					}
					DeployUtility.shutDownTomcat(prodServer, messages, false);
					try {
						//Wait to give the server time to stop
						Thread.sleep(5000);
					} 
					catch (InterruptedException e1) {
						e1.printStackTrace();
						log.debug(e1);
					}
					boolean isTomcatRunning=DeployUtility.isTomcatRunning(prodServer, isAnInternalDeploy);

					if(log.isDebugEnabled()){
						log.debug(METHOD_NAME+": isTomcatRunning="+isTomcatRunning);
					}
					if(!isTomcatRunning){
						if(log.isDebugEnabled()){
							log.debug(METHOD_NAME+": Starting up tomcat");
						}
						DeployUtility.startUpTomcat(prodServer, messages, false);
					}
					try {
						//Wait for SLEEP_AFTER_STARTUP seconds
						Thread.sleep(SLEEP_AFTER_STARTUP);
					} 
					catch (InterruptedException e1) {
						e1.printStackTrace();
						log.debug(e1);
					}
					if(log.isDebugEnabled()){
						log.debug(METHOD_NAME+": finish waiting for "+SLEEP_AFTER_STARTUP+" seconds for tomcat server to start up");
					}
					try {
						if(log.isDebugEnabled()){
							log.debug(METHOD_NAME+" Now trying to enable: "+aNode);
						}
						//Now we should enable the node, this throws a TimeoutException
						JCloudsUtility.enableNode(clb, loadBalancer, aNode.getId());
						//This call throws a JCloudException
						DeployUtility.enableEntityChecks(entities, aNode);
					} 
					catch (TimeoutException e) {
						e.printStackTrace();
						log.error(METHOD_NAME+": TimeoutException caught while trying to enable node: "+prodServer+
								" trying to eanble the Node one more time");
						log.error(e);
						//We should try to enable the node one more time
						try{
							JCloudsUtility.enableNode(clb, loadBalancer, aNode.getId());
						}
						//This is the second failure, although we could not enable this node, we should move on
						//TODO we may want to re-think if we should just move on or if we should halt and return false
						catch(TimeoutException e2){
							e2.printStackTrace();
							log.fatal(e2);
							log.fatal(METHOD_NAME+": 2nd Timeout Exception caught while trying to enable node: "+ prodServer);
						}
					}
					if(log.isDebugEnabled()){
						log.debug(METHOD_NAME+": retVal="+retVal);
					}
					//If we failed an scp, we need to add an error message and break out
					if(!retVal){
						Date dateNow=new Date();
						Long endtime=dateNow.getTime();
						this.markJobsEndTime(jobs, endtime);
						this.updateJobs(jobs, DeployUtility.STATUS_FAILED, "Scp failed for first prod node: "+prodServer);
						DeployUtility.addABadMessage("error", messages,"<span class='failuremessage'>Error: "+
								" Update of other nodes failed</span>");
						break;
					}
				}
				else{
					log.debug(METHOD_NAME+"~~~~~~!!!!!prodServer: "+prodServer+" is empty or null, just skip this prodServer");
				}
			}
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END retVal="+retVal);
		}
		return retVal;
	}


	private class Frozen{
		private String frozen;
		
		public void setFrozenFalse(){
			this.frozen="false";
		}
		
		public void setFrozenTrue(){
			this.frozen="true";
		}
		
		public String getFrozen(){
			return this.frozen;
		}
	}
	
	private void allowOrigin(HttpServletRequest request, HttpServletResponse response){
		String METHOD_NAME="allowOrigin()";
		if(log.isDebugEnabled()){
		    log.debug(METHOD_NAME+": START:");	
		}		
		String headerOrigin=request.getHeader("Origin");
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": headerOrigin="+headerOrigin);
		}
		if(null!=headerOrigin ){//&& (headerOrigin.toLowerCase()).endsWith("rackspace.com")){
			response.setHeader("Access-Control-Allow-Origin", headerOrigin);
		}
		else{
			String serverName=request.getServerName();
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": serverName="+serverName);				
			}
			response.setHeader("Access-Control-Allow-Origin", serverName);
		}
		if(log.isDebugEnabled()){
		    log.debug(METHOD_NAME+": END:");	
		}
	}
	
//	public static void main(String[]args){
//		String str="{\"deploytimes\":[{\"adeploytime\":\"Tue Nov 19 2013 15:41:08\",\"foldername\":\"ROOT\"}]}";
//		try {
//			JSONObject jsonObj=new JSONObject(str);
//			JSONArray jsonArr=jsonObj.getJSONArray("deploytimes");
//			JSONObject deployTime=jsonArr.getJSONObject(0);
//			System.out.println("~~~~~~~~~~deployTime.getString(\"adeploytime\")="+deployTime.getString("adeploytime"));
//		} 
//		catch (org.json.JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
}
