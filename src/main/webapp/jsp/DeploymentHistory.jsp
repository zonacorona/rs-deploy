
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"   
         pageEncoding="ISO-8859-1"   
%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
</head>
<body>
<% 

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
response.setStatus(301);
response.setHeader("Location", "/rax-autodeploy/DeployHistory");
response.setHeader("Connection","close");

%>

</body>
</html>