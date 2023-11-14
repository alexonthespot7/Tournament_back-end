package com.myproject.tournamentapp.forms;

import java.util.List;

import com.myproject.tournamentapp.model.Stage;

public class BracketPageInfo {
	private List<Stage> stages;
	private List<RoundPublicInfo> publicRounds;
	private String winner;
	
	public BracketPageInfo(List<Stage> stages, List<RoundPublicInfo> publicRounds, String winner) {
		this.stages = stages;
		this.publicRounds = publicRounds;
		this.winner = winner;
	}
	
	public List<Stage> getStages() {
		return stages;
	}
	public void setStages(List<Stage> stages) {
		this.stages = stages;
	}
	public List<RoundPublicInfo> getPublicRounds() {
		return publicRounds;
	}
	public void setPublicRounds(List<RoundPublicInfo> publicRounds) {
		this.publicRounds = publicRounds;
	}
	public String getWinner() {
		return winner;
	}
	public void setWinner(String winner) {
		this.winner = winner;
	}
	
}
