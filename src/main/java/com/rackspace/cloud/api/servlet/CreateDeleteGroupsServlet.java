//package com.rackspace.cloud.api.servlet;
//
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.util.ArrayList;
//import java.util.Enumeration;
//import java.util.List;
//
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServlet;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import javax.servlet.http.HttpSession;
//
//import org.apache.log4j.Logger;
//import org.hibernate.HibernateException;
//import org.hibernate.Session;
//import org.hibernate.SessionFactory;
//import org.hibernate.Transaction;
//import org.json.JSONObject;
//
//import com.rackspace.cloud.api.HibernateUtil;
//import com.rackspace.cloud.api.database.Group;
//import com.rackspace.cloud.api.database.Member;
//import com.rackspace.cloud.api.impl.GroupDaoImpl;
//import com.rackspace.cloud.api.impl.MemberDaoImpl;
//
//public class CreateDeleteGroupsServlet extends HttpServlet {
//	
//
//	/**
//	 * 
//	 */
//	private static final long serialVersionUID = -8597643287779099818L;
//	private static Logger log = Logger.getLogger(CreateDeleteGroupsServlet.class);
//	
//	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
//		String METHOD_NAME="doPost()";
//		
//		if(log.isDebugEnabled()){
//			log.debug(METHOD_NAME+": START:");
//		}
//		JSONObject retVal=new JSONObject();
//		HttpSession session=request.getSession();
//		if(log.isDebugEnabled()){
//			Enumeration<String> sessAttrs=session.getAttributeNames();
//			Enumeration<String> headerNames=request.getHeaderNames();
//
//			log.debug("@@@@@@@@@@@@@@@@@@@@@Header Values START:@@@@@@@@@@@@@@@@@@@@@");
//			while(headerNames.hasMoreElements()){
//				String aHeaderKey=(String)headerNames.nextElement();
//				String aHeaderValue=(String)request.getHeader(aHeaderKey);
//				log.debug(METHOD_NAME+": request.getHeader("+aHeaderKey+")="+aHeaderValue);
//
//			}
//			log.debug("@@@@@@@@@@@@@@@@@@@@@Header Values END:@@@@@@@@@@@@@@@@@@@@@");                	
//
//			log.debug(METHOD_NAME+": ^^^^^^^^^^^^^Session Attributes BEGIN:^^^^^^^^^^^^^");
//			while(sessAttrs.hasMoreElements()){
//				String aKey=sessAttrs.nextElement();
//				log.debug("@@@@@@@@@@@@@session.getAttribute("+aKey+")="+session.getAttribute(aKey));
//			}
//			log.debug(METHOD_NAME+": ^^^^^^^^^^^^^Session Attributes END:^^^^^^^^^^^^^");
//		}		
//		String action=request.getParameter("action");
//		String groupName=request.getParameter("groupname");
//		String deleteGroups=request.getParameter("deletegroups");
//		if(log.isDebugEnabled()){
//			log.debug(METHOD_NAME+": action="+action+" groupName="+groupName+" deleteGroups="+deleteGroups);
//		}
//		PrintWriter out=response.getWriter();
//		if(null!=action && !action.isEmpty()){
//
//			response.setContentType("application/json");
//			String xLDAPUsername=(String)session.getAttribute("x-ldap-username");
//			String username=null;
//			String xLDAPGroupname=null;
//			if(null==xLDAPUsername || xLDAPUsername.isEmpty()){
//				xLDAPUsername=request.getHeader("x-ldap-username");
//				username=request.getHeader("username");
//				xLDAPGroupname=request.getHeader("x-ldap-groupname");
//
//			}
//			else{
//				username=(String)session.getAttribute("username");
//				xLDAPGroupname=(String)session.getAttribute("x-ldap-groupname");
//			}
//			if(log.isDebugEnabled()){
//				log.debug(METHOD_NAME+": xLDAPUsername="+xLDAPUsername+" username="+username+" xLDAPGroupname="+xLDAPGroupname);
//			}	
//			if(null!=xLDAPUsername && !xLDAPUsername.isEmpty()){
//
//				if(action.length()<10){
//					if(action.equalsIgnoreCase("create")){
//						this.handleCreateGroup(retVal, groupName);
//					}
//					else if(action.equalsIgnoreCase("delete")){
//						this.handleDeleteGroup(retVal, deleteGroups);
//					}
//					else{
//						ServletHelper.addMessage(retVal, "errormessage",
//						"The request parameter: action, has an invalid value: "+action);
//					}
//				}
//				else{
//					ServletHelper.addMessage(retVal, "errormessage",
//					"The request parameter: action, has an invalid value: "+action);
//				}
//			}
//			//xLDAPUsername is null or empty, we cannot verify that the user has the correct access
//			else{
//				if(null==xLDAPUsername || xLDAPUsername.isEmpty()){
//					ServletHelper.addMessage(retVal, "noldapuser", 
//				    "CreateDeleteGroupsServlet.doPost(): Error, header/session parameter: x-ldap-username/username "+
//					" is null or empty, delete browser cache and log back onto page.");
//				}
//			}
//		}
//		else{
//			ServletHelper.addMessage(retVal, "errormessage", 
//			"The parameter: action must have a value, the request failed");
//		}		
//		out.print(retVal.toString());
//		if(log.isDebugEnabled()){
//			log.debug(METHOD_NAME+": END:");
//		}
//	}
//
//	
//	private void handleCreateGroup(JSONObject jsonObj, String groupName){		
//		String METHOD_NAME="handleCreateGroup()";
//		if(log.isDebugEnabled()){
//			log.debug(METHOD_NAME+": START: jsonObj="+jsonObj+" groupName="+groupName);
//		}
//		if(ServletHelper.validateParameter(jsonObj, "groupname", groupName, 70)){
//			HibernateUtil util=new HibernateUtil();
//			SessionFactory sessionFactory=util.getSessionFactory();			    		    
//			//First we need to check to make sure that the group doesn't already exist
//			Session sess=sessionFactory.openSession();
//			MembersDaoImpl memberDaoImpl=new MembersDaoImpl();
//			Transaction trans=sess.getTransaction();
//			trans.begin();
//			List<Member>membersList=memberDaoImpl.findAllMembers(sess);
//			trans.commit();
//			boolean duplicateFound=false;
//			for(Member aMember:membersList){
//				String aGroupName=aMember.getMemberPK().getGroupname();
//				if(null!=aGroupName && aGroupName.equalsIgnoreCase(groupName)){
//					duplicateFound=true;
//					break;
//				}
//			}
//			if(duplicateFound){
//				ServletHelper.addMessage(jsonObj, "error", 
//				"The groupname: "+groupName+" already exists, delete it first");
//			}
//			else{
//				try{
//					GroupDaoImpl groupDaoImpl=new GroupDaoImpl();
//					Group newGroup=new Group();
//					newGroup.setName(groupName);
//					List<Group>newListGroups=new ArrayList<Group>();
//					newListGroups.add(newGroup);
//
//					sess=sessionFactory.openSession();
//					trans=sess.getTransaction();
//					trans.begin();
//					groupDaoImpl.save(newListGroups, sess);
//					trans.commit();	
//
//					ServletHelper.addMessage(jsonObj, "successmessage", "Group: "+groupName+" successfully created");
//				}
//				catch(HibernateException e){
//					log.error(METHOD_NAME+": HibernateException caught: error message: "+e.getMessage());
//					log.error(METHOD_NAME+" "+e);
//					ServletHelper.addMessage(jsonObj, "errormessage", "Group creation failed error message: "+e.getMessage());		
//					e.printStackTrace();
//				}					
//			}
//		}
//		if(log.isDebugEnabled()){
//			log.debug(METHOD_NAME+": END:");
//		}
//	}
//	
//	private void handleDeleteGroup(JSONObject jsonObj, String deleteGroups){
//		String METHOD_NAME="deleteGroups()";
//		if(log.isDebugEnabled()){
//			log.debug(METHOD_NAME+": START: jsonObj="+jsonObj+" deleteGroups="+deleteGroups);
//		}		
//		if(ServletHelper.validateParameter(jsonObj, "deletegroups", deleteGroups, 1000)){
//			String[] deleteGroupsArray=deleteGroups.split(":");
//			List<Group>deleteGroupsList=new ArrayList<Group>();
//			for(String aDeleteGroup:deleteGroupsArray){
//				Group groupToDelete=new Group();
//				groupToDelete.setName(aDeleteGroup);
//				deleteGroupsList.add(groupToDelete);
//			}
//			if(log.isDebugEnabled()){
//				log.debug(METHOD_NAME+": deleteGroupsList.size()="+deleteGroupsList.size());
//			}
//			if(deleteGroupsList.size()>0){
//				try{
//					GroupDaoImpl groupDaoImpl=new GroupDaoImpl();
//
//					HibernateUtil util=new HibernateUtil();
//					SessionFactory sessionFactory=util.getSessionFactory();			    		    
//					//First we need to check to make sure that the group doesn't already exist
//					Session sess=sessionFactory.openSession();
//					Transaction trans=sess.getTransaction();
//					trans.begin();
//					groupDaoImpl.delete(deleteGroupsList, sess);
//					trans.commit();
//				}
//				catch(HibernateException e){
//					log.error(METHOD_NAME+": HibernateException caught: error message: "+e.getMessage());
//					log.error(METHOD_NAME+" "+e);
//					ServletHelper.addMessage(jsonObj, "errormessage",
//					"Group deletion failed error message: "+e.getMessage());
//					e.printStackTrace();
//				}
//			}
//		}
//		if(log.isDebugEnabled()){
//			log.debug(METHOD_NAME+": END: ");
//		}
//	}
//	
//}
