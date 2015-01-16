<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
    
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>User Administration</title>
</head>
<body ng-app>
<%
    //String ldapUser=(String)session.getAttribute("x-ldap-username");
    //There is no logged in user,go toe DeploymentAdmin.jsp to force login
    //if(null==ldapUser||ldapUser.isEmpty()){
    	//response.setStatus(301);
    	//response.setHeader("Location", "/rax-autodeploy/DeployAdmin");
    	//response.setHeader("Connection","close");
    //}

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
    String propsDebug=(String)request.getAttribute("debug");
    if(userName==null||userName.isEmpty()){
    	if(propsDebug==null||(!propsDebug.equalsIgnoreCase("true"))){
    		String serverName=request.getServerName();    		
    	    response.setStatus(301);
    	    if(null!=serverName && 
    	      (serverName.toLowerCase().contains("content-services")||(serverName.toLowerCase().contains("staging-haas-server")))){
    	    	response.setHeader("Location", "http://content-services-staging.rackspace.com/rax-autodeploy/jsp/LoginErrorExt.jsp");
    	    }
    	    else{
    	    	response.setHeader("Location", "http://docs-staging.rackspace.com/rax-autodeploy/jsp/LoginErrorExt.jsp");
    	    }
    	    response.setHeader("Connection","close");    	
    	}
    }
    
