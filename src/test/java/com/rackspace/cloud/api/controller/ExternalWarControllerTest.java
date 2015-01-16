package com.rackspace.cloud.api.controller;


import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;

import com.rackspace.cloud.api.dao.IDeployjobDao;
import com.rackspace.cloud.api.dao.IFreezeDao;
import com.rackspace.cloud.api.dao.IGroupsDao;
import com.rackspace.cloud.api.dao.IMembersDao;
import com.rackspace.cloud.api.dao.IUsersDao;
import com.rackspace.cloud.api.dao.impl.DeployJobDaoImpl;
import com.rackspace.cloud.api.dao.impl.FreezeDaoImpl;
import com.rackspace.cloud.api.dao.impl.GroupsDaoImpl;
import com.rackspace.cloud.api.dao.impl.MembersDaoImpl;
import com.rackspace.cloud.api.dao.impl.UsersDaoImpl;
import com.rackspace.cloud.api.entity.Deployjob;
import com.rackspace.cloud.api.entity.Groups;
import com.rackspace.cloud.api.entity.Status;
import com.rackspace.cloud.api.entity.Users;

//TODO Need to do more research on how to test more complicated methods in the Controller
//may wantt o check out Spy for mockito
//http://www.petrikainulainen.net/programming/spring-framework/integration-testing-of-spring-mvc-applications-controllers/
public class ExternalWarControllerTest {

	@Mock
	private IUsersDao user;	
	
	@Mock
	private IFreezeDao freezeDao;
	
	
	@Mock
	private IGroupsDao groupsDao;
	
	@Mock
	private IMembersDao membersDao;
	
	
	@Mock
	private IDeployjobDao deployjobDao;

	@InjectMocks
	private ExternalWarController controller;

	private MockMvc mockMvc;
	
	@Before
	public void setup(){
		
		Mockito.mock(ExternalWarController.class);
		Mockito.mock(DeployJobDaoImpl.class);
		Mockito.mock(UsersDaoImpl.class);
		Mockito.mock(FreezeDaoImpl.class);
		Mockito.mock(GroupsDaoImpl.class);
		Mockito.mock(MembersDaoImpl.class);
		MockitoAnnotations.initMocks(this);

		//This did not work as expected, we get a ClassNotFound exception, need to do additional research on
		//java.lang.NoClassDefFoundError: javax/servlet/SessionCookieConfig
		//how to mock test the Controller
		//this.mockMvc=MockMvcBuilders.standaloneSetup(controller).build();
		//this.mockMvc=MockMvcBuilders.webAppContextSetup(context)
	}
	
	@Test
	public void testController(){

	    List<Deployjob>jobs=new ArrayList<Deployjob>();
	    Deployjob deployJob1=new Deployjob();
	    deployJob1.setArtifactid("artifact1");
	    long currTime=System.currentTimeMillis();
	    deployJob1.setEndtime(currTime);
	    deployJob1.setFailreason("failed1");
	    deployJob1.setGroupid("group1");
	    deployJob1.setId(1L);
	    deployJob1.setLdapname("ldapname1");
	    deployJob1.setPomname("pomname1");
	    deployJob1.setStarttime(currTime);
	    Status status=new Status();
	    status.setValue("done");
	    deployJob1.setStatus(status);
	    deployJob1.setType("type");
	    deployJob1.setWarname("warname");
	    
	    jobs.add(deployJob1);
	    
	    Mockito.when(this.deployjobDao.findAll()).thenReturn(jobs);
	    List<Deployjob>expectedDeployjobs=controller.getAllDeployJobs();
	    
	    Assert.assertNotNull(expectedDeployjobs);
	    Assert.assertSame(expectedDeployjobs, jobs);
	    
	    List<Groups>expectedGroups=new ArrayList<Groups>();
	    
	    Groups aGroup=new Groups();
	    aGroup.setName("groupname");
	    aGroup.setUserses(new LinkedHashSet<Users>());;
	    
	    expectedGroups.add(aGroup);
	    
	    Mockito.when(this.groupsDao.findAll()).thenReturn(expectedGroups);
	    
	    controller.getAllGroups();
	    
	}
}
