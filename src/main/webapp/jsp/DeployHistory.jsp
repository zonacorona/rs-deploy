<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>  
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Deployment History and Status</title>
</head>
<body>

<div id="controller" ng-controller="DeployHistoryCtrl">
<br>
<span id="top">
    <img id="doctoolsimgid" src="/rax-autodeploy/resources/images/doctoolslogo3.png"/>
    <form id="filterformid" name="filterform">
        <div id="filterspan" class="filterspan">    
          <select name="ldapnames" id="ldapnamesid" multiple size="6">
            <option class="anldapnameselect" value="All">All</option>

            <c:forEach var="aUser" items="${usersList}" varStatus="status">
              <option class="anldapnameselect" value="${aUser.ldapname}">${aUser.ldapname}</option>
            </c:forEach> 
            
                                               
          </select> 
          <input id="filterbuttonid" class="filterbutton" type="button" value="Filter" 
                 ng-mouseenter="changeButtonColor('filterbuttonid','#B7C5D4');"
                 ng-mouseleave="changeButtonColor('filterbuttonid','#CCDDEE');"
                 ng-click="filterRow($event);")/> 
          <input type="hidden" name="filtervalue" id="filtervalueid" value="all"/>
      </div>   
    </form>
</span>
<span id="spanhistorytableid">

<table class="deployhistable" id="deploymenthistorytableid">
<tr>
    <th class="tableheaderclass">&nbsp;</th>
    <th class="tableheaderclass">User</th> 
    <th class="tableheaderclass">War Name</th>
    <th class="tableheaderclass">Doc Name</th>
    <th class="tableheaderclass">Type</th>
    <th class="tableheaderclass">Start Time (CST)</th>
    <th class="tableheaderclass">End Time (CST)</th>
    <th class="tableheaderclass">Status</th>
    <th class="tableheaderclass failreasonthclass">
        Fail Reason       
    <div id="previousnextbuttontopid">
    <c:choose>
        <c:when test="${shouldDisplayPreviousLink}">           
            <div id="clickprevioustopid" class="clickpreviousclass" 
                                          ng-mouseenter="changeColor('clickprevioustopid','#888AAA');" 
                                          ng-mouseleave="removeBackGroundColor('clickprevioustopid');"
                                          ng-click="getPreviousJobs($event);">
                <img src="resources/images/previous.png"/>
            </div>  
            <c:choose>                             
            <c:when test="${shouldDisplayNextLink}">
                <div id="clicknexttopid" class="clicknextclass" 
                                          ng-mouseenter="changeColor('clicknexttopid','#888AAA');" 
                                          ng-mouseleave="removeBackGroundColor('clicknexttopid');"
                                          ng-click="getNextJobs($event);">
                     <img src="resources/images/next.png"/>
                 </div>
            </c:when>
            <c:otherwise>
                 <div id="clicknexttopid" class="clicknextclass" style="display:none"
                                          ng-mouseenter="changeColor('clicknexttopid','#888AAA');" 
                                          ng-mouseleave="removeBackGroundColor('clicknexttopid');"
                                          ng-click="getNextJobs($event);">
                     <img src="resources/images/next.png"/>
                 </div>           
            </c:otherwise>
            </c:choose>
        </c:when>
        <c:otherwise> 
            <div id="clickprevioustopid" class="clickpreviousclass" style="display:none;"
                                          ng-mouseenter="changeColor('clickprevioustopid','#888AAA');" 
                                          ng-mouseleave="removeBackGroundColor('clickprevioustopid');"
                                          ng-click="getPreviousJobs($event);">
                <img src="resources/images/previous.png"/>
            </div>                        
            <c:choose>                             
            <c:when test="${shouldDisplayNextLink}">
                <div id="clicknexttopid" class="clicknextclass" 
                                          ng-mouseenter="changeColor('clicknexttopid','#888AAA');" 
                                          ng-mouseleave="removeBackGroundColor('clicknexttopid');"
                                          ng-click="getNextJobs($event);">
                     <img src="resources/images/next.png"/>
                 </div>
            </c:when>
            <c:otherwise>
                 <div id="clicknexttopid" class="clicknextclass" style="display:none"
                                          ng-mouseenter="changeColor('clicknexttopid','#888AAA');" 
                                          ng-mouseleave="removeBackGroundColor('clicknexttopid');"
                                          ng-click="getNextJobs($event);">
                     <img src="resources/images/next.png"/>
                 </div>           
            </c:otherwise>
            </c:choose>  
        </c:otherwise>
    </c:choose>       
    </div>        
        
    </th>
