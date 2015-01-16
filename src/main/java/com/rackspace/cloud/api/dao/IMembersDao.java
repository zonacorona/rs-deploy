package com.rackspace.cloud.api.dao;

import java.util.List;

import com.rackspace.cloud.api.entity.Members;

public interface IMembersDao extends IAbstractDao<Members> {

	List<Members>findByLdapname(String ldapname);
}
