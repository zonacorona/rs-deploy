// default package
// Generated Nov 5, 2013 4:33:57 PM by Hibernate Tools 3.4.0.CR1
package com.rackspace.cloud.api.entity;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.springframework.transaction.annotation.Transactional;

/**
 * Deployjobs generated by hbm2java
 */
@Entity
@Table(name = "deployjobs", catalog = "docs")
public class Deployjobs implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3192490122622988664L;
	
	private DeployjobsId id;
	private Status status;
	private String type;
	private Long endtime;
	private String failreason;

	public Deployjobs() {
	}

	public Deployjobs(DeployjobsId id) {
		this.id = id;
	}

	public Deployjobs(DeployjobsId id, Status status, String type,
			Long endtime, String failreason) {
		this.id = id;
		this.status = status;
		this.type = type;
		this.endtime = endtime;
		this.failreason = failreason;
	}

	@EmbeddedId
	@AttributeOverrides({
			@AttributeOverride(name = "ldapname", column = @Column(name = "ldapname", nullable = false, length = 70)),
			@AttributeOverride(name = "groupid", column = @Column(name = "groupid", nullable = false, length = 100)),
			@AttributeOverride(name = "artifactid", column = @Column(name = "artifactid", nullable = false, length = 100)),
			@AttributeOverride(name = "warname", column = @Column(name = "warname", nullable = false, length = 100)),
			@AttributeOverride(name = "pomname", column = @Column(name = "pomname", nullable = false, length = 100)),
			@AttributeOverride(name = "starttime", column = @Column(name = "starttime", nullable = false)) })
	public DeployjobsId getId() {
		return this.id;
	}

	public void setId(DeployjobsId id) {
		this.id = id;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "status")
	@Transactional
	@JsonIgnore
	public Status getStatus() {
		return this.status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	@Column(name = "type", length = 7)
	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Column(name = "endtime")
	public Long getEndtime() {
		return this.endtime;
	}

	public void setEndtime(Long endtime) {
		this.endtime = endtime;
	}

	@Column(name = "failreason", length = 100)
	public String getFailreason() {
		return this.failreason;
	}

	@Transactional
	public void setFailreason(String failreason) {
		this.failreason = failreason;
	}
	
	public String toString(){
		StringBuffer str=new StringBuffer("");
		str.append("{");
		str.append("id:");
		str.append(this.id.toString());
		str.append(", status:");
		str.append(this.status);
		str.append(",type:");
		str.append(this.type);
		str.append(",endtime:");
		if(null!=endtime){
		    str.append(new SimpleDateFormat("M-dd-yyyy HH:mm:ss").format(new Date(endtime)));
		}
		else{
			str.append("null");
		}
		str.append("}");
		
		return str.toString();
	}

}