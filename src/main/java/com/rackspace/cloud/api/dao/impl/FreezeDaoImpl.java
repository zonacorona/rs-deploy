package com.rackspace.cloud.api.dao.impl;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.rackspace.cloud.api.dao.IFreezeDao;
import com.rackspace.cloud.api.entity.Freeze;


@Repository
public class FreezeDaoImpl extends AbstractDaoImpl<Freeze> implements IFreezeDao {

	public FreezeDaoImpl(){
		super(Freeze.class);
	}

}
