
function DeployAdminCtrl($scope, $http){

	$scope.usersList=[];
	$scope.members=[];
	$scope.foruser='';
	
	$scope.getInitialUsers=function(){
		$('#progressid').show();
		$http({method:'GET', url:"/rax-autodeploy/initialAdminUsers"}).
	    success(function(data, status){
	    	$('#progressid').hide();
	    	$scope.usersList=data;	    	
	    }).
	    error(function(data, status){
	    	$('#progressid').hide();
	    	alert("Could not contact server for initial data load.");
	    });			
	};
	
	$scope.getInitialUsers();
	
	$scope.changeButtonColor=function(id, color){
		$(('#'+id)).removeAttr('style');
        $(('#'+id)).css('background-color',color);		
	};
	
	$scope.showCreateUserForm=function(){
		 $('#createuserformdivid').show();
	};
	
	$scope.showCreateGroupForm=function(){
		$('#creategroupformdivid').show();
	};
	
	$scope.closeAndClearForm=function(divFormId,formId){
		
		//Only clear the text fields if a formId was passed in
		if(null!=formId && formId!=undefined){
			var formInputs=$('#'+formId+' :input');
			if(null!=formInputs && formInputs!=undefined){
				$.each(formInputs, function(index,anInput){
					if(anInput.type=='text'){
						anInput.value='';
						$(anInput).removeAttr('style');
					}
				});
			}
		}
		
		$('#'+divFormId).hide();
	};
	
	$scope.showDeleteGroupForm=function(){
		/*
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
	    */	
	};
	
	$scope.editMembersRow=function(event,theUser){
        var ldapUser=$.trim($($($($(event.target).parent()).parent()[0]).children()[2]).html());
        $scope.foruser=ldapUser;
        var theUrl=("/rax-autodeploy/getGroupMembershipForUser?ldapname="+ldapUser);
			
        $http({method:'GET', url:theUrl}).
            success(function(data, status){
                $('#editgroupnamesformdivid').show();
                $scope.members=data;
            }).
            error(function(data,status){
                alert('Error unable to contact server to retrieve Group membership list.');
            });	
	};
	
	$scope.updateGroupsForUser=function(event,loggedInUser){
		var selectedMembers=$('#editgroupnamesformid :input[name="member"]:checked');
		var theUrl="/rax-autodeploy/updateGroupsForUser?ldapname=";	
		var forTheUser=$scope.foruser;
		theUrl+=(forTheUser);
		theUrl+=("&loggedInUser="+loggedInUser);
		
		var parameters="";
		$.each(selectedMembers,function(index,aMember){
			parameters+="&member=";
			parameters+=aMember.value;
		});
		theUrl+=parameters;
		$('#progressid').show();
		$http({method:'POST', url:theUrl}).//,data:theData}).
	    success(function(data, status){
	    	$('#editgroupnamesformdivid').hide();
	    	$('#progressid').hide();
	    	window.location.reload(true);
	    }).
	    error(function(data, status){
	    	$('#progressid').hide();
	    	var errormessages="Error returned from server.\n";
	    	var messages=data.errormessages;
	    	if(null!=messages){
	    	    $.each(messages,function(index,aMessage){
	    		    errormessages+=aMessage;
	    		    errormessage+="\n";
	    	    });
	        }
	    	alert(messages);
	    });		
	};
	
	$scope.createNewUser=function(event, loggedInUser){
		var fname=$('#fnameformid')[0].value;
		var lname=$('#lnameformid')[0].value;
		var ldapname=$('#ldapnameformid')[0].value;
		var email=$('#emailformid')[0].value;
		
		if(null!=ldapname && ldapname!=undefined && $.trim(ldapname)!=''){
			if(null==loggedInUser||loggedInUser==undefined||loggedInUser==''){
				alert('No logged in user was found, please clear the browser cache and log back in.');
			}
			else{
				var theUrl="/rax-autodeploy/createNewUser?";
				theUrl+=("fname="+fname);
				theUrl+=("&lname="+lname);
				theUrl+=("&ldapname="+ldapname);
				theUrl+=("&email="+email);
				theUrl+=("&loggedInUser="+loggedInUser);

				$('#progressid').show();
				$http({method:'POST', url:theUrl}).//,data:theData}).
				success(function(data, status){
					$($('#ldapnameformid')[0]).removeAttr('style');
					$('#createuserformdivid').hide();
					$scope.closeAndClearForm('createuserformdivid', 'creategroupformid');
					$('#progressid').hide();
					window.location.reload(true);
				}).
				error(function(data, status){
					$('#progressid').hide();
					var errormessages="Error returned from server.\n";
					var messages=data.errormessages;
					if(null!=messages){
						$.each(messages,function(index,aMessage){
							errormessages+=aMessage;
							errormessage+="\n";
						});
					}
					alert(messages);
				});			

			}	
		}
		else{
			alert('LDAP name field must have a value.');
			$($('#ldapnameformid')[0]).removeAttr('style');
			$($('#ldapnameformid')[0]).css('border-color','red');
		}
	};
	
	$scope.createNewGroup=function(event,loggedInUser){
		var groupName=$('#creategroupnameid')[0].value;
		
		if(null!=groupName && groupName!=undefined){
			var theUrl="/rax-autodeploy/createNewGroup?groupName=";
			theUrl+=groupName;
			theUrl+="&loggedInUser=";
			if(loggedInUser!=null && loggedInUser!=undefined){
			    theUrl+=loggedInUser;
				$('#progressid').show();
				$http({method:'POST', url:theUrl}).//,data:theData}).
			    success(function(data, status){
			    	$($('#creategroupnameid')[0]).removeAttr('style');
			    	$scope.closeAndClearForm('creategroupformdivid', 'creategroupformid');
			    	$('#progressid').hide();
			    	window.location.reload(true);
			    }).
			    error(function(data, status){
			    	$('#progressid').hide();
			    	var errormessages="Error returned from server.\n";
			    	var messages=data.errormessages;
			    	if(null!=messages){
			    	    $.each(messages,function(index,aMessage){
			    		    errormessages+=aMessage;
			    		    errormessage+="\n";
			    	    });
			        }
			    	alert(messages);
			    });				    
			    
			}
			else{
				alert('No logged in user was found, please clear the browser cache and log back in.');
			}
			
		}
		else{			
			$($('#creategroupnameid')[0]).removeAttr('style');
			$($('#creategroupnameid')[0]).css('border-color','red');
			alert('Group name field must have a value.');
		}
	};
	
}