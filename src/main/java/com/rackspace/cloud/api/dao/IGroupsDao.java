package com.rackspace.cloud.api.dao;

import java.util.List;

import com.rackspace.cloud.api.entity.Groups;

public interface IGroupsDao extends IAbstractDao<Groups> {
	
	public List<Groups>findAll();

}
