package com.rackspace.cloud.api.dao;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.HSQLDialect;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
import org.junit.After;
import org.springframework.orm.hibernate3.HibernateTemplate;

public abstract class DaoTestTemplate {
	//
	protected HibernateTemplate hibernateTemplate;
	protected SessionFactory sessionFactory;
	protected Configuration annotatedConfiguration;
	protected String hsqlURL = "jdbc:hsqldb:mem:DAOUnitTest";
	//
	protected String hbm2ddl = "create-drop";
	//
	public void setup() {
		annotatedConfiguration = new Configuration();
		annotatedConfiguration.setProperty(Environment.DRIVER, "org.hsqldb.jdbcDriver");
		annotatedConfiguration.setProperty(Environment.URL, hsqlURL);
		annotatedConfiguration.setProperty(Environment.USER, "sa");
		annotatedConfiguration.setProperty(Environment.DIALECT, HSQLDialect.class.getName());
		annotatedConfiguration.setProperty(Environment.SHOW_SQL, "true");
		//
		if (hbm2ddl != null) annotatedConfiguration.setProperty(Environment.HBM2DDL_AUTO, hbm2ddl);
		//
		//Add the annotated Classes.
		Class[] classes = getAnnotatedHBOClasses();
		if (classes != null)
			for (Class clazz : classes) {
				annotatedConfiguration.addAnnotatedClass(clazz);
			}
		//
		ServiceRegistry serviceRegistry=new ServiceRegistryBuilder().applySettings(annotatedConfiguration.getProperties()).buildServiceRegistry();
		sessionFactory = annotatedConfiguration.buildSessionFactory(serviceRegistry);
		hibernateTemplate = new HibernateTemplate(sessionFactory);
	}
	//
	@After
	public void tearDown() {
		hibernateTemplate.clear();
		sessionFactory.close();
	}
	//
	/**
	 * Class configured using annotations.
	 * @return
	 */
	protected abstract Class[] getAnnotatedHBOClasses();

}
