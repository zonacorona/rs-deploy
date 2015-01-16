<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>   

<%@page import="java.util.Properties"%>
<%@page import="com.rackspace.cloud.api.controller.ExternalWarController"%>
<%@page import="java.io.FileInputStream"%>
<%@page import="java.io.InputStream"%>
<%@page import="java.io.File"%>
   
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>  

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html ng-app>
<head>
    <link type="text/css" rel="stylesheet" href="<c:url value='/resources/css/autodeploy.css'/>" />  
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Publish Internal Documentation</title>
</head>
<body id="controller" ng-controller="AutoDeployCtrl">
<%
    /*
      The code below is necessary so that by the time we get to ExternalWarController.doPost(), which processes
      the ajax call: /rax-autodeploy/deployToProd?..., the x-ldap-username exists in session.     
    */
    String userName=request.getHeader("x-ldap-username");
    String groups=request.getHeader("x-ldap-groups");
    if(null!=userName && !userName.isEmpty()){
	    session.setAttribute("x-ldap-username", userName);
    }
    else{
	    userName=(String)session.getAttribute("x-ldap-username");
    }
    if(null!=groups && !groups.isEmpty()){
	    session.setAttribute("x-ldap-groups",groups);
    }
    else{
	    groups=(String)session.getAttribute("x-ldap-groups");
    }      
    //If the userName is null or empty, we check to see if debuggin is enabled, if debugging is enabled
    //just display everything
    if(userName==null||userName.isEmpty()){
    	//first we need to check if debug is enabled
		File file=new File("/home/docs/DeployWars/props.properties");			
		InputStream inny=null;

		if(file.exists()){
			inny=new FileInputStream(file);
		}
		else{
			inny=ExternalWarController.class.getClassLoader().getResourceAsStream("/props.properties");	
		}
		
		Properties propsFile=new Properties();
		propsFile.load(inny);
		
		String debug=propsFile.getProperty("debug","false");
		//debug is set to false, we should redirect to an error page
		if(debug.equalsIgnoreCase("false")){
		
            response.setStatus(301);
    	    response.setHeader("Location", "http://docs-internal-staging.rackspace.com/rax-autodeploy/jsp/LoginErrorInt.jsp");
    	    response.setHeader("Connection","close");
		}
		else{
			//Otherwise, do nothing and allow InternalWarController to render the page
		}
    }    
    
%>
<%-- 
Should Display: ${shouldDisplay}
Should Freeze: ${shouldFreeze}
doesUserHaveAccess: ${doesUserHaveAccess}
--%>

<br><br>
<div>
  <div id="top">
  <img id="doctoolsimg" src="<c:url value='/resources/images/doctoolslogo2.png'/>"/>
  
  <c:choose> 
    <c:when test="${shouldDisplay==true}">
    
 

        <form id="resetformid" name="resetform"> 
          <input type="button" id="clearbutton" 
          ng-click="clearForm()" 
          ng-mouseenter="changeButtonColor('clearbutton','#F0E080')"
          ng-mouseleave="changeButtonColor('clearbutton','#FFF380')"
          value="Reset"/>  
          
        </form>  
                         
      
      <c:if test="${isUserAnAdmin}">
        <form method="POST" action="/InstalledWarsServlet" id="freezeuiformid" name="fullpageloadform">
          <input type="radio" name="freeze" value="false" checked>False <br>
          <input type="radio" name="freeze" value="true">True  
        
          <input type="hidden" name="isexternal" id="isexternalid" value="true"/>

          <input type="button" id="freezeuibutton" 
            ng-click="freezeUI($event, '${loggedInUser}', 'true');" 
            ng-mouseenter="changeButtonColor('freezeuibutton','#F0E080');"
            ng-mouseleave="changeButtonColor('freezeuibutton','#FFF380');"
            value="Freeze UI"/>               
        </form>
        <br>      
      </c:if>
      
        <form id="changepasswordform"> 
          
          <input type="button" 
                 style="background-color:#5FAAFA;border-radius: 16px;border: 4px solid grey;padding: 7px;cursor:pointer;position: relative;bottom: 40px;left: 17px;"
                 id="changepasswordbtn" 
                 class="changepasswordbtn"
                 ng-click="openChangePasswordForm()" 
                 ng-mouseenter="changeButtonColor('changepasswordbtn','#3782D2')"
                 ng-mouseleave="changeButtonColor('changepasswordbtn','#5FAAFA')"
                 value="Change My Password"/>  
          
        </form>             

      <c:if test="${!shouldFreeze}">
  
        <form id="filterformid" name="filterform">
          <span id="filterspan" class="filterspan">    
            <select name="docbooks" id="docbooks" multiple size="6">
              <option class="dockbookoption" value="All">All</option>
              <c:forEach var="adocbook" items="${intFilters}" varStatus="status">
                <option class="dockbookoption" value="deployrow<c:out value="${(status.index+1)}"/>"><c:out value="${adocbook}"/></option>
              </c:forEach>             
            </select> 
            <input id="filterbuttonid" class="filterbutton" type="button" value="Filter" 
                 ng-click="filterWars($event)"
                 ng-mouseenter="changeButtonColor('filterbuttonid','#F0E080');"
                 ng-mouseleave="changeButtonColor('filterbuttonid','#FFF380');"/> 
          </span>   
        </form>

       </c:if>
