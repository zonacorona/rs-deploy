

$(document).ready(function() { 

    $('select').change(function(e){
    	var changed=$(e.target);
    	populateUsers();
    });
    
    $('#closetextid').click(function(){
    	$('#createuserformid').hide();
    	$('.createforminputclass').val('');
    	$('.asteriskclass').hide();
    });
    
    $('#cancelcreatenewuserbtnid').click(function(){
    	$('#createuserformid').hide();
    	$('.createforminputclass').val('');
    	$('.asteriskclass').hide();
    });
    
    $('#closenewgrouptextid').click(function(){
    	$('#creategroupformid').hide();
    	$('#creategroupnameid').val('');
    	$('#groupnameformasteriskid').hide();
    });
    
    $('#cancelcreategroupbtnid').click(function(){
    	$('#creategroupformid').hide();
    	$('#creategroupnameid').val('');
    	$('#groupnameformasteriskid').hide();
    });
    
    $('#deletegroupsclosetextid').click(function(){
    	$('#deletegroupformid').hide();
    });
    
    $('#canceldeletegroupsid').click(function(){
    	$('#deletegroupformid').hide();
    });
});

function showCreateGroupForm(){
	$('#creategroupformid').show();
}

function createNewGroup(event){
	var groupname=$('#creategroupnameid').val();
	if(null==groupname||groupname==undefined||groupname==''){
		$('#groupnameformasteriskid').show();
		alert("Group name must have a value");
	}
	else{
		$('#groupnameformasteriskid').hide();
		if(groupname.length>90){
			alert("Group name can not be longer than 90 characters long");
		}
		else{
		    var url="/rax-autodeploy/CreateDeleteGroupsServlet?action=create&groupname="+groupname;

	        $('#progressid').show();
            $('#progressid').ajaxStart(function(){
	            $('#progressid').show();
            })
            .ajaxStop(function(){
                $('#progressid').hide();
            });				    
		    
            $.post(url,{"groupname" : groupname},function(data){           	    
        	    var errormessage=data.errormessage;
            	//noldapuser would be set from CreateNewUserServlet.checkAccess(JSONObject,String,String,String)
            	var noldapuser=data.noldapuser;
            	if(null!=noldapuser && noldapuser!=undefined){
            		alert(noldapuser);
            		window.location.reload(true);
            	}        	    
            	else if(errormessage!=null && errormessage!=undefined){
        		    alert(errormessage);
        	    }
        	    else{
	                $('#creategroupformid').hide();
        	        var successmessage=data.successmessage;
        	        if(null!=successmessage && successmessage!=undefined){
        	    	    alert(successmessage);
        	        }
        	        else{
        	    	    alert("Successfully created new group: "+groupname);
        	        }
    	            $('#creategroupformid').hide();
    	            $('#creategroupnameid').val('');
    	            $('#groupnameformasteriskid').hide();
        	    }        	        
            })
            .done(function() { 
	            $('#progressid').hide();
            })
            .fail(function(data){       	    
	            alert("Server ERROR!!! Could not create group: "+groupname);
            });		    
		    
		}
	}
	
}

function createDeleteGroupForm(){
	
	//Now we have to make a REST call to get the data
    var url="/rax-staging-services/rest/services/getallgroups";
    
	$('#progressid').show();
    $('#progressid').ajaxStart(function(){
	    $('#progressid').show();
    })
    .ajaxStop(function(){
        $('#progressid').hide();
    });		

	$.getJSON(url,{"none" : "none"},function(data){    	 
	    $('#progressid').hide();   
	    
	})
	.done(function(data) { 
        var str=getGroups(data);
        $('#deletegroupformid').html(str);
        $('#deletegroupformid').show();
	})
	.fail(function(data){
		alert("ERROR!!! Unable to contact server");
		$('#progressid').hide();
    }); 	
}

