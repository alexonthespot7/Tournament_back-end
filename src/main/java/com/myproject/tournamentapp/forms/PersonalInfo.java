package com.myproject.tournamentapp.forms;

import java.util.List;

public class PersonalInfo {
	private String username;
	private String email;
	private boolean isOut;
	private String stage;
	private boolean isCompetitor;
	private int roundsQuantity; //if the total rounds quantity is 0, then the bracket wasn't made yet
	private List<RoundPublicInfo> publicRounds;
	
	public PersonalInfo() {}
	
	public PersonalInfo(String username, String email, boolean isOut, String stage,
			boolean isCompetitor, int roundsQuantity, List<RoundPublicInfo> publicRounds) {
		this.username = username;
		this.email = email;
		this.isOut = isOut;
		this.stage = stage;
		this.isCompetitor = isCompetitor;
		this.roundsQuantity = roundsQuantity;
		this.publicRounds = publicRounds;
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
	
	public boolean isCompetitor() {
		return isCompetitor;
	}

	public void setCompetitor(boolean isCompetitor) {
		this.isCompetitor = isCompetitor;
	}

	public int getRoundsQuantity() {
		return roundsQuantity;
	}

	public void setRoundsQuantity(int roundsQuantity) {
		this.roundsQuantity = roundsQuantity;
	}

	public List<RoundPublicInfo> getPublicRounds() {
		return publicRounds;
	}

	public void setPublicRounds(List<RoundPublicInfo> publicRounds) {
		this.publicRounds = publicRounds;
	}
}
