package com.rackspace.cloud.api.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.rackspace.cloud.api.DeployUtility;
import com.rackspace.cloud.api.InstalledWar;
import com.rackspace.cloud.api.dao.IDeployjobDao;
import com.rackspace.cloud.api.dao.IFreezeDao;
import com.rackspace.cloud.api.dao.IGroupsDao;
import com.rackspace.cloud.api.dao.IMembersDao;
import com.rackspace.cloud.api.dao.IUsersDao;
import com.rackspace.cloud.api.entity.Groups;
import com.rackspace.cloud.api.entity.Users;

@Controller
@RequestMapping(value="/")
@Configuration
public class InternalWarController implements InitializingBean{
	
	private static Logger log = Logger.getLogger(InternalWarController.class);
	private static Properties propsFile;
	private static final Object lock = new Object();
	//First map is keyed by users.ldapname, the inner Set contains all the groups the user belongs to 
	//private Map<String,Set<String>>userAccessToGroupsMap;

	private Map<String, InstalledWar>internalNReviewerWars;
	private  List<String>internalNReviewerFilterList;

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
	public static final String ROOT_ARTIFACT_ID="rax-indexwar-landing";
	public static final String ROOT_GROUP_ID="com.rackspace.cloud.api";


	@Override
	//This gets called upon Bean construction
	public void afterPropertiesSet() throws Exception {
		String METHOD_NAME="afterPropertiesSet()";
		try{
			// TODO Auto-generated method stub
			InputStream inny=ExternalWarController.class.getClassLoader().getResourceAsStream("/props.properties");
			if(log.isDebugEnabled()){
			    log.debug(METHOD_NAME+":!!!!!!!!!!opening props.properties");
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
		}
		catch(Throwable e){
			e.printStackTrace();
			log.debug(e);
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": caught Throwable message="+e.getMessage());
			}
		}
	}
	
	@RequestMapping(value="/DeployWarsInternal", method=RequestMethod.GET)
	public String addInternalModelObjects(Model model, HttpSession session, HttpServletRequest request){
		String METHOD_NAME="addInternalModelObjects()";
		String retVal="DeployWarsInternal";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+":START:");			
		}
		if(log.isDebugEnabled()){
			ControllerHelper.outputRequestHeadersAndSessionAttributes(request,session);
		}
		String loggedInUser=(String)request.getHeader("x-ldap-username");
		if(null==loggedInUser || loggedInUser.trim().isEmpty()){
			loggedInUser=(String)session.getAttribute("x-ldap-username");
		}
		if(null!=loggedInUser){
			model.addAttribute("x-ldap-username",loggedInUser);
		}

		String propsDebug=propsFile.getProperty("debug");
		request.setAttribute("debug", propsDebug);
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": loggedInUser="+loggedInUser+" propsDebug="+propsDebug);
		}
		//We only procede if we have the x-ldap-username value
		if(null!=loggedInUser || (null!=propsDebug&&propsDebug.equalsIgnoreCase("true"))){
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

			List<String>internalFilters=this.getInternalNReviewerFilterList(loggedInUser);
			model.addAttribute("intFilters",internalFilters);

			Map<String,InstalledWar>internalWars=this.getInternalWars(session,loggedInUser);
			if(null!=internalWars && internalWars.size()==0){
				return "NoAccess";
			}
			model.addAttribute("intWars",internalWars);
		}
		else{
			retVal="redirect:/Login";
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END: retVal="+retVal);			
		}
		return retVal;
	}
	
	@RequestMapping(value="/intNReviewerFilters",method=RequestMethod.GET)
	@ResponseBody
	public List<String>getInternalNReviewerFilterList(String loggedInUser){
		if(null==this.internalNReviewerFilterList||this.internalNReviewerFilterList.size()==0){
			if(null==this.internalNReviewerFilterList){
				this.internalNReviewerFilterList=new ArrayList<String>();
			}
			this.loadInternalNReviewerFilters(loggedInUser);
		}
		return this.internalNReviewerFilterList;
	}
	
	
	private void loadInternalNReviewerFilters(String xldapUserName){
		String METHOD_NAME="loadInternalNReviewerFilters()";	
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+":START");
		}
		List<String>tempFilters=new ArrayList<String>();
		if(null==this.internalNReviewerWars||this.internalNReviewerWars.size()==0){
			if(null==this.internalNReviewerWars){
				this.internalNReviewerWars=new HashMap<String, InstalledWar>();
			}
			ControllerHelper.loadAllWars(this.servletCtx,this.internalNReviewerWars,false);
		}
		boolean debug=false;
		String propsDebug=propsFile.getProperty("debug");
		if(null!=propsDebug && propsDebug.equalsIgnoreCase("true")){
			debug=true;
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": debug="+debug);
			log.debug(METHOD_NAME+":this.internalNReviewerWars.size()="+this.internalNReviewerWars.size());
		}
		Set<String>tempSortedInstalledWars=new TreeSet<String>();
		Users user=userDao.findById(xldapUserName);
		Set<Groups>groupsMembership=user.getGroupses();
		for(InstalledWar aWar:this.internalNReviewerWars.values()){
			String pomName=aWar.getPomName();	
			String group=ControllerHelper.retrieveLoggedInUserAccessToWar(aWar, propsFile,groupsMembership);
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+":pomName="+pomName); 
				log.debug(METHOD_NAME+":group="+group); 
				log.debug(METHOD_NAME+": !tempSortedInstalledWars.contains("+pomName+")="+
				(!tempSortedInstalledWars.contains(pomName)));
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
			if(null==this.internalNReviewerFilterList){
				this.internalNReviewerFilterList=tempFilters;
			}
			else{
				synchronized(this.internalNReviewerFilterList){
					this.internalNReviewerFilterList=tempFilters;
				}
			}
		}
		//Make sure at the very least, it's an empty list
		if(null==this.internalNReviewerFilterList){
			this.internalNReviewerFilterList=new ArrayList<String>();
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+":END this.internalNReviewerFilterList.size()="+internalNReviewerFilterList.size());
		}
	}
	
	@RequestMapping(value="/getInternalWars", method=RequestMethod.GET)
	@ResponseBody
	public Map<String,InstalledWar> getInternalWars(HttpSession session, @RequestParam(value="x-ldap-username") String xldapUserName) {

		if(null==this.internalNReviewerWars){
			this.internalNReviewerWars=new HashMap<String, InstalledWar>();
		}
		return ControllerHelper.getWars(session,this.servletCtx,this.internalNReviewerWars,this.userDao,
				xldapUserName,propsFile,false);
	}
}
