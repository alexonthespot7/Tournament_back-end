package com.myproject.tournamentapp.forms;

import javax.validation.constraints.NotBlank;

public class AddUserFormForAdmin extends SignupForm {
	@NotBlank(message = "Role is mandatory")
	private String role;
	private boolean isVerified;

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public boolean getIsVerified() {
		return isVerified;
	}

	public void setVerified(boolean isVerified) {
		this.isVerified = isVerified;
	}
}
