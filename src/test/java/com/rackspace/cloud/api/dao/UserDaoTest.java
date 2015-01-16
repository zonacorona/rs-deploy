package com.rackspace.cloud.api.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.rackspace.cloud.api.dao.impl.UsersDaoImpl;
import com.rackspace.cloud.api.entity.Users;


public class UserDaoTest {
	
	private SessionFactory sessionFactory;
	private Session session;
	private Query query;
	
	@Before
	public void setupData(){
		sessionFactory=Mockito.mock(SessionFactory.class);
		session=Mockito.mock(Session.class);
		query=Mockito.mock(Query.class);
	}
	
	@Test
	public void testUserDao(){
		//"from Users u order by u.fname, u.lname, u.ldapname";
		String hql="from Users u order by u.fname, u.lname, u.ldapname";
		List<Users>expectedUsers=new ArrayList<Users>();
		Users user1=new Users();
		user1.setFname("thu");
		user1.setLname("doan");
		user1.setEmail("thu.doan@rackspace.com");
		user1.setStatus("active");
		user1.setLdapname("thu.doan");

		Users user2=new Users();
		user2.setFname("thu2");
		user2.setLname("doan2");
		user2.setEmail("thu2.doan2@rackspace.com");
		user2.setStatus("active");
		user2.setLdapname("thu2.doan2");
		
		expectedUsers.add(user1);
		expectedUsers.add(user2);
		
		UsersDaoImpl usersDaoImpl=new UsersDaoImpl();
		usersDaoImpl.setSessionFactory(sessionFactory);
		Mockito.when(sessionFactory.getCurrentSession()).thenReturn(session);
		
		Mockito.when(this.session.createQuery(hql)).thenReturn(query);
		Mockito.when(this.query.list()).thenReturn(expectedUsers);
		
		List<Users>actualUsersList=usersDaoImpl.findAll();
		
		Assert.assertNotNull(actualUsersList);
		Assert.assertSame(expectedUsers, actualUsersList);
		
		String hql2="from Users u where u.status='active' order by u.fname, u.lname, u.ldapname";
		
		Mockito.when(this.session.createQuery(hql2)).thenReturn(query);
		Mockito.when(this.query.list()).thenReturn(expectedUsers);
		
		actualUsersList=usersDaoImpl.findAllActive();
		
		Assert.assertNotNull(actualUsersList);
		Assert.assertSame(expectedUsers, actualUsersList);
		
		String hql3="from Users u where u.status='inactive' order by u.fname, u.lname, u.ldapname";
		
		Mockito.when(this.session.createQuery(hql3)).thenReturn(query);
		Mockito.when(this.query.list()).thenReturn(new ArrayList<Users>());
		
		actualUsersList=usersDaoImpl.findAllInactive();
		
		Assert.assertNotNull(actualUsersList);
		Assert.assertEquals(0, actualUsersList.size());
		
	}

}
