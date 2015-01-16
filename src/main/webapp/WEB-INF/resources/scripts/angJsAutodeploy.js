function AutoDeployCtrl($scope, $http){
	
	$scope.mypasswordcurrent='';
	$scope.mypasswordnew='';
	$scope.mypasswordconfirm='';
	$scope.changepasswordwidget=false;
	$scope.changepasswordwidgetinternal=false;
	$scope.disableButtons=false;
	$scope.detailsLoading=false;
	$scope.details=[];
	$scope.extWars={};
	
    $('input:checkbox').on('change',function(event){
    	var theCheckBox=$(this);
        detectCheckboxClick(event,theCheckBox);
     });
	
	$scope.clearForm=function(){
		$('.successicons, .erroricons, .progimg, .returnmessage').hide();	
		$('.warfoldernames').removeAttr('checked');
		$('.warfoldernames, .deploybutton, .revertbutton').removeAttr('disabled');
		$('.deployrow').css("background-color","#FFFFFF");
		$('.detailsimage').attr('onclick','handleDetails(event);');		
	};
	
	$scope.changeButtonColor=function(btnId, color){
		if(!$scope.disableButtons){
		    $(('#'+btnId)).css('background-color',color);
		}
	};
		
	$scope.filterWars=function(event){
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
	};
	
	$scope.freezeUI=function(event,loggedInUser,isExternal){
		var freezeui=$('input[name=freeze]:checked', '#freezeuiformid').val();
		
		var theUrl="/rax-autodeploy/deployTheWars?username="+loggedInUser+"&freezeui="+freezeui+"&isexternal="+isExternal;
		//var theData='{"username":'+loggedInUser+',"freezui":'+freezeui+',"isexternal":'+isExternal+'}';

		$scope.detailsLoading=true;
		//$http.jsonp(theUrl,{method:'POST'});
		$http({method:'POST', url:theUrl}).//,data:theData}).
	    success(function(data, status){
	    	window.location.reload(true);
	    	$scope.detailsLoading=false;
	    	$scope.disableButtons=true;
	    	freezeButtons($scope);
	    }).
	    error(function(data, status){
	    	//We will get the cross domain JSON scripting error, but we already set the 
	    	//Allow-Control-Access in the head within the service. We need to switch to
	    	//JSONP to get rid of this error, but the call will work since server side,
	    	//we allow cross domain scripting
	    	//alert("Error: server is not responding");	
	    	//$scope.disableButtons=false;
	    	//$scope.detailsLoading=false;
	    	window.location.reload(true);
	    	$scope.detailsLoading=false;
	    	$scope.disableButtons=true;
	    	freezeButtons($scope);	    	
	    });		
				
	};
	
	$scope.closeDetails=function(event){
		//First check to see if there are any books checked
		var $checkedBooks=$('input[name="warfoldernames"]:checked');
		//Nothing is checked just make sure everything is enabled
		if(null==$checkedBooks||$checkedBooks==undefined||$checkedBooks.length==0){
		    $('.warfoldernames, .deploybutton, .revertbutton, #filterspan').removeAttr('disabled');
		}
		else{
			//Only enable the correct check boxes, deploy, and revert buttons
			//Enable the relevant check boxes
			$checkedBooks.parent().parent().children().find('input[name="warfoldernames"]').removeAttr('disabled');
			//Enable the details button
			$($($checkedBooks.parent().parent().parent().parent().children()[2]).find('input')[0]).removeAttr('disabled');
			//Enable the deploy button
			$($($checkedBooks.parent().parent().parent().parent().children()[3]).find('input[name="deploybutton"]')[0]).removeAttr('disabled');
			//Enable the revert button
			$($($checkedBooks.parent().parent().parent().parent().children()[3]).find('input[name="revertbutton"]')[0]).removeAttr('disabled');
		}
		$('.deploytable, #top').removeClass('opacityclass');
		//Make sure the buttons are enabled
		$scope.disableButtons=false;
		$('#detailsid').hide();
	};
	
	
	$scope.handleDetails=function(event){
		//Disable all the button actions
		$scope.disableButtons=false;
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
		    var theUrl="/rax-autodeploy/getDetails?folders="+query;
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
		    
			$http({method:'GET', url:theUrl}).
		    success(function(data, status){
		    	$('#progressid').hide();
		    	$scope.details=data;
		        $('#detailsid').show();			    	
		    }).
		    error(function(data, status){
		    	$('#progressid').hide();
				alert("Unable to contact server.");
				$('#progressid').hide();
				$('.warfoldernames, .deploybutton, .revertbutton, #filterspan').removeAttr('disabled');	    
				$('.deploytable').removeClass('opacityclass');
				$('#top').removeClass('opacityclass');	 		    	
		    });		   
	    }
	};
	
	
	$scope.deployToProd=function(deployrowid, app, count,isInternal){
		//Only execute the button if buttons are NOT disabled, buttons are disabled when the details are being viewed,
		//or when changing the password
		if($scope.disableButtons==false){
			var $checkedBooks=$('input[name="warfoldernames"]:checked');
			var keepGoing=true;
			//Make sure that at least one war was selected
			if(null!=$checkedBooks && $checkedBooks!=undefined && $checkedBooks.length==0){
				keepGoing=false;
			}
			if(keepGoing){
				var freezeURL="/rax-autodeploy/getShouldFreeze";

				$http({method:'GET', url:freezeURL}).
				success(function(data, status){
					if(null!=data && null!=data.frozen && data.frozen!=undefined){
						if(data.frozen=="true"){
							window.location.reload(true);
						}
						else{
							handleDeploy($http, deployrowid,app,count,isInternal);
						}
					}
					//if we can't detect whether shouldfreeze is set, just go ahead and try to deploy
					else{
						handleDeploy($http, deployrowid, app, count, isInternal);
					} 		    	
				}).
				error(function(data, status){
					alert("Error: server is not responding with status: "+status); 		    	
				});		        
			}
			else{
				alert("No war was selected. Select at least 1 war to deploy.");
			}
		}
	};
	
	$scope.revertDeployedWarsConfirmation=function(deployrowid, app, count, internaldeploy){
		//Only execute the button if buttons are NOT disabled, buttons are disabled when the details are being viewed,
		//or when changing the password
		if($scope.disableButtons==false){
			var $checkedBooks=$('input[name="warfoldernames"]:checked');
			var keepGoing=true;
			//Make sure that at least on war was selected
			if(null!=$checkedBooks && $checkedBooks!=undefined && $checkedBooks.length==0){
				keepGoing=false;
			}
			if(keepGoing){
				var freezeURL="/rax-autodeploy/getShouldFreeze";
				$('#progressid').show();
				$http({method:'GET', url:freezeURL}).
				success(function(data, status){
					if(null!=data && null!=data.frozen && data.frozen!=undefined){
						if(data.frozen=="true"){
							window.location.reload(true);
						}
						else{
							doRevert($http, deployrowid, app, count, internaldeploy);
						}
					}
					//if we can't detect whether shouldfreeze is set, just go ahead and try to deploy
					else{
						doRevert($http, deployrowid, app, count, internaldeploy);
					} 		    	
				}).
				error(function(data, status){
					$('#progressid').hide();
					alert("Error: server is not responding with status: "+status); 		    	
				});	
			}
			else{
				$(".deployrow").css("background-color","#FFFFFF");
				$("#"+deployrowid).css("background-color","#DD4B39");
				alert("No war was selected. Select at least 1 war to deploy.");
			}	   
		}
	};
	
	$scope.openChangePasswordForm=function(){
		$scope.disableButtons=true;
		//the change button was clicked, the first thing we should do
		//is check to make sure that the session is still valid
		$http({
			method: 'GET',
			url: '/rax-autodeploy/checkValidSession'
		}).
		success(function(data, status, headers, config){
			$scope.disableButtons=false;
			var status=data.status;
			if(null==status||status=='failed'){
				//Go to the login page
				window.location.href="/rax-autodeploy/Login";
			}
			else{
				//show the change password widget
				$scope.changepasswordwidget=true;
				$scope.changepasswordwidgetinternal=true;
			}
		}).
		error(function(data, status, headers, config){
			$scope.disableButtons=false;
			//Go to the login page
			window.location.href="/rax-autodeploy/Login";			
		});
	};	
	
	$scope.closePassword=function(){
		$('#detailsid').hide();
		$scope.mypasswordcurrent='';
		$scope.mypasswordnew='';
		$scope.mypasswordconfirm='';
		$scope.changepasswordwidget=false;
		$scope.changepasswordwidgetinternal=false;
	};
	
	$scope.chagePassword=function(){
		if($scope.validateChangePasswordForm()){
			var theData={'username':$('#id_username')[0].value, 
					'password':$scope.mypasswordcurrent,
					'confirmpassword':$scope.mypasswordconfirm,
					'newpassword':$scope.mypasswordnew
			};
			$http({
				method:'POST',
				data:theData,
				url:'/rax-autodeploy/changePassword'

			}).
			success(function(data, status, headers, config){
				if(data.status==='failed'){
					alert(data.message);
				}
				else{
				    $scope.closePassword();
				    alert("Password change successful");
				}
			}).
			error(function(data,status,headers,config){
				$scope.closePassword();
				//something went wrong 
				alert("Error changing password status: "+status);
			});
		}
	};
	
	$scope.validateChangePasswordForm=function(){
		var retVal=true;
		if($.trim($scope.mypasswordcurrent)===''){
			retVal=false;
			alert("Current Password field cannot be empty");
		}
		else if($scope.mypasswordcurrent.length>20){
			retVal=false;
			alert("Current Password field length cannot be greater than 20 characters.");
		}
		else if($scope.mypasswordcurrent.length<5){
			retVal=false;
			alert("Current Password field must be greater than 4 characters.");
		}
		else if($scope.mypasswordcurrent.indexOf('<')!=-1 || ($scope.mypasswordcurrent.indexOf('>')!=-1)||
				($scope.mypasswordcurrent.indexOf(' ')!=-1) || ($scope.mypasswordcurrent.indexOf('	')!=-1)){
			retVal=false;
			alert("Current Password field contains illegal characters.");
		}
		else if($.trim($scope.mypasswordnew)===''){
			retVal=false;
			alert("New Password field cannot be empty");
		}
		else if($scope.mypasswordnew.length>20){
			retVal=false;
			alert("New Password field length cannot be greater than 20 characters.");
		}
		else if($scope.mypasswordnew.length<5){
			retVal=false;
			alert("New Password field length must be greater than 4 characters.");
		}	
		else if($scope.mypasswordnew.indexOf('<')!=-1 || ($scope.mypasswordnew.indexOf('>')!=-1)||
				($scope.mypasswordnew.indexOf(' ')!=-1) || ($scope.mypasswordnew.indexOf('	')!=-1)){
			retVal=false;
			alert("New Password field contains illegal characters.");
		}	
		else if($.trim($scope.mypasswordconfirm)===''){
			retVal=false;
			alert("Confirm New Password field cannot be empty");
		}
		else if($scope.mypasswordconfirm.length>20){
			retVal=false;
			alert("Confirm New Password field length cannot be greater than 20 characters.");
		}
		else if($scope.mypasswordconfirm.length<5){
			retVal=false;
			alert("Confirm New Password field length must be greater than 4 characters.");
		}	
		else if($scope.mypasswordconfirm.indexOf('<')!=-1 || ($scope.mypasswordconfirm.indexOf('>')!=-1)||
				($scope.mypasswordconfirm.indexOf(' ')!=-1) || ($scope.mypasswordconfirm.indexOf('	')!=-1)){
			retVal=false;
			alert("Confirm New Password field contains illegal characters.");
		}
		else if($scope.mypasswordnew!=$scope.mypasswordconfirm){
			retVal=false;
			alert("New Password and Confirm New Password values do not match");
		}
		return retVal;
	};
}


