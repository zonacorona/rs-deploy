package com.rackspace.cloud.api;

import java.util.Comparator;

import com.rackspace.cloud.api.entity.Members;

public class MembersSort implements Comparator<Members> {

	public int compare(Members o1, Members o2) {
		final int BEFORE=-1;
		final int AFTER=1;
		if(null==o2){
			return (BEFORE*-1);
		}
		
		String thisMember=o1.getId().getGroupname();
		String thatMember=o2.getId().getGroupname();
		
		if(null==thisMember){
			return (AFTER*1);
		}
		else if(null==thatMember){
			return (BEFORE*-1);
		}
		else{
			return (thisMember.compareTo(thatMember));
		}
	}

}
