package com.myproject.tournamentapp.forms;

public class CompetitorPublicInfo {
	private String username;
	private boolean isOut;
	private String stage;
	
	public CompetitorPublicInfo(String username, boolean isOut, String stage) {
		this.username = username;
		this.isOut = isOut;
		this.stage = stage;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
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
	
	
}
