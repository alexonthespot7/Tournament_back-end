package com.myproject.tournamentapp.forms;

import java.util.List;

public class StageForBracketInfo {
	private String stage;
	private boolean isCurrent;
	private List<RoundPublicInfo> rounds;

	public StageForBracketInfo(String stage, boolean isCurrent, List<RoundPublicInfo> rounds) {
		this.stage = stage;
		this.isCurrent = isCurrent;
		this.rounds = rounds;
	}

	public String getStage() {
		return stage;
	}

	public void setStage(String stage) {
		this.stage = stage;
	}

	public boolean isCurrent() {
		return isCurrent;
	}

	public void setCurrent(boolean isCurrent) {
		this.isCurrent = isCurrent;
	}

	public List<RoundPublicInfo> getRounds() {
		return rounds;
	}

	public void setRounds(List<RoundPublicInfo> rounds) {
		this.rounds = rounds;
	}
	
}
