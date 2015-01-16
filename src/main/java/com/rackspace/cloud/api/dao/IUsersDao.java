package com.rackspace.cloud.api.dao;

import java.util.List;

import com.rackspace.cloud.api.entity.Users;

public interface IUsersDao extends IAbstractDao<Users> {
	List<Users>findAll();
	List<Users>findAllActive();
	List<Users>findAllInactive();
}
