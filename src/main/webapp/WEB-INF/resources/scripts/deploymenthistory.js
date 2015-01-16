

$(document).ready(function() { 
	
    $('#filterbuttonid').click(function(){
        var selectedVal=$('#ldapnamesid').val();
        
        if(null==selectedVal || selectedVal==undefined){
        	alert('No filter value was selected, please select a filter value.');
        }
        else{
            $('#filtervalueid').val(selectedVal);
        	$.getJSON("/rax-staging-services/rest/services/getbyldapname?ldapname="+selectedVal,
    		{"ldapname" : selectedVal},
    		 function(data){
    		     var thetable=updateTable(data);
    		     $('#spanhistorytableid').html(thetable);
    	     }
        ).fail(function(){alert("Server Error: could not retrieve table rows for filter value: "+selectedVal);});
        }
    });
    	
});

function changeTextColor(id, color){
	$(('#'+id)).css('color',color);
}

function changeButtonColor(btnId, color){
	$(('#'+btnId)).css('background-color',color);
}

function getPreviousJobs(){
	var firstRowDom=$('#firstrowid').get();
	var innerValue=firstRowDom[0].children[0].innerHTML;
	--innerValue;
	var ldapname=$("#filtervalueid").val();

	$('#progressid').show();
    $('#progressid').ajaxStart(function(){
	    $('#progressid').show();
    })
    .ajaxStop(function(){
        $('#progressid').hide();
    });		

	$.getJSON("/rax-staging-services/rest/services/getpreviousjobs?ldapname="+ldapname+"&startindex="+innerValue,
	    {"ldapname" : ldapname,"startindex" : innerValue},
	     function(data){
	         var thetable=updateTable(data);
	         $('#spanhistorytableid').html(thetable);
	         $('#progressid').hide();
         }
         ).fail(function(){alert("Server Error: could not retrieve table rows.");});	
	//alert("The bean firstrowid innerHTML="+innerValue+" ldapname="+ldapname);
}

function getNextJobs(){
	//Use the first row of the table to retrieve the start table index
	var firstRowDom=$('#firstrowid').get();
	var innerValue=firstRowDom[0].children[0].innerHTML;
	--innerValue;
	var ldapname=$("#filtervalueid").val();
	
	$('#progressid').show();
    $('#progressid').ajaxStart(function(){
	    $('#progressid').show();
    })
    .ajaxStop(function(){
        $('#progressid').hide();
    });	
    
	$.getJSON("/rax-staging-services/rest/services/getnextjobs?ldapname="+ldapname+"&startindex="+innerValue,
		{"ldapname" : ldapname,"startindex" : innerValue},
		 function(data){
		     var thetable=updateTable(data);
		     $('#spanhistorytableid').html(thetable);
		     $('#progressid').hide();
	     }
    ).fail(function(){alert("Server Error: could not retrieve table rows.");});
	
	//alert("The bean firstrowid innerHTML="+innerValue+" ldapname="+ldapname);
}

