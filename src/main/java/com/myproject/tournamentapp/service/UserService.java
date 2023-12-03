package com.myproject.tournamentapp.service;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Optional;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.myproject.tournamentapp.forms.AddUserFormForAdmin;
import com.myproject.tournamentapp.forms.UsersPageAdminForm;
import com.myproject.tournamentapp.model.RoundRepository;
import com.myproject.tournamentapp.model.StageRepository;
import com.myproject.tournamentapp.model.User;
import com.myproject.tournamentapp.model.UserRepository;

import net.bytebuddy.utility.RandomString;

@Service
public class UserService {
	@Autowired
	private UserRepository urepository;

	@Autowired
	private RoundRepository rrepository;

	@Autowired
	private StageRepository srepository;

	@Autowired
	private JavaMailSender mailSender;

	public static final String FRONT_END_URL = "http://localhost:3000";

	public UsersPageAdminForm getUsersForAdmin() {
		boolean isBracketMade = rrepository.findAll().size() != 0;

		List<User> users = urepository.findAll();

		// the flag to indicate whether to show make bracket button for admin or not.
		// The bracket can be made if it wasn't made before and there are more than 2
		// user
		// competitors
		boolean showMakeBracket = !isBracketMade && urepository.findAllCompetitors().size() > 2;

		// the flag to indicate whether to show make all competitors button for admin.
		// Admin can make all users competitors only if the bracket was not made yet and
		// there are more verified users than there are already competitors
		boolean showMakeAllCompetitors = !isBracketMade
				&& urepository.findAllVerifiedUsers().size() != urepository.findAllCompetitors().size();

		// flag to indicate whether to show reset button on admin's users page. The
		// reset can be activated only if there was a bracket made already
		boolean showReset = isBracketMade;

		return new UsersPageAdminForm(users, showMakeBracket, showMakeAllCompetitors, showReset, isBracketMade);
	}

	public ResponseEntity<?> addNewUserByAdmin(AddUserFormForAdmin userForm)
			throws MessagingException, UnsupportedEncodingException {
		// check if the username or email are already in use
		if (urepository.findByEmail(userForm.getEmail()) != null)
			return new ResponseEntity<>("Email is already in use", HttpStatus.NOT_ACCEPTABLE);
		if (urepository.findByUsername(userForm.getUsername()) != null)
			return new ResponseEntity<>("Username is already in use", HttpStatus.CONFLICT);

		User newUser = createUserAdmin(userForm);

		setCompetitorStatus(newUser, userForm);

		// check if admin created a verified user
		if (userForm.getIsVerified()) {
			setVerifiedUser(newUser);
			return new ResponseEntity<>("The user was added to database", HttpStatus.OK);
		}

		setUnverifiedUser(newUser);
		return new ResponseEntity<>("We sent verification link to your email address", HttpStatus.OK);
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
		if (rrepository.findAll().size() == 0) {
			newUser.setIsCompetitor(userForm.getIsCompetitor());
			newUser.setIsOut(!userForm.getIsCompetitor());
		}
	}

	private void setVerifiedUser(User newUser) {
		newUser.setAccountVerified(true);
		urepository.save(newUser);
	}

	private void setUnverifiedUser(User newUser) throws MessagingException, UnsupportedEncodingException {
		String randomCode = RandomString.make(64);
		newUser.setVerificationCode(randomCode);
		urepository.save(newUser);
		this.sendVerificationEmail(newUser);
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

	public ResponseEntity<?> updateUserByAdmin(long userId, User updatedUser) {
		Optional<User> optionalCurrentUser = urepository.findById(userId);

		if (!optionalCurrentUser.isPresent())
			return new ResponseEntity<>("User cannot be find by the specified user id", HttpStatus.BAD_REQUEST);

		User currentUser = optionalCurrentUser.get();

		if (updatedUser.getId() != currentUser.getId())
			return new ResponseEntity<>("There is a user id missmatch in request body and path", HttpStatus.CONFLICT);

		currentUser.setUsername(updatedUser.getUsername());
		currentUser.setEmail(updatedUser.getEmail());

		this.updateRole(currentUser, updatedUser);

		this.updateVerification(currentUser, updatedUser);

		this.updateCompetitorStatus(currentUser, updatedUser);

		urepository.save(currentUser);

		return new ResponseEntity<>("User was updated successfully", HttpStatus.OK);
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
		if (rrepository.findAll().size() == 0) {
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

		if (rrepository.findAll().size() > 0 && user.getIsCompetitor())
			return new ResponseEntity<>("The competitor cannot be deleted after the competition has started",
					HttpStatus.CONFLICT);

		urepository.deleteById(userId);

		return new ResponseEntity<>("The user was deleted successfully", HttpStatus.OK);
	}

	public ResponseEntity<?> makeAllCompetitors() {
		// check if the bracket was already made and if there are some verified users
		// who are not competitors yet, assuming only users are participants;
		boolean brackeWasMade = rrepository.findAll().size() != 0;
		boolean areAllCompetitors = urepository.findAllVerifiedUsers().size() == urepository.findAllCompetitors()
				.size();

		if (brackeWasMade || areAllCompetitors)
			return new ResponseEntity<>(
					"It's not allowed to use this method once the bracket is made or all verified users are competitors",
					HttpStatus.NOT_ACCEPTABLE);

		// the actual functionality of the method
		this.makeUsersCompetitors();

		return new ResponseEntity<>("The method was invoked successfully", HttpStatus.OK);
	}

	private void makeUsersCompetitors() {
		List<User> users = urepository.findAllVerifiedUsers();
		for (User user : users) {
			user.setIsCompetitor(true);
			user.setIsOut(false);
			urepository.save(user);
		}
	}

}
