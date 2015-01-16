package com.rackspace.cloud.api.dao.impl;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.rackspace.cloud.api.dao.IMembersDao;
import com.rackspace.cloud.api.entity.Members;

@Repository
public class MembersDaoImpl extends AbstractDaoImpl<Members> implements
		IMembersDao {
	
	public MembersDaoImpl(){
		super(Members.class);
	}
	
	@Transactional(readOnly=true)
	public List<Members>findByLdapname(String ldapname){
		Session session=super.getCurrentSession();
		Query query=session.createQuery("from Members M where M.id.ldapname = :ldapname order by M.id.ldapname, M.id.groupname");
		query.setParameter("ldapname", ldapname);		
		return query.list();
	}

}
