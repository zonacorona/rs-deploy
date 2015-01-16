package com.rackspace.cloud.api;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

public class HibernateUtil {
	//This cannot be static, because we need one for each servlet request
	private SessionFactory sessionFactory;
	
	private static SessionFactory buildSessionFactory(){		
		Configuration configuration=new Configuration();
		configuration.configure();
		ServiceRegistry serviceRegistry=new ServiceRegistryBuilder().applySettings(configuration.getProperties()).
				                            buildServiceRegistry();		
		
		return configuration.buildSessionFactory(serviceRegistry);
	}
		
	public SessionFactory getSessionFactory(){
		this.sessionFactory=buildSessionFactory();
		return this.sessionFactory;
	}
}