<%-- End of div id="top" --%>       
  </div>
    <div id="detailsid" class="details" style="display:none;" >

      <span style="white-space:nowrap;"><span ng-click="closeDetails($event);" id="closelinkid">Close</span><img id="cloudimageid" src="<c:url value='/resources/images/clouds.png"'/>"/></span>
      <div id="divcontentid">
        <table class="detailstableclass" id="detailstableid">
          <tr>
            <th class="detailsth" id="booknameth">Book Name</th>
            <th class="detailsth" id="deployedwarth">War Name</th>
            <th class="detailsth" id="lastdeployth">Prod Deploy Time</th>
            <th class="detailsth" id="lastdeployth">Last Build Status</th>                     
          </tr>
          <tr ng-repeat="detail in details" ng-model="details">
            <td class="detailscol">{{detail.docName}}</td>
            <td class="detailscol">{{detail.folderName}}</td>
            <td class="detailscol">{{detail.lastModified}}</td>
            <td class="detailscol">
                <a href="{{detail.href}}" target="_blank">
                   <img class="jenkinsresultimg" 
                        src="{{detail.imgSrc}}" 
                        title="{{detail.title}}"/>
                </a>            
            </td>
          </tr>
        </table> 
      </div>
    <%-- End of detailsid div --%>
    </div>
    <div id="progressid" style="display:none;">
      <img id="progimgid" src="<c:url value='/resources/images/progress-indicator.gif'/>"/>
    </div>
    <c:choose>
      <c:when test="${shouldFreeze}">
          <div id="freezemessageid">
          Page frozen in preparation for server shutdown. No deployments or reverts allowed at this time.
          Please try again later.
          </div>
      </c:when>          
      <c:otherwise>    
        <table class="deploytable" id="deploytable">
          <tr>
            <th id="docbookprojth">Docbook Project</th>
            <th id="deployedwarth">Book Name</th>
            <th id="detailsth">Details</th>
            <th colspan="3"></th>          
          </tr>
          <%--Display the ROOT.war row only if the user has admin access --%>
          <c:if test="${isUserAnAdmin}">
            <%-- 
            <form METHOD="GET" action="/rax-autodeploy/InstalledWarsServlet" name="deployform0">
            --%>           
              <tr class="deployrow" id="deployrow0">
                <td>ROOT.war</td>
                <td class="deploywarcol">
                  <ul>
                    <li>
                    <input type="checkbox" class="warfoldernames internal" name="warfoldernames" value="ROOT" title="ROOT" >
                      <span style="position:relative;left:-30px;margin:5px 5px 5px 33px;" title="ROOT">ROOT</span>
                    </li>
                  </ul>          
                </td>
                <td class="detailscol">
              
                    <input type="image" src="<c:url value='/resources/images/magnifyingglass.jpeg'/>" class="detailsimage internal" id="detailsimageid0" title="Click for detailed information"
                      ng-click="handleDetails($event);"/>
                    <%--
                    <img src"<c:url value='/resources/images/magnifyingglass.jpeg'>" class="detailsimage internal" id="detailsimageid0" title="Click for detailed information"
                      style="width:20px;"/>
                    --%>               
             
                </td>

                <td class="deploybuttoncol">             
                  <input style="background-color:rgb(95,170,259);" id="deploybutton0" type="button" class="deploybutton external" name="deploybutton" value="Deploy to Production"  
                    ng-click="deployToProd('deployrow0','com.rackspace.cloud.api~~~rax-indexwar','0','true');"
                    ng-mouseenter="changeButtonColor('deploybutton0','#3782D2')"
                    ng-mouseleave="changeButtonColor('deploybutton0','#5FAAFA')"
                  /> 
                  <p>
                    <input style="background-color:rgb(204,102,51);" id="revertbutton0" type="button" class="revertbutton external" name="revertbutton" value="Revert to Backup"  
                      ng-click="revertDeployedWarsConfirmation('deployrow0',
                      'com.rackspace.cloud.api~~~rax-indexwar','0', 'true')"
                      ng-mouseenter="changeButtonColor('revertbutton0','#AF6633')"
                      ng-mouseleave="changeButtonColor('revertbutton0','#CC6633')"
                    />               
                  </p>                           
                </td>
                <td class="successcol">
                  <img class="successicons" style="display:none;position:relative;right:240px;" id="successicon0" alt="Success" src="<c:url value='/resources/images/success_icon.png'/>" />
                  <img class="erroricons" style="display:none;position:relative;right:240px" id="erroricon0" alt="Error" src="<c:url value='/resources/images/icon_error.gif'/>" />
                  <img class="progimg" id="progimg0" style="display:none;position:relative;right:240px" alt="progress icon" src="<c:url value='/resources/images/progress-indicator.gif'/>"/>
                </td>
                <td class="returnmessage" id="returnmessage0">
                  <span style="display:none;position:relative;right:550px;" class="returnmessageclass" id="returnmessagespan0">
              
                  </span>
                  <span class="cannotdeploymessage" style="display:none;">
                    <a href="http://docs-staging.rackspace.com/jenkins/job/com.rackspace.cloud.api---rax-indexwar/" target="_blank">
                    A jenkins build</a> is running, deployment is not available at this time.<p>Refresh browser to check on status.</p>
                  </span>
                </td>                      
              </tr>    
           <%-- 
            </form>
            --%>                      
          <%-- end of <c:if> for when user has admin access --%>
          </c:if>
          
         <c:forEach var="awar" items="${intWars}" varStatus="status">
             <tr class="deployrow" id="deployrow${(status.index+1)}" >
                 <td>
                     <c:out value="${awar.value.pomName}"/>
                 </td>
                 <td class="deploywarcol">
                     <ul>
                     <c:forEach var="adocnamenfolder" items="${awar.value.docNameNFolderNamesList}">              
                  
                      <c:choose> 
                          <c:when test="${fn:endsWith(adocnamenfolder.folderName,'internal')}">
                             <c:set var="docname" value="${adocnamenfolder.docName}" scope="page"/>
                             <c:set var="docnamelengthpadding" value="${fn:length(docname)+269}" scope="page"/>
                             <li>
                                 <c:choose>    
                                     <c:when test="${awar.value.clouddocsDocbook=='true'}"> 
                                         <input type="checkbox" class="warfoldernames internal" name="warfoldernames" value="<c:out value='${adocnamenfolder.folderName}'/>" title="<c:out value='${adocnamenfolder.folderName}'/>" />
                                         <div class="deployedbooknames" title="<c:out value='${adocnamenfolder.folderName}'/>">
                                             <c:out value="${adocnamenfolder.docName}"/>
                                             <img class="internalicon" src="resources/images/internal.png"/>
                                         </div>
                                     </c:when>
                                     <c:otherwise>
                                         <div class="deployedbooknames" title="<c:out value='${adocnamenfolder.folderName}'/>">
                                             <c:out value="${adocnamenfolder.docName}"/> cannot be deployed.
                                              It does not depend on the <a href="https://github.com/rackerlabs/clouddocs-maven-plugin-docbook">clouddocs-maven-plugin-docbook plugin.</a>
                                         </p>
                                     </c:otherwise>
                                 </c:choose>
                             </li> 
                          </c:when>
                      
                          <c:when test="${fn:endsWith(adocnamenfolder.folderName,'reviewer')}">
                              <c:set var="docname" value="${adocnamenfolder.docName}" scope="page"/>
                              <c:set var="docnamelengthpadding" value="${fn:length(docname)+269}" scope="page"/>

                              <li> 
                              
                              <c:choose>    
                                  <c:when test="${awar.value.clouddocsDocbook=='true'}">  
                                      <input style="display:none;" type="checkbox" class="warfoldernames external" name="warfoldernames" value="<c:out value='${adocnamenfolder.folderName}'/>" title="<c:out value='${adocnamenfolder.folderName}'/>" ng-change="handleCheckBoxChange()"/>                            
                                      <div class="deployedbooknamesreviewer" title="<c:out value='${adocnamenfolder.folderName}'/>">
                                          <c:out value="${adocnamenfolder.docName}"/>
                                          <img class="reviewicon" src="<c:url value='/resources/images/rimage.jpg'/>"/>
                                      </div>
                                   </c:when>
                                   <c:otherwise>
                                      <div class="deployedbooknamesreviewer" title="<c:out value='${adocnamenfolder.folderName}'/>">
                                          <div class="deployedbooknamesreviewer" title="<c:out value='${adocnamenfolder.folderName}'/>"> cannot be deployed.
                                          It does not depend on the <a href="https://github.com/rackerlabs/clouddocs-maven-plugin-docbook">clouddocs-maven-plugin-docbook plugin.</a>
                                      </div>
                                   </c:otherwise>
                              </c:choose>                              
                              </li>                                                      
                          </c:when>                      
                      
                          <c:otherwise>
                              <li>
                              <c:choose>    
                                  <c:when test="${awar.value.clouddocsDocbook=='true'}">                              
                                      <input type="checkbox" class="warfoldernames external" name="warfoldernames" value="<c:out value='${adocnamenfolder.folderName}'/>" title="<c:out value='${adocnamenfolder.folderName}'/>" />
                                      <span style="position:relative;left:-30px;margin:5px 5px 5px 33px;" title="<c:out value='${adocnamenfolder.folderName}'/>"><c:out value="${adocnamenfolder.docName}"/></span>
                                  </c:when>
                                  <c:otherwise>
                                      <span style="position:relative;left:-30px;margin:5px 5px 5px 33px;" title="<c:out value='${adocnamenfolder.folderName}'/>"><c:out value="${adocnamenfolder.docName}"/> cannot be deployed.</span>
                                      It does not depend on the <a href="https://github.com/rackerlabs/clouddocs-maven-plugin-docbook">clouddocs-maven-plugin-docbook plugin.</a>
                                      
                                  </c:otherwise>
                              </c:choose>                              
                              </li>                              
                          </c:otherwise>
                      </c:choose>                 
                     </c:forEach>
                     </ul>
                 </td>  
                 <td class="detailscol">
                  
                     <input type="image" src="<c:url value='/resources/images/magnifyingglass.jpeg'/>" class="detailsimage internal" id="detailsimageid${status.index+1}" title="Click for detailed information"
                     ng-click="handleDetails($event);" />
                     <%-- 
                     <img src="<c:url value='/resources/images/magnifyingglass.jpeg'/>" class="detailsimage internal" id="detailsimageid${status.index+1}" title="Click for detailed information"
                     style="width:20px;"/>
                     --%>                  
           
                     <input type="hidden" name="groupid" id="groupid" value="${status.index+1}" />
                     <input type="hidden" name="artifactid" id="artifactid" value="${status.index+1}" />  
                     <input type="hidden" name="rownumber" id="rownumber" value="${status.index+1}" /> 
                 </td>

                 <td class="deploybuttoncol">   
                 
                 <c:choose>    
                     <c:when test="${awar.value.clouddocsDocbook=='true'}">      
                         <input style="display:<c:out value='${awar.value.displayDeployButton}'/>;" id="deploybuttonid${status.index+1}" type="button" class="deploybutton external" name="deploybutton" value="Deploy to Production"  
                           ng-click="deployToProd('deployrow${status.index+1}','<c:out value='${awar.value.id}'/>','${status.index+1}','false');"
                           ng-mouseenter="changeButtonColor('deploybuttonid${status.index+1}','#3782D2')"
                           ng-mouseleave="changeButtonColor('deploybuttonid${status.index+1}','#5FAAFA')"                      
                         /> 
                        <p>
                        <input style="display:<c:out value='${awar.value.displayDeployButton}'/>;" id="revertbuttonid<c:out value='${status.index+1}'/>" type="button" class="revertbutton external" name="revertbutton" value="Revert to Backup"  
                           ng-click="revertDeployedWarsConfirmation('deployrow${status.index+1}',
                           '<c:out value='${awar.value.id}'/>','${status.index+1}', 'false')"
                           ng-mouseenter="changeButtonColor('revertbuttonid${status.index+1}','#AF6633')"
                           ng-mouseleave="changeButtonColor('revertbuttonid${status.index+1}','#CC6633')"
                         />               
                        </p>  
                    </c:when>
                    <c:otherwise>
                        <div id="doesnotdependonplugin">
                        This project cannot be deployed because it does not depend on the clouddocs-maven-plugin-docbook plugin.
                        To use the Deploy page, you must depend on the clouddocs-maven-plugin-docbook plugin:
                        <a href="https://github.com/rackerlabs/clouddocs-maven-plugin-docbook">clouddocs-maven-plugin-docbook repo</a>
                        </div>
                    </c:otherwise>                  
                 </c:choose>                                             
          </td>
          <td class="successcol">
              <img class="successicons" style="display:none;position:relative;right:240px;" id="successicon${status.index+1}" alt="Success" src="<c:url value='/resources/images/success_icon.png'/>" />
              <img class="erroricons" style="display:none;position:relative;right:240px" id="erroricon${status.index+1}" alt="Error" src="<c:url value='/resources/images/icon_error.gif'/>" />
              <img class="progimg" id="progimg${status.index+1}" style="display:none;position:relative;right:240px" alt="progress icon" src="<c:url value='/resources/images/progress-indicator.gif'/>"/>
          </td>
          <td class="returnmessage" id="returnmessage${status.index+1}">
              <span style="display:none;position:relative;right:550px;" class="returnmessageclass" id="returnmessagespan${status.index+1}">
              
              </span>
              <span class="cannotdeploymessage" style="display:<c:out value='${awar.value.displayCannotDeployMessage}'/>;">
                  <a href="<c:out value='${awar.value.lastJenkinsBuildUrl}'/>" target="_blank">
                  A jenkins build</a> is running, deployment is not available at this time.<p>Refresh browser to check on status.</p>
              </span>
          </td>                          
                             
             </tr>          
         </c:forEach>
          <%--
          <c:choose>
            
            
            <c:when test="${xLdapUserName!=null && xLdapUserName!=''}">
              <span data-ng-init="getExtWars('${xLdapUserName}')"></span>
             <tr class="deployrow" id="deployrow" ng-repeat="war in extWars">
               
             </tr>
            </c:when>
            <c:otherwise>
                <td colspan="6">
                  Session has timed out, please delete browser cache and log back in
                </td>
            </c:otherwise>
          </c:choose>
          
          --%>
          <%-- iterate through all the war folders installed on tomcat, grouping them accordingly --%>
          
          
          
          <%-- 
          <form METHOD="GET" action="/rax-autodeploy/InstalledWarsServlet" name="deployform${status.index+1}">
          --%>
          
          <%-- 
          </form>
          --%>
           
        </table>
      </c:otherwise>
     </c:choose>
      
    </c:when>
    <c:otherwise>
      <div class="wrongserver">
        This page should only be used on the INTERNAL STAGING server. If you need to deploy internally, go to: <a href="http://docs-internal-staging.rackspace.com/rax-autodeploy/DeployWarsInternal">Docs Internal Deploy</a>
      </div>    
    </c:otherwise>
  </c:choose>

