package com.rackspace.cloud.api.dao.impl;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.rackspace.cloud.api.dao.IUsersDao;
import com.rackspace.cloud.api.entity.Users;

@Repository
public class UsersDaoImpl extends AbstractDaoImpl<Users> implements IUsersDao {

	public UsersDaoImpl(){
		super(Users.class);
	}

	@Transactional(readOnly=true)
	public List<Users>findAll(){
		Session session=super.getCurrentSession();
		Query query=session.createQuery("from Users u order by u.fname, u.lname, u.ldapname");
		return query.list();
	}
	
	@Transactional(readOnly=true)
	public List<Users>findAllActive(){
		Session session=super.getCurrentSession();
		Query query=session.createQuery("from Users u where u.status='active' order by u.fname, u.lname, u.ldapname");
		return query.list();
	}
	
	@Transactional(readOnly=true)
	public List<Users>findAllInactive(){
		Session session=super.getCurrentSession();
		Query query=session.createQuery("from Users u where u.status='inactive' order by u.fname, u.lname, u.ldapname");
		return query.list();
	}

}
