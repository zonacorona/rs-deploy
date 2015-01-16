// default package
// Generated Nov 5, 2013 4:33:57 PM by Hibernate Tools 3.4.0.CR1
package com.rackspace.cloud.api.entity;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.springframework.transaction.annotation.Transactional;

/**
 * Groups generated by hbm2java
 */
@Entity
@Table(name = "groups", catalog = "docs")
public class Groups implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2739820789460794630L;
	
	private String name;
	private Set<Users> userses = new HashSet<Users>(0);

	public Groups() {
	}

	public Groups(String name) {
		this.name = name;
	}

	public Groups(String name, Set<Users> userses) {
		this.name = name;
		this.userses = userses;
	}

	@Id
	@Column(name = "name", unique = true, nullable = false, length = 90)
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@ManyToMany(fetch = FetchType.EAGER, mappedBy = "groupses")
	@Transactional
	@JsonIgnore
	public Set<Users> getUserses() {
		return this.userses;
	}

	public void setUserses(Set<Users> userses) {
		this.userses = userses;
	}
	
	public String toString(){
		StringBuffer retVal=new StringBuffer("");
		retVal.append("{groupName=");
		retVal.append(this.name);
		retVal.append("}");
		return retVal.toString();
	}

}
