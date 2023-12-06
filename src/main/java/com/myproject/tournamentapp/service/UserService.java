package com.myproject.tournamentapp.service;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.mail.MessagingException;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.myproject.tournamentapp.MyUser;
import com.myproject.tournamentapp.forms.AddUserFormForAdmin;
import com.myproject.tournamentapp.forms.ChangePasswordForm;
import com.myproject.tournamentapp.forms.CompetitorPublicInfo;
import com.myproject.tournamentapp.forms.EmailForm;
import com.myproject.tournamentapp.forms.LoginForm;
import com.myproject.tournamentapp.forms.PersonalInfo;
import com.myproject.tournamentapp.forms.RoundPublicInfo;
import com.myproject.tournamentapp.forms.SignupForm;
import com.myproject.tournamentapp.forms.UsersPageAdminForm;
import com.myproject.tournamentapp.forms.VerificationCodeForm;
import com.myproject.tournamentapp.model.Round;
import com.myproject.tournamentapp.model.StageRepository;
import com.myproject.tournamentapp.model.User;
import com.myproject.tournamentapp.model.UserRepository;

@Service
public class UserService {
	@Autowired
	private UserRepository urepository;

	@Autowired
	private StageRepository srepository;

	@Autowired
	private QuantityService quantityService;
	
	@Autowired
	private MakeRoundsPublicService makeRoundsPublicService;

	@Autowired
	private MailService mailService;

	@Autowired
	private AuthenticationService jwtService;

	@Autowired
	private AuthenticationManager authenticationManager;

	public static final String FRONT_END_URL = "http://localhost:3000";

	public UsersPageAdminForm getUsersForAdmin() {
		int roundsQuantity = quantityService.findRoundsQuantity();
		
		boolean isBracketMade = roundsQuantity != 0;

		List<User> users = urepository.findAll();

		// the flag to indicate whether to show make bracket button for admin or not.
		// The bracket can be made if it wasn't made before and there are more than 2
		// user competitors
		int competitorsQuantity = quantityService.findCompetitorsQuantity();

		boolean showMakeBracket = !isBracketMade && competitorsQuantity > 2;

		// the flag to indicate whether to show make all competitors button for admin.
		// Admin can make all users competitors only if the bracket was not made yet and
		// there are more verified users than there are already competitors
		int verifiedUsersQuantity = this.findVerifiedUsersQuantity();

		boolean showMakeAllCompetitors = !isBracketMade && verifiedUsersQuantity != competitorsQuantity;

		// flag to indicate whether to show reset button on admin's users page. The
		// reset can be activated only if there was a bracket made already
		boolean showReset = isBracketMade;

		return new UsersPageAdminForm(users, showMakeBracket, showMakeAllCompetitors, showReset, isBracketMade);
	}

	
	private int findVerifiedUsersQuantity() {
		List<User> allVerifiedUsers = urepository.findAllVerifiedUsers();
		int verifiedUsersQuantity = allVerifiedUsers.size();
		
		return verifiedUsersQuantity;
	}

	public List<CompetitorPublicInfo> listCompetitorsPublicInfo() {
		List<User> allUsers = urepository.findAll();

		List<CompetitorPublicInfo> allCompetitors = this.makeAllUsersPublicCompetitors(allUsers);

		return allCompetitors;
	}

	private List<CompetitorPublicInfo> makeAllUsersPublicCompetitors(List<User> allUsers) {
		List<CompetitorPublicInfo> allCompetitors = new ArrayList<>();
		CompetitorPublicInfo competitor;

		for (User user : allUsers) {
			if (user.getIsCompetitor()) {
				competitor = new CompetitorPublicInfo(user.getUsername(), user.getIsOut(), user.getStage().getStage());
				allCompetitors.add(competitor);
			}
		}

		return allCompetitors;
	}

	public PersonalInfo getPersonalInfoById(Long userId, Authentication auth) {
		// double check authentication
		if (!auth.getPrincipal().getClass().toString().equals("class com.myproject.tournamentapp.MyUser"))
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You are not authorized");

		User user = this.findUserByAuth(auth);

		if (user == null || user.getId() != userId)
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have an access to this page");

		List<Round> allRoundsOfUser = this.findAllUsersRounds(user);

		List<RoundPublicInfo> publicUserRounds = makeRoundsPublicService.makeRoundsPublic(allRoundsOfUser);

		int roundsQuantity = quantityService.findRoundsQuantity();

		PersonalInfo personalInfoInstance = new PersonalInfo(user.getUsername(), user.getEmail(), user.getIsOut(),
				user.getStage().getStage(), user.getIsCompetitor(), roundsQuantity, publicUserRounds);

		return personalInfoInstance;
	}

