

$(document).ready(function() { 
    $('#filterbuttonid').click(function(){
    	var selectedVals=$('#docbooks').val();
    	$('.deployrow').hide();
    	
    	if(null!=selectedVals && selectedVals!=undefined && selectedVals.length>0){
    		for(var index=0;index<selectedVals.length;++index){
    			var aSelectedVal=selectedVals[index];
    			if(aSelectedVal=='All'){
    				$('.deployrow').show();
    				$('#deployrow0').hide();
    				break;
    			}
    			else{
    				$('#'+aSelectedVal).show();
    			}
    		}    		    		
    	}
    });	
     

   /*$('.detailsimage').on('click',function(event){ 
    	event.preventDefault;
    	var theTarget=event.target;
    	var classname=theTarget.className;
    	var isexternal=false;
    	if(classname=='detailsimage external'){
    		isexternal=true;
    	}    	
    	handleDetails(event);    	
    });*/
    
});

function postIt(event){
	var url="/rax-autodeploy/CreateNewUserServlet";

	$.post(url,{"ldapname" : "blah"},function(data){    	 
		alert("Success data="+data);
	})
	.done(function() { 
		
	})
	.fail(function(data){
		alert("ERROR!!! ");
    });
}

function freezeUI(event, loggedInUser, isExternal){	
	var freezeui=$('input[name=freeze]:checked', '#freezeuiformid').val();
	
	var theUrl="/rax-autodeploy/InstalledWarsServlet?username="+loggedInUser+"&freezeui="+freezeui+"&isexternal="+isExternal;

	$('#progressid').show();
	freezeButtons();
    $('#progressid').ajaxStart(function(){
        $('#progressid').show();
        freezeButtons();
	})
	.ajaxStop(function(){
	    $('#progressid').hide();
	    enableButtons(event);
	});		
	//$("#freezeuiformid").submit();
	//$.post(theUrl);
	$.post(theUrl, function(data){  
	   $('#progressid').hide();	
       window.location.reload(true);
       enableButtons(event);
        
    }).fail(function(data){
    	$('#progressid').hide();
    	alert("Unable to contact server.");        
        //$('#progressid').hide();
	});	
    
}

function freezeButtons(){
	$('.warfoldernames, .deploybutton, .revertbutton, #filterspan').attr('disabled','true');	    
	$('.deploytable').addClass('opacityclass');
	$('#top').addClass('opacityclass');	  

    var details=document.getElementsByClassName('detailsimage');
    for(var i=0;i<details.length;++i){
    	details[i].removeAttribute('onclick');
    }
    
}

function enableButtons(event){

	$('.deploytable').removeClass('opacityclass');
	$('#top').removeClass('opacityclass');	 
	$('.detailsimage').attr('onclick','handleDetails(event);');
}


function closeDetails(event){
	$('.warfoldernames, .deploybutton, .revertbutton, #filterspan').removeAttr('disabled');
	//$('.detailsimage').on('click',handleDetails(event));
	//$('.detailsimage').attr('onclick','handleDetails(event);');
	$('.deploytable, #top').removeClass('opacityclass');
	$('#detailsid').hide();
}

