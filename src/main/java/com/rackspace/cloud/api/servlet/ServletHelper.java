//package com.rackspace.cloud.api.servlet;
//
//import org.apache.log4j.Logger;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//public class ServletHelper {
//
//	private static Logger log = Logger.getLogger(ServletHelper.class);
//	
//	public static boolean checkAccess(JSONObject jsonObj,String xLDAPName, String xLDAPGroupname, String username, int MAXSIZE){
//		String METHOD_NAME="checkAccess()";
//		if(log.isDebugEnabled()){
//			log.debug(METHOD_NAME+":START: jsonObj="+jsonObj+"\nxLDAPName="+xLDAPName+" xLDAPGroupname="+
//		    xLDAPGroupname+" username="+username);
//		}
//		boolean retVal=true;
//		if(null==xLDAPName||xLDAPName.isEmpty()){
//			if(null==username||username.isEmpty()){
//				retVal=false;
//				try{
//				jsonObj.put("noldapuser","No logged in user could be found clear browser cache and log back in");
//				}
//				catch(JSONException e){
//					e.printStackTrace();
//				}
//			}
//			else{
//				xLDAPName=username;		
//			}
//		}
//		//The return value is still true, we can go on
//		if(retVal){
//			//Make sure that xLDAPName is not greater than 70 characters
//			if(xLDAPName.length()>MAXSIZE){
//				String errorMess=null;
//				try {
//					errorMess = (String)jsonObj.get("errormessage");
//					StringBuffer newErrorMessage=new StringBuffer("");
//					newErrorMessage.append("xLDAPName: "+xLDAPName+" is longer than the maximun allowed lenth (70).");
//					if(null!=errorMess && !errorMess.isEmpty()){
//						newErrorMessage.append("\n");
//						newErrorMessage.append(errorMess);
//					}
//					jsonObj.put("errormessage", errorMess.toString());
//				} 
//				catch (JSONException e) {
//					e.printStackTrace();
//				}
//				retVal=false;
//			}
//			//Now check to make sure that the xLDAPUsername has the correct access
//			else{
////				HibernateUtil util=new HibernateUtil();
////				SessionFactory sessionFactory=util.getSessionFactory();
////				
////				MembersDaoImpl membership=new MembersDaoImpl();				
////				
////				Session session=sessionFactory.openSession();
////				Transaction trans=session.getTransaction();
////				trans.begin();
////				List<Member>membershipList=membership.findByLdapname(xLDAPName, session);
////				
////				boolean foundAccess=false;
////				for(Member aMember:membershipList){
////					String aGroupName=aMember.getMemberPK().getGroupname();
////					//admin and addmemberadmin access give
////					if(null!=aGroupName && 
////					(aGroupName.equalsIgnoreCase("admin")||aGroupName.equalsIgnoreCase("addmemberadmin"))){
////						foundAccess=true;
////						break;
////					}
////				}
////				if(log.isDebugEnabled()){
////					log.debug(METHOD_NAME+": foundAccess="+foundAccess);
////				}
////				if(foundAccess){
////					retVal=true;
////				}
////				else{
////					retVal=false;
////				}
//				
//			}
//		}
//		if(log.isDebugEnabled()){
//			log.debug(METHOD_NAME+":END: retVal="+retVal);
//		}
//		return retVal;
//	}
//	
//	public static void addMessage(JSONObject jsonObj, String messageKey, String aMessage){
//		String METHOD_NAME="addMessage()";
//		if(log.isDebugEnabled()){
//			log.debug(METHOD_NAME+": START: messageKey="+messageKey+" aMessage="+aMessage+
//					" jsonObj.toString()="+jsonObj.toString());
//		}
//		if(null!=jsonObj){
//			try{
//				String theMessage=null;
//				if(jsonObj.has(messageKey)){
//					theMessage=(String)jsonObj.getString(messageKey);
//				}
//				if(log.isDebugEnabled()){
//					log.debug(METHOD_NAME+": theMessage="+theMessage);
//				}
//				if(null==theMessage||theMessage.isEmpty()){
//					theMessage=aMessage;
//				}
//				else{
//					theMessage+="\n";
//					theMessage+=aMessage;
//				}
//				jsonObj.put(messageKey, theMessage);			
//			}
//			catch(JSONException e){
//				log.error(METHOD_NAME+":"+ e);
//				e.printStackTrace();
//			}
//			catch(Throwable e){
//				log.error(METHOD_NAME+":"+ e);
//				e.printStackTrace();
//			}
//		}
//		if(log.isDebugEnabled()){
//			log.debug(METHOD_NAME+": END: jsonObj.toString()="+jsonObj.toString());
//		}
//	}
//	
//	public static boolean validateParameter(JSONObject jsonObj, String parameterName, String parameterValue, int maxsize){
//		boolean retVal=true;
//		if(null==parameterValue || parameterValue.isEmpty()){
//			retVal=false;
//			ServletHelper.addMessage(jsonObj, "errormessage",
//			"The parameter: "+parameterName+ " must have a value, the request failed");				
//		}
//		else if(parameterValue.length()>maxsize){
//			retVal=false;
//			ServletHelper.addMessage(jsonObj, "errormessage",
//			"The parameter: "+parameterName+" is too long max("+maxsize+"): "+parameterValue);
//		}
//		return retVal;
//	}
//}