</tr>
<c:set var="count" value="1" scope="page" />
<tr ng-repeat="deployJob in deployJobs" ng-model="deployJobs" class="{{deployJob.user}} historyrow" id="historyrow{{deployJob.count}}">
    <td ng-class-even="'deploymenthistortableevenrow'" ng-class-odd="'deploymenthistortableoddrow'">
        {{deployJob.count}}
    </td>
    <td ng-class-even="'deploymenthistortableevenrow'" ng-class-odd="'deploymenthistortableoddrow'">{{deployJob.user}}</td>
    <td ng-class-even="'deploymenthistortableevenrow'" ng-class-odd="'deploymenthistortableoddrow'">{{deployJob.warName}}</td>
    <td ng-class-even="'deploymenthistortableevenrow'" ng-class-odd="'deploymenthistortableoddrow'">{{deployJob.docName}}</td>
    <td ng-class-even="'deploymenthistortableevenrow'" ng-class-odd="'deploymenthistortableoddrow'">{{deployJob.type}}</td>
    <td ng-class-even="'deploymenthistortableevenrow'" ng-class-odd="'deploymenthistortableoddrow'">{{deployJob.startTime}}</td>
    <td ng-class-even="'deploymenthistortableevenrow'" ng-class-odd="'deploymenthistortableoddrow'">{{deployJob.endTime}}</td>
    <td ng-class-even="'deploymenthistortableevenrow'" ng-class-odd="'deploymenthistortableoddrow'">
        <div class="statusclass">
            <!-- The image will only appear if the status is not done failed -->
            {{deployJob.status}} <img class="progressimgclass" ng-src="{{deployJob.progressImg}}"/>
        </div>
    </td>
    <td ng-class-even="'deploymenthistortableevenrow'" ng-class-odd="'deploymenthistortableoddrow'">{{deployJob.failReason}}</td>      
</tr>
<tr id="lastrowid">
    <td colspan="9" ng-class-even="'deploymenthistortableevenrow'" ng-class-odd="'deploymenthistortableoddrow'">
  <div id="previousnextbuttonid">
    <c:choose>
        <c:when test="${shouldDisplayPreviousLink}">           
            <div id="clickpreviousid" class="clickpreviousclass" style="display:block;"
                                          ng-mouseenter="changeColor('clickpreviousid','#888AAA');" 
                                          ng-mouseleave="removeBackGroundColor('clickpreviousid');"
                                          ng-click="getPreviousJobs($event);">
                <img src="resources/images/previous.png"/>
            </div>  
            <c:choose>                             
            <c:when test="${shouldDisplayNextLink}">
                <div id="clicknextid" class="clicknextclass" style="display:block" 
                                          ng-mouseenter="changeColor('clicknextid','#888AAA');" 
                                          ng-mouseleave="removeBackGroundColor('clicknextid');"
                                          ng-click="getNextJobs($event);">
                     <img src="resources/images/next.png"/>
                 </div>
            </c:when>
            <c:otherwise>
                 <div id="clicknextid" class="clicknextclass" style="display:none"
                                          ng-mouseenter="changeColor('clicknextid','#888AAA');" 
                                          ng-mouseleave="removeBackGroundColor('clicknextid');"
                                          ng-click="getNextJobs($event);">
                     <img src="resources/images/next.png"/>
                 </div>           
            </c:otherwise>
            </c:choose>
        </c:when>
        <c:otherwise> 
            <div id="clickpreviousid" class="clickpreviousclass" style="display:none;"
                                          ng-mouseenter="changeColor('clickpreviousid','#888AAA');" 
                                          ng-mouseleave="removeBackGroundColor('clickpreviousid');"
                                          ng-click="getPreviousJobs($event);">
                <img src="resources/images/previous.png"/>
            </div>                        
            <c:choose>                             
            <c:when test="${shouldDisplayNextLink}">
                <div id="clicknextid" class="clicknextclass" style="display:block" 
                                          ng-mouseenter="changeColor('clicknextid','#888AAA');" 
                                          ng-mouseleave="removeBackGroundColor('clicknextid');"
                                          ng-click="getNextJobs($event);">
                     <img src="resources/images/next.png"/>
                 </div>
            </c:when>
            <c:otherwise>
                 <div id="clicknextid" class="clicknextclass" style="display:none"
                                          ng-mouseenter="changeColor('clicknextid','#888AAA');" 
                                          ng-mouseleave="removeBackGroundColor('clicknextid');"
                                          ng-click="getNextJobs($event);">
                     <img src="resources/images/next.png"/>
                 </div>           
            </c:otherwise>
            </c:choose>  
        </c:otherwise>
    </c:choose>       
    </div>                
    </td>
</tr>
</table>                

</div>
</body>
   <link rel="stylesheet" type="text/css" href="<c:url value='/resources/css/deploymenthistory.css'/>"/>
   <script type="text/javascript" src="<c:url value='/resources/scripts/jquery-1.10.2.min.js'/>"></script>
   <script type="text/javascript" src="<c:url value='/resources/scripts/jquery.lightbox.js'/>"></script>
   <script type="text/javascript" src="<c:url value='/resources/scripts/angJsHistory.js'/>"></script>
   <script type="text/javascript" src="<c:url value='/resources/scripts/angular.min.js'/>"></script>
</html>