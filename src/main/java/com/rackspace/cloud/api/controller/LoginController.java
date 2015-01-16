package com.rackspace.cloud.api.controller;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.rackspace.cloud.api.ReturnMessage;
import com.rackspace.cloud.api.dao.IUsersDao;
import com.rackspace.cloud.api.entity.Users;
import com.rackspace.cloud.api.view.model.LoginUser;
import com.rackspace.cloud.api.view.model.NewCredentials;

@Service
@RequestMapping(value="/")
@Configuration
public class LoginController {

	public static final String SUCCESSFUL_LOGIN="success";
	public static final String INVALID_USER="invalid user";
	public static final String INVALID_PASSWORD="invalid password";
	public static final String X_LDAP_USERNAME="x-ldap-username";

	public static final Set<String> invalidCharsSet=new HashSet<String>();

	static{
		invalidCharsSet.add("<");
		invalidCharsSet.add(">");
	}

	private static Logger log = Logger.getLogger(LoginController.class);

	@Autowired
	private IUsersDao users;


	@RequestMapping(value="/checkValidSession", method=RequestMethod.GET)
	@ResponseBody
	public ReturnMessage checkIfValidSessionExists(HttpSession session){
		String METHOD_NAME="checkIfValidSessionExists()";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START:");
		}
		ReturnMessage retVal=new ReturnMessage();

		retVal.setStatus("passed");
		retVal.setMessage("passed");

