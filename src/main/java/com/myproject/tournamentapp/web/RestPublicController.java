package com.myproject.tournamentapp.web;

import java.io.UnsupportedEncodingException;

import javax.mail.MessagingException;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.myproject.tournamentapp.forms.EmailForm;
import com.myproject.tournamentapp.forms.LoginForm;
import com.myproject.tournamentapp.forms.SignupForm;
import com.myproject.tournamentapp.forms.VerificationCodeForm;
import com.myproject.tournamentapp.model.RoundRepository;
import com.myproject.tournamentapp.model.User;
import com.myproject.tournamentapp.model.UserRepository;
import com.myproject.tournamentapp.service.AuthenticationService;
import com.myproject.tournamentapp.service.UserService;

import net.bytebuddy.utility.RandomString;

@RestController
@RequestMapping("/api")
public class RestPublicController {
	@Autowired
	UserRepository urepository;

	@Autowired
	RoundRepository rrepository;

	@Autowired
	private UserService userService;

	// Method to send the quantity of the rounds to the main page to conditionally
	// render buttons
	@RequestMapping(value = "/roundsquantity", method = RequestMethod.GET)
	public @ResponseBody String getRoundsQuantity() {
		int roundQuantity = rrepository.findAll().size();

		return String.valueOf(roundQuantity);
	}

	// Restful login functionality by username or email
	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public ResponseEntity<?> loginMethod(@RequestBody LoginForm credentials) {

		return userService.loginMethod(credentials);

	}

	// Restful method for signing-up page: creates unverified user instance and
	// sends mail with verification link to the users' email
	@RequestMapping(value = "/signup", method = RequestMethod.POST)
	public ResponseEntity<?> signUp(@Valid @RequestBody SignupForm signupForm)
			throws UnsupportedEncodingException, MessagingException {

		return userService.signUp(signupForm);

	}

	// method to verify the user's verification code and enable account
	@RequestMapping(value = "/verify", method = RequestMethod.PUT)
	public ResponseEntity<?> verifyRequest(@RequestBody VerificationCodeForm verificationForm) {

		return userService.verifyRequest(verificationForm);

	}

	// method to reset user's password by email
	@RequestMapping(value = "/resetpassword", method = RequestMethod.PUT)
	public ResponseEntity<?> resetPassword(@RequestBody EmailForm emailForm)
			throws UnsupportedEncodingException, MessagingException {
		
		return userService.resetPassword(emailForm);
	
	}
}
