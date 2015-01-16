<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>  

<!DOCTYPE html>

<html ng-app>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Publish Documentation</title>
</head>
<body>
  <div id="top">
      <img id="doctoolsimg" src="<c:url value='/resources/images/doctoolslogo2.png'/>"/>
  </div>
  <h3>No access for logged in user ${loggedInUser}</h3>

</body>

</html>