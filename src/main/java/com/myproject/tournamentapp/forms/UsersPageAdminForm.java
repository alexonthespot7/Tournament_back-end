package com.myproject.tournamentapp.forms;

import java.util.List;

import com.myproject.tournamentapp.model.User;

public class UsersPageAdminForm {
	private List<User> users;
	private boolean showMakeBracket;
	private boolean showMakeAllCompetitors;
	private boolean showReset;
	private boolean isBracketMade;
	
	public UsersPageAdminForm() {}
	
	public UsersPageAdminForm(List<User> users, boolean showMakeBracket, boolean showMakeAllCompetitors,
			boolean showReset, boolean isBracketMade) {
		this.users = users;
		this.showMakeBracket = showMakeBracket;
		this.showMakeAllCompetitors = showMakeAllCompetitors;
		this.showReset = showReset;
		this.isBracketMade = isBracketMade;
	}
	
	public List<User> getUsers() {
		return users;
	}
	public void setUsers(List<User> users) {
		this.users = users;
	}
	public boolean isShowMakeBracket() {
		return showMakeBracket;
	}
	public void setShowMakeBracket(boolean showMakeBracket) {
		this.showMakeBracket = showMakeBracket;
	}
	public boolean isShowMakeAllCompetitors() {
		return showMakeAllCompetitors;
	}
	public void setShowMakeAllCompetitors(boolean showMakeAllCompetitors) {
		this.showMakeAllCompetitors = showMakeAllCompetitors;
	}
	public boolean isShowReset() {
		return showReset;
	}
	public void setShowReset(boolean showReset) {
		this.showReset = showReset;
	}

	public boolean isBracketMade() {
		return isBracketMade;
	}

	public void setBracketMade(boolean isBracketMade) {
		this.isBracketMade = isBracketMade;
	}
	
}
