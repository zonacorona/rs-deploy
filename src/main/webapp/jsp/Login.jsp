<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
    
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="loginapp">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Login Page</title>
</head>
<body>
   <div id="controller" ng-controller="LoginController as log">
   <div id="content" class="clearfix center-login-form">
    <div id="warning-messages"></div>
    <div id="login_wrapper" class="clearfix" >
      <div id="rackspace_logo_container"></div>
      <div id="login_block">
        <span class="welcome_message">Welcome to Docs Deployment Login</span>
        <form:form method="post" id="login-form" name="loginform" action="/rax-autodeploy/userlogin" 
                   autocapitalize="off" autocorrect="off">
          <fieldset>
            <div class="rs-control-group">
              <label for="username">Username</label>
              <input ng-model="myusername" type="text" name="username" id="id_username" 
                     data-bound-key="username" class="rs-input-large" ng-maxlength="20" ng-minlength="5" required>
              <span id="usernameerror" class="inputerror" ng-model="usernameerror" ng-show="usernameerror">
                  <i id="usernameimgrequired" class="rs-validation-indicator"></i>
                  Username is required.
              </span>

              <span id="usernametoolong" class="inputerror" ng-model="usernametoolong" ng-show="usernametoolong">
                  <i id="usernameimgmaxlength" class="rs-validation-indicator"></i>
                  Username cannot be over 20 characters.
              </span>
              
            </div>
            <div class="rs-control-group">
              <label for="password">Password</label>
              <input ng-model="mypassword" type="password" name="password" id="id_password" 
                     data-bound-key="password" class="rs-input-large" required ng-maxlength="20" ng-minlength="5" required>
              <span id="passworderror" class="inputerror" ng-model="passworderror" ng-show="passworderror">
                  <i id="passwordimgrequired" class="rs-validation-indicator"></i>
                  Password is required.
              </span>  
              <span id="passwordtoolong" class="inputerror" ng-model="passwordtoolong" ng-show="passwordtoolong">
                  <i id="passwordimgmaxlength" class="rs-validation-indicator"></i>
                  Password cannot be over 20 characters.
              </span>                           
            </div>

            <input id="id_type" name="type" type="hidden" value="password">

            <c:choose>
            <c:when test="${loginstatus=='failed'}">
                <div id="login-status">              
                    <div class="rs-control-group ">                
                      <span class="inputerror">
                        <i class="rs-validation-indicator"></i>
                        The username or password you entered is incorrect. Please try again.
                      </span>
                
                    </div>              
                </div>                    
            </c:when>
            <c:otherwise>
                 <div id="login-status" style="display:none;">              
                    <div class="rs-control-group ">                
                      <span class="inputerror">
                        <i class="rs-validation-indicator"></i>
                        The username or password you entered is incorrect. Please try again.
                      </span>
                
                    </div>              
                </div>             
            </c:otherwise>
            </c:choose>
            <div class="login-action" ng-click="log.userLogin()">
              <button class="save rs-btn rs-btn-login">Log In</button>
              <span class="form_throb" ng-show="passwordprogress" ng-model="passwordprogress"></span>
            </div>
          </fieldset>
        </form:form>
      </div>
    </div>
    <div class="links">
    
    </div>
  </div>

<%-- 
    <div class="ck-widgets-popover ck-widgets-popover-bottom-left" ng-click="toggleHelpFromInsideBox()" >
        <div class="ck-widgets-popover-outer">
            <div class="ck-widgets-popover-pointer"></div>
            <div class="ck-widgets-popover-content">
                <div>
                    <div class="">
                        <div class="help-popover">
                            <section>
                                <a href="https://manage.rackspacecloud.com/ReachForgotPassword.do" class="forgot-password">
                                    I forgot my password...
                                </a>
                                <a href="https://manage.rackspacecloud.com/ReachForgotUsername.do" class="forgot-username">
                                    I forgot my username...
                                </a>
                            </section>
                        </div>
                    </div>
                </div>
            </div>
            <div class="ck-widgets-popover-loading"></div>
        </div>
        <div class="ck-widgets-popover-background"></div>
    </div>
--%>    
</div>
</body>
   <link type="text/css" rel="stylesheet" href="<c:url value='/resources/css/login.css'/>" />   
   <script type="text/javascript" src="<c:url value='/resources/scripts/jquery-1.10.2.min.js'/>"></script>   
   <script type="text/javascript" src="<c:url value='/resources/scripts/angular.min.js'/>"></script>
   <script type="text/javascript" src="<c:url value='/resources/scripts/login.js'/>"></script>



</body>


</html>
