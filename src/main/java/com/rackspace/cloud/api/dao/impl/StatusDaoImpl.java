package com.rackspace.cloud.api.dao.impl;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.rackspace.cloud.api.dao.IStatusDao;
import com.rackspace.cloud.api.entity.Status;

@Repository
public class StatusDaoImpl extends AbstractDaoImpl<Status> implements
		IStatusDao {
	
	public StatusDaoImpl(){
		super(Status.class);
	}

}
