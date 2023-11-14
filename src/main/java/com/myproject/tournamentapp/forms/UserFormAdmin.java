package com.myproject.tournamentapp.forms;


public class UserFormAdmin {	
	private String firstname = "";
	
	private String lastname = "";
	
	private boolean isCompetitor = false;

	private String username = "";

    private String password = "";

    private String passwordCheck = "";

    private String role = "USER";
    
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