function getGroups(data){
	
    var retStr="<FORM id='deletegroupsid'>\n";
    var groups=data.groups;
    
    retStr+="<div id='groupstitleid'>Select Groups to Delete: </div>\n";
    retStr+="<span id='closedeletegroupsid' onclick='closeCreateGroups();'>Close</span>\n";
    
    retStr+="<div id='groupscolumnid'>\n";
    for(var i=0;i<groups.length;++i){
    	var agroup=groups[i];
    	retStr+=('<input class="groupnamecheckbox" type="checkbox" name="group" value="'+agroup.groupname+'">'+agroup.groupname+'<br>'); 	
    }
    retStr+="</div>\n";
    retStr+=('<br><input id=\'deletegroupsbtnid\' type=\'button\' onclick="deleteGroups(event);" value=\'Delete\'');
    retStr+='onmouseover="changeButtonColor(\'deletegroupsbtnid\',\'#FFEE66\');" onmouseout="changeButtonColor(\'deletegroupsbtnid\',\'#FFFF66\');">';
    retStr+="<input id='canceldeletegroupsid' type='button' onclick='cancelDeleteGroups();' value='Cancel' ";
    retStr+='onmouseover="changeButtonColor(\'canceldeletegroupsid\',\'#FFEE66\');" onmouseout="changeButtonColor(\'canceldeletegroupsid\',\'#FFFF66\');">';    
    
	retStr+="</FORM>\n";
	
	return retStr;
}

function cancelDeleteGroups(event){
	$('#deletegroupformid').hide();
	$('.groupnamecheckbox').prop('checked',false);
	
}

function closeCreateGroups(){
	$('#deletegroupformid').hide();
	$('.groupnamecheckbox').prop('checked',false);
}

//function cancelDeleteGroups(){
//	$('#deletegroupformid').hide();
//	var somethingsChecked=false;
//	$('input[type=checkbox]:checked').each(function(){
//		    somethingsChecked=true;
//		    return false;
//	});
//	if(somethingsChecked){
//	    $('.groupnamecheckbox').prop('checked',false);
//	}
//}



function deleteGroups(event){

    var selectedValues="";
    var deletedGroups="";
	$('input[type=checkbox]:checked').each(function(){
		selectedValues+=$(this).val();
		selectedValues+=":";
		deletedGroups+=$(this).val();
		deletedGroups+=", ";
	});
	selectedValues=selectedValues.substring(0,(selectedValues.length-1));
	deletedGroups=deletedGroups.substring(0,(deletedGroups.length-2));
	
	var url="/rax-autodeploy/CreateDeleteGroupsServlet?action=delete&deletegroups=";
	url+=selectedValues;

	$('#progressid').show();
	
    $('#progressid').ajaxStart(function(){
	    $('#progressid').show();
    })
    .ajaxStop(function(){
        $('#progressid').hide();
    });	
	
    $.post(url,{"deletegroups" : selectedValues},function(data){   
        $('#progressid').hide();
        var errormessage=data.errormessage;
    	//noldapuser would be set from CreateNewUserServlet.checkAccess(JSONObject,String,String,String)
    	var noldapuser=data.noldapuser;
    	if(null!=noldapuser && noldapuser!=undefined){
    		alert(noldapuser);
    		window.location.reload(true);
    	}        
    	else if(errormessage!=null && errormessage!=undefined){
            alert(errormessage);
        }
        else{
	        $('#createuserformid').hide();
        	var successmessage=data.successmessage;
        	if(null!=successmessage && successmessage!=undefined){
        	    alert(successmessage);
        	}
        	else{
        	    alert("Successfully deleted group(s): "+deletedGroups);
        	}
        	$('.createforminputclass').val('');
        	window.location.reload(true);
        }        	        
    })
    .done(function() { 
	
    })
    .fail(function(data){
        $('#createuserformid').hide();
	    alert("Server ERROR!!! could delete group(s): "+selectedValues);
    });	

}