%>
<div id="controller" ng-controller="DeployAdminCtrl">
    <c:choose>
    <c:when test="${hasAccess}">
	<div id="top"> <img id="doctoolsimgid" src="/rax-autodeploy/resources/images/doctoolslogo4.png" />
		<form id="filterformid" name="filterform">
			<span id="filterspan" class="filterspan"> <select
				name="filterselect" id="filterselectid" size="6">
					<option class="filteroption" value="activeonly" selected="selected">Active
						Users Only</option>
					<option class="filteroption" value="inactiveonly">Inactive
						Users Only</option>
					<option class="filteroption" value="all">All Users</option>
			</select>

			</span>

		</form>
	</div>

    <table class="deployhistable" id="deploymenthistorytableid">
		<tr>
			<th class="deployadminheaderclass">&nbsp;</th>
			<th class="deployadminheaderclass"><div id='spanuserid'>User</div>
				<div id="createnewuserbuttonareaid">
					<input id="createnewusertopbtnid" type="button"
						value="Create New User"
						ng-click="showCreateUserForm();"
						ng-mouseenter="changeButtonColor('createnewusertopbtnid','#A18191');"
						ng-mouseleave="changeButtonColor('createnewusertopbtnid','#AA8899');">
				</div>
			</th>
			<th class="deployadminheaderclass">LDAP Name</th>
			<th class="deployadminheaderclass">Status</th>
			<th class="deployadminheaderclass groupnameheaderclass">
			    <div id="groupnamestextid">
			        Group Names 
			    </div>
				<div id="grouprelatedbuttonsid">
				    <input type="button" value="Create Group" id="creategroupbtnid"
				    ng-click="showCreateGroupForm();"
				    ng-mouseenter="changeButtonColor('creategroupbtnid','#A18191');"
				    ng-mouseleave="changeButtonColor('creategroupbtnid','#AA8899');">
				    <%-- 
				    <input type="button" value="Delete Group" id="deletegroupbtnid"
				    ng-click="showDeleteGroupForm();"
				    ng-mouseenter="changeButtonColor('deletegroupbtnid','#A18191');"
				    ng-mouseleave="changeButtonColor('deletegroupbtnid','#AA8899');">
				    --%>
				</div>

			</th>
		</tr>
		<tr ng-repeat="aUser in usersList" ng-model="usersList" id="row{{$index}}" >
		    <td ng-class-even="'deploymenthistortableevenrow'" ng-class-odd="'deploymenthistortableoddrow'" class="deployadmincolclass">
		        {{aUser.count}}
		    </td>
			<td ng-class-even="'deploymenthistortableevenrow'" ng-class-odd="'deploymenthistortableoddrow'" class="deployadmincolclass">
			    {{aUser.user.fname}}
		    </td>
			<td ng-class-even="'deploymenthistortableevenrow'" ng-class-odd="'deploymenthistortableoddrow'" class="deployadmincolclass">
			    {{aUser.user.ldapname}}
			</td>
			<td ng-class-even="'deploymenthistortableevenrow'" ng-class-odd="'deploymenthistortableoddrow'" class="deployadmincolclass">
			    {{aUser.user.status}}
			</td>
			<td ng-class-even="'deploymenthistortableevenrow'" ng-class-odd="'deploymenthistortableoddrow'" class="deployadmincolclass">
				<ul>
				    <li ng-repeat="aGroup in aUser.groupsList" >
				        {{aGroup.name}}
				    </li>
				</ul> 
				<input id="editGroupButton{{aUser.count}}"
				       type="button" class="deployadmineditbutton" name="deploybutton"
				       value="Edit"
				       ng-click="editMembersRow($event,aUser.user.ldapname)"
				       ng-mouseenter="changeButtonColor('editGroupButton'+aUser.count,'#AABBCC');"
                       ng-mouseleave="changeButtonColor('editGroupButton'+aUser.count,'#CCDDEE');"/>

			</td>		    
		    
		</tr>
	</table>
	<div id="progressid" style="display: none;">
		<img id="progimgid" src="/rax-autodeploy/resources/images/progress-indicator.gif" />
	</div>


	<div id="createuserformdivid" style="display: none;">
		<form class='newuserform' id="createuserformid">
			<div id='createnewusertitleid'>
				Create A New User <span id='closetextid' ng-click="closeAndClearForm('createuserformdivid','createuserformid')">Close</span>
			</div>
			<br>
			<div class='createformclass'>
				First name: <input class="createforminputclass" id='fnameformid' type='text' name='fname' size='40'><span class="asteriskclass" id='fnameformasteriskid' style='display:none;'>*</span><br>
			</div>
			<div class='createformclass'>
				Last name: <input class="createforminputclass" id='lnameformid' type='text' name='lname' size='40'><span class="asteriskclass" id='lnameformasteriskid' style='display:none;'>*</span><br>
			</div>
			<div class='createformclass'>
				LDAP name: <input class="createforminputclass" id='ldapnameformid' type='text' name='ldapname' size='70'><span class="asteriskclass" id='ldapnameformasteriskid' style='display:none;'>*</span><br>
			</div>
			<div class='createformclass'>
				Email: <input class="createforminputclass" id='emailformid' type='text' name='email' size='70'><span class="asteriskclass" id='emailformasteriskid' style='display:none;'>*</span><br>
			</div>
			<br> <br> <br> 
			<input id='createnewuserbottombtnid'
				type='button' value='Create User'
				ng-mouseenter="changeButtonColor('createnewuserbottombtnid','#FFEE66');"
				ng-mouseleave="changeButtonColor('createnewuserbottombtnid','#FFFF66');"
				ng-click="createNewUser($event,'${loggedInUser}');"> 
			<input
				id="cancelcreatenewuserbtnid" type="button" value="Cancel"
				ng-mouseenter="changeButtonColor('cancelcreatenewuserbtnid','#FFEE66');"
				ng-mouseleave="changeButtonColor('cancelcreatenewuserbtnid','#FFFF66');"
				ng-click="closeAndClearForm('createuserformdivid','createuserformid')">

		</form>
	</div>
	<div id="creategroupformdivid" style="display:none;">
		<form class='creategroupform' id="creategroupformid">
			<div id='createnewgrouptitleid'>
				Create A New Group <span id='closenewgrouptextid' ng-click="closeAndClearForm('creategroupformdivid','creategroupformid')">Close</span>
			</div>
			<br>
			<div class='creategroupformclass'>
				Group name: <input class="creategroupforminputclass" id='creategroupnameid' type='text' name='groupname' size='60'><span class="asteriskclass" id='groupnameformasteriskid' style='display:none;'>*</span><br>
			</div>
			<br> <br> <br> 
			<input id='createnewgroupid'
				type='button' value='Create Group'
				ng-mouseenter="changeButtonColor('createnewgroupid','#FFEE66');"
				ng-mouseleave="changeButtonColor('createnewgroupid','#FFFF66');"
				ng-click="createNewGroup($event,'${loggedInUser}');"> 
			<input
				id="cancelcreategroupbtnid" type="button" value="Cancel"				
				ng-mouseenter="changeButtonColor('cancelcreategroupbtnid','#FFEE66');"
				ng-mouseleave="changeButtonColor('cancelcreategroupbtnid','#FFFF66');"
				ng-click="closeAndClearForm('creategroupformdivid','creategroupform')">

		</form>	
	</div>
    <div id="deletegroupformdivid" style="display:none;">
        <form id='deletegroupformid'>
            <div id='groupstitleid'>Select Groups to Delete: </div>
            <span id='closedeletegroupsid' onclick='closeCreateGroups();'>Close</span>
            <div id='groupscolumnid'>
                <input class="groupnamecheckbox" type="checkbox" name="group" value="{{}}" >{{}}<br>
            </div>
            <br>
            <input id='deletegroupsbtnid' type='button' onclick="deleteGroups(event);" value='Delete'
                ng-mouseenter="changeButtonColor('deletegroupsbtnid','#FFEE66');" 
                ng-mouseleave="changeButtonColor('deletegroupsbtnid','#FFFF66');">
            <input id='canceldeletegroupsid' type='button' onclick='cancelDeleteGroups();' value='Cancel'
                ng-mouseenter="changeButtonColor('canceldeletegroupsid','#FFEE66');" 
                ng-mouseleave="changeButtonColor('canceldeletegroupsid','#FFFF66');">    
        </form>
    </div>
	<div id="editgroupnamesformdivid" style="display: none;">
	    <form id="editgroupnamesformid">
	        <div id='userstatusid'></div>
	        <div id='updategroupsclosetext' ng-click="closeAndClearForm('editgroupnamesformdivid',null)">Close</div>
	        <div id='statusid' style="display:none;">Status:
	            <div class='statusclass'><input id="statusinputactiveid" type='radio' name='status' value='active' checked>active <br></div>
	            <span class='statusclass'><input id="statusinputinactiveid" type='radio' name='status' value='inactive'>inactive <br></span>
	        </div> 
	        <div id='usergroupmembershipid'>Group Membership:</div> 
	        <ul id='memberscolumnid' >
	            <li ng-repeat="member in members" >
	                <input class="groupnamecheckbox" type="checkbox" name="member" value="{{member.groupName}}"  ng-model="member.checked">{{member.groupName}}<br>
	            </li>
	        </ul> 
	        <br>
	        <input id="updatebuttonid" type="button" ng-click="updateGroupsForUser($event,'${loggedInUser}');" value="Update"	         
	               ng-mouseenter="changeButtonColor('updatebuttonid','#7FAACA');" 
                   ng-mouseleave="changeButtonColor('updatebuttonid','#5FAAFA');"
	        />
	        <input id="cancelbuttonid" type="button" ng-click="closeAndClearForm('editgroupnamesformdivid',null)" value="Cancel"
	               ng-mouseenter="changeButtonColor('cancelbuttonid','#7FAACA');" 
                   ng-mouseleave="changeButtonColor('cancelbuttonid','#5FAAFA');"	        
	        /> 	        
	        <input id="cancelbuttonid" type="hidden" name="foruser" id="foruserid" ng-model="foruser" value="{{foruser}}" />  
	    </form>
	</div> 
	</c:when>
	<c:otherwise>
	    <H3>User: ${loggedInUser} does not have access to this page.</H3>
	</c:otherwise>
	</c:choose>   	
</div>
</body>

    <link rel="stylesheet" type="text/css" href="<c:url value='/resources/css/deploymenthistory.css'/>"/>
    <link rel="stylesheet" type="text/css" href="<c:url value='/resources/css/deployadmin.css'/>" />
    <script type="text/javascript" src="<c:url value='/resources/scripts/jquery-1.10.2.min.js'/>"></script>
    <script language="javascript" type="text/javascript" src="<c:url value='/resources/scripts/angJsDeployAdmin.js'/>"></script>
    <script type="text/javascript" src="<c:url value='/resources/scripts/angular.min.js'/>"></script>
    
</html>