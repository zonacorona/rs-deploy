package com.rackspace.cloud.api.controller;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.rackspace.cloud.api.dao.IGroupsDao;
import com.rackspace.cloud.api.dao.IUsersDao;
import com.rackspace.cloud.api.entity.Groups;
import com.rackspace.cloud.api.entity.Users;
import com.rackspace.cloud.api.view.model.DeployAdminToolUsersModel;
import com.rackspace.cloud.api.view.model.ResponseMessage;
import com.rackspace.cloud.api.view.model.UserAccessModel;
import com.rackspace.cloud.api.view.model.UserGroupMembershipModel;

@Controller
@RequestMapping(value="/")
@Configuration
public class DeployAdminController implements InitializingBean{

	private static Logger log = Logger.getLogger(DeployAdminController.class);

	@Autowired
	private IUsersDao userDao;

	@Autowired
	private IGroupsDao groupDao;


	public static final String ALLOW_GROUP_EDIT="addmemberadmin";
	public static final String ADMIN_GROUP="admin";

	@RequestMapping(value="/DeployAdmin", method=RequestMethod.GET)
	public String addDeployAdminModelObjects(Model model, HttpSession session, HttpServletRequest request){
		String METHOD_NAME="addDeployAdminModelObjects()";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START");
		}

		List<DeployAdminToolUsersModel>allUsersModel=this.getAllUsers();

		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": adding allUsers bean");
		}		
		model.addAttribute("allUsers",allUsersModel);

		String loggedInUser=(String)request.getHeader("x-ldap-username");
		if(null==loggedInUser || loggedInUser.trim().isEmpty()){
			loggedInUser=(String)session.getAttribute("x-ldap-username");
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": loggedInUser="+loggedInUser);
		}
		if(null==loggedInUser){
			loggedInUser="";
		}
		model.addAttribute("loggedInUser",loggedInUser);
		UserAccessModel userAccess=this.doesUserHaveAccess(loggedInUser);
		model.addAttribute("hasAccess",userAccess.hasAccess());
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END");
		}
		return "DeployAdmin";
	}

	@RequestMapping(value="/updateGroupMembers",method=RequestMethod.POST)
	public void updateGroupMembers(HttpServletRequest request, HttpServletResponse response){
		String METHOD_NAME="updateGroupMembers()";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START:");
		}
		HttpSession session=request.getSession();
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
		if(null!=request){
			String[] selectedGroups=request.getParameterValues("groupNames");
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
						"DeployAdminController.updateGroupMemers(): Error, header/session parameter: x-ldap-username/username "+
						"is null or empty, delete browser cache and log back into page.");
			}
			//We have a x-ldap-username value, we can continue with the deployment
			else{
				if(log.isDebugEnabled()){			
					log.debug(METHOD_NAME+":xLDAPUsername="+xLDAPUsername+" is not null nor empty");
					log.debug(METHOD_NAME+" checking credentials for user: "+xLDAPUsername);
				}
				UserAccessModel accessModel=this.checkEditingGroupMembershipAccessForUser(xLDAPUsername);
				boolean hasAccess=accessModel.hasAccess();
				if(log.isDebugEnabled()){			
					log.debug(METHOD_NAME+":hasAccess="+hasAccess);
				}
				if(hasAccess){
					if(log.isDebugEnabled()){			
						log.debug(METHOD_NAME+":selectedGroups="+selectedGroups);
					}
					if(null!=selectedGroups){
						if(log.isDebugEnabled()){			
							log.debug(METHOD_NAME+": selectedGroups.length="+selectedGroups.length);
						}
						Set<Groups>groupsMembership=new HashSet<Groups>();
						for(String aGroup:selectedGroups){
							Groups aNewGroup=new Groups();
							aNewGroup.setName(aGroup);
							groupsMembership.add(aNewGroup);
						}
						//Now we add the group to the user
						Users theUser=this.userDao.findById(xLDAPUsername);
						if(log.isDebugEnabled()){			
							log.debug(METHOD_NAME+": theUser="+theUser);
						}
						if(null!=theUser){
							if(log.isDebugEnabled()){			
								log.debug(METHOD_NAME+": theUser.getFname()="+theUser.getFname());
							}
							theUser.setGroupses(groupsMembership);
							if(log.isDebugEnabled()){			
								log.debug(METHOD_NAME+": updating the user Groups Set");
							}						
							this.userDao.save(theUser);
						}
					}
				}
				else{
					ControllerHelper.addMessage(retVal, "noaccesstoeditgroupmembership", 
							"DeployAdminController.updateGroupMemers(): user: "+xLDAPUsername+" does not have access to edit group membership.");
				}
			}
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END:");
		}
	}

	@RequestMapping(value="/initialAdminUsers", method=RequestMethod.GET)
	@ResponseBody
	public List<DeployAdminToolUsersModel>getInitialUsersForDeployAdmin(){
		return this.getAllUsers();
	}

	@RequestMapping(value="/getGroupMembershipForUser",method=RequestMethod.GET)
	@ResponseBody
	public List<UserGroupMembershipModel>getGroupMembershipForUser(@RequestParam("ldapname") String ldapname){
		List<UserGroupMembershipModel>retVal=new ArrayList<UserGroupMembershipModel>();
		String METHOD_NAME="getGroupMembershipForUser()";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START: ldapname="+ldapname);
		}
		if(null!=ldapname){

			List<Groups>allTheGroups=this.getAllGroups();

			Users theUser=this.userDao.findById(ldapname);
			Set<Groups>userGroupMembership=theUser.getGroupses();
			Map<String,Groups>userGroupMembershipMap=new HashMap<String, Groups>();

			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": userGroupMembership.size()="+userGroupMembership.size()+"\n"+
						"User: "+ldapname+" belongs to the following groups: ");
			}
			//Put the group name as the key
			for(Iterator<Groups>iter=userGroupMembership.iterator();iter.hasNext();){
				Groups aGroup=iter.next();			
				if(log.isDebugEnabled()){
					log.debug(METHOD_NAME+": aGroup.getName()="+aGroup.getName());
				}
				userGroupMembershipMap.put(aGroup.getName(), aGroup);
			}

			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": allTheGroups.size()="+allTheGroups.size());
			}
			//Now we iterate through all the groups, check to see which one the user has membership to and 
			//add to the return List with the respective Groups checked
			for(Groups aGroup:allTheGroups){
				String aGroupName=aGroup.getName();
				if(log.isDebugEnabled()){
					log.debug(METHOD_NAME+": aGroupName="+aGroupName);
					log.debug(METHOD_NAME+": userGroupMembershipMap.contains("+aGroupName+")="+
							(userGroupMembershipMap.containsKey(aGroupName)));
				}
				//Create a Model Object
				UserGroupMembershipModel aModel=new UserGroupMembershipModel();
				aModel.setGroupName(aGroupName);
				if(userGroupMembershipMap.containsKey(aGroupName)){
					aModel.setChecked(true);
					aModel.setHtml("<input class=\"groupnamecheckbox\" type=\"checkbox\" name=\"member\" value=\""+aGroupName+"\" checked >"+aGroupName+"<br>");
				}
				else{
					aModel.setChecked(false);
					aModel.setHtml("<input class=\"groupnamecheckbox\" type=\"checkbox\" name=\"member\" value=\""+aGroupName+"\" >"+aGroupName+"<br>");
				}
				retVal.add(aModel);
			}

		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END: retVal.size()="+retVal.size());
		}				
		return retVal;
	}

	public List<Groups>getAllGroups(){
		return this.groupDao.findAll();
	}


	private List<DeployAdminToolUsersModel>getAllUsers(){	
		String METHOD_NAME="getAllUsers()";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START");
		}
		List<Users>allUsers=this.userDao.findAll();
		List<DeployAdminToolUsersModel>retVal=new ArrayList<DeployAdminToolUsersModel>();
		if(null!=allUsers){	
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": allUsers.size()="+allUsers.size());
			}
			for(int i=0;i<allUsers.size();++i){
				Users aUser=allUsers.get(i);
				DeployAdminToolUsersModel aUserModel=new DeployAdminToolUsersModel();
				aUserModel.setCount((i+1));
				aUserModel.setUser(aUser);
				aUserModel.setGroupsList(aUser.getGroupses());
				retVal.add(aUserModel);
			}
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END retVal.size()="+retVal.size());
		}
		return retVal;
	}

	@RequestMapping(value="/doesUserHaveAccess", method=RequestMethod.GET)
	@ResponseBody
	public UserAccessModel doesUserHaveAccess(@RequestParam("ldapname") String ldapname){
		String METHOD_NAME="doesUserHaveAccess()";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START");
		}
		UserAccessModel retVal=this.checkEditingGroupMembershipAccessForUser(ldapname);
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END retVal="+retVal);
		}
		return retVal;
	}

	private UserAccessModel checkEditingGroupMembershipAccessForUser(String ldapname){
		String METHOD_NAME="checkEditingGroupMembershipAccessForUser()";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START:");
		}		
		UserAccessModel retVal=new UserAccessModel();
		retVal.setHasAccess(false);
		Users theUser=this.userDao.findById(ldapname);
		if(null!=theUser){
			Set<Groups>theUsersGroups=theUser.getGroupses();
			if(null!=theUsersGroups){
				for(Iterator<Groups>iter=theUsersGroups.iterator();iter.hasNext();){
					Groups aGroup=iter.next();
					String aGroupName=aGroup.getName();
					if(log.isDebugEnabled()){
						log.debug(METHOD_NAME+": aGroupName="+aGroupName);
					}
					if(aGroupName.equals(DeployAdminController.ADMIN_GROUP)||aGroupName.equals(DeployAdminController.ALLOW_GROUP_EDIT)){
						retVal.setHasAccess(true);
						break;
					}
				}
			}
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END:");
		}
		return retVal;
	}

	@RequestMapping(value="/createNewUser", method=RequestMethod.POST)
	@ResponseBody
	public ResponseMessage  createNewUser(HttpServletRequest request, HttpServletResponse response)throws Throwable{
		ResponseMessage retVal=new ResponseMessage();
		String METHOD_NAME="createNewUser()";

		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START:");
		}
		String fname=request.getParameter("fname");
		String lname=request.getParameter("lname");
		String ldapname=request.getParameter("ldapname");
		String email=request.getParameter("email");
		String loggedInUser=request.getParameter("loggedInUser");
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": fname="+fname);
			log.debug(METHOD_NAME+": lname="+lname);
			log.debug(METHOD_NAME+": ldapname="+ldapname);
			log.debug(METHOD_NAME+": email="+email);
			log.debug(METHOD_NAME+": loggedInUser="+loggedInUser);
		}
		
		if(null!=loggedInUser && !loggedInUser.isEmpty()){	
			UserAccessModel doesUserHaveAccessObj=this.doesUserHaveAccess(loggedInUser);
			boolean doesUserHaveAccess=doesUserHaveAccessObj.hasAccess();
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": doesUserHaveAccess="+doesUserHaveAccess);
			}
			if(doesUserHaveAccess){

				if(null!=ldapname && !ldapname.isEmpty()){
					Users aNewUser=new Users();
					aNewUser.setFname(fname);
					aNewUser.setLname(lname);
					aNewUser.setLdapname(ldapname);
					aNewUser.setEmail(email);
					aNewUser.setStatus("active");
					aNewUser.setPassword("password");
					
					try{
						this.userDao.save(aNewUser);
						retVal.addAMessage("Success: Created a new user with ldapname="+ldapname);
					}
					catch(Throwable e){
						retVal.addAMessage("Error: "+e.getMessage());
						throw e;
					}
				}
				else{
					retVal.addAMessage("Error: Cannot create a new user, ldapname is missing.");
				}
			}
			else{
				retVal.addAMessage("Error: user: "+loggedInUser+" does not have access to create a new user.");
			}
		}
		else{
			retVal.addAMessage("Error: no logged in user could be found, delete browser cache and log back in.");
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END:");
		}
		return retVal;
	}

	@RequestMapping(value="/createNewGroup", method=RequestMethod.POST)
	@ResponseBody
	public ResponseMessage  createNewGroup(HttpServletRequest request, HttpServletResponse response)throws Throwable{
		ResponseMessage retVal=new ResponseMessage();
		String METHOD_NAME="createNewGroup()";

		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START:");
		}
		String groupName=request.getParameter("groupName");
		String loggedInUser=request.getParameter("loggedInUser");
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": groupName="+groupName);
			log.debug(METHOD_NAME+": loggedInUser="+loggedInUser);
		}
		
		if(null!=loggedInUser && !loggedInUser.isEmpty()){	
			UserAccessModel doesUserHaveAccessObj=this.doesUserHaveAccess(loggedInUser);
			boolean doesUserHaveAccess=doesUserHaveAccessObj.hasAccess();
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": doesUserHaveAccess="+doesUserHaveAccess);
			}
			if(doesUserHaveAccess){
				Groups aNewGroup=new Groups();
				aNewGroup.setName(groupName);
				try{
					if(log.isDebugEnabled()){
						log.debug(METHOD_NAME+": Saving aNewGroup");
					}
					this.groupDao.save(aNewGroup);
					if(log.isDebugEnabled()){
						log.debug(METHOD_NAME+": After saving aNewGroup");
					}
				}
				catch(Throwable e){
					retVal.addAMessage("Error: "+e.getMessage());
					throw e;
				}
			}
			else{
				retVal.addAMessage("Error: user: "+loggedInUser+" does not have access to create a new user.");
			}
		}
		else{
			retVal.addAMessage("Error: no logged in user could be found, delete browser cache and log back in.");
		}
		
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END:");
		}
		return retVal;
	}

	@RequestMapping(value="/updateGroupsForUser",method=RequestMethod.POST)
	@ResponseBody
	public ResponseMessage updateGroupsForUser(HttpServletRequest request, HttpServletResponse response){
		ResponseMessage retVal=new ResponseMessage();
		String METHOD_NAME="updateGroupsForUser()";

		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START:");
		}

		HttpSession session=request.getSession();
		response.setContentType("application/json");
		if(log.isDebugEnabled()){
			Enumeration<String> sessAttrs=session.getAttributeNames();
			Enumeration<String> headerNames=request.getHeaderNames();

			log.debug("!!!!!!!!!!!Header Values START:!!!!!!!!!!!");
			while(headerNames.hasMoreElements()){
				String aHeaderKey=(String)headerNames.nextElement();
				String aHeaderValue=(String)request.getHeader(aHeaderKey);
				log.debug(METHOD_NAME+": request.getHeader("+aHeaderKey+")="+aHeaderValue);

			}
			log.debug("!!!!!!!!!!!Header Values END:!!!!!!!!!!!");                	

			log.debug(METHOD_NAME+": !!!!!!!!!!!Session Attributes BEGIN:!!!!!!!!!!!");
			while(sessAttrs.hasMoreElements()){
				String aKey=sessAttrs.nextElement();
				log.debug("!!!!!!!!!!!session.getAttribute("+aKey+")="+session.getAttribute(aKey));
			}
			log.debug(METHOD_NAME+": !!!!!!!!!!!Session Attributes END:!!!!!!!!!!!");
		}		
		String requestParams=request.getParameter("ldapname");
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": requestParams="+requestParams);
		}		
		if(null!=requestParams){


			//Now we get the values
			String ldapname=request.getParameter("ldapname");
			String loggedInUser=request.getParameter("loggedInUser");
			String[] members=request.getParameterValues("member");
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": ldapname="+ldapname);
				log.debug(METHOD_NAME+": loggedInUser="+loggedInUser);
				log.debug(METHOD_NAME+": members="+members);
			}
			if(null!=ldapname && !ldapname.isEmpty() && null!=loggedInUser && !loggedInUser.isEmpty() && 
					null!=members && members.length>0){
				UserAccessModel doesUserHaveAccessObj=this.doesUserHaveAccess(loggedInUser);
				boolean doesUserHaveAccess=doesUserHaveAccessObj.hasAccess();
				if(log.isDebugEnabled()){
					log.debug(METHOD_NAME+": doesUserHaveAccess="+doesUserHaveAccess);
				}
				if(doesUserHaveAccess){
					if(log.isDebugEnabled()){
						log.debug(METHOD_NAME+": members.length="+members.length);
						log.debug(METHOD_NAME+": Getting user with ldapname: "+ldapname);
					}
					Set<Groups>groupsSet=new HashSet<Groups>();
					//List<Members>groupsList=new ArrayList<Members>();
					Users user=this.userDao.findById(ldapname);
					if(log.isDebugEnabled()){
						log.debug(METHOD_NAME+": user="+user);
					}
					if(null!=user){
						for(String aGroup:members){
							if(log.isDebugEnabled()){
								log.debug(METHOD_NAME+": aGroup="+aGroup);
							}
							if(null!=aGroup && !aGroup.isEmpty()){
								Groups aNewGroup=new Groups();
								aNewGroup.setName(aGroup);
								//Members aNewMember=new Members();
								//MembersId aNewMembersId=new MembersId();
								//aNewMembersId.setLdapname(ldapname);
								//aNewMembersId.setGroupname(aGroup);
								if(log.isDebugEnabled()){
									//log.debug(METHOD_NAME+": aNewMembersId.getLdapname()="+aNewMembersId.getLdapname());
									//log.debug(METHOD_NAME+": aNewMembersId.getGroupname()="+aNewMembersId.getGroupname());
									log.debug(METHOD_NAME+": aNewGroup.getName()="+aNewGroup.getName());					    				
								}
								//aNewMember.setId(aNewMembersId);
								//membersList.add(aNewMember);
								groupsSet.add(aNewGroup);
							}
						}
						//this.memberDao.save(membersList);
						user.setGroupses(groupsSet);
						if(log.isDebugEnabled()){
							log.debug(METHOD_NAME+": Saving user: "+user);
						}
						this.userDao.save(user);
						if(log.isDebugEnabled()){
							log.debug(METHOD_NAME+": finished saving");
						}
					}

					retVal.addAMessage("Update successful.");
				}
				else{
					retVal.addAMessage("Error: User: "+loggedInUser+" does not have access to update Group membership.");
				}
			}
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END:");
		}
		return retVal;
	}


	@Override
	//This gets called upon Bean construction
	public void afterPropertiesSet() throws Exception {
		String METHOD_NAME="afterPropertiesSet()";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START: ");
		}		

		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END: ");
		}
	}

	public static void main(String[] args){
		String myStr="=thu4404";

		String[] myStrArr=myStr.split("=");

		System.out.println("myStrArr.length="+myStrArr.length+"  myStrArr[0]="+myStrArr[0] +"  myStrArr[1]="+myStrArr[1]);
	}
}