function handleDetails(event){
	//alert('Inside handleDetails');
    var $theTarget=event.target;
    
    if($theTarget!=null && $theTarget!=undefined && $theTarget.className!=undefined && 
    		($theTarget.className=='detailsimage external' || $theTarget.className=='detailsimage internal')){
    	var isExternal=false;
    	if($theTarget.className=='detailsimage external'){
    		isExternal=true;
    	}
	    //var $liElements=$theTarget.parentElement.parentElement.parentElement.children[1].children[0].children;
    	var $liElements=$($theTarget).closest('tr').find('li');
	    var isChecked=false;
	    var query="";
	    var query1="";
	    var query2="";
	    
	    for(var i=0;i<$liElements.length;++i){
		    var $anOption=$liElements[i];
		    var $aCheck=$anOption.firstElementChild;
		    var value=$aCheck.value;
		    //The check box is selected
		    if($aCheck.checked){
		        //We only need to change the value to true once
		        if(!isChecked){
		            isChecked=$aCheck.checked;		    
		        }		        
		        if(i!=0 && query1!=""){
		    	    query1+=":";
		        }
		        query1+=value;
		    }
		    //query2 contains all the selected doc books whether the box is checked or not
		    if(i!=0){
		    	query2+=":";
		    }
		    query2+=value;	    
        }
	    if(isChecked){
	    	query=query1;
	    }
	    else{
	    	query=query2;
	    }
	    //if(isExternal){
	       //theUrl=("http://localhost:8080/rax-staging-services/rest/services/getdetails?folders="+query);
	    //}
	    //else{
	    	//theUrl=("http://localhost:9090/rax-staging-services/rest/services/getdetails?folders="+query);
	    //}
	    var theUrl="/rax-staging-services/rest/services/getdetails?folders="+query;
	    if(!isExternal){
	        theUrl+="&internaldeploy=true";
	    }
	    else{
	    	theUrl+="&internaldeploy=false";
	    }
	    $('.warfoldernames, .deploybutton, .revertbutton, #filterspan').attr('disabled','true');	    
	    $('.deploytable').addClass('opacityclass');
	    $('#top').addClass('opacityclass');	  
	    
	    $('#progressid').show();
        $('#progressid').ajaxStart(function(){
    	    $('#progressid').show();
	    })
	    .ajaxStop(function(){
	        $('#progressid').hide();
	    });		    
	    
	    $.getJSON(theUrl,{"folders" : query},function(data){    	
	    	var markup=getDetailsTableMarkup(data);
	        $('#divcontentid').html(markup);
	        $('#detailsid').show();	 
	         $('#progressid').hide();
		 }).fail(function(data){
			 alert("Unable to contact server.");
			 $('#progressid').hide();
			 $('.warfoldernames, .deploybutton, .revertbutton, #filterspan').removeAttr('disabled');	    
			 $('.deploytable').removeClass('opacityclass');
			 $('#top').removeClass('opacityclass');	 
		 });
    }
}

function getDetailsTableMarkup(data){

    //var retStr='<table class="detailstableclass" id="detailstableid" style="position:relative;left:42px;text-align:left;width:750px;font-family: Lucida Sans Unicode, Lucida Grande, Sans-Serif;">\n';
    var retStr='<table class="detailstableclass" id="detailstableid">\n';
    retStr+='<tr>\n';	
    retStr+='<th class="detailsth" id="booknameth">Book Name</th>\n';
    retStr+='<th class="detailsth" id="deployedwarth">War Name</th>\n';
    retStr+='<th class="detailsth" id="lastdeployth">Prod Deploy Time</th>\n';
    retStr+='<th class="detailsth" id="lastdeployth">Last Build Status</th>\n';
    retStr+='</tr>\n';
    var details=data.details;
    for(var i=0;i<details.length;++i){
	    var aDetail=details[i];
	    var jenkinsBuildResult=aDetail.result;
	    retStr+='<tr>\n';
	    retStr+=('<td class="detailscol">'+aDetail.docname+'</td>\n');
	    retStr+=('<td class="detailscol">'+aDetail.foldername+'.war</td>\n');
	    retStr+=('<td class="detailscol">'+aDetail.lastmodified+'</td>\n');
	    if(null!=jenkinsBuildResult && jenkinsBuildResult!=undefined){
	    
	    	if(jenkinsBuildResult=='SUCCESS' || jenkinsBuildResult=='success'){
	    	    retStr+='<td class="detailscol">';
	    	    retStr+='<a href="http://docs-staging.rackspace.com/jenkins/job/';
	    	    var groupid=aDetail.groupid;
	    	    var artifactid=aDetail.artifactid;
	    	    if(null!=groupid && groupid!=undefined && null!=artifactid && artifactid!=undefined){
	    	    	retStr+=groupid;
	    	    	retStr+='---';
	    	    	retStr+=artifactid;
	    	    	retStr+='/lastBuild" target="_blank">\n';
	    	    }
	    	    retStr+='<img class="jenkinsresultimg" src="../images/lastbuild-success.png" title="Successful Jenkins Build"/>\n';
	    	    retStr+='</a>\n';
	    	    retStr+='</td>\n';
	    	}
	    	else if(jenkinsBuildResult=='FAILURE' || jenkinsBuildResult=='failure'){
	    	    retStr+='<td class="detailscol">';
	    	    retStr+='<img class="jenkinsresultimg" src="../images/lastbuild-success.png" title="Failed Jenkins Build"/>';
	    	    retStr+='</td>\n';	        	    		
	    	}
	    	else if(jenkinsBuildResult=='ABORTED' || jenkinsBuildResult=='aborted'){
	    	    retStr+='<td class="detailscol">';
	    	    retStr+='<img class="jenkinsresultimg" src="../images/lastbuild-aborted.png" title="Aborted Jenkins Build"/>';
	    	    retStr+='</td>\n';	    	    	
	        }
	    	else{
	    	    retStr+='<td class="detailscol">';
	    	    retStr+='<img class="jenkinsresultimg" src="../images/lastbuild-unknown.png" title="Aborted Jenkins Build"/>';
	    	    retStr+='</td>\n';		        	    		
	    	}
	    }
	    else{
	    	retStr+='<td class="detailscol">';
	    	retStr+='<img class="jenkinsresultimg" src="../images/lastbuild-unknown.png" title="Unknown Jenkins Build Result"/>';
	    	retStr+='</td>\n';
	    }
	    retStr+='</tr>\n';
    }
    retStr+='</table>\n';
    return retStr;
}


