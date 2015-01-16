<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
    
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
<title>Angular Test</title>
   <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/angularjs/1.2.5/angular.min.js"></script>
   
   <script type="text/javasxript" src="http://code.jquery.com/jquery-1.11.1.js"></script>
   <script type="text/javascript" src="<c:url value='/resources/scripts/login2.js'/>"></script>
</head>
<body>
    <div ng-app="loginapp">
    <form ng-controller="LoginController as log" name="loginform" id="loginformid" class="loginformclass" novalidate>
    
        <fieldset class="loginscreen">
            <div id="usernamelabelandinput">
                <label id="usernameid" class="loginlabel">User Name</label>
                <input ng-model="myusername" type="text" class="logininput" placeholder="Rackspace SSO Username" required/>
            </div>
            <div ng-model="showProgress" id="progressid" ng-show="showProgress">progress</div>
            <div id="passwordlabelandinput">
                <label id="passwordid" class="loginlabel">Password</label>
                <input ng-model="mypassword" type="password" class="logininput" placeholder="Rackspace SSO Username" required/>
            </div>
            <div id="submitbuttonid">
                <input type="button"  value="Submit" ng-click="log.userLogin()"/>
            </div>
        </fieldset>
    
    </form>
    </div>
</body>
</html>