function freezeButtons($scope){
	$('.warfoldernames, .deploybutton, .revertbutton, #filterspan').attr('disabled','true');	    
	$('.deploytable').addClass('opacityclass');
	$('#top').addClass('opacityclass');	  

	
    /*var details=document.getElementsByClassName('detailsimage');
    for(var i=0;i<details.length;++i){
    	details[i].removeAttribute('onclick');
    } 
    */   
}

function detectCheckboxClick(event,$theCheckBox){

    var $parentUl = $theCheckBox.closest('ul');
    var $parentTr = $theCheckBox.closest('tr');
    var $checkBoxesForRow=$parentUl.children().children();
    var $theButton=$($parentTr.find('td')[3]).children()[0];  
    var $revertButton=$($($parentTr.find('td')[3]).children()[1]).children()[0];    
    
    var $detailsImg=$($($parentTr[0]).children()[2]).find('input')[0];   
    var $theButtonClass=$theButton.className;
    
    if(null!=$theButtonClass && $theButtonClass!=undefined && $theButtonClass=='deploybutton internal'){
    	$detailsImg=$parentTr.find('span').find('img');
    }
    //A checkbox was clicked, disable all rows except for the row that was clicked
    if ($theCheckBox.is(':checked')) {
        $('.deploybutton, .detailsimage, .revertbutton, .warfoldernames').attr('disabled','true');
        //$('.warfoldernames, .detailsimage, .revertbutton').unbind('click');
        
        $($checkBoxesForRow).removeAttr('disabled');
        $($theButton).removeAttr('disabled');
        $($revertButton).removeAttr('disabled');
        $($detailsImg).removeAttr('disabled'); 
        
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
            $('.deploybutton, .detailsimage, .revertbutton, .warfoldernames').removeAttr('disabled');
        }
    }     
}