function fullpageload(isexternal, username){
	//alert('doing a full page load isexternal='+isexternal);
	$('.warfoldernames, .deploybutton, .revertbutton, #filterspan').attr('disabled','true');
	$('.deploytable').addClass('opacityclass');
	$('#top').addClass('opacityclass');	

	$('#progressid').show();
    $('#progressid').ajaxStart(function(){
    	$('#progressid').show();
	})
	.ajaxStop(function(){
	    $('#progressid').hide();
	});	

	$.getJSON("/rax-autodeploy/InstalledWarsServlet?isexternal="+isexternal+"&username="+username,{"isexternal" : isexternal,"username" : username},function(data){
		 loadPage(data, isexternal);
		 $('#progressid').hide();
	 });
}

function loadPage(data, isExternal){
	//alert('Returned from ajax call data='+data);
	$('.warfoldernames, .deploybutton, .revertbutton, #filterspan').removeAttr('disabled');
	$('.deploytable, #top').removeClass('opacityclass');
	
	//Now we need to build the list
    var filterList=getFilterList(data);	
    $('#docbooks').html(filterList);
    var table=getTable(data, isExternal);
    $('#deploytable').html(table);
}

function getTable(data, isExternal){
	var tablerows=data.installedwarsmap;
	
	var retStr='<table class="deploytable" id="deploytable">\n';
	retStr+='<tr>\n';
	retStr+='<th id="docbookprojth">Docbook Project</th>\n';
	retStr+='<th id="deployedwarth">Book Name</th>\n';
	retStr+='<th id="lastdeployth">Details</th>\n';
	retStr+='<th colspan="3"></th>\n';
	retStr+='</tr>\n';
	
	for(var i=0;i<tablerows.length;++i){
		var aRow=tablerows[i];
		var deployedbooks=aRow.deployedbooks;
	    
		retStr+='<tr class="deployrow" id="deployrow"'+(i+1)+'>\n';
		retStr+='<form method="GET" action="/rax-autodeploy/InstalledWarsServlet" name="deployform'+(i+1)+'">\n';
		retStr+='<td class="pomnamecol">'+aRow.pomname+'</td>\n';
		retStr+='<td class="deploywarcol">\n';
		retStr+='<ul>\n';
		
		for(var x=0;x<deployedbooks.length;++x){
			var abook=deployedbooks[x];
			retStr+='<li>\n';
			retStr+='<input type="checkbox" class="warfoldernames" value="'+abook.foldername+'" title="'+abook.foldername+'">\n';
			retStr+='<span style="position:relative;left:-30px;margin:5px 5px 5px 33px;" title="'+abook.foldername+'">'+abook.docname;
			retStr+='</span>\n';
			retStr+='<\li>\n';
		}	
		retStr+='</ul>\n';
		retStr+='</td>\n';
		
		retStr+='<td class="detailscol">\n';
		retStr+='<button type="button" class="detailsbutton" id="detailsbuttonid'+(i+1)+'">\n';
		if(isExternal){
		    retStr+='<img src="../images/magnifyingglass.jpeg" class="detailsimage external" id="detailsimageid'+(i+1)+'" title="Click for detailed information" onclick="handleDetails(event);" style="width:20px;">\n';
		}
		else{
			retStr+='<img src="../images/magnifyingglass.jpeg" class="detailsimage internal" title="Click for detailed information" onclick="handleDetails(event);" style="width:20px;">\n';
		}
		retStr+="</button>\n";
		retStr+='<input type="hidden" name="groupid" id="groupid'+(i+1)+'" value="'+aRow.groupid+'">\n';
		retStr+='<input type="hidden" name="artifactid" id="artifactid'+(i+1)+'" value="'+aRow.groupid+'">\n';
        retStr+='<input type="hidden" name="rownumber" id="rownumber'+(i+1)+'" value="'+(i+1)+'"> \n';
		retStr+='</td>\n';
				
		retStr+='<td class="deploybuttoncol">\n';
		retStr+='<input style="display:'+aRow.displaydeploybutton+
		        '" id="deploybutton'+(i+1)+
		        '" type="button" class="deploybutton" name="deploybutton" value="Deploy to Production" onclick="deployToProd(\'deployform'+
		        (i+1)+'\',\'deployrow'+(i+1)+'\',\''+aRow.groupid+'~~~'+aRow.artifactid+'\', \''+(i+1)+
		        '\');" onmouseover="changeButtonColor(\'deploybutton'+(i+1)+'\', \'#3782D2\');" onmouseout="changeButtonColor(\'deployButton'+
		        (i+1)+'\', \'#5FAAFA\');">\n';
		retStr+='<p>\n';
		retStr+='<input style="display:'+aRow.displaydeploybutton+
        '" id="revertbutton'+(i+1)+
        '" type="button" class="revertbutton" name="revertbutton" value="Revert to Backup" onclick="revertbutton(\'deployform'+
        (i+1)+'\',\'deployrow'+(i+1)+'\',\''+aRow.groupid+'~~~'+aRow.artifactid+'\', \''+(i+1)+
        '\', \'false\');" onmouseover="changeButtonColor(\'revertbutton'+(i+1)+'\', \'#AF6633\');" onmouseout="changeButtonColor(\'revertbutton'+
        (i+1)+'\', \'#CC6633\');">\n';		
		retStr+='</p>\n';
		retStr+='</td>\n';

        retStr+='<td class="successcol">\n';
        retStr+='<img class="successicons" style="display:none;position:relative;right:240px;" id="successicon1" alt="Success" src="../images/success_icon.png">\n';
        retStr+='<img class="erroricons" style="display:none;position:relative;right:240px" id="erroricon1" alt="Error" src="../images/icon_error.gif">\n';
        retStr+='<img class="progimg" id="progimg1" style="display:none;position:relative;right:240px" alt="progress icon" src="../images/progress-indicator.gif">\n';
        retStr+='</td>\n';

        retStr+='<td class="returnmessage" id="returnmessage'+(i+1)+'">\n';
        retStr+='<span style="display:none;position:relative;right:550px;" class="returnmessageclass" id="returnmessagespan'+
                (i+1)+'"></span>\n';  
        retStr+='<span class="cannotdeploymessage" style="display:none;">\n';
        retStr+='<a href="'+aRow.lastjenkinsbuildurl+'" target="_blank">A jenkins build</a> is running, deployment is not available at this time.\n';
        retStr+='<p>Refresh browser to check on status.</p>\n';
        retStr+='</span>\n';	
        retStr+='</td>\n';
        retStr+='</form>\n';
		retStr+='</tr>\n';
	}
	
    retStr+="</table>\n";
    return retStr;
}

