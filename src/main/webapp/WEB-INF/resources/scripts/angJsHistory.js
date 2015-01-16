function DeployHistoryCtrl($scope, $http){
	
	$scope.deployJobs=[];
	$scope.deployJobsMaster=[];
	$scope.MAX_DISPLAY=50;

	$scope.getInitialJobs=function(){

		$('#progressid').show();
		$http({method:'GET', url:"/rax-autodeploy/getInitialJobs"}).
	    success(function(data, status){
	    	$('#progressid').hide();
	    	$scope.deployJobsMaster=data;
	    	if($scope.deployJobsMaster.length!=undefined){
	    	    for(var i=0;i<$scope.MAX_DISPLAY && i<$scope.deployJobsMaster.length;++i){
	    	    	$scope.deployJobs.push($scope.deployJobsMaster[i]);
	    	    }
	    	}	    	
	    }).
	    error(function(data, status){
	    	$('#progressid').hide();
	    	alert('Unable to contact server, could not load data.');
	    });
		
	};
	//Get the inital Deployjobs
	$scope.getInitialJobs();
	
	$scope.changeColor=function(id, color){
		$(('#'+id)).removeAttr('background-color');
        $(('#'+id)).css('background-color',color);		
	};
	
	$scope.removeBackGroundColor=function(id){
		$(('#'+id)).removeAttr('style');
	};
	
	$scope.getNextJobs=function(event){
		
		var tableRows=$($('#deploymenthistorytableid').children()).children();
		if(null!=tableRows && tableRows!=undefined){
		    var lastDataRow=tableRows[(tableRows.length)-2];
		    var lastDataRowNumber=parseInt($.trim($($(lastDataRow).children()[0]).html()));
		   
		    
		    var masterLength=$scope.deployJobsMaster.length;
		    //There are more rows to display, get them
		    if(lastDataRowNumber<masterLength ){
		    	$scope.deployJobs=[];
		    	for(var i=lastDataRowNumber;(i<(lastDataRowNumber+$scope.MAX_DISPLAY)) && (i<masterLength);++i){
		    		$scope.deployJobs.push($scope.deployJobsMaster[i]);
		    	}
		    	var newTableLength=$scope.deployJobs.length;
		    	var firstJob=$scope.deployJobs[0];
		    	
		    	if((firstJob.count)>=$scope.MAX_DISPLAY){
		    		$('#clickprevioustopid').show();
		    		$('#clickpreviousid').show();
		    		$($($('#clickprevioustopid').find('img'))[0]).show();
		    		$($($('#clickpreviousid').find('img'))[0]).show();		    		
		    	}

		    	if((lastDataRowNumber+newTableLength)>=masterLength){
		    		$('#clicknexttopid').hide();
		    		$('#clicknextid').hide();
		    		$($($('#clicknexttopid').find('img'))[0]).hide();
		    		$($($('#clicknextid').find('img'))[0]).hide();
		    	}
		    }	
		    else if(lastDataRowNumber==masterLength){
	    		$('#clicknexttopid').hide();
	    		$('#clicknextid').hide();
	    		$($($('#clicknexttopid').find('img'))[0]).hide();
	    		$($($('#clicknextid').find('img'))[0]).hide();
		    }
		}
	};
	
	$scope.getPreviousJobs=function(event){
		var tableRows=$($('#deploymenthistorytableid').children()).children();
		if(null!=tableRows && tableRows!=undefined){
		    var firstDataRow=tableRows[1];
		    var firstDataRowNumber=parseInt($.trim($($(firstDataRow).children()[0]).html()));

		    if(firstDataRowNumber>$scope.MAX_DISPLAY){
		    	$scope.deployJobs=[];
		    	var lastIndex=(firstDataRowNumber-1);
		    	var firstIndex=(lastIndex-$scope.MAX_DISPLAY);
		    	if(firstIndex<0){
		    		firstIndex=0;
		    	}
		    	for(var i=firstIndex;i<lastIndex;++i){
		    		$scope.deployJobs.push($scope.deployJobsMaster[i]);
		    	}
		    	var firstRow=$scope.deployJobs[0];
		    	firstIndex=firstRow.count;
		    	if(firstIndex==1){
		    		$('#clickprevioustopid').hide();
		    		$('#clickpreviousid').hide();
		    		$($($('#clickprevioustopid').find('img'))[0]).hide();
		    		$($($('#clickpreviousid').find('img'))[0]).hide();
		    	}
		    	var lastRow=$scope.deployJobs[($scope.deployJobs.length-1)];
		    	lastIndex=lastRow.count;
		    	if(lastIndex<($scope.deployJobsMaster.length)){
		    		$('#clicknexttopid').show();
		    		$('#clicknextid').show();
		    		$($($('#clicknexttopid').find('img'))[0]).show();
		    		$($($('#clicknextid').find('img'))[0]).show();
		    	}
		    }
		}
	};
	
	$scope.filterRow=function(event){
		var selectedVals=$('#ldapnamesid').val();
		if(null!=selectedVals && selectedVals!=undefined && selectedVals.length>0){
		    $('.historyrow').hide();
    		for(var index=0;index<selectedVals.length;++index){
    			var aSelectedVal=selectedVals[index];
    			if(aSelectedVal=='All'){
    				$('.historyrow').show();
    				break;
    			}
    			else{
                    var periodsHandled=aSelectedVal.replace(/\./g,'\\.');
                    var selector='.'+periodsHandled;
                    $(selector).show();
    			}
    		} 
	    }
	};
	
}