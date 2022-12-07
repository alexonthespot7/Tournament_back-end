package com.myproject.chesstournamenttest.model;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class ChangePasswordForm {
	
	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	
	@NotEmpty
    @Pattern(regexp = "\\S{5,30}")
    private String username = authentication.getName();

	@NotEmpty
    private String oldPassword = "";
	
	@NotEmpty
    @Size(min=4, max=30)
    private String password = "";

    @NotEmpty
    @Size(min=4, max=30)
    private String passwordCheck = "";
    
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getOldPassword() {
		return oldPassword;
	}

	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPasswordCheck() {
		return passwordCheck;
	}

	public void setPasswordCheck(String passwordCheck) {
		this.passwordCheck = passwordCheck;
	}
}