function getFilterList(data){
	var filters=data.filterlist;
	//var retStr='<select name="docbooks" id="docbooks" multiple size="6">\n';
	var retStr='<option class="docbookoption" value="All">All</option>\n';
	for(var i=0;i<filters.length;++i){
		retStr+='<option class="docbookoption" value="deployrow'+(i+1)+'">'+filters[i].filter+'</option>\n';
	}
	
	//retStr+='</select>';
    return retStr;
}

function changeButtonColor(btnId, color){
	$(('#'+btnId)).css('background-color',color);
}

function clearForm(){
	$('.successicons, .erroricons, .progimg, .returnmessage').hide();

	//$('.draftstatusoff').removeAttr('disabled');
	//$('.draftstatusoff').attr('checked','checked');
	//$('.draftstatuson').removeAttr('disabled');
	//$('.draftstatuson').removeAttr('checked');	
	$('.warfoldernames').removeAttr('checked');
	$('.warfoldernames, .deploybutton, .revertbutton').removeAttr('disabled');
	$('.deployrow').css("background-color","#FFFFFF");
	$('.detailsimage').attr('onclick','handleDetails(event);');
}


function deployToProd(deployformname, deployrowid, app, count){
	$('#progressid').show();
	var freezeURL='/rax-staging-services/rest/services/getshouldfreeze';
	    
    $.getJSON(freezeURL,function(data){
	    var shouldfreeze=data.shouldfreeze;
	    if(null!=shouldfreeze && shouldfreeze!=undefined){
	    	if(shouldfreeze){
	    		window.location.reload(true); 
	    	}
	    	else{
	    		handleDeploy(deployformname, deployrowid, app, count, "false");
	    	}
	    }
	    //if we can't detect whether shouldfreeze is set, just go ahead and try to deploy
	    else{
	    	handleDeploy(deployformname, deployrowid, app, count, "false");
	    }
    }).fail(function(data){
	    alert("Error: server is not responding");
    });	
	
}

