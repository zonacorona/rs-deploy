package com.rackspace.cloud.api.dao;

import java.util.List;

import com.rackspace.cloud.api.entity.Deployjob;

public interface IDeployjobDao extends IAbstractDao<Deployjob> {
	public List<Deployjob>findDeployJobByStartTime(Long starttime);
	//public void updateJobs(List<Deployjobs>jobs);
	public List<Deployjob>findAll();
	public List<Deployjob>findFirst500OrderByStartDate();
}