function createNewUser(event){
	var url="/rax-autodeploy/CreateNewUserServlet?";

	var $fname=$('#fnameformid').val();
	var $lname=$('#lnameformid').val();
	var $ldapname=$('#ldapnameformid').val();
	var $email=$('#emailformid').val();
	
	var goOn=true;
	
	if($fname==''){
		$('#fnameformasteriskid').show();
		goOn=false;
	}
	else if($fname.length>40){
		alert('First name cannot be longer than 40 characters');
		$('#fnameformasteriskid').show();
		return;
	}	
	else{
		$('#fnameformasteriskid').hide();
		url+="fname=";
		url+=$fname;
	}
	
	if($lname==''){
		$('#lnameformasteriskid').show();
		goOn=false;
	}
	else if($lname.length>40){
		alert('Last name cannot be longer than 40 characters');
		$('#lnameformasteriskid').show();
		return;
	}
	else{
		$('#lnameformasteriskid').hide();
		url+="&lname=";
		url+=$lname;
	}
	
	if($ldapname==''){
		$('#ldapnameformasteriskid').show();		
		goOn=false;
	}
	else if($ldapname.length>70){
		alert('LDAP name cannot be longer than 70 characters');
		$('#ldapnameformasteriskid').show();
		return;
	}
	else{
		$('#ldapnameformasteriskid').hide();
		url+="&ldapname=";
		url+=$ldapname;
	}

	if($email==''){
		$('#emailformasteriskid').show();	
		goOn=false;
	}
	else if($email.length>70){
		alert('Email cannot be longer than 70 characters');
		$('#emailformasteriskid').show();
		return;
	}
	else{
		$('#emailformasteriskid').hide();
		url+="&email=";
		url+=$email;
	}
	
	if(goOn){
	    $('#progressid').show();
        $('#progressid').ajaxStart(function(){
	        $('#progressid').show();
        })
        .ajaxStop(function(){
            $('#progressid').hide();
        });	
	
        $.post(url,{"ldapname" : "blah"},function(data){   
        	$('#progressid').hide();
        	
        	//noldapuser would be set from CreateNewUserServlet.checkAccess(JSONObject,String,String,String)
        	var noldapuser=data.noldapuser;
        	if(null!=noldapuser && noldapuser!=undefined){
        		alert(noldapuser);
        		window.location.reload(true);
        	}
        	else{
        		var errormessage=data.errormessage;
        	    if(errormessage!=null && errormessage!=undefined){
        		    alert(errormessage);
        	    }
        	    else{
	                $('#createuserformid').hide();
        	        var successmessage=data.successmessage;
        	        if(null!=successmessage && successmessage!=undefined){
        	    	    alert(successmessage);
        	        }
        	        else{
        	    	    alert("Successfully created new user");
        	        }
        	        $('.createforminputclass').val('');
        	        window.location.reload(true);
        	    } 
        	}
        })
        .done(function() { 
	
        })
        .fail(function(data){
        	 $('#createuserformid').hide();
	        alert("Server ERROR!!! could not create new user: "+fname+ " "+ lname);
        });
    }
    else{
    	alert('All fields are required');
    }
}

function populateUsers(){
	var filteroption=$('#filterselectid option:selected')[0].value;
	var url='/rax-staging-services/rest/services/getfilteredusers?filteroption=';
	url+=filteroption;

	$('#progressid').show();
    $('#progressid').ajaxStart(function(){
	    $('#progressid').show();
    })
    .ajaxStop(function(){
        $('#progressid').hide();
    });		
	
	$.getJSON(url,{"filteroption" : filteroption},function(data){    	 
	    $('#progressid').hide();   
	    
	})
	.done(function(data) { 
        var str=getUsersCallback(data);
        $('#deploymenthistorytableid').html(str);
	})
	.fail(function(data){
		alert("ERROR!!! Unable to contact server");
		$('#progressid').hide();
    }); 
}

function getPrevious(event){
	getPrevOrNext(event,false);
}

