package com.myproject.tournamentapp.web;

import java.io.UnsupportedEncodingException;

import jakarta.mail.MessagingException;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.myproject.tournamentapp.forms.EmailForm;
import com.myproject.tournamentapp.forms.LoginForm;
import com.myproject.tournamentapp.forms.SignupForm;
import com.myproject.tournamentapp.forms.VerificationCodeForm;
import com.myproject.tournamentapp.model.RoundRepository;
import com.myproject.tournamentapp.model.UserRepository;
import com.myproject.tournamentapp.service.RoundService;
import com.myproject.tournamentapp.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api")
@Tag(name = "Public Methods", description = "Methods that don't require any authentication")
@ApiResponses(value = {
		@ApiResponse(responseCode = "401", description = "Error: Full authentication is required to access this resource. Excessively provided jwt token is wrong", content = {
				@Content(mediaType = "application/json", schema = @Schema(implementation = ResponseStatusException.class)) 
		}) 
})
public class RestPublicController {
	@Autowired
	UserRepository urepository;

	@Autowired
	RoundRepository rrepository;

	@Autowired
	private UserService userService;
	
	@Autowired
	private RoundService roundService;

	// Method to send the quantity of the rounds to the main page to conditionally
	// render buttons
	@Operation(summary = "Gets the rounds quantity", description = "The rounds quantity value indicates if the bracket was already made or not")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "OK", content = {
					@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class), examples = {
							@ExampleObject(value = "0")
					}) 
			}) 
	})
	@RequestMapping(value = "/roundsquantity", method = RequestMethod.GET)
	public @ResponseBody String getRoundsQuantity() {

		return roundService.getRoundsQuantity();

	}

	// Restful login functionality by username or email
	@Operation(summary = "Login method", description = "The login method by username/email and password")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "OK", headers = {
					@Header(name = "Authorization", description = "JSON web token value to be used for the authentication"),
					@Header(name = "Allow", description = "User's role (ADMIN/USER)"),
					@Header(name = "Host", description = "User's id"),
					@Header(name = "Origin", description = "User's username") 
			}), 
			@ApiResponse(responseCode = "400", description = "BAD_REQUEST, username/email is wrong.", content = {
					@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class), examples = {
							@ExampleObject(value = "No user was found for the provided username/email")
					})
			}),
			@ApiResponse(responseCode = "401", description = "Error: Bad credentials. The password is wrong", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = ResponseStatusException.class)) 
			}),
			@ApiResponse(responseCode = "409", description = "CONFLICT, the account isn't verified", content = {
					@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class), examples = {
							@ExampleObject(value = "The account wasn't verified")
					})
			})
	})
	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public ResponseEntity<?> loginMethod(@RequestBody LoginForm credentials) {

		return userService.loginMethod(credentials);

	}

	// Restful method for signing-up page: creates unverified user instance and
	// sends mail with verification link to the users' email
	@Operation(summary = "Signup method", description = "The registration method. Requires following fields: username, email, password. Also isCompetitor property is required, but it is applied only if the bracket wasn't made yet")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "The user was created and requires verification", content = {
					@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class), examples = {
							@ExampleObject(value = "We sent verification link to your email address")
					}) 
			}), 
			@ApiResponse(responseCode = "406", description = "NOT_ACCEPTABLE, email is in use", content = {
					@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class), examples = {
							@ExampleObject(value = "Email is already in use")
					})
			}),
			@ApiResponse(responseCode = "409", description = "CONFLICT, Username is in use", content = {
					@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class), examples = {
							@ExampleObject(value = "Username is already in use")
					}) 
			}),
			@ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR, the user was added to database, but the smtp service credentials are incorrect or can't reach the service, therefore user should be verified by admin or try signup later.", content = {
					@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class), examples = {
							@ExampleObject(value = "The smtp service authentication fail, ask admin to verify account")
					})
			})
	})
	@RequestMapping(value = "/signup", method = RequestMethod.POST)
	public ResponseEntity<?> signUp(@Valid @RequestBody SignupForm signupForm)
			throws UnsupportedEncodingException, MessagingException {

		return userService.signUp(signupForm);

	}

	// method to verify the user's verification code and enable account
	@Operation(summary = "Verify user's account method", description = "The method searches for the user by provided verification code to enable account in the database.")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "OK, verification went well", content = {
					@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class), examples = {
							@ExampleObject(value = "Verification went well")
					}) 
			}),
			@ApiResponse(responseCode = "409", description = "CONFLICT, code is wrong or user is verified", content = {
					@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class), examples = {
							@ExampleObject(value = "Verification code is incorrect or you are already verified")
					}) 
			})
	})
	@RequestMapping(value = "/verify", method = RequestMethod.PUT)
	public ResponseEntity<?> verifyRequest(@RequestBody VerificationCodeForm verificationForm) {

		return userService.verifyRequest(verificationForm);

	}

	// method to reset user's password by email
	@Operation(summary = "Reset password method", description = "The method to reset user's password by provided email")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "OK, new randomly generated password was sent to provided email address", content = {
					@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class), examples = {
							@ExampleObject(value = "A temporary password was sent to your email address")
					}) 
			}), 
			@ApiResponse(responseCode = "400", description = "BAD_REQUEST, email was not find in database", content = {
					@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class), examples = {
							@ExampleObject(value = "User with this email (example@email.com) doesn't exist")
					})
			}),
			@ApiResponse(responseCode = "409", description = "CONFLICT, user is not verified", content = {
					@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class), examples = {
							@ExampleObject(value = "User with this email (example@email.com) is not verified")
					}) 
			}),
			@ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR, the smtp service credentials are incorrect or can't reach the service.", content = {
					@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class), examples = {
							@ExampleObject(value = "The smtp service authentication fail, ask admin to verify account")
					})
			})
	})
	@RequestMapping(value = "/resetpassword", method = RequestMethod.PUT)
	public ResponseEntity<?> resetPassword(@RequestBody EmailForm emailForm)
			throws UnsupportedEncodingException, MessagingException {

		return userService.resetPassword(emailForm);

	}
}
