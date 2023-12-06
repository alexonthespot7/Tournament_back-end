package com.myproject.tournamentapp.forms;

public class EmailForm {
    private String email;
    
    public EmailForm() {}
    
    public EmailForm(String email) {
    	this.email = email;
    }
	
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}