function getPrevOrNext(event, isNext){
	var selectedVal=$('#filterselectid option:selected')[0].value;
	var currentIndex=$('tr:first').next().children()[0].innerHTML;	
	var url='';
	if(isNext){
		url='rax-staging-services/rest/services/getnextusers?currentindex=';
	}
	else{
		url='rax-staging-services/rest/services/getprevioususers?currentindex=';
	}
	url+=currentIndex;
	url+='&filteroption=';
	url+=selectedVal;
	
	$('#progressid').show();
    $('#progressid').ajaxStart(function(){
	    $('#progressid').show();
    })
    .ajaxStop(function(){
        $('#progressid').hide();
    });	

	$.getJSON(url,{"ldapname" : ldapname},function(data){    	 
	    $('#progressid').hide();        	        
	})
	.done(function(data) { 
        var str=getUsersCallback(data);
        $('#deploymenthistorytableid').html(str);
	})
	.fail(function(data){
		alert("ERROR!!! Unable to contact server");
		$('#progressid').hide();
    }); 	
}

function getNext(event){
	getPrevOrNext(event,true);	
}

function showCreateUserForm(){
	 $('#createuserformid').show();	
}

function getUsersCallback(data){
	var retStr="<tr>\n";
	retStr+="<th class='deployadminheaderclass'>&nbsp;</th>\n";
	retStr+="<th class='deployadminheaderclass'><span id='spanuserid'>User</span>\n";
	retStr+="<div id='createnewuserbuttonareaid'>\n";
	retStr+="<input id='createnewusertopbtnid' class='createnewuserbutton' type='button' value='Create new User'\n";
	retStr+='onclick="showCreateUserForm();"\n';
	retStr+='onmouseover="changeButtonColor(\'createnewusertopbtnid\',\'#A28899\');"\n';
	retStr+='onmouseout="changeButtonColor(\'createnewusertopbtnid\',\'#AA8899\');">\n';	
	retStr+="</div>\n";
	retStr+="</th>\n";
	
	retStr+="<th class='deployadminheaderclass'>LDAP Name</th>\n";	
	retStr+="<th class='deployadminheaderclass'>Status</th>\n";	
	
	retStr+="<th class='deployadminheaderclass groupnameheaderclass'>Group Names\n";
	retStr+="<span id='grouprelatedbuttons'>\n";
	
	retStr+="<input type='button' value='Create Group' id='creategroupbtnid'\n";
	retStr+='onclick="showCreateGroupForm();"\n';
	retStr+='onmouseover="changeButtonColor(\'creategroupbtnid\',\'#A28899\');"\n';
	retStr+='onmouseout="changeButtonColor(\'creategroupbtnid\',\'#AA8899\');">\n';	
	retStr+="<input type='button' value='Delete Group' id='deletegroupbtnid'\n";
	retStr+="onclick='createDeleteGroupForm();'\n";
	retStr+='onmouseover="changeButtonColor(\'deletegroupbtnid\',\'#A28899\');"\n';
	retStr+='onmouseout="changeButtonColor(\'deletegroupbtnid\',\'#AA8899\');">\n';	
		
	retStr+="</span>\n";
	retStr+="<span id='previousnextbuttontopid'></span>\n";	
	
	retStr+="</th>\n";
	retStr+="</tr>\n";
	
	var users=data.users;
	var startIndex=data.startindex;
	var countIndex=(++startIndex);
	
	for(var i=0;i<users.length;++i){
	    var aUser=users[i];	
	    var evenorodd=(countIndex%2);
	    if(evenorodd==0){
	    	retStr+="<tr id='row"+countIndex+"' class='deploymenthistortableoddrow'>\n";	    	
	    }
	    else{
	        retStr+="<tr id='row"+countIndex+"' class='deploymenthistortableevenrow'>\n";	    
	    }
	    retStr+="<td class='deployadmincolclass'>"+countIndex+"</td>\n";
	    retStr+="<td class='deployadmincolclass'>"+aUser.fname+" "+aUser.lname+"</td>\n";
	    retStr+="<td class='deployadmincolclass'>"+aUser.ldapname+"</td>\n";
	    retStr+="<td class='deployadmincolclass'>"+aUser.status+"</td>\n";
	    
	    retStr+="<td class='deployadmincolclass'>\n";
	    
	    var memberships=aUser.membership;
	    
	    if(memberships.length>0){
            retStr+="<ul>\n";
	        for(var j=0;j<memberships.length;++j){
	        	var aMembership=memberships[j];
	        	retStr+="<li>"+aMembership.groupname+"</li>\n";
	        }
	        retStr+="</ul>\n";	    
	    }
	    retStr+='<input id="deployadminbutton'+countIndex+'" type="button" class="deployadmineditbutton" \n';
	    retStr+='name="deploybutton" value="Edit"';
	    retStr+=('onclick="editMembersRow(\'deployadminbutton'+countIndex+'\', \'row'+countIndex+'\', \''+aUser.fname);
	    retStr+=('\', \''+aUser.lname+'\', \''+aUser.ldapname+'\');"\n'); 
	    retStr+=('onmouseover="changeButtonColor(\'deployadminbutton'+countIndex+',\'#AABBCC\');" \n');
	    retStr+=('onmouseout="changeButtonColor(\'deployadminbutton'+countIndex+'\',\'#CCDDEE\');"/> \n'); 
	    
	    retStr+="</td>\n";
	    retStr+="</tr>\n";
	    ++countIndex;
	}
	
	return retStr;
}

