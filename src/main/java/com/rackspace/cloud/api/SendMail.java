package com.rackspace.cloud.api;

import java.util.List;
import java.util.Map;

import com.rackspace.cloud.api.entity.Users;

public class SendMail implements Runnable {
	
	private List<Email>emails;
	private Map<String,List<String>>messages;
	private String action;
	private Users loggedInuser;
	
	public SendMail(List<Email>emails, Map<String,List<String>>messages, String action, Users loggedInuser){
		this.emails=emails;
		this.messages=messages;
		this.action=action;
		this.loggedInuser=loggedInuser;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		if(null!=this.emails){
			DeployUtility.sendEmails(this.emails, this.messages, this.action);
		}
	}

}
