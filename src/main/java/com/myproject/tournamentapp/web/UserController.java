package com.myproject.tournamentapp.web;

import java.io.UnsupportedEncodingException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.myproject.tournamentapp.forms.AddUserFormForAdmin;
import com.myproject.tournamentapp.forms.EmailForm;
import com.myproject.tournamentapp.forms.LoginForm;
import com.myproject.tournamentapp.forms.SignupForm;
import com.myproject.tournamentapp.forms.VerificationCodeForm;
import com.myproject.tournamentapp.model.RoundRepository;
import com.myproject.tournamentapp.model.StageRepository;
import com.myproject.tournamentapp.model.User;
import com.myproject.tournamentapp.model.UserRepository;
import com.myproject.tournamentapp.service.AuthenticationService;

import net.bytebuddy.utility.RandomString;

@Controller
public class UserController {
	private static final Logger log = LoggerFactory.getLogger(UserController.class);

	public static final String FRONT_END_URL = "https://tournament-axos.netlify.app";

	@Autowired
	private UserRepository repository;

	@Autowired
	private StageRepository srepository;

	@Autowired
	private RoundRepository rrepository;

	@Autowired
	private AuthenticationService jwtService;

	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	private JavaMailSender mailSender;

	// Restful login functionality by username or email
	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public ResponseEntity<?> loginMethod(@RequestBody LoginForm credentials) {

		User user = repository.findByEmail(credentials.getUsername());

		if (user == null) {
			user = repository.findByUsername(credentials.getUsername());
			// if the user wasn't found neither by mail not by username method returns
			// unauthorized response entity
			if (user == null)
				return new ResponseEntity<>("No user was found for the provided username/email",
						HttpStatus.UNAUTHORIZED);
		}

		// if user was found, but the account wasn't verified yet, the method returns
		// conflict http status
		if (!user.isAccountVerified())
			return new ResponseEntity<>("The account wasn't verified", HttpStatus.CONFLICT);

		UsernamePasswordAuthenticationToken authenticationInstance = new UsernamePasswordAuthenticationToken(
				user.getUsername(), credentials.getPassword());
		Authentication auth = authenticationManager.authenticate(authenticationInstance);
		String jwts = jwtService.getToken(auth.getName());

		// Checking the authentication instance (password, double-check username)
		User secondCheckUser = repository.findByUsername(auth.getName());

		if (secondCheckUser == null)
			return new ResponseEntity<>("Bad credentials", HttpStatus.UNAUTHORIZED);

		// sending jwt as the authorization header, user's role as ALLOW header and
		// user's id as HOST header in response entity
		return ResponseEntity.ok().header(HttpHeaders.AUTHORIZATION, "Bearer " + jwts)
				.header(HttpHeaders.ALLOW, secondCheckUser.getRole())
				.header(HttpHeaders.HOST, secondCheckUser.getId().toString())
				.header(HttpHeaders.ORIGIN, secondCheckUser.getUsername())
				.header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Authorization, Allow", "Host", "Origin").build();
	}

