package com.myproject.tournamentapp.forms;

import java.util.List;

import com.myproject.tournamentapp.model.Round;

public class PersonalInfo {
	private String firstname;
	private String lastname;
	private String username;
	private String email;
	private boolean isOut;
	private String stage;
	private List<Round> rounds;
	
	public PersonalInfo(String firstname, String lastname, String username, String email, boolean isOut, String stage,
			List<Round> rounds) {
		this.firstname = firstname;
		this.lastname = lastname;
		this.username = username;
		this.email = email;
		this.isOut = isOut;
		this.stage = stage;
		this.rounds = rounds;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public boolean isOut() {
		return isOut;
	}

	public void setOut(boolean isOut) {
		this.isOut = isOut;
	}

	public String getStage() {
		return stage;
	}

	public void setStage(String stage) {
		this.stage = stage;
	}

	public List<Round> getRounds() {
		return rounds;
	}

	public void setRounds(List<Round> rounds) {
		this.rounds = rounds;
	}
}
