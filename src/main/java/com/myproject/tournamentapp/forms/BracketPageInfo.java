package com.myproject.tournamentapp.forms;

import java.util.List;

public class BracketPageInfo {
	private List<StageForBracketInfo> stages;
	private String winner;
	
	public BracketPageInfo(List<StageForBracketInfo> stages, String winner) {
		this.stages = stages;
		this.winner = winner;
	}
	
	public List<StageForBracketInfo> getStages() {
		return stages;
	}
	public void setStages(List<StageForBracketInfo> stages) {
		this.stages = stages;
	}
	
	public String getWinner() {
		return winner;
	}
	public void setWinner(String winner) {
		this.winner = winner;
	}
	
}
