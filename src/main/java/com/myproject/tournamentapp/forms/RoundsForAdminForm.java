package com.myproject.tournamentapp.forms;

import java.util.List;

import com.myproject.tournamentapp.model.Round;

public class RoundsForAdminForm {
	private List<Round> allRounds;
	private boolean doesWinnerExist;
	private boolean isCurrentStageFinished;
	
	public RoundsForAdminForm(List<Round> allRounds, boolean doesWinnerExist, boolean isCurrentStageFinished) {
		this.allRounds = allRounds;
		this.doesWinnerExist = doesWinnerExist;
		this.isCurrentStageFinished = isCurrentStageFinished;
	}

	public List<Round> getAllRounds() {
		return allRounds;
	}

	public void setAllRounds(List<Round> allRounds) {
		this.allRounds = allRounds;
	}

	public boolean isDoesWinnerExist() {
		return doesWinnerExist;
	}

	public void setDoesWinnerExist(boolean doesWinnerExist) {
		this.doesWinnerExist = doesWinnerExist;
	}

	public boolean isCurrentStageFinished() {
		return isCurrentStageFinished;
	}

	public void setCurrentStageFinished(boolean isCurrentStageFinished) {
		this.isCurrentStageFinished = isCurrentStageFinished;
	}
}