	// Restful method for signing-up page: creates unverified user instance and
	// sends mail with verification link to the users' email
	@RequestMapping(value = "/signup", method = RequestMethod.POST)
	public ResponseEntity<?> signUp(@Valid @RequestBody SignupForm signupForm)
			throws UnsupportedEncodingException, MessagingException {
		// check if the username or email are already in use
		if (repository.findByEmail(signupForm.getEmail()) != null)
			return new ResponseEntity<>("Email is already in use", HttpStatus.NOT_ACCEPTABLE);
		if (repository.findByUsername(signupForm.getUsername()) != null)
			return new ResponseEntity<>("Username is already in use", HttpStatus.CONFLICT);

		BCryptPasswordEncoder bc = new BCryptPasswordEncoder();
		String hashPwd = bc.encode(signupForm.getPassword());
		String randomCode = RandomString.make(64);

		User newUser = new User(signupForm.getUsername(), hashPwd,
				"USER", true, false, srepository.findByStage("No").get(0), signupForm.getEmail(), randomCode);

		// check if the competition has already started and whether we should allow to
		// set participant status
		if (rrepository.findAll().size() == 0) {
			newUser.setIsOut(!signupForm.getIsCompetitor());
			newUser.setIsCompetitor(signupForm.getIsCompetitor());
		}

		// try sending email, if it has errors then the sign-up function isn't available
		try {
			repository.save(newUser);
			this.sendVerificationEmail(newUser);
			return new ResponseEntity<>("We sent verification link to your email address", HttpStatus.OK);
		} catch (MailAuthenticationException e) {
			return new ResponseEntity<>("The smtp service authentication fail", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// method to verify the user's verification code and enable account
	@RequestMapping(value = "/verify", method = RequestMethod.POST)
	public ResponseEntity<?> verifyRequest(@RequestBody VerificationCodeForm verificationForm) {
		String verificationCode = verificationForm.getVerificationCode();

		User user = repository.findByVerificationCode(verificationCode);

		if (user == null || user.isAccountVerified())
			return new ResponseEntity<>("Verification code is incorrect or you are already verified",
					HttpStatus.CONFLICT);

		user.setVerificationCode(null);
		user.setAccountVerified(true);
		repository.save(user);

		return new ResponseEntity<>("Verification went well", HttpStatus.OK);
	}

	// method to reset user's password by email
	@RequestMapping(value = "/resetpassword", method = RequestMethod.POST)
	public ResponseEntity<?> resetPassword(@RequestBody EmailForm emailForm)
			throws UnsupportedEncodingException, MessagingException {
		User user = repository.findByEmail(emailForm.getEmail());

		if (user == null)
			return new ResponseEntity<>("User with this email (" + emailForm.getEmail() + ") doesn't exist",
					HttpStatus.BAD_REQUEST);
		if (!user.isAccountVerified())
			return new ResponseEntity<>("User with this email (" + emailForm.getEmail() + ") is not verified",
					HttpStatus.CONFLICT);

		String password = RandomString.make(15);

		BCryptPasswordEncoder bc = new BCryptPasswordEncoder();
		String hashPwd = bc.encode(password);
		user.setPasswordHash(hashPwd);

		// try sending email, if it has errors then the reset password function isn't
		// available
		try {
			this.sendPasswordEmail(user, password);
			repository.save(user);
			return new ResponseEntity<>("A temporary password was sent to your email address", HttpStatus.OK);
		} catch (MailAuthenticationException e) {
			return new ResponseEntity<>("The smtp service authentication fail", HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	// method to save new user created by admin
	@RequestMapping(value = "/admin/adduser", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('ADMIN')")
	public ResponseEntity<?> addNewUserByAdmin(@RequestBody AddUserFormForAdmin userForm)
			throws UnsupportedEncodingException, MessagingException {
		// check if the username or email are already in use
		if (repository.findByEmail(userForm.getEmail()) != null)
			return new ResponseEntity<>("Email is already in use", HttpStatus.NOT_ACCEPTABLE);
		if (repository.findByUsername(userForm.getUsername()) != null)
			return new ResponseEntity<>("Username is already in use", HttpStatus.CONFLICT);

		BCryptPasswordEncoder bc = new BCryptPasswordEncoder();
		String hashPwd = bc.encode(userForm.getPassword());

		User newUser = new User(userForm.getUsername(), hashPwd,
				userForm.getRole(), true, false, srepository.findByStage("No").get(0), userForm.getEmail(), null);

		// check if the competition has started and whether we can change a participant
		// status
		if (rrepository.findAll().size() == 0) {
			newUser.setIsCompetitor(userForm.getIsCompetitor());
			newUser.setIsOut(!userForm.getIsCompetitor());
		}
		
		
		// check if admin created a verified user
		if (userForm.getIsVerified()) {
			newUser.setAccountVerified(true);
			repository.save(newUser);
			return new ResponseEntity<>("The user was added to database", HttpStatus.OK);
		}

		String randomCode = RandomString.make(64);
		newUser.setVerificationCode(randomCode);
		repository.save(newUser);
		this.sendVerificationEmail(newUser);
		return new ResponseEntity<>("We sent verification link to your email address", HttpStatus.OK);
	}

	// Email sending method for password reset
	private void sendPasswordEmail(User user, String password) throws MessagingException, UnsupportedEncodingException {
		String toAddress = user.getEmail();
		String fromAddress = "aleksei.application.noreply@gmail.com";
		String senderName = "No reply";
		String subject = "Reset password";
		String content = "Dear [[name]],<br>" + "Here is your new TEMPORARY password for tournament app:<br><br>"
				+ "<h3>[[PASSWORD]]</h3>" + "Please change this password once you logged in<br><br>Thank you,<br>"
				+ "AXOS inc.";

		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);

		helper.setFrom(fromAddress, senderName);
		helper.setTo(toAddress);
		helper.setSubject(subject);

		content = content.replace("[[name]]", user.getUsername());

		content = content.replace("[[PASSWORD]]", password);

		helper.setText(content, true);

		mailSender.send(message);

	}

	// Email sending method for verification
	private void sendVerificationEmail(User user) throws MessagingException, UnsupportedEncodingException {
		String toAddress = user.getEmail();
		String fromAddress = "aleksei.application.noreply@gmail.com";
		String senderName = "No reply";
		String subject = "Please verify your registration";
		String content = "Dear [[name]],<br>"
				+ "Please click the link below to verify your registration on the tournament app:<br>"
				+ "<h3><a href=\"[[URL]]\" target=\"_self\">VERIFY</a></h3>" + "Thank you,<br>" + "AXOS inc.";
		String endpoint = "/verify?code=";

		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);

		helper.setFrom(fromAddress, senderName);
		helper.setTo(toAddress);
		helper.setSubject(subject);

		content = content.replace("[[name]]", user.getUsername());
		String mainURL = FRONT_END_URL + endpoint + user.getVerificationCode();

		content = content.replace("[[URL]]", mainURL);

		helper.setText(content, true);

		mailSender.send(message);
	}
}
