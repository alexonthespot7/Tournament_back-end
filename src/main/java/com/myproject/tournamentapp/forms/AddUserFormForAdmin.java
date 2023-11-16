package com.myproject.tournamentapp.forms;

public class AddUserFormForAdmin extends SignupForm {
	private String role;
	private boolean isVerified;

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public boolean isVerified() {
		return isVerified;
	}

	public void setVerified(boolean isVerified) {
		this.isVerified = isVerified;
	}
}
