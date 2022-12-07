package com.myproject.chesstournamenttest.model;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import javax.validation.constraints.Pattern;

public class SignupForm {
	@NotEmpty
	private String firstname = "";
	
	@NotEmpty
	private String lastname = "";
	
	private boolean isCompetitor = false;
	
    @NotEmpty
    @Pattern(regexp = "\\S{5,30}")
    private String username = "";

    @NotEmpty
    @Size(min=4, max=30)
    private String password = "";

    @NotEmpty
    @Size(min=4, max=30)
    private String passwordCheck = "";

    @NotEmpty
    private String role = "USER";
    
    @NotEmpty
    @Email
    private String email = "";
    
    public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}
	
	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}
	
	public boolean getIsCompetitor() {
		return isCompetitor;
	}

	public void setIsCompetitor(boolean isCompetitor) {
		this.isCompetitor = isCompetitor;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
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

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}
	
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}