function updateTable(data){
	
	var jobssize=data.jobssize;
	var newstartindex=data.newstartindex;
	
	if(null==newstartindex || newstartindex==undefined){
		newstartindex=0;
	}
	
    var totaljobs=data.totaljobssize;
	
	var retStr='<span id="spanhistorytableid">';	
    
    //This section builds the table
	retStr+='<table class="deployhistable" id="deploymenthistorytableid">\n';
	
	//This first row is for the header columns
	retStr+='<tr>\n';
	retStr+='<th class="tableheaderclass">&nbsp;</th>\n';

	retStr+='<th class="tableheaderclass">User</th>\n';
	retStr+='<th class="tableheaderclass">War Name</th>\n';
	retStr+='<th class="tableheaderclass">Doc Name</th>\n';
	retStr+='<th class="tableheaderclass">Type</th>\n';
	retStr+='<th class="tableheaderclass">Start Time (CST)</th>\n';
	retStr+='<th class="tableheaderclass">End Time (CST)</th>\n';
	retStr+='<th class="tableheaderclass">Status</th>\n';	
	retStr+='<th class="tableheaderclass failreasonthclass">Fail Reason';
	
	//next section is for the previous and next buttons at the top of the table
	retStr+='<span id="previousnextbuttontopid">';
	//we need to include the previous link
	if(newstartindex>0){
        retStr+="<span id=\"clickprevioustopjsid\" class=\"clickpreviousclass\" onmouseover=\"changeTextColor('clickprevioustopjsid','#888AAA');\"\n"; 
        	
        retStr+="onmouseout=\"changeTextColor('clickprevioustopjsid','#1144C1');\"\n";
        retStr+="onclick=\"getPreviousJobs();\">\n";
        retStr+="<img src='../images/previous.png'/>\n";
        retStr+="</span>\n";
        
        //there are more jobs to show, we need to include the Next link
        if((newstartindex+30)<totaljobs){
            retStr+="<span id=\"clicknexttopjsid\" class=\"clicknextclass\" onmouseover=\"changeTextColor('clicknexttopjsid','#888AAA');\"\n"; 
            retStr+="onmouseout=\"changeTextColor('clicknexttopjsid','#1144C1');\"\n";
            retStr+="onclick=\"getNextJobs();\">\n";
            retStr+="<img src='../images/next.png'/>\n";
            retStr+="</span>\n";        	
        }
	}
	else{
	    //there are more jobs to show, we need to include the Next Link
	    if((newstartindex+30)<totaljobs){
            retStr+="<span id=\"clicknexttopjsid\" class=\"clicknextclass\" onmouseover=\"changeTextColor('clicknexttopjsid','#888AAA');\"\n"; 
            retStr+="onmouseout=\"changeTextColor('clicknexttopjsid','#1144C1');\"\n";
            retStr+="onclick=\"getNextJobs();\">\n";
            retStr+="<img src='../images/next.png'/>\n";
            retStr+="</span>\n";   	
	    }
	}	
    retStr+='</span>\n';    
	
    retStr+='</th>\n';
	
	

	retStr+='</tr>\n';
	
	//retStr+='<th class="tableheaderclass">Fail Reason \n';
	//retStr+='<span id="clicknexttopid" class="clicknextclass" ';
	//retStr+='onmouseover="changeTextColor(\'clicknexttopid\',\'#888AAA\');" ';
	//retStr+='onmouseout="changeTextColor(\'clicknexttopid\',\'#1144C1\');" onclick="getNextJobs();"';
	//retStr+=' style="color: rgb(17, 68, 193);"><img src="../images/next.png"></span>\</th>\n';	

	//This builds all the data rows
    retStr+=getRows(data);
    
	retStr+='<tr id="lastrowid" class="deploymenthistortableevenrow">\n';
	retStr+='<td colspan="9">\n';

	//we need to include the previous link
	if(newstartindex>0){
        retStr+="<span id=\"clickpreviousid\" onmouseover=\"changeTextColor('clickpreviousid','#888AAA');\"\n"; 
        	
        retStr+="onmouseout=\"changeTextColor('clickpreviousid','#1144C1');\"\n";
        retStr+="onclick=\"getPreviousJobs();\">\n";
        retStr+="<!--  Next--><img src='../images/previous.png'/>\n";
        retStr+="</span>\n";
        
        //there are more jobs to show, we need to include the Next link
        if((newstartindex+30)<totaljobs){
            retStr+="<span id=\"clicknextid\" class=\"clicknextclass\" onmouseover=\"changeTextColor('clicknextid','#888AAA');\"\n"; 
            retStr+="onmouseout=\"changeTextColor('clicknextid','#1144C1');\"\n";
            retStr+="onclick=\"getNextJobs();\">\n";
            retStr+="<!--  Next--><img src='../images/next.png'/>\n";
            retStr+="</span>\n";        	
        }
	}
	else{
	    //there are more jobs to show, we need to include the Next Link
	    if((newstartindex+30)<totaljobs){
            retStr+="<span id=\"clicknextid\" class=\"clicknextclass\" onmouseover=\"changeTextColor('clicknextid','#888AAA');\"\n"; 
            retStr+="onmouseout=\"changeTextColor('clicknextid','#1144C1');\"\n";
            retStr+="onclick=\"getNextJobs();\">\n";
            retStr+="<!--  Next--><img src='../images/next.png'/>\n";
            retStr+="</span>\n"; 	
	    }
	}
	retStr+='</td>\n';
	retStr+='</tr>\n';
	retStr+='</table>\n';
	retStr+='</span>\n';
	return retStr;
}

