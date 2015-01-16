package com.rackspace.cloud.api.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.rackspace.cloud.api.entity.Users;

/**
 * Servlet implementation class LoginServlet
 */

public class LoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static Logger log = Logger.getLogger(LoginServlet.class);
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public LoginServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request,response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String METHOD_NAME="doPost()";
	    if(log.isDebugEnabled()){
	    	log.debug(METHOD_NAME+": START:");
	    }
		
		String username=request.getParameter("username");
		String password=request.getParameter("password");
		
	    if(log.isDebugEnabled()){
	    	log.debug(METHOD_NAME+": ~~~~~username="+username);
	    	log.debug(METHOD_NAME+": ~~~~~password="+password);
	    }
	    if(log.isDebugEnabled()){
	    	log.debug(METHOD_NAME+":(null!=username)="+(null!=username));
	    }
	    if(null!=username){
	    	//Get a connection to the server 
	    	Context initContext=null;
	    	DataSource ds=null;
	    	PreparedStatement prepStmt;
	    	String ldapname=null;
	    	String theUserPassword=null;
	    	
			try {
				initContext=new InitialContext();
				Context envCtx = (Context) initContext.lookup("java:comp/env");
			    if(log.isDebugEnabled()){
			    	log.debug(METHOD_NAME+": getting datasource");
			    }
				ds=(DataSource)envCtx.lookup("jdbc/docsdbtomcat");
			    if(log.isDebugEnabled()){
			    	log.debug(METHOD_NAME+": After lookup ds="+ds);
			    }
			    Connection conn=ds.getConnection("anewuser", "Fanatical7");
			    if(log.isDebugEnabled()){
			    	log.debug(METHOD_NAME+": After getting connection");
			    }
			    String query="select * from users where ldapname='"+username+"'";
			    if(log.isDebugEnabled()){
			    	log.debug(METHOD_NAME+": query="+query);
			    }
			    ResultSet rs= conn.createStatement().executeQuery(query);

			    if(log.isDebugEnabled()){
			    	log.debug(METHOD_NAME+": After executeQuery");
			    }
		    	rs.next();
		    	
		    	ldapname=rs.getString("ldapname");
		    	theUserPassword=rs.getString("password");
		    	
		    	if(log.isDebugEnabled()){
			    	log.debug(METHOD_NAME+":ldapname="+ldapname);
			    }	    	
		    	if(null!=ldapname){
		    		HttpSession session=request.getSession();
					if(log.isDebugEnabled()){
						log.debug(METHOD_NAME+":(null!=theUserPassword)="+(null!=theUserPassword));
						log.debug(METHOD_NAME+": (theUserPassword.equals(\""+password+"\")="+(theUserPassword.equals(password)));
					}
					if(null!=theUserPassword&&theUserPassword.equals(password)){
						session.setAttribute("x-ldap-username", username);
						if(log.isDebugEnabled()){
						    log.debug(METHOD_NAME+": ((String)session.getAttribute(\"x-ldap-username\"))="+
						    ((String)session.getAttribute("x-ldap-username")));
						}
						String servername=request.getServerName();
						//By default deploy to the internal deployment site
						String redirectionUrl="/rax-autodeploy/DeployWarsInternal";
						if(null!=servername && !servername.toLowerCase().contains("internal")){
						    redirectionUrl="/rax-autodeploy/DeployWars";
						}
						if(log.isDebugEnabled()){
							log.debug(METHOD_NAME+": redirectionUrl="+redirectionUrl);
							log.debug(METHOD_NAME+": About to redriect");
						}
						try {
							response.sendRedirect(redirectionUrl);
						} 
						catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
					else{
						session.removeAttribute("x-ldap-username");
					}	    		
		    	}		    	
			} 
			catch(NamingException e){
				e.printStackTrace();
			}
			catch (SQLException e) {
				// TODO Auto-generated catch block
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

}
