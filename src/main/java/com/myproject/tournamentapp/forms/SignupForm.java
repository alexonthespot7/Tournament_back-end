package com.myproject.tournamentapp.forms;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class SignupForm {

	private boolean isCompetitor;

    @Size(max = 15, message = "Username must be less than or equal to 15 characters")
    @Pattern(regexp = "\\S+", message = "Username cannot contain whitespace")
	@NotBlank(message = "Username is mandatory")
    private String username;

	@NotBlank(message = "Password is mandatory")
    private String password;
    
    @NotBlank(message = "Email is mandatory")
    @Email(message = "Invalid email format")
    private String email;
	
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
	
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}