function getRows(data){
	
	var newstartindex=data.newstartindex;
	if(null==newstartindex||newstartindex==undefined){
		newstartindex=0;
	}
	var tablerows=data.jobs;
	var rowcounter=(newstartindex+1);
	var counter=0;
	var retStr="";
    for(var i=0;i<tablerows.length;++i){
		var aRow=tablerows[i];
	    
	    var ldapname=aRow.ldapname;
	    var endtime=aRow.endtime;
	    if(null==endtime || endtime==undefined){
	    	endtime="";
	    }
	    var warname=aRow.warname;
	    var starttime=aRow.starttime;
	    if(null==starttime || starttime==undefined){
	    	starttime="";
	    }
	    var status=aRow.status;
	    var pomname=aRow.pomname;
	    var failreason=aRow.failreason;
	    if(null==failreason||failreason==undefined){
	    	failreason="";
	    }
	    var type=aRow.type;	
	    var isEven=false;

		//This is an even row
		if((counter%2)==0){
			isEven=true;
		    //This is the first row
		    if(i==0){
			    retStr+="<tr id=\"firstrowid\" class=\"deploymenthistortableevenrow\">\n";
		    }
		    else{
		    	retStr+="<tr class=\"deploymenthistortableevenrow\">\n";
		    }
		   
		}
		else{
			retStr+="<tr class=\"deploymenthistortableoddrow\">\n";
		}
		retStr+="<td class=\"historycolclass\">";
		retStr+=rowcounter;
		retStr+="</td>\n";
		
		retStr+="<td class=\"historycolclass\">";
		retStr+=ldapname;
		retStr+="</td>\n";
		
		retStr+="<td class=\"historycolclass\">";
		retStr+=warname;
		retStr+="</td>\n";
		
		retStr+="<td class=\"historycolclass\">";
		retStr+=pomname;
		retStr+="</td>\n";
		
		retStr+="<td class=\"historycolclass\">";
		retStr+=type;
		retStr+="</td>\n";
		
		retStr+="<td class=\"historycolclass\">";
		retStr+=starttime;
		retStr+="</td>\n";

		retStr+="<td class=\"historycolclass\">";
		retStr+=endtime;
		retStr+="</td>\n";		

		if(null!=status && status!=undefined){
			if(status=="done"||status=='failed'){
				retStr+="<td class=\"historycolclass\">\n";								
				retStr+=status;
			}
			else{
				retStr+="<td class=\"inprogressclass\">";
				retStr+="<span class=\"progressindicatorclass\">\n";
				retStr+=status;
				if(!isEven){
					retStr+=" <img class=\"progressimgclass\" src=\"../images/progress-indicator2blue.gif\">\n";
				}
				else{
				    retStr+=" <img class=\"progressimgclass\" src=\"../images/progress-indicator2.gif\">\n";				
				}
				retStr+="</span>\n";
			}
		}
		else{
		    retStr+="<td class=\"historycolclass\">";
		    status="&nbsp;";
		    retStr+=status;
		}
		
		
		retStr+="</td>\n";

		retStr+="<td class=\"historycolclass\">";
		retStr+=failreason;
		retStr+="</td>\n";
			
		retStr+="</tr>\n";
		
		++counter;
		++rowcounter;

    }
    
    return retStr;
}