function getUserColumns(users){
	var retStr="";
}



function changeButtonColor(btnId, color){
	$(('#'+btnId)).css('background-color',color);
}


function editMembersRow(deployadminbuttonid, rowid, fname, lname, ldapname){
	//alert("deployadminbuttonid="+deployadminbuttonid+" rowid="+rowid+" ldapname="+ldapname);
	
	//getldapname
	var theUrl="/rax-staging-services/rest/services/getmembers?ldapname="+ldapname;

	$('#progressid').show();
    $('#progressid').ajaxStart(function(){
	    $('#progressid').show();
    })
    .ajaxStop(function(){
        $('#progressid').hide();
    });	
    
	$.getJSON(theUrl,{"ldapname" : ldapname},function(data){    	
	    var markup=getMembers(data, fname, lname, ldapname);
	    $('#editgroupnamesformid').html(markup);	 
	    $('#progressid').hide();	    
		})
		.done(function() { 
			$('#editgroupnamesformid').show();
		})
		.fail(function(data){
		       alert("Unable to contact server.");
		       $('#progressid').hide();
		   });    	
}

function getMembers(data, fname, lname, ldapname){
	var members=data.members;
	membersmap={};
    //alert(members);
    var retVal="<FORM class='membershipform'>";
    
    retVal+="<div id='userstatusid'>User: "+fname+" "+lname+" (LDAP Name: "+ldapname+")</div>\n";
    retVal+="<span id='deletegroupsclosetext' onclick='closeMembership(event);'>Close</span>\n";
    var status=data.status;
    
    if(null!=status && status!=undefined){
        retVal+="<div id='statusid'>Status:</div>\n";
        
        if(status=='active'){
            retVal+="<span class='statusclass'><input type='radio' name='status' value='active' checked>active <br></span>\n";
            retVal+="<span class='statusclass'><input type='radio' name='status' value='inactive'>inactive <br></span>\n";
        }
        else{
            retVal+="<span class='statusclass'><input type='radio' name='status' value='active'>active <br></span>\n";
            retVal+="<span class='statusclass'><input type='radio' name='status' value='inactive' checked>inactive <br></span>\n";        	
        }
    
    }
    retVal+=("<div id='usergroupmembershipid'>Group Membership:</div>\n");
    
    retVal+="<div id='memberscolumnid'>\n";
    for(var i=0;i<members.length;++i){
    	var amember=members[i];
    	var isAMember=amember.containsgroup;
    	var groupName=amember.groupname;
    	if(isAMember=="true"){
    	    retVal+=('<input class="groupnamecheckbox" type="checkbox" name="member" value="'+groupName+'" checked>'+groupName+'<br>\n');
    	    membersmap[groupName]=groupName;
    	}
    	else{
    		retVal+=('<input class="groupnamecheckbox" type="checkbox" name="member" value="'+groupName+'">'+groupName+'<br>\n');
    	}
    }
    retVal+="</div>\n";
    retVal+=('<br><input id=\'updatebuttonid\' type=\'button\' onclick="updateMembers(event,\''+fname+'\',\''+lname+'\',\''+ldapname+'\');" value=\'Update\' \n');
    retVal+='onmouseover="changeButtonColor(\'updatebuttonid\',\'#3782D2\');" onmouseout="changeButtonColor(\'updatebuttonid\',\'#5FAAFA\');"> \n';
    retVal+="<input id='cancelbuttonid' type='button' onclick='cancelUpdateMembers(event);' value='Cancel' \n";
    retVal+='onmouseover="changeButtonColor(\'cancelbuttonid\',\'#3782D2\');" onmouseout="changeButtonColor(\'cancelbuttonid\',\'#5FAAFA\');"> \n';
    retVal+="</FORM>\n";
    return retVal;
    
}

