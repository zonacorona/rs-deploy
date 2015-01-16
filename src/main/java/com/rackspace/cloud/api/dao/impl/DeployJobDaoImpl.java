package com.rackspace.cloud.api.dao.impl;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.rackspace.cloud.api.dao.IDeployjobDao;
import com.rackspace.cloud.api.entity.Deployjob;


@Repository
public class DeployJobDaoImpl extends AbstractDaoImpl<Deployjob> implements IDeployjobDao {
	private static Logger log = Logger.getLogger(DeployJobDaoImpl.class);
	
	public DeployJobDaoImpl(){
		super(Deployjob.class);
	}

	@Override
	@Transactional(readOnly=true)
	public List<Deployjob>findDeployJobByStartTime(Long starttime) {
		/*
		HibernateUtil hibUtils=new HibernateUtil();
		SessionFactory sessFact=hibUtils.getSessionFactory();
		Session session=sessFact.openSession();
		*/
		Session session=super.getCurrentSession();
		Query query=session.createQuery("from Deployjob d where d.id.starttime = :starttime");
		query.setParameter("starttime", starttime);
		List<Deployjob>retVal= query.list();
		/*
		session.close();
		sessFact.close();
		*/
		return retVal;
	}

	@Override
	@Transactional(readOnly=true)
	public List<Deployjob> findAll() {
		Session session=super.getCurrentSession();
		Query query=session.createQuery("from Deployjob d order by d.id.starttime");
		return query.list();
	}

	@Override
	@Transactional(readOnly=true)
	public List<Deployjob> findFirst500OrderByStartDate() {
		Session session=super.getCurrentSession();
		Query query=session.createQuery("from Deployjob d order by d.id.starttime desc");
		query.setMaxResults(500);
		return query.list();
	}



}
