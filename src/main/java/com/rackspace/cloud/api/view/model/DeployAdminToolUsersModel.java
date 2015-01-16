package com.rackspace.cloud.api.view.model;

import java.io.Serializable;
import java.util.Set;

import com.rackspace.cloud.api.entity.Groups;
import com.rackspace.cloud.api.entity.Users;

public class DeployAdminToolUsersModel implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -502644281284812442L;
	
	private int count;
	private Users user;
	private Set<Groups>groupsList;
	
	public Set<Groups> getGroupsList() {
		return groupsList;
	}
	public void setGroupsList(Set<Groups> groupsList) {
		this.groupsList = groupsList;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public Users getUser() {
		return user;
	}
	public void setUser(Users user) {
		this.user = user;
	}

}