function deployToProdInternal(deployformname, deployrowid, app, count){
	var freezeURL='/rax-staging-services/rest/services/getshouldfreeze';
	
        $('#progressid').show();
        $('#progressid').ajaxStart(function(){
    	    $('#progressid').show();
	    })
	    .ajaxStop(function(){
	        //$('#progressid').hide();
	    });		

    $.getJSON(freezeURL,function(data){
	    var shouldfreeze=data.shouldfreeze;
	    if(null!=shouldfreeze && shouldfreeze!=undefined){
	    	if(shouldfreeze){
	    		window.location.reload(true); 
	    	}
	    	else{
	    		handleDeploy(deployformname, deployrowid, app, count, "true");
	    	}
	    }
	    //if we can't detect whether shouldfreeze is set, just go ahead and try to deploy
	    else{
	    	handleDeploy(deployformname, deployrowid, app, count, "true");
	    }
    }).fail(function(data){
    	$('#progressid').hide();
	    alert("Error: server is not responding");
    });	
}

function revertDeployedWarsConfirmation(deployformname, deployrowid, app, count, internaldeploy){

	var freezeURL='/rax-staging-services/rest/services/getshouldfreeze';
	
    $.getJSON(freezeURL,function(data){
	    var shouldfreeze=data.shouldfreeze;
	    if(null!=shouldfreeze && shouldfreeze!=undefined){
	    	if(shouldfreeze){
	    		window.location.reload(true); 
	    	}
	    	else{
	    		doRevert(deployformname, deployrowid, app, count, internaldeploy);
	    	}
	    }
	    //if we can't detect whether shouldfreeze is set, just go ahead and try to revert
	    else{
	    	doRevert(deployformname, deployrowid, app, count, internaldeploy);
	    }
    }).fail(function(data){
	    alert("Error: server is not responding");
    });		

   
}

function doRevert(deployformname, deployrowid, app, count, internaldeploy){
	var theForm=document.forms[deployformname];
	var formElements=theForm.elements;
	var selectedWars="";
	var somethingChecked=false;
	var temp="";
	for(var i=0;i<formElements.length;++i){
		var theElement=formElements[i];
		if(theElement.type!='button'){
		    if(theElement.type=='checkbox'){
			    if(theElement.checked==true){
			    	somethingChecked=true;
			    	//selectedWars+=("\n*"+theElement.nextElementSibling.innerText);
			    	temp=theElement.nextElementSibling.childNodes[0].data;
			    	temp=temp.replace(/\s/g, '');
			    	selectedWars+=("\n*"+temp);
			    }
		    }
	    }
    }
	if(somethingChecked==false){
		$(".deployrow").css("background-color","#FFFFFF");
		$("#"+deployrowid).css("background-color","#DD4B39");
		alert("No war was selected. Select at least 1 war to revert.");
	}
	else{
		var message="Are you sure you want to revert to backup for the following doc projects?"+selectedWars;
		
        if(confirm(message)){
    	    revertDeployedWars(deployformname, deployrowid, app, count, internaldeploy);
        }
        else{
    	    //alert("Revert to backup aborted");
        }
    }	
}


