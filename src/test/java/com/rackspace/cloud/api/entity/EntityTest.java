package com.rackspace.cloud.api.entity;

import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.rackspace.cloud.api.dao.IFreezeDao;
import com.rackspace.cloud.api.dao.IUsersDao;
import com.rackspace.cloud.api.dao.impl.FreezeDaoImpl;
import com.rackspace.cloud.api.dao.impl.UsersDaoImpl;

public class EntityTest {
	@Mock
	private IUsersDao user;
	@Mock
	private IFreezeDao freezeDao;

	
	@Before
	public void setup(){	
		user=Mockito.mock(UsersDaoImpl.class);
		freezeDao=Mockito.mock(FreezeDaoImpl.class);
	}
	
	@Test
	public void testEntities(){
		
	    List<Users> all=new LinkedList<Users>();
	    
	    Users user1=new Users();
	    user1.setEmail("thu@blah.com");
	    user1.setFname("thu");
	    user1.setLname("doan");
	    user1.setLdapname("blah.blah");
	    all.add(user1);
		
	    Mockito.when(user.findAll()).thenReturn(all);
	    
	    List<Users>result=user.findAll();
	    Users resultUser=result.get(0);
	    
	    Assert.assertEquals("thu",resultUser.getFname());
	    Assert.assertEquals("doan", resultUser.getLname());
	    
	    Assert.assertEquals(1, result.size());
	    
	    Freeze freeze=new Freeze();
	    freeze.setId(((short)1));
	    freeze.setShouldfreeze(true);
	    
	    Mockito.when(freezeDao.findById(1)).thenReturn(freeze);

	    Freeze mockFreeze=freezeDao.findById(1);
	    
	    Assert.assertEquals(((short)1), mockFreeze.getId());
	    Assert.assertEquals(true, mockFreeze.getShouldfreeze());
	    

	}
}
