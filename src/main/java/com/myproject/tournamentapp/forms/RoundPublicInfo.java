package com.myproject.tournamentapp.forms;

public class RoundPublicInfo {
	private String usernameOfCompetitor1;
	private String usernameOfCompetitor2;
	private String stage;
	private String result;
	
	public RoundPublicInfo() {}
	
	public RoundPublicInfo(String usernameOfCompetitor1, String usernameOfCompetitor2, String stage, String result) {
		this.usernameOfCompetitor1 = usernameOfCompetitor1;
		this.usernameOfCompetitor2 = usernameOfCompetitor2;
		this.stage = stage;
		this.result = result;
	}

	public String getUsernameOfCompetitor1() {
		return usernameOfCompetitor1;
	}

	public void setUsernameOfCompetitor1(String usernameOfCompetitor1) {
		this.usernameOfCompetitor1 = usernameOfCompetitor1;
	}

	public String getUsernameOfCompetitor2() {
		return usernameOfCompetitor2;
	}

	public void setUsernameOfCompetitor2(String usernameOfCompetitor2) {
		this.usernameOfCompetitor2 = usernameOfCompetitor2;
	}

	public String getStage() {
		return stage;
	}

	public void setStage(String stage) {
		this.stage = stage;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}
	
}