</div>

<div id="changepasswordinternalid" 
     ng-show="changepasswordwidgetinternal" 
     class="changepasswordbox"
     style="background-color: #FFFFFF;
            background-image: none;
            border-radius: 5px 5px 5px 5px;
            border-style: solid;
            border-color: #CCCCCC; 
            color: #222222;
            font-family: arial;
            left: 38%;
            position: fixed;
            top: 15%;
            z-index: 100 !important;">
        <form name="changepasswordform" novalidate>
            <span ng-click="closePassword()" id="closechangepasswordlinkid">Close</span>
            <div class="rs-control-group">
              <label class="passchangelabel" for="username">Username</label>
              <input type="text" name="username" id="id_username" 
                     data-bound-key="username" class="rs-input-large" readonly="readonly" value="${loggedInUser}">              
            </div>
            
            <div class="rs-control-group">
              <label class="passchangelabel" for="password">Current Password</label>
              <input ng-model="mypasswordcurrent" type="password" name="passwordcurrent" id="id_passwordcurrent" 
                     data-bound-key="password" class="rs-input-large" required>
              <span id="passworderrorcurrent" class="inputerror" style="display:none;">
                  <i id="passwordimgcurrentrequired" class="rs-validation-indicator"></i>
                  Password is required.
              </span>  
              <span id="passwordtoolongcurrent" class="inputerror" style="display:none;">
                  <i id="passwordimgcurrentmaxlength" class="rs-validation-indicator"></i>
                  Password cannot be over 20 characters.
              </span>                           
            </div>
            <div class="rs-control-group">
              <label class="passchangelabel" for="password">New Password</label>
              <input ng-model="mypasswordnew" type="password" name="passwordnew" id="id_passwordnew" 
                     data-bound-key="password" class="rs-input-large" required>
              <span id="passworderrornew" class="inputerror" style="display:none;">
                  <i id="passwordimgnewrequired" class="rs-validation-indicator"></i>
                  New Password is required.
              </span>  
              <span id="passwordtoolongnew" class="inputerror" style="display:none;">
                  <i id="passwordimgnewmaxlength" class="rs-validation-indicator"></i>
                  Password cannot be over 20 characters.
              </span>                           
            </div>
            <div class="rs-control-group">
              <label class="passchangelabel" for="password">Confirm New Password</label>
              <input ng-model="mypasswordconfirm" type="password" name="password" id="id_passwordconfirmnew" 
                     data-bound-key="password" class="rs-input-large" required>
              <span id="passworderrorconfirmnew" class="inputerror" style="display:none;">
                  <i id="passwordimgconfirmnewrequired" class="rs-validation-indicator"></i>
                  Confirm New Password is required.
              </span>  
              <span id="passwordtoolongconfirmnew" class="inputerror" style="display:none;">
                  <i id="passwordimgconfirmnewmaxlength" class="rs-validation-indicator"></i>
                  Confirm New Password cannot be over 20 characters.
              </span>     
              <span id="passwordsdontmatch" class="inputerror" style="display:none;">
                  <i id="passwordimgdontmatch" class="rs-validation-indicator"></i>
                  Passwords do not match
              </span>                                     
            </div>
           

            <div class="login-action">
              <button class="save rs-btn rs-btn-login"
                      id="changethepasswordbtn"
                      style="background-color:#5FAAFA;" 
                      ng-click="chagePassword()" 
                      ng-mouseenter="changeButtonColor('changethepasswordbtn','#3782D2')"
                      ng-mouseleave="changeButtonColor('changethepasswordbtn','#5FAAFA')">
                  Submit
              </button>
              <span class="form_throb" ></span>
            </div>
        </form>          
</div>

    
</body>   
   <script type="text/javascript" src="<c:url value='/resources/scripts/jquery-1.10.2.min.js'/>"></script>
   <script type="text/javascript" src="<c:url value='/resources/scripts/jquery.lightbox.js'/>"></script>
   <script type="text/javascript" src="<c:url value='/resources/scripts/angular.min.js'/>"></script>
   <script type="text/javascript" src="<c:url value='/resources/scripts/angJsAutodeploy.js'/>"></script> 
   <%-- 
   <script type="text/javascript" src="<c:url value='/resources/scripts/angular.min.js'/>"></script>
   --%>
</html>