function handleDeploy($http,deployrowid, app, count, deployToInternal){
	//alert('deployformname='+deployformname+" deployrowid="+deployrowid+" app="+app+" count="+count);	

	var requestUrlNParams="/rax-autodeploy/deployTheWars?";	
	var somethingChecked=false;
	//This url is used to check the status of the build
	var temp="";
	//Get the list of check input
    var listOfCheckBoxes=$($($($('#'+deployrowid)).children()[1]).find('ul')).children();
    
    $.each(listOfCheckBoxes,function(index,aList){
    	var aCheckBoxInput=$(aList).find('input')[0];
    	if(null!=aCheckBoxInput && aCheckBoxInput!=undefined && aCheckBoxInput.checked){
    		requestUrlNParams+=aCheckBoxInput.name;
    		requestUrlNParams+="=";
    		requestUrlNParams+=aCheckBoxInput.value;
    		temp+=aCheckBoxInput.value;
    		if(index!=(aList.length-1)){
    			temp+=":";
    			requestUrlNParams+="&";
    		}
    		somethingChecked=true;
    	}
    });
    //Remove the trailing : char if it exists
	if(temp.match(/:$/)){
		temp=temp.substring(0,(temp.length-1));
	}    
    //Add an & only if one does not exist at the end
	if(!requestUrlNParams.match(/&$/)){
		requestUrlNParams+="&";
	}
	
	//Now we have to get the three hidden fields
    var hiddenFields=$($($('#'+deployrowid)).children()[2]).children();
    
    $.each(hiddenFields,function(index,aField){
    	var type=aField.type;
    	if(null!=type && type!=undefined && type=="hidden"){
    		requestUrlNParams+=aField.name;
    		requestUrlNParams+="=";
    		requestUrlNParams+=aField.value;
    		if(index!=(hiddenFields.length-1)){
    			requestUrlNParams+="&";
    		}
    	}
    });
    
    var theUrl='/rax-autodeploy/getDetails?folders='+temp;
    
    //Add an & only if one does not exist at the end
	if(!requestUrlNParams.match(/&$/)){
		requestUrlNParams+="&";
	}
	requestUrlNParams+=(("internaldeploy=")+deployToInternal);
    //Add an & only if one does not exist at the end
	if(!theUrl.match(/&$/)){
		theUrl+="&";
	}
	theUrl+=(("internaldeploy=")+deployToInternal);
	
	if(somethingChecked==false){
		$(".deployrow").css("background-color","#FFFFFF");
		$("#"+deployrowid).css("background-color","#DD4B39");
		alert("No war was selected. Select at least 1 war to deploy.");
	}
	else{

        $('#progressid').show();

		$http({method:'GET', url:theUrl}).
	    success(function(data, status){
	    	//$('#progressid').hide();
	    	var aDetail=data[0];
	    	if(null!=aDetail){
	    		var theResult=aDetail.result;
	    	    if(theResult==null || theResult==undefined){
	    		    var conf=confirm("A Jenkins build is in progress or could not be found do you want to continue?");
	    		    if(conf==true){
	    			    continueWithDeploy($http,requestUrlNParams, count);
	    		    }
		    	    else{
		    		    alert('Deployment aborted.');	    		    
		    	    }	    		
	    	    }
	    	    else{
	    		    if((theResult.toLowerCase().indexOf('success'))!=-1){
	                    continueWithDeploy($http,requestUrlNParams, count);
	    		    }
	    		    else{
		    		    var conf=confirm("The last Jenkins build was not successful would you like to continue?");
		    		    if(conf==true){
		    			    continueWithDeploy($http,requestUrlNParams, count);
		    		    }	    			
		    		    else{
		    			    //alert('Deployment aborted.');
		    		    }
	    		    }
	    	    }	    	    
	    	}		    	
	    }).
	    error(function(data, status){
			 $('#progressid').hide();
			 alert("Unable to contact server.");
			 $('.warfoldernames, .deploybutton, .revertbutton, #filterspan').removeAttr('disabled');	    
			 $('.deploytable').removeClass('opacityclass');
			 $('#top').removeClass('opacityclass');	 		    		    	
	    });        
	}		
}