	private User findUserByAuth(Authentication auth) {
		MyUser myUserInstance = (MyUser) auth.getPrincipal();
		User user = urepository.findByUsername(myUserInstance.getUsername());

		return user;
	}

	private List<Round> findAllUsersRounds(User user) {
		List<Round> allRounds = user.getRounds1();
		allRounds.addAll(user.getRounds2());

		return allRounds;
	}

	public ResponseEntity<?> loginMethod(LoginForm credentials) {
		User user = urepository.findByEmail(credentials.getUsername());

		if (user == null) {
			user = urepository.findByUsername(credentials.getUsername());
			// if the user wasn't found neither by mail not by username method returns
			// unauthorized response entity
			if (user == null)
				return new ResponseEntity<>("No user was found for the provided username/email",
						HttpStatus.BAD_REQUEST);
		}

		// if user was found, but the account wasn't verified yet, the method returns
		// conflict http status
		if (!user.isAccountVerified())
			return new ResponseEntity<>("The account wasn't verified", HttpStatus.CONFLICT);

		Authentication auth = this.authenticateUser(user, credentials);

		String jwts = jwtService.getToken(auth.getName());

		// Checking the authentication instance (password, double-check username)
		User authenticatedUser = urepository.findByUsername(auth.getName());

		if (authenticatedUser == null)
			return new ResponseEntity<>("Bad credentials", HttpStatus.UNAUTHORIZED);

		// sending jwt as the authorization header, user's role as ALLOW header and
		// user's id as HOST header in response entity
		return ResponseEntity.ok().header(HttpHeaders.AUTHORIZATION, "Bearer " + jwts)
				.header(HttpHeaders.ALLOW, authenticatedUser.getRole())
				.header(HttpHeaders.HOST, authenticatedUser.getId().toString())
				.header(HttpHeaders.ORIGIN, authenticatedUser.getUsername())
				.header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Authorization, Allow", "Host", "Origin").build();
	}

	private Authentication authenticateUser(User user, LoginForm credentials) {
		UsernamePasswordAuthenticationToken authenticationInstance = new UsernamePasswordAuthenticationToken(
				user.getUsername(), credentials.getPassword());
		Authentication auth = authenticationManager.authenticate(authenticationInstance);

		return auth;
	}

