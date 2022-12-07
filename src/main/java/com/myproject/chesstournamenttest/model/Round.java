package com.myproject.chesstournamenttest.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;


@Entity
public class Round {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "roundid", nullable = false, updatable = false)
	private Long roundid;
	
	@Column(name = "result")
	private String result;
	
	@ManyToOne(optional = true)
	@JoinColumn(name = "user1_id", referencedColumnName = "id")
	private User user1;
	
	@ManyToOne(optional = true)
	@JoinColumn(name = "user2_id", referencedColumnName = "id")
	private User user2;
	
	@ManyToOne
	@JoinColumn(name = "stageid", nullable = false)
	private Stage stage;
	
	public Round() {}

	public Round(String result, User user1, User user2, Stage stage) {
		super();
		this.result = result;
		this.user1 = user1;
		this.user2 = user2;
		this.stage = stage;
	}
	
	public Round(String result, User user1, Stage stage) {
		super();
		this.result = result;
		this.user1 = user1;
		this.user2 = null;
		this.stage = stage;
	}
	
	public Round(String result, Stage stage, User user2) {
		super();
		this.result = result;
		this.user1 = null;
		this.user2 = user2;
		this.stage = stage;
	}
	
	public Round(String result, Stage stage) {
		super();
		this.result = result;
		this.user1 = null;
		this.user2 = null;
		this.stage = stage;
	}

	public Long getRoundid() {
		return roundid;
	}

	public void setRoundid(Long roundid) {
		this.roundid = roundid;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public User getUser1() {
		return user1;
	}

	public void setUser1(User user1) {
		this.user1 = user1;
	}

	public User getUser2() {
		return user2;
	}

	public void setUser2(User user2) {
		this.user2 = user2;
	}

	public Stage getStage() {
		return stage;
	}

	public void setStage(Stage stage) {
		this.stage = stage;
	}
	
}