function continueWithDeploy($http,requestUrlNParams, count){
    //$.post(requestUrlNParams);   
	

	$('#progressid').hide();
	$('.successicons').hide();
	$('.erroricons').hide();
	$('.returnmessageclass').hide();
    $('#returnmessagespan'+count).html("Your deployment request has been submitted. You will receive<br>an e-Mail notifiction once your request has been processed.");
    $('#returnmessagespan'+count).show();
    
    //TODO before we call the post, we should check to see if the x-user-name is still in session
    
    //We just want to call the post method without a callback because it could take a long time for
    //the server to return a status, therefore just call the post
	$http({method:'POST', url:requestUrlNParams}); 
    //window.location.href="http://docs-staging.rackspace.com/history";
	
	var windowUrl=window.location.hostname;
	if(null!=windowUrl){
		var index=windowUrl.indexOf("internal");
		if(-1==index){
			window.location.href="http://docs-staging.rackspace.com/rax-autodeploy/DeployHistory";
		}
		else{
			window.location.href="http://docs-internal-staging.rackspace.com/rax-autodeploy/DeployHistory";		
		}
	}
	else{
        window.open("http://docs-staging.rackspace.com/history");
	}
}


function doRevert($http, deployrowid, app, count, internaldeploy){
	
	//Make sure that all rows are white
	$(".deployrow").css("background-color","#FFFFFF");
	$('#progressid').hide();
	
	var selectedWarsStr='';
	var selectedWars=$('input[name="warfoldernames"]:checked');
	var temp="";
	var temp2="";
	var warsSelectedParam='';
	$.each(selectedWars,function(index,aSelectedWar){
		temp2=$(aSelectedWar).attr('name');
		temp2=temp2.replace(/\s/g, '');
		
		temp=$(aSelectedWar).attr('value');
		temp=temp.replace(/\s/g, '');
				
		warsSelectedParam+=temp2;
		warsSelectedParam+="=";
		warsSelectedParam+=temp;
		if(index!=(selectedWars.length-1)){
			warsSelectedParam+="&";
		}				
		selectedWarsStr+=("\n*"+temp);
	});
	var message="Are you sure you want to revert to backup for the following doc projects?"+selectedWarsStr;
		
    if(confirm(message)){
    	revertDeployedWars($http, deployrowid, app, count, internaldeploy,warsSelectedParam);
    }
    else{
    	//alert("Revert to backup aborted");
    }				
}


function revertDeployedWars($http, deployrowid, app, count, internaldeploy, warsSelectedParam){

	var requestUrlNParams="/rax-autodeploy/deployTheWars?";
	requestUrlNParams+=warsSelectedParam;

	if(!requestUrlNParams.match(/&$/)){
		requestUrlNParams+="&";
	}
    requestUrlNParams+="action=revert";
    requestUrlNParams+="&internaldeploy="+internaldeploy;		
    
    $('#progressid').hide();
    
	$('#progressid').hide();
	$('.successicons').hide();
	$('.erroricons').hide();
	$('.returnmessageclass').hide();
    $('#returnmessagespan'+count).html("Your revert request has been submitted. You will receive<br>an e-Mail notifiction once your request has been processed.");
    $('#returnmessagespan'+count).show();	
    
  //$.post(requestUrlNParams);
	$http({method:'POST', url:requestUrlNParams});
}





