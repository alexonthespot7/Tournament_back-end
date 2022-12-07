package com.myproject.chesstournamenttest.model;

import java.sql.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class Stage {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "stageid", nullable = false, updatable = false)
	private Long stageid;
	
	@Column(name = "stage", nullable = false, unique = true)
	private String stage;
	
	@Column(name = "startdate", nullable = false)
	private Date startdate;
	
	@Column(name = "enddate", nullable = false)
	private Date enddate;
	
	@Column(name="isCurrent", nullable = false)
	private boolean isCurrent;
	
	@JsonIgnore
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "stage")
	private List<User> users;
	
	@JsonIgnore
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "stage")
	private List<Round> rounds;
	
	public Stage() {}

	public Stage(String stage, Date startdate, Date enddate, boolean isCurrent) {
		super();
		this.stage = stage;
		this.startdate = startdate;
		this.enddate = enddate;
		this.isCurrent = isCurrent;
	}
	
	public Stage(String stage, Date startdate, Date enddate) {
		super();
		this.stage = stage;
		this.startdate = startdate;
		this.enddate = enddate;
		this.isCurrent = false;
	}

	public Long getStageid() {
		return stageid;
	}

	public void setStageid(Long stageid) {
		this.stageid = stageid;
	}

	public String getStage() {
		return stage;
	}

	public void setStage(String stage) {
		this.stage = stage;
	}

	public Date getStartdate() {
		return startdate;
	}

	public void setStartdate(Date startdate) {
		this.startdate = startdate;
	}

	public Date getEnddate() {
		return enddate;
	}

	public void setEnddate(Date enddate) {
		this.enddate = enddate;
	}
	
	public boolean getIsCurrent() {
		return isCurrent;
	}

	public void setIsCurrent(boolean isCurrent) {
		this.isCurrent = isCurrent;
	}

	public List<Round> getRounds() {
		return rounds;
	}

	public void setRounds(List<Round> rounds) {
		this.rounds = rounds;
	}

	public List<User> getUsers() {
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}
	
	
}
