package com.rackspace.cloud.api.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.rackspace.cloud.api.dao.IDeployjobDao;
import com.rackspace.cloud.api.dao.IFreezeDao;
import com.rackspace.cloud.api.dao.IGroupsDao;
import com.rackspace.cloud.api.dao.IMembersDao;
import com.rackspace.cloud.api.dao.IUsersDao;
import com.rackspace.cloud.api.entity.Deployjob;
import com.rackspace.cloud.api.entity.Users;
import com.rackspace.cloud.api.view.model.HistoryModel;

@Controller
@RequestMapping(value="/")
@Configuration
public class DeployHistoryController implements InitializingBean{

	public static int MAX_DISPLAY_JOBS=30;
	private int currentStartIndex;
	private boolean displayNextLink;
	private boolean displayPreviousLink;

	private static Logger log = Logger.getLogger(DeployHistoryController.class);
	@Autowired
	private ServletContext servletCtx;

	@Autowired
	private IUsersDao userDao;

	@Autowired
	private IFreezeDao freezeDao;

	@Autowired
	private IDeployjobDao deployjobDao;

	@Autowired
	private IGroupsDao groupsDao;

	@Autowired
	private IMembersDao membersDao;

	private List<HistoryModel>deployJobs;

//	public DeployHistoryController(){
//		String METHOD_NAME="DeployHistoryController()";
//		if(log.isDebugEnabled()){
//			log.debug(METHOD_NAME+": START");
//		}
//		this.deployJobs=this.deployjobDao.findFirst500OrderByStartDate();
//
//		if(log.isDebugEnabled()){
//			log.debug(METHOD_NAME+": END this.deployJobs.size()="+this.deployJobs.size());
//		}
//	}

	@RequestMapping(value="/DeployHistory", method=RequestMethod.GET)
	public String addDeployHistoryModelObjects(Model model, HttpSession session, HttpServletRequest request){

		String METHOD_NAME="addDeployHistoryModelObjects()";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START");
		}
		//Set the currentStartIndex to 0 since this is when the page get loaded
		this.currentStartIndex=0;

		this.deployJobs=new ArrayList<HistoryModel>();
		
		List<Users>users=this.userDao.findAll();
		//Get the Deployjobs first and then call methods to set displayNextLink and displayPreviousLink
		List<Deployjob>jobs=this.deployjobDao.findFirst500OrderByStartDate();
		addRows(jobs);	
		model.addAttribute("historyTable",jobs);
		
		model.addAttribute("usersList",users);
		model.addAttribute("shouldDisplayPreviousLink",this.shouldDisplayPreviousLink());
		model.addAttribute("shouldDisplayNextLink",this.shouldDisplayNextLink());
		model.addAttribute("currentStartIndex",this.currentStartIndex);
		model.addAttribute("deployJobs",this.deployJobs);
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END: deployJobs.size()="+deployJobs.size()+" users.size()="+users.size()+
					" this.shouldDisplayNextLink()="+this.shouldDisplayNextLink()+" this.shouldDisplayPreviousLink()="+this.shouldDisplayPreviousLink());
		}
		return "DeployHistory";
	}

	@RequestMapping(value="/getInitialJobs", method=RequestMethod.GET)
	@ResponseBody
	public List<HistoryModel> getInitialJobs(){
		List<Deployjob>jobs=this.deployjobDao.findFirst500OrderByStartDate();
		this.deployJobs=new ArrayList<HistoryModel>();
		addRows(jobs);
		return this.deployJobs;
	}
	
	private void addRows(List<Deployjob>jobs){
		String METHOD_NAME="addRows()";
		
		for(int i=0; i<jobs.size();++i){
			HistoryModel aRow=new HistoryModel();			
			Deployjob aJob=jobs.get(i);
			String status=aJob.getStatus().getValue();
			aRow.setStatus(status);
			int count=(i+1);
			aRow.setCount(count);
			//This is an even number
			if((count%2)==0){
				aRow.isEven(true);
				if(null!=status&&(!status.equalsIgnoreCase("done")&&!status.equalsIgnoreCase("failed"))){
				    aRow.setProgressImg("resources/images/progress-indicator2blue.gif");
				}
				else{
					aRow.setProgressImg("");
				}
			}
			else{
				aRow.isEven(false);
				if(null!=status&&(!status.equalsIgnoreCase("done")&&!status.equalsIgnoreCase("failed"))){
				    aRow.setProgressImg("resources/images/progress-indicator2.gif");
				}
				else{
					aRow.setProgressImg("");
				}
			}	
			aRow.setUser(aJob.getLdapname());
			aRow.setWarName(aJob.getWarname());
			aRow.setDocName(aJob.getPomname());
			aRow.setType(aJob.getType());
			
			SimpleDateFormat dFormat=new SimpleDateFormat("EE MMM dd yyyy: HH:mm:ss");
			Long startTime=aJob.getStarttime();
			String startTimeStr="";
			if(null!=startTime){
				startTimeStr=dFormat.format(new Date(aJob.getStarttime()));
			}
			else{
				startTimeStr=" ";
			}
			aRow.setStartTime(startTimeStr);
			Long endTime=aJob.getEndtime();
			String endTimeStr="";
			if(null!=endTime){
				endTimeStr=dFormat.format(new Date(aJob.getEndtime()));
			}
			else{
				endTimeStr=" ";
			}
			aRow.setEndTime(endTimeStr);
			
			aRow.setFailReason(aJob.getFailreason());
			
			this.deployJobs.add(aRow);
		}		
	}

	@RequestMapping(value="/shouldDisplayPreviousLink", method=RequestMethod.GET)
	@ResponseBody
	public boolean shouldDisplayPreviousLink() {
		this.displayPreviousLink=false;
		if(null!=this.deployJobs){
			if((this.currentStartIndex-MAX_DISPLAY_JOBS)>=0){
				this.displayPreviousLink=true;
			}
		}
		return this.displayPreviousLink;
	}

	@RequestMapping(value="/shouldDisplayNextLink",method=RequestMethod.GET)
	@ResponseBody
	public boolean shouldDisplayNextLink(){
		this.displayNextLink=false;
		if(null!=this.deployJobs){
			if(this.deployJobs.size()>(this.currentStartIndex+MAX_DISPLAY_JOBS)){
				this.displayNextLink=true;
			}
		}
		return this.displayNextLink;
	}