function revertDeployedWars(deployformname, deployrowid, app, count, internaldeploy){
	var theForm=document.forms[deployformname];
	var formElements=theForm.elements;
	var requestUrlNParams="/rax-autodeploy/InstalledWarsServlet?";
	//var changedApp=app.replace('~~~','---');
	//var somethingChecked=false;
	
	for(var i=0;i<formElements.length;++i){
		var theElement=formElements[i];
		if(theElement.type!='button'){
		    if(theElement.type=='checkbox'){
			    if(theElement.checked==true){
			    	//somethingChecked=true;
			        requestUrlNParams+=theElement.name;
			        requestUrlNParams+="="+theElement.value;
			    }
		    }
		    else if(theElement.type=='radio'){
		    	if(theElement.checked){
			        requestUrlNParams+=theElement.name;
			        requestUrlNParams+="="+theElement.value;		    		
		    	}
		    }
		    else{
		        requestUrlNParams+=theElement.name;
		        requestUrlNParams+="="+theElement.value;
		    }
		    //Add a '&' only if there are more parameters and the str does not already end in an &
		    if(i!=(formElements.length-1) && (requestUrlNParams.indexOf("&", requestUrlNParams.length-1)==-1)){
			    requestUrlNParams+="&";
		    }
		}	
	}
	if(!requestUrlNParams.match(/&$/)){
		requestUrlNParams+="&";
	}
    requestUrlNParams+="action=revert";
    requestUrlNParams+="&internaldeploy="+internaldeploy;
    
	$('.successicons').hide();
	$('.erroricons').hide();
	$('.returnmessageclass').hide();
    $('#returnmessagespan'+count).html("Your revert request has been submitted. You will receive<br>an e-Mail notifiction once your request has been processed.");
    $('#returnmessagespan'+count).show();
		
    $.post(requestUrlNParams);
}

function sleep(ms)
{
	var dt = new Date();
	dt.setTime(dt.getTime() + ms);
	while (new Date().getTime() < dt.getTime());
}


