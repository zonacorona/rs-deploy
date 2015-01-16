package com.rackspace.cloud.api.dao.impl;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.rackspace.cloud.api.dao.IGroupsDao;
import com.rackspace.cloud.api.entity.Groups;

@Repository
public class GroupsDaoImpl extends AbstractDaoImpl<Groups> implements
		IGroupsDao {
	
	public GroupsDaoImpl(){
		super(Groups.class);
	}
	
	@Transactional(readOnly=true)
	public List<Groups>findAll(){
		Session session=super.getCurrentSession();
		Query query=session.createQuery("from Groups g order by g.name");
		return query.list();
	}

}