function closeMembership(event){
	$('#editgroupnamesformid').hide();
}

function updateMembers(event, fname, lname, ldapname){ 	
	var updateMembersSelection="/rax-autodeploy/UpdateUserGroupMembersServlet?ldapname="+ldapname+"&status=";
	//var removedSelections="/rax-staging-services/rest/services/removemembers?ldapname="+ldapname+"&members=";
	var selectedMap={};
	
    var statusvalue=$('input[name=status]:checked', '.membershipform').val();
    updateMembersSelection+=statusvalue;	

    updateMembersSelection+="&addmembers=";
    
	//first find all the newly selected rows
    $("input:checkbox[name='member']:checked").each(function(){
    	var aSelectedMember=$(this).val();
    	//update the selectedMap
    	selectedMap[aSelectedMember]=aSelectedMember;
    	var alreadyAMember = membersmap.hasOwnProperty(aSelectedMember);
    	//this is a newly selected 
    	if(!alreadyAMember){
    		var lastIndex=updateMembersSelection.lastIndexOf("=");
    		var lastNewSelectionsChar=(updateMembersSelection.length-1);
    		//updateMembersSelection ends with =
    		if(lastIndex==lastNewSelectionsChar){
    			updateMembersSelection+=aSelectedMember;	
    		}
    		//updateMembersSelection does not end with =
    		else{
    			updateMembersSelection+=":";
    			updateMembersSelection+=aSelectedMember;    			
    		}  		
    	}
    });
    
    updateMembersSelection+="&removemembers=";
    //Now iterate through the global membersmap and see if there are any unselected groups
    for(var agroup in membersmap){
    	var unchecked=(!selectedMap.hasOwnProperty(agroup));
    	//The group is un-selected
    	if(unchecked){
    		var lastIndex=updateMembersSelection.lastIndexOf("=");
    		var lastRemovedSelectionsChar=(updateMembersSelection.length-1);
    		//updateMembersSelection ends with =
    		if(lastIndex==lastRemovedSelectionsChar){
    			updateMembersSelection+=agroup;
    		}
    		else{
    			updateMembersSelection+=":";
    			updateMembersSelection+=agroup;
    		}
    	}
    }
    //Now have the rest call
    var updateMembersSelectionStr=updateMembersSelection;

	$('#progressid').show();
    $('#progressid').ajaxStart(function(){
	    $('#progressid').show();
    })
    .ajaxStop(function(){
        $('#progressid').hide();
    });	

	$.post(updateMembersSelectionStr,{"ldapname" : ldapname},function(data){    	 
	        $('#progressid').hide();
	        $('#editgroupnamesformid').hide();	     
	        window.location.reload(true);	        	        
	})
	.done(function(data) { 
		$('#editgroupnamesformid').hide();
		var successMessage=data.successmessage;
		if(null!=successMessage && successMessage!=undefined){
			alert(successMessage);
		}
		else{
		    alert("Update for "+fname+" "+lname+" was successful.");
		}
	})
	.fail(function(data){
		alert("ERROR!!! Group membership update for "+fname+" "+lname+" failed");
		$('#progressid').hide();
    });     
    
}

function cancelUpdateMembers(event){
	$('#editgroupnamesformid').hide();
}