function handleDeploy(deployformname, deployrowid, app, count, deployToInternal){
	//alert('deployformname='+deployformname+" deployrowid="+deployrowid+" app="+app+" count="+count);	
	$('#progressid').show();
	var theForm=document.forms[deployformname];
	var formElements=theForm.elements;
	var requestUrlNParams="/rax-autodeploy/InstalledWarsServlet?";	
	var somethingChecked=false;
	//This url is used to check the status of the build
	var temp="";
    
	for(var i=0;i<formElements.length;++i){
		var theElement=formElements[i];
		if(theElement.type!='button'){
		    if(theElement.type=='checkbox'){
			    if(theElement.checked==true){
			        somethingChecked=true;
			        requestUrlNParams+=theElement.name;
			        requestUrlNParams+="="+theElement.value;
			        if(i!=0 && temp!=""){
			        	temp+=":";
			        }
			        temp+=theElement.value;
			    }
		    }
		    else if(theElement.type=='radio'){
		    	if(theElement.checked){
			        requestUrlNParams+=theElement.name;
			        requestUrlNParams+="="+theElement.value;			        
		    	}
		    }
		    else{
		        requestUrlNParams+=theElement.name;
		        requestUrlNParams+="="+theElement.value;
		    }
		    //Add an & only if there are more parameters and the str does not already end in an &
		    if(i!=(formElements.length-1) && (requestUrlNParams.indexOf("&", requestUrlNParams.length-1)==-1)){
			    requestUrlNParams+="&";
		    }
		}
		
	}
	var theUrl='/rax-staging-services/rest/services/getdetails?folders='+temp;
	
	if(!requestUrlNParams.match(/&$/)){
		requestUrlNParams+="&";
	}
	requestUrlNParams+=(("internaldeploy=")+deployToInternal);
	
	if(somethingChecked==false){
		$(".deployrow").css("background-color","#FFFFFF");
		$("#"+deployrowid).css("background-color","#DD4B39");
		alert("No war was selected. Select at least 1 war to deploy.");
	}
	else{
		
        //First we need to check to make sure that 	
        
    	//var temp=app.replace('~~~','---');
    	//theUrl+=temp;
    	//var jenkinsLastBuildUrl=theUrl;
    	//jenkinsLastBuildUrl+="/lastBuild";
    	//theUrl+="/lastBuild/api/json";
        	       
        $('#progressid').show();
        $('#progressid').ajaxStart(function(){
    	    $('#progressid').show();
	    })
	    .ajaxStop(function(){
	        $('#progressid').hide();
	    });		    
	    
	    $.getJSON(theUrl,{"theurl" : theUrl},function(data){  
	    	$('#progressid').hide();
	    	var aDetail=data.details[0];
	    	if(null!=aDetail && aDetail!=undefined){
	    	    var theResult=aDetail.result;
	    	    if(theResult==null || theResult==undefined){
	    		    var conf=confirm("A Jenkins build is in progress do you want to continue?");
	    		    if(conf==true){
	    			    continueWithDeploy(requestUrlNParams, count);
	    		    }
		    	    else{
		    		    alert('Deployment aborted.');
		    	    }	    		
	    	    }
	    	    else{
	    		    if(theResult=='SUCCESS' || theResult=='success'){
	                    continueWithDeploy(requestUrlNParams, count);
	    		    }
	    		    else{
		    		    var conf=confirm("The last Jenkins build was not successful would you like to continue?");
		    		    if(conf==true){
		    			    continueWithDeploy(requestUrlNParams, count);
		    		    }	    			
		    		    else{
		    			    alert('Deployment aborted.');
		    		    }
	    		    }
	    	    }
		    }
		    else{
		    	 var conf=confirm("Unable check lastes Jenkins build would you like to continue?");
				 if(conf==true){
					 continueWithDeploy(requestUrlNParams, count);
				 }
				 else{
					 alert("Deployment aborted.");
				 }		    	 
		    }
	    	
		 }).fail(function(data){			 
			 $('#progressid').hide();
			 var conf=confirm("Unable to contact Jenkins server for latest build status. Do you want to continue?");
			 if(conf==true){
				 continueWithDeploy(requestUrlNParams, count);
			 }
			 else{
				 alert("Deployment aborted.");
			 }
		 });
	}	
}

function continueWithDeploy(requestUrlNParams, count){
	$('.successicons').hide();
	$('.erroricons').hide();
	$('.returnmessageclass').hide();
    $('#returnmessagespan'+count).html("Your deployment request has been submitted. You will receive<br>an e-Mail notifiction once your request has been processed.");
    $('#returnmessagespan'+count).show();

    $.post(requestUrlNParams);
    $('#progressid').hide();
    //window.location.href="http://docs-staging.rackspace.com/history";
    window.open("http://docs-staging.rackspace.com/history");
}

function revertCallBack(data){
    $(".deployrow").css("background-color","#FFFFFF");
    var groupid=data.groupid;
    var artifact=data.artifactid;
    var success=data.success;
    var messages=data.messages;
    var rownumber=data.rownumber;
    //var draftstatus=data.drafstatus;
    	
    var messageStr="";
    if(null!=messages && messages!=undefined){
    	for(var i=0;i<messages.length;++i){
    		var aJSONMessage=messages[i];
    		messageStr+=("<p>"+(aJSONMessage.message)+"</p>");
    	} 	    		
    }
    	
    if(success){
    	//message='Operation for groupid: '+groupid+" and artifactid: "+artifact+" was successful";
        $('#successicon'+rownumber).show();	
        
    }
    else{
    	//message='Operation for groupid: '+groupid+" and artifactid: "+artifact+" failed";
    	$('#erroricon'+rownumber).show();
    }
    
    if(messageStr!=""){
    	$('#returnmessagespan'+rownumber).html(messageStr);
    	$('#returnmessagespan'+rownumber).show();
    }
}

