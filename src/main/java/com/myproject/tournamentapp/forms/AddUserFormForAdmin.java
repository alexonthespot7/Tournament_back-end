package com.myproject.tournamentapp.forms;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AddUserFormForAdmin extends SignupForm {
	@NotBlank(message = "Role is mandatory")
	private String role;
	private boolean isVerified;
	
	public AddUserFormForAdmin() {}

	public AddUserFormForAdmin(boolean isCompetitor,
			@Size(max = 15, message = "Username must be less than or equal to 15 characters") @Pattern(regexp = "\\S+", message = "Username cannot contain whitespace") @NotBlank(message = "Username is mandatory") String username,
			@NotBlank(message = "Password is mandatory") String password,
			@NotBlank(message = "Email is mandatory") @Email(message = "Invalid email format") String email,
			@NotBlank(message = "Role is mandatory") String role, boolean isVerified) {
		super(isCompetitor, username, password, email);
		this.role = role;
		this.isVerified = isVerified;
	}

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
