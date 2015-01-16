<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
    
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Login Page</title>
</head>
<body>
   <div id="controller" ng-controller="LoginController">
   <div id="content" class="clearfix center-login-form">
    <div id="warning-messages"></div>
    <div id="login_wrapper" class="clearfix" >
      <div id="rackspace_logo_container"></div>
      <div id="login_block">
        <span class="welcome_message">Welcome to Docs Deployment Login</span>
        <form method="post" id="login-form" autocapitalize="off" autocorrect="off" name="loginform" action="/rax-autodeploy/LoginServlet">
          <fieldset>
            <div class="rs-control-group">
              <label for="username">Username</label>
              <input type="text" name="username" id="id_username" data-bound-key="username" class="rs-input-large">
              <span id="usernameerror" class="inputerror" style="display:none;">
                  <i id="usernameimg" class="rs-validation-indicator"></i>
                  Username is required.
              </span>

              <span id="usernametoolong" class="inputerror" style="display:none;">
                  <i id="usernameimg" class="rs-validation-indicator"></i>
                  Username cannot be over 20 characters.
              </span>
              
            </div>
            <div class="rs-control-group">
              <label for="password">Password</label>
              <input type="password" name="password" id="id_password" data-bound-key="password" class="rs-input-large">
              <span id="passworderror" class="inputerror" style="display:none;">
                  <i id="passwordimg" class="rs-validation-indicator"></i>
                  Password is required.
              </span>  
              <span id="passwordtoolong" class="inputerror" style="display:none;">
                  <i id="passwordimg" class="rs-validation-indicator"></i>
                  Password cannot be over 20 characters.
              </span>                           
            </div>

            <input id="id_type" name="type" type="hidden" value="password">

            <div id="login-status" style="display:none;">              
                <div class="rs-control-group ">                
                  <span class="inputerror">
                    <i class="rs-validation-indicator"></i>
                    The username or password you entered is incorrect. Please try again.
                  </span>
                
                </div>              
            </div>                    

            <div class="login-action" ng-click="login2();">
              <button class="save rs-btn rs-btn-login">Log In</button>
              <span class="form_throb" style="display:none;"></span>
            </div>
          </fieldset>
        </form>
      </div>
    </div>
    <div class="links">

    </div>
  </div>


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
</div>
</body>
   <link type="text/css" rel="stylesheet" href="<c:url value='/resources/css/login.css'/>" />
   <script type="text/javascript" src="<c:url value='/resources/scripts/jquery-1.10.2.min.js'/>"></script>
   <script type="text/javascript" src="<c:url value='/resources/scripts/login.js'/>"></script>
   <script type="text/javascript" src="<c:url value='/resources/scripts/angular.js'/>"></script>



</body>


</html>
