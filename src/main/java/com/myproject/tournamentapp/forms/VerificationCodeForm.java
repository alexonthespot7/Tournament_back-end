package com.myproject.tournamentapp.forms;

public class VerificationCodeForm {
	private String verificationCode;
	
	public VerificationCodeForm(String verificationCode) {
		this.verificationCode = verificationCode;
	}

	public String getVerificationCode() {
		return verificationCode;
	}

	public void setVerificationCode(String verificationCode) {
		this.verificationCode = verificationCode;
	}
}
