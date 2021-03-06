// default package
// Generated Nov 5, 2013 4:33:57 PM by Hibernate Tools 3.4.0.CR1
package com.rackspace.cloud.api.entity;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.springframework.transaction.annotation.Transactional;


/**
 * Users generated by hbm2java
 */
@Entity
@Table(name = "users", catalog = "docs")
public class Users implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8590278061391317996L;
	
	private String ldapname;
	private String fname;
	private String lname;
	private String email;
	private String status;
	private String password;
	private Set<Groups> groupses = new HashSet<Groups>(0);

	public Users() {
	}

	public Users(String ldapname) {
		this.ldapname = ldapname;
	}

	public Users(String ldapname, String fname, String lname, String email, String password,
			String status, Set<Groups> groupses) {
		this.password=password;
		this.ldapname = ldapname;
		this.fname = fname;
		this.lname = lname;
		this.email = email;
		this.status = status;
		this.groupses = groupses;
	}

	@Id
	@Column(name = "ldapname", unique = true, nullable = false, length = 70)
	public String getLdapname() {
		return this.ldapname;
	}

	public void setLdapname(String ldapname) {
		this.ldapname = ldapname;
	}
	
	@Column(name = "password", length = 40)
	public String getPassword(){
		return this.password;
	}
	
	public void setPassword(String password){
		this.password=password;
	}

	@Column(name = "fname", length = 40)
	public String getFname() {
		return this.fname;
	}

	public void setFname(String fname) {
		this.fname = fname;
	}

	@Column(name = "lname", length = 40)
	public String getLname() {
		return this.lname;
	}

	public void setLname(String lname) {
		this.lname = lname;
	}

	@Column(name = "email", length = 70)
	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Column(name = "status", length = 15)
	public String getStatus() {
		return this.status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "members", catalog = "docs", joinColumns = { @JoinColumn(name = "ldapname", nullable = false, updatable = false) }, inverseJoinColumns = { @JoinColumn(name = "groupname", nullable = false, updatable = false) })
	@Transactional
	@JsonIgnore
	public Set<Groups> getGroupses() {
		return this.groupses;
	}

	public void setGroupses(Set<Groups> groupses) {
		this.groupses = groupses;
	}
	
	public String toString(){
		StringBuffer retVal=new StringBuffer("");
		retVal.append("{");
		retVal.append("ldapname:"+this.ldapname);
		retVal.append(",fname:"+this.fname);
		retVal.append(",lname:"+this.lname);
		retVal.append(",email:"+this.email);
		retVal.append(",status:"+this.status);
		retVal.append(",groupses:"+this.groupses);
		retVal.append("}");
		return retVal.toString();
	}

}