//	@RequestMapping(value="/getNextJobs",method=RequestMethod.GET)
//	@ResponseBody
//	public List<Deployjob>getNextJobs(){
//		String METHOD_NAME="getNextJobs()";
//		if(log.isDebugEnabled()){
//			log.debug(METHOD_NAME+": START this.currentStartIndex="+this.currentStartIndex+" this.jobs="+this.deployJobs);
//		}
//		this.deployJobs=this.deployjobDao.findFirst500OrderByStartDate();
//		if(log.isDebugEnabled()){
//			log.debug(METHOD_NAME+": this.deployJobs.size()="+this.deployJobs.size());
//		}
//		List<Deployjob>retVal=null;
//		if(null!=this.deployJobs && ((this.currentStartIndex+MAX_DISPLAY_JOBS))<this.deployJobs.size()){
//			this.currentStartIndex+=MAX_DISPLAY_JOBS;
//			retVal=this.deployJobs;
//		}		
//		else{
//			retVal=new ArrayList<Deployjob>();
//		}
//		if(log.isDebugEnabled()){
//			log.debug(METHOD_NAME+": END: retVal.size()="+retVal.size() +" this.currentStartIndex="+this.currentStartIndex);
//		}
//		return retVal;
//	}
//		
//
//	@RequestMapping(value="/getPreviousJobs",method=RequestMethod.GET)
//	@ResponseBody
//	public List<Deployjob>getPreviousJobs(){
//		String METHOD_NAME="getPreviousJobs()";
//		if(log.isDebugEnabled()){
//			log.debug(METHOD_NAME+": START this.currentStartIndex="+this.currentStartIndex+" this.jobs="+this.deployJobs);
//		}	
//		List<Deployjob>retVal=null;
//		if(null!=this.deployJobs && (this.currentStartIndex>MAX_DISPLAY_JOBS)){
//			this.currentStartIndex-=MAX_DISPLAY_JOBS;
//			retVal=this.deployJobs;
//		}		
//		else{
//			retVal=new ArrayList<Deployjob>();
//		}		
//		if(log.isDebugEnabled()){
//			log.debug(METHOD_NAME+": END: retVal.size()="+retVal.size() +" this.currentStartIndex="+this.currentStartIndex);
//		}
//		return retVal;		
//	}

	@Override
	//This gets called upon Bean construction
	public void afterPropertiesSet() throws Exception {
		String METHOD_NAME="afterPropertiesSet()";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START: ");
		}		

		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END: ");
		}
	}


}