	public ResponseEntity<?> signUp(SignupForm signupForm) throws UnsupportedEncodingException, MessagingException {
		// check if the username or email are already in use
		if (urepository.findByEmail(signupForm.getEmail()) != null)
			return new ResponseEntity<>("Email is already in use", HttpStatus.NOT_ACCEPTABLE);
		if (urepository.findByUsername(signupForm.getUsername()) != null)
			return new ResponseEntity<>("Username is already in use", HttpStatus.CONFLICT);

		User newUser = this.createSignupUser(signupForm);

		this.setCompetitorStatusSignup(newUser, signupForm);

		// try sending email, if it has errors then the sign-up function isn't available
		try {
			urepository.save(newUser);
			mailService.sendVerificationEmail(newUser);
			return new ResponseEntity<>("We sent verification link to your email address", HttpStatus.OK);
		} catch (MailAuthenticationException e) {
			return new ResponseEntity<>("The smtp service authentication fail, ask admin to verify account",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private User createSignupUser(SignupForm signupForm) {
		BCryptPasswordEncoder bc = new BCryptPasswordEncoder();
		String hashPwd = bc.encode(signupForm.getPassword());
		String randomCode = RandomStringUtils.random(64);

		User newUser = new User(signupForm.getUsername(), hashPwd, "USER", true, false,
				srepository.findByStage("No").get(0), signupForm.getEmail(), randomCode);

		return newUser;
	}

	private void setCompetitorStatusSignup(User newUser, SignupForm signupForm) {
		// check if the competition has already started and whether we should allow to
		// set participant status
		int roundsQuantity = quantityService.findRoundsQuantity();
		
		if (roundsQuantity == 0) {
			newUser.setIsOut(!signupForm.getIsCompetitor());
			newUser.setIsCompetitor(signupForm.getIsCompetitor());
		}
	}

	public ResponseEntity<?> addNewUserByAdmin(AddUserFormForAdmin userForm)
			throws MessagingException, UnsupportedEncodingException {
		// check if the username or email are already in use
		if (urepository.findByEmail(userForm.getEmail()) != null)
			return new ResponseEntity<>("Email is already in use", HttpStatus.NOT_ACCEPTABLE);
		if (urepository.findByUsername(userForm.getUsername()) != null)
			return new ResponseEntity<>("Username is already in use", HttpStatus.CONFLICT);

		User newUser = this.createUserAdmin(userForm);

		this.setCompetitorStatus(newUser, userForm);

		// check if admin created a verified user
		if (userForm.getIsVerified()) {
			this.setVerifiedUser(newUser);
			return new ResponseEntity<>("The user was added to database", HttpStatus.OK);
		}

		this.setVerificationCode(newUser);

		// try sending email, if it has errors then the sign-up function isn't available
		try {
			urepository.save(newUser);
			mailService.sendVerificationEmail(newUser);
			return new ResponseEntity<>("We sent verification link to your email address", HttpStatus.OK);
		} catch (MailAuthenticationException e) {
			return new ResponseEntity<>("The smtp service authentication fail, ask admin to verify account",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private User createUserAdmin(AddUserFormForAdmin userForm) {
		BCryptPasswordEncoder bc = new BCryptPasswordEncoder();
		String hashPwd = bc.encode(userForm.getPassword());

		User newUser = new User(userForm.getUsername(), hashPwd, userForm.getRole(), true, false,
				srepository.findByStage("No").get(0), userForm.getEmail(), null);

		return newUser;
	}

	private void setCompetitorStatus(User newUser, AddUserFormForAdmin userForm) {
		// check if the competition has started and whether we can change a participant
		// status
		int roundsQuantity = quantityService.findRoundsQuantity();

		if (roundsQuantity == 0) {
			newUser.setIsCompetitor(userForm.getIsCompetitor());
			newUser.setIsOut(!userForm.getIsCompetitor());
		}
	}

	private void setVerifiedUser(User newUser) {
		newUser.setAccountVerified(true);
		urepository.save(newUser);
	}

	private void setVerificationCode(User newUser) {
		String randomCode = RandomStringUtils.random(64);
		newUser.setVerificationCode(randomCode);
	}

	public ResponseEntity<?> verifyRequest(VerificationCodeForm verificationForm) {
		String verificationCode = verificationForm.getVerificationCode();

		User user = urepository.findByVerificationCode(verificationCode);

		if (user == null || user.isAccountVerified())
			return new ResponseEntity<>("Verification code is incorrect or you are already verified",
					HttpStatus.CONFLICT);

		this.verifyUser(user);

		return new ResponseEntity<>("Verification went well", HttpStatus.OK);
	}

	private void verifyUser(User user) {
		user.setVerificationCode(null);
		user.setAccountVerified(true);
		urepository.save(user);
	}

	public ResponseEntity<?> changePassword(ChangePasswordForm changePasswordForm, Authentication auth) {
		// check authentication;
		if (!auth.getPrincipal().getClass().toString().equals("class com.myproject.tournamentapp.MyUser"))
			return new ResponseEntity<>("Not authenticated", HttpStatus.UNAUTHORIZED);

		User user = this.findUserByAuth(auth);

		if (user == null)
			return new ResponseEntity<>("Not authenticated", HttpStatus.UNAUTHORIZED);

		BCryptPasswordEncoder bcEncoder = new BCryptPasswordEncoder();
		// check the old password provided by user
		if (!bcEncoder.matches(changePasswordForm.getOldPassword(), user.getPasswordHash()))
			return new ResponseEntity<>("The old password is incorrect", HttpStatus.FORBIDDEN);

		this.updatePassword(bcEncoder, changePasswordForm, user);

		return new ResponseEntity<>("The password was successfully changed", HttpStatus.OK);
	}

	private void updatePassword(BCryptPasswordEncoder bcEncoder, ChangePasswordForm changePasswordForm, User user) {
		String newHashedPwd = bcEncoder.encode(changePasswordForm.getNewPassword());
		user.setPasswordHash(newHashedPwd);
		urepository.save(user);
	}

	public ResponseEntity<?> resetPassword(EmailForm emailForm)
			throws UnsupportedEncodingException, MessagingException {
		User user = urepository.findByEmail(emailForm.getEmail());

		if (user == null)
			return new ResponseEntity<>("User with this email (" + emailForm.getEmail() + ") doesn't exist",
					HttpStatus.BAD_REQUEST);
		if (!user.isAccountVerified())
			return new ResponseEntity<>("User with this email (" + emailForm.getEmail() + ") is not verified",
					HttpStatus.CONFLICT);

		String randomPassword = this.setRandomPassword(user);

		// try sending email, if it has errors then the reset password function isn't
		// available
		try {
			mailService.sendPasswordEmail(user, randomPassword);
			urepository.save(user);
			return new ResponseEntity<>("A temporary password was sent to your email address", HttpStatus.OK);
		} catch (MailAuthenticationException e) {
			return new ResponseEntity<>("The smtp service authentication fail", HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	private String setRandomPassword(User user) {
		String password = RandomStringUtils.random(15);

		BCryptPasswordEncoder bc = new BCryptPasswordEncoder();
		String hashPwd = bc.encode(password);
		user.setPasswordHash(hashPwd);

		return password;
	}

	public ResponseEntity<?> updateUser(Long userId, PersonalInfo personalInfo) {
		Optional<User> optionalUser = urepository.findById(userId);

		if (!optionalUser.isPresent())
			return new ResponseEntity<>("Something wrong with authentication", HttpStatus.CONFLICT);

		User user = optionalUser.get();

		this.updateUsersInfo(user, personalInfo);

		return new ResponseEntity<>("User info was updated successfully", HttpStatus.OK);
	}

	private void updateUsersInfo(User user, PersonalInfo personalInfo) {
		int roundsQuantity = quantityService.findRoundsQuantity();

		if (roundsQuantity == 0) {
			user.setIsCompetitor(personalInfo.isCompetitor());
			user.setIsOut(!personalInfo.isCompetitor());
		}

		urepository.save(user);
	}

	public ResponseEntity<?> updateUserByAdmin(Long userId, User updatedUser) {
		Optional<User> optionalCurrentUser = urepository.findById(userId);

		if (!optionalCurrentUser.isPresent())
			return new ResponseEntity<>("User cannot be find by the specified user id", HttpStatus.BAD_REQUEST);

		User currentUser = optionalCurrentUser.get();

		if (updatedUser.getId() != currentUser.getId())
			return new ResponseEntity<>("There is a user id missmatch in request body and path", HttpStatus.CONFLICT);

		this.updateUserData(currentUser, updatedUser);

		urepository.save(currentUser);

		return new ResponseEntity<>("User was updated successfully", HttpStatus.OK);
	}

	private void updateUserData(User currentUser, User updatedUser) {
		this.updateUsernameAndEmail(currentUser, updatedUser);

		this.updateRole(currentUser, updatedUser);

		this.updateVerification(currentUser, updatedUser);

		this.updateCompetitorStatus(currentUser, updatedUser);
	}

	private void updateUsernameAndEmail(User currentUser, User updatedUser) {
		currentUser.setUsername(updatedUser.getUsername());
		currentUser.setEmail(updatedUser.getEmail());
	}

	private void updateRole(User currentUser, User updatedUser) {
		if (!currentUser.getRole().equals("ADMIN")) {
			currentUser.setRole(updatedUser.getRole());
		}
	}

	private void updateVerification(User currentUser, User updatedUser) {
		if (updatedUser.isAccountVerified() && !currentUser.isAccountVerified()) {
			currentUser.setAccountVerified(true);
			currentUser.setVerificationCode(null);
		}
	}

	private void updateCompetitorStatus(User currentUser, User updatedUser) {
		int roundsQuantity = quantityService.findRoundsQuantity();

		if (roundsQuantity == 0) {
			currentUser.setIsOut(!updatedUser.getIsCompetitor());
			currentUser.setIsCompetitor(updatedUser.getIsCompetitor());
		}
	}

	public ResponseEntity<?> deleteUserForAdmin(Long userId) {
		Optional<User> optionalUser = urepository.findById(userId);

		if (!optionalUser.isPresent())
			return new ResponseEntity<>("Cannot find user with specified id", HttpStatus.BAD_REQUEST);

		User user = optionalUser.get();

		// user can be deleted only if the bracket wasn't made yet or the user is not a
		// competitor and the role of the user is not ADMIN
		if (user.getRole() == "ADMIN")
			return new ResponseEntity<>("You cannot delete ADMIN", HttpStatus.CONFLICT);

		int roundsQuantity = quantityService.findRoundsQuantity();

		if (roundsQuantity > 0 && user.getIsCompetitor())
			return new ResponseEntity<>("The competitor cannot be deleted after the competition has started",
					HttpStatus.CONFLICT);

		urepository.deleteById(userId);

		return new ResponseEntity<>("The user was deleted successfully", HttpStatus.OK);
	}

	public ResponseEntity<?> makeAllCompetitors() {
		// check if the bracket was already made and if there are some verified users
		// who are not competitors yet, assuming only users are participants;
		int roundsQuantity = quantityService.findRoundsQuantity();

		boolean brackeWasMade = roundsQuantity != 0;
		
		int verifiedUsersQuantity = this.findVerifiedUsersQuantity();
		int competitorsQuantity = quantityService.findCompetitorsQuantity();

		boolean areAllCompetitors = verifiedUsersQuantity == competitorsQuantity;

		if (brackeWasMade || areAllCompetitors)
			return new ResponseEntity<>(
					"It's not allowed to use this method once the bracket is made or all verified users are competitors",
					HttpStatus.NOT_ACCEPTABLE);

		// the actual functionality of the method
		this.makeUsersCompetitors();

		return new ResponseEntity<>("The method was invoked successfully", HttpStatus.OK);
	}

	private void makeUsersCompetitors() {
		List<User> verifiedUsers = urepository.findAllVerifiedUsers();
		for (User user : verifiedUsers) {
			user.setIsCompetitor(true);
			user.setIsOut(false);
			urepository.save(user);
		}
	}

}