		String ldapusername=(String)session.getAttribute(X_LDAP_USERNAME);
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": ldapusername="+ldapusername);
		}	
		//There is an x-ldap-username value in session
		if(null!=ldapusername && !ldapusername.isEmpty()){
			//check to make ure the x-ldap-username is valid
			Users user=users.findById(ldapusername);
			//the x-ldap-username is not valid
			if(null==user){
				retVal.setStatus("failed");
				retVal.setMessage("invalid x-ldap-username in session");
			}
		}
		else{
			retVal.setStatus("failed");
			retVal.setMessage("no x-ldap-username in session");
		}		
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END:");
		}
		return retVal;
	}

	@RequestMapping(value="/changePassword")
	@ResponseBody
	public ReturnMessage changePassword(HttpSession session, @RequestBody NewCredentials cred){
		String METHOD_NAME="changePassword()";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START:");
		}
		ReturnMessage retVal=new ReturnMessage();
		retVal.setStatus("passed");
		retVal.setMessage("passed");
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": cred="+cred);
		}
		if(null!=cred){
			String username=cred.getUsername();
			String newpassword=cred.getNewpassword();
			String password=cred.getPassword();
			String confirmPass=cred.getConfirmpassword();
			String sessionUsername=(String)session.getAttribute(X_LDAP_USERNAME);

			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": username="+username);
				log.debug(METHOD_NAME+": sessionUsername="+sessionUsername);
				log.debug(METHOD_NAME+": (null!=password && !password.isEmpty())="+
						(null!=password && !password.isEmpty()));
				log.debug(METHOD_NAME+": (null!=newpassword && !newpassword.isEmpty())="+
						(null!=newpassword && !newpassword.isEmpty()));
				log.debug(METHOD_NAME+": (null!=confirmPass && !confirmPass.isEmpty())="+
						(null!=confirmPass && !confirmPass.isEmpty()));
			}
			if(null!=username && !username.isEmpty() && null!=password && !password.isEmpty() &&
					null!=newpassword && !newpassword.isEmpty() && null!=confirmPass && !confirmPass.isEmpty()){
				//check to make sure that the x-ldap-usename is the same as what is submitted
				if(null!=sessionUsername && sessionUsername.equals(username)){
					if(newpassword.length()>20){
						retVal.setStatus("failed");
						retVal.setMessage("New Password field length cannot be greater than 20 characters.");
					}
					else{
						if(newpassword.length()<5){
							retVal.setStatus("failed");
							retVal.setMessage("New Password field length must be greater than 4 characters.");
						}
						else{
							if(confirmPass.length()>20){
								retVal.setStatus("failed");
								retVal.setMessage("Confirm New Password field cannot be greater than 20 characters.");								
							}
							else{
								if(confirmPass.length()<5){
									retVal.setStatus("failed");
									retVal.setMessage("Confirm New Password field length must be greater than 4 characters.");	
								}
								else{
									if(newpassword.contains("<")||newpassword.contains(">")||newpassword.contains(" ")||newpassword.contains("	")){
										retVal.setStatus("failed");
										retVal.setMessage("New Password field contains illegal charcter.");
									}
									else{
										if(confirmPass.contains("<")||confirmPass.contains(">")||confirmPass.contains(" ")||confirmPass.contains("	")){
											retVal.setStatus("failed");
											retVal.setMessage("Confirm New Password field contains illegal charcter.");
										}
										else{
											//Now check to make sure that the two passwords match
											if(newpassword.equals(confirmPass)){
												//Get the user and make sure the password matches the one in the form
												Users theUser=users.findById(username);
												if(null!=theUser){
													String storedPassword=theUser.getPassword();
													//Make sure the stored password matches the old password in the form
													if(null!=storedPassword && storedPassword.equals(password)){
														theUser.setPassword(newpassword);
														//Now update the user
														users.update(theUser);
													}
													//The stored password does not match the typed in old password in the form
													else{
														retVal.setStatus("failed");
														retVal.setMessage("Invalid password");
													}
												}
												else{
													retVal.setStatus("failed");
													retVal.setMessage("Invalid user");
												}
											}
											else{
												retVal.setStatus("failed");
												retVal.setMessage("New password and Confirm New Password fields does not match");
											}
										}
									}
								}
							}
						}
					}
				}
				else{
					retVal.setStatus("failed");
					retVal.setMessage("Session error: x-ldap-username and form username doe not match");
				}
			}
			//A field value is missing
			else{
				retVal.setStatus("failed");
				retVal.setMessage("Missing field values");
			}			
		}
		else{
			retVal.setStatus("failed");
			retVal.setMessage("No input field values");
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END: retVal="+retVal);
		}		
		return retVal;
	}

	@RequestMapping(value="/Login", method=RequestMethod.GET)
	public String addModelObjectsForLoginPage(Model model, HttpSession session, HttpServletRequest request){
		String METHOD_NAME="addModelObjectForLoginPage()";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START");
		}		
		//The first thing we do is check the session to see if we have an x-ldap-username value in session, if so just 
		//forward to the correct Deploy page
		String xldapusername=(String)session.getAttribute(X_LDAP_USERNAME);
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": xldapusername="+xldapusername);
			log.debug(METHOD_NAME+": (null!=xldapusername && !xldapusername.trim().isEmpty())="+
					(null!=xldapusername && !xldapusername.trim().isEmpty()));
		}
		if(null!=xldapusername && !xldapusername.trim().isEmpty()){
			String serverName=request.getServerName();
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": serverName="+serverName);
			}
			if(null!=serverName && !serverName.toLowerCase().contains("internal")){
				return "redirect:/DeployWars";
			}
			else{
				return "redirect:/DeployWarsInternal";
			}
		}

		String loginstatus=(String)session.getAttribute("loginstatus");
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": loginstatus="+loginstatus);
		}
		if(null!=loginstatus){
			if(loginstatus.equals("failed")){
				model.addAttribute("loginstatus", "failed");
			}
			else{
				model.addAttribute("loginstatus", "success");
			}
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START");
		}
		return "Login";
	}

	@RequestMapping(value="/LoginError", method=RequestMethod.GET)
	public String addModelObjectsForLoginErrorPage(Model model, HttpSession session, HttpServletRequest request){

		return "LoginError";
	}

	@RequestMapping(value="/userlogin", method=RequestMethod.POST)
	public String userlogin(Model model, HttpSession session, HttpServletRequest req, HttpServletResponse resp, 
			@ModelAttribute("loginuser") LoginUser loginuser, BindingResult result){
		String METHOD_NAME="userlogin()";
		String retVal="redirect:/DeployWarsInternal";

		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START: loginuser="+loginuser);
			log.debug(METHOD_NAME+": (null!=loginuser)="+(null!=loginuser));
		}	
		if(null!=loginuser){
			String userName=loginuser.getUsername();
			String password=loginuser.getPassword();
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": (null!=userName)="+(null!=userName));
				log.debug(METHOD_NAME+": (null!=password)="+(null!=password));
				log.debug(METHOD_NAME+": (null!=userName && !userName.isEmpty() && userName.length()<=20)="+
						(null!=userName && !userName.isEmpty() && userName.length()<=20));
				log.debug(METHOD_NAME+": (null!=password && !password.isEmpty() && password.length()<=20)="+
						(null!=password && !password.isEmpty() && password.length()<=20));
			}
			if(null!=userName && !userName.isEmpty() && userName.length()<=20 && 
					null!=password && !password.isEmpty() && password.length()<=20){
				Users theUser=this.users.findById(userName);
				if(log.isDebugEnabled()){
					log.debug(METHOD_NAME+": (null!=theUser)="+(null!=theUser));				
				}
				if(null!=theUser){
					//change the message to invalid password by default once we have confirmed the user

					String theUserPassword=theUser.getPassword();
					if(log.isDebugEnabled()){
						log.debug(METHOD_NAME+":(null!=theUserPassword)="+(null!=theUserPassword));
						log.debug(METHOD_NAME+": (theUserPassword.equals(cred.getUsername())="+(theUserPassword.equals(userName)));
					}
					//The passwords must match
					if(null!=theUserPassword&&theUserPassword.equals(password)){

						session.setAttribute(X_LDAP_USERNAME, userName);
						resp.addHeader("X_LDAP_USERNAME", userName);

						if(log.isDebugEnabled()){
							log.debug(METHOD_NAME+": ((String)session.getAttribute(\""+X_LDAP_USERNAME+"\"))="+
									((String)session.getAttribute(X_LDAP_USERNAME)));
						}
						String servername=req.getHeader("referer");
						
						if(log.isDebugEnabled()){
							log.debug(METHOD_NAME+": servername="+servername);
						}
						if(null!=servername && !servername.toLowerCase().toLowerCase().contains("internal")){
							retVal="redirect:/DeployWars";
						}						
						else{
							retVal="redirect:/DeployWarsInternal";
						}
						model.addAttribute("loginstatus", "success");
						session.setAttribute("loginstatus", "success");
					}
					//passwords do not match
					else{
						model.addAttribute("loginstatus", "failed");
						session.setAttribute("loginstatus", "failed");
						retVal="redirect:/Login";
					}
				}
				//Could not find user
				else{
					model.addAttribute("loginstatus","failed");
					session.setAttribute("loginstatus", "failed");
					retVal="redirect:/Login";
				}
			}
			//userName and/or password are/is not valid
			else{
				model.addAttribute("loginstatus", "failed");
				session.setAttribute("loginstatus", "failed");
				retVal="redirect:/Login";
			}
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END retVal="+retVal);
		}
		return retVal;
	}


	@RequestMapping(value="/Login2", method=RequestMethod.GET)
	public String login2(Model model, HttpSession session, HttpServletRequest request){
		return "Login2";
	}

}