function deployCallBack(data){
    $(".deployrow").css("background-color","#FFFFFF");
    var groupid=data.groupid;
    var artifact=data.artifactid;
    var success=data.success;
    var messages=data.messages;
    var rownumber=data.rownumber;
    //var draftstatus=data.drafstatus;
    	
    var messageStr="";
    if(null!=messages && messages!=undefined){
    	for(var i=0;i<messages.length;++i){
    		var aJSONMessage=messages[i];
    		messageStr+=("<p>"+(aJSONMessage.message)+"</p>");
    	} 	    		
    }
    	
    if(success){
    	//message='Operation for groupid: '+groupid+" and artifactid: "+artifact+" was successful";
        $('#successicon'+rownumber).show();	
        
    }
    else{
    	//message='Operation for groupid: '+groupid+" and artifactid: "+artifact+" failed";
    	$('#erroricon'+rownumber).show();
    }
    
    if(messageStr!=""){
    	$('#returnmessagespan'+rownumber).html(messageStr);
    	$('#returnmessagespan'+rownumber).show();
    }
}


function detectCheckboxClick(event,$theCheckBox){

    //var parentLi = $theCheckBox.parent();
    //var parentUl = parentLi.parent();
    var $parentUl = $theCheckBox.closest('ul');
    //var parentTd = parentUl.parent();
    //var parentTr = parentTd.parent();
    var $parentTr = $theCheckBox.closest('tr');


    var $checkBoxesForRow=$parentUl.children().children();

    var $theButton=($parentTr.find('td')[3]).children[0];  
    var $revertButton=($parentTr.find('td')[3]).children[1].children[0];    
    
    var $detailsImg=$parentTr.find('img')[0];
    
    var $theButtonClass=$theButton.className;
    
    if(null!=$theButtonClass && $theButtonClass!=undefined && $theButtonClass=='deploybutton internal'){
    	$detailsImg=$parentTr.find('span').find('img');
    }
    
    //var draftStatusOff=parentTr.children([3]).children().children();
    //var draftStatusOn=parentTr.children([3]).children().children();

    //A checkbox was clicked, disable all rows except for the row that was clicked
    if ($theCheckBox.is(':checked')) {
        $('.warfoldernames, .deploybutton, .revertbutton').attr('disabled','true');
        //$('.deploybutton').attr('disabled','true');
        //$('.revertbutton').attr('disabled','true');
        $('.detailsimage').unbind('click');              
        
        var details=document.getElementsByClassName('detailsimage');
        for(var i=0;i<details.length;++i){
        	details[i].removeAttribute('onclick');
        }
        
        //$('.draftstatusoff').attr('disabled',true);
        //$('.draftstatuson').attr('disabled',true);
        
        $checkBoxesForRow.removeAttr('disabled');
        //$theButton.removeAttr('disabled');
        document.getElementById($theButton.id).removeAttribute('disabled');
        //$revertButton.removeAttr('disabled');
        document.getElementById($revertButton.id).removeAttribute('disabled');
        //$detailsImg.on('click', handleDetails(event));
        var $detailsImgId=$detailsImg.id;
        if(null==$detailsImgId || $detailsImgId==undefined){
        	$detailsImgId=$detailsImg.attr('id');
        }
        document.getElementById($detailsImgId).setAttribute('onclick','handleDetails(event);');
        
    }
    else{
        var somethingChecked=false;
        for(var i=0;i<$checkBoxesForRow.length;++i){
            var $aCheckBox=$checkBoxesForRow[i];
            if($aCheckBox.checked==true){
                somethingChecked=true;
                break;
            }
        }
        //No check boxes are selected, enable all rows
        if(!somethingChecked){
                $('.warfoldernames, .deploybutton, .revertbutton').removeAttr('disabled');
                $('.detailsimage').attr('onclick','handleDetails(event);');
                /*
                var theDetails=document.getElementsByClassName('detailsimage');
                if(theDetails!=undefined){
                	for(var i=0;i<theDetails.length;++i){
                	    var aDetail=theDetails[i];
                	    if(!aDetail.hasAttribute('onclick')){
                	    	aDetail.setAttribute('onclick','handleDetails(event);');
                	    }
                	}
                }*/
                //$('.deploybutton').removeAttr('disabled');
                //$('.revertbutton').removeAttr('disabled');
                //$('.detailsbutton').on('click', handleDetails);

        }
    }
       
}
    
