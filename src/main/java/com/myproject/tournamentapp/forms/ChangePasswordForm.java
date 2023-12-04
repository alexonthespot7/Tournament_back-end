package com.myproject.tournamentapp.forms;

import jakarta.validation.constraints.NotBlank;

public class ChangePasswordForm {
    private String oldPassword;
	
    @NotBlank(message = "Password is mandatory")
    private String newPassword;

    public ChangePasswordForm(String oldPassword, String newPassword) {
    	this.oldPassword = oldPassword;
    	this.newPassword = newPassword;
    }

	public String getOldPassword() {
		return oldPassword;
	}

	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}
}
