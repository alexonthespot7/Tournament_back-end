package com.myproject.tournamentapp.web;

import java.io.UnsupportedEncodingException;
import java.util.Optional;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.myproject.tournamentapp.forms.ChangePasswordForm;
import com.myproject.tournamentapp.forms.EmailForm;
import com.myproject.tournamentapp.forms.LoginForm;
import com.myproject.tournamentapp.forms.SignupForm;
import com.myproject.tournamentapp.forms.UserFormAdmin;
import com.myproject.tournamentapp.model.RoundRepository;
import com.myproject.tournamentapp.model.StageRepository;
import com.myproject.tournamentapp.model.User;
import com.myproject.tournamentapp.model.UserRepository;
import com.myproject.tournamentapp.service.AuthenticationService;

import net.bytebuddy.utility.RandomString;

@Controller
public class UserController {
	public static final String FRONT_END_URL = "http://localhost:3000";

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
				.header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Authorization, Allow", "Host").build();
	}

	// Restful method for signing-up page: creates unverified user instance and
	// sends mail with verification link to the users' email
	@RequestMapping(value = "/signup", method = RequestMethod.POST)
	public ResponseEntity<?> signUp(@RequestBody SignupForm signupForm)
			throws UnsupportedEncodingException, MessagingException {
		// check if the username or email are already in use
		if (repository.findByEmail(signupForm.getEmail()) != null)
			return new ResponseEntity<>("Email is already in use", HttpStatus.NOT_ACCEPTABLE);
		if (repository.findByUsername(signupForm.getUsername()) != null)
			return new ResponseEntity<>("Username is already in use", HttpStatus.CONFLICT);

		BCryptPasswordEncoder bc = new BCryptPasswordEncoder();
		String hashPwd = bc.encode(signupForm.getPassword());
		String randomCode = RandomString.make(64);

		User newUser = new User(signupForm.getFirstname(), signupForm.getLastname(), signupForm.getUsername(), hashPwd,
				"USER", !signupForm.getIsCompetitor(), signupForm.getIsCompetitor(),
				srepository.findByStage("No").get(0), signupForm.getEmail(), randomCode);

		repository.save(newUser);
		this.sendVerificationEmail(newUser);
		return new ResponseEntity<>("We sent verification link to your email address :)", HttpStatus.OK);
	}

	@RequestMapping(value = "signup")
	public String addStudent(Model model) {
		model.addAttribute("signupform", new SignupForm());
		model.addAttribute("rounds", rrepository.findAll().size());
		return "signup";
	}

	// Add new user (admin)
	@RequestMapping("/adduser")
	@PreAuthorize("hasAuthority('ADMIN')")
	public String addUser(Model model) {
		model.addAttribute("form", new UserFormAdmin());
		model.addAttribute("rounds", rrepository.findAll().size());
		return "adduser";
	}

	// Edit user (admin)
	@RequestMapping("/edituser/{id}")
	@PreAuthorize("hasAuthority('ADMIN')")
	public String editUser(@PathVariable("id") Long userId, Model model) {
		model.addAttribute("user", repository.findById(userId));
		Optional<User> user = repository.findById(userId);
		user.ifPresent(userIn -> {
			model.addAttribute("isVerified", userIn.isAccountVerified());
		});
		if (!user.isPresent()) {
			model.addAttribute("isVerified", false);
		}

		model.addAttribute("rounds", rrepository.findAll().size());
		return "edituser";
	}

	// Edit user (user)
	@RequestMapping("/edituserbyname/{username}")
	@PreAuthorize("authentication.getPrincipal().getUsername() == #username")
	public String editUser(@PathVariable("username") String username, Model model) {
		model.addAttribute("user", repository.findByUsername(username));
		model.addAttribute("rounds", rrepository.findAll().size());
		return "edituserbyname";
	}

	// change password for user:
	@RequestMapping("/changepassword/{username}")
	@PreAuthorize("authentication.getPrincipal().getUsername() == #username")
	public String changePassword(@PathVariable("username") String username, Model model) {
		User user = repository.findByUsername(username);
		model.addAttribute("user", user);
		model.addAttribute("form", new ChangePasswordForm());
		return "changepassword";
	}

	// reset password by email
	@RequestMapping("/resetbyemail")
	public String resetPassword(Model model) {
		model.addAttribute("emailform", new EmailForm());
		return "resetfirst";
	}

	// Reset password functionality
	@RequestMapping(value = "resetpassword", method = RequestMethod.POST)
	public String changePassword(@ModelAttribute("emailform") EmailForm emailForm, BindingResult bindingResult)
			throws UnsupportedEncodingException, MessagingException {
		if (!bindingResult.hasErrors()) {
			if (repository.findByEmail(emailForm.getEmail()) != null) {
				User curruser = repository.findByEmail(emailForm.getEmail());
				if (curruser.isAccountVerified()) {
					String password = RandomString.make(15);

					BCryptPasswordEncoder bc = new BCryptPasswordEncoder();
					String hashPwd = bc.encode(password);
					curruser.setPasswordHash(hashPwd);
					repository.save(curruser);

					this.sendPasswordEmail(curruser, password);
				} else {
					bindingResult.rejectValue("email", "err.email",
							"User with this email is not verified: " + emailForm.getEmail());
					return "resetfirst";
				}
			} else {
				bindingResult.rejectValue("email", "err.email",
						"There is no user with such email: " + emailForm.getEmail());
				return "resetfirst";
			}
		} else {
			return "resetfirst";
		}
		return "redirect:/login";
	}

	// saving user after changing password by authenticated user
	@RequestMapping(value = "savepassword", method = RequestMethod.POST)
	@PreAuthorize("authentication.getPrincipal().getUsername() == #form.getUsername()")
	public String savePassword(@ModelAttribute("form") ChangePasswordForm form, BindingResult bindingResult) {
		if (!bindingResult.hasErrors()) { // validation errors
			User curruser = repository.findByUsername(form.getUsername());
			BCryptPasswordEncoder bc = new BCryptPasswordEncoder();

			if (bc.matches(form.getOldPassword(), curruser.getPasswordHash())) { // check if old password matches
				if (form.getPassword().equals(form.getPasswordCheck())) { // check password match
					String pwd = form.getPassword();
					String hashPwd = bc.encode(pwd);

					curruser.setPasswordHash(hashPwd);
					repository.save(curruser);
				} else {
					bindingResult.rejectValue("passwordCheck", "err.passCheck", "Passwords does not match");
					return "changepassword";
				}
			} else {
				bindingResult.rejectValue("oldPassword", "err.oldPass", "Old password is incorrect");
				return "changepassword";
			}
		} else {
			return "changepassword";
		}
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return "redirect:competitors/" + authentication.getName();
	}

	/**
	 * Create new user Check if user already exists & form validation
	 * 
	 * @param signupForm
	 * @param bindingResult
	 * @return
	 */
	@RequestMapping(value = "saveuser", method = RequestMethod.POST)
	public String save(@ModelAttribute("signupform") SignupForm signupForm, BindingResult bindingResult,
			HttpServletRequest request) throws UnsupportedEncodingException, MessagingException {
		if (!bindingResult.hasErrors()) { // validation errors
			if (signupForm.getPassword().equals(signupForm.getPasswordCheck())) { // check password match
				String pwd = signupForm.getPassword();
				BCryptPasswordEncoder bc = new BCryptPasswordEncoder();
				String hashPwd = bc.encode(pwd);

				String randomCode = RandomString.make(64);

				User newUser = new User();
				newUser.setPasswordHash(hashPwd);
				newUser.setUsername(signupForm.getUsername());
				newUser.setRole("USER");
				newUser.setFirstname(signupForm.getFirstname());
				newUser.setLastname(signupForm.getLastname());
				newUser.setIsOut(!signupForm.getIsCompetitor());
				newUser.setIsCompetitor(signupForm.getIsCompetitor());
				newUser.setStage(srepository.findByStage("No").get(0));
				newUser.setEmail(signupForm.getEmail());
				newUser.setAccountVerified(false);
				newUser.setVerificationCode(randomCode);

				if (repository.findByUsername(signupForm.getUsername()) == null) { // Check if user exists
					if (repository.findByEmail(signupForm.getEmail()) == null) { // check if email already exists
						repository.save(newUser);
						this.sendVerificationEmail(newUser);
					} else {
						bindingResult.rejectValue("email", "err.email", "Email is already in use");
						return "signup";
					}
				} else {
					bindingResult.rejectValue("username", "err.username", "Username already exists");
					return "signup";
				}
			} else {
				bindingResult.rejectValue("passwordCheck", "err.passCheck", "Passwords does not match");
				return "signup";
			}
		} else {
			return "signup";
		}
		return "redirect:/signupsuccess";
	}

	// Signup success page
	@RequestMapping("/signupsuccess")
	public String successPage() {
		return "successpage";
	}

	// User verification checking page
	@RequestMapping("/verify")
	public String verifyUser(@Param("code") String code) {
		if (this.verify(code)) {
			return "verifysuccess";
		} else {
			return "verifyfail";
		}
	}

	// save user functionality for admin
	@RequestMapping(value = "admin/saveuser", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('ADMIN')")
	public String saveAdminUser(@ModelAttribute("form") UserFormAdmin formUserAdmin, BindingResult bindingResult,
			HttpServletRequest request) throws UnsupportedEncodingException, MessagingException {
		if (!bindingResult.hasErrors()) { // validation errors
			if (formUserAdmin.getPassword().equals(formUserAdmin.getPasswordCheck())) { // check password match
				String pwd = formUserAdmin.getPassword();
				BCryptPasswordEncoder bc = new BCryptPasswordEncoder();
				String hashPwd = bc.encode(pwd);

				String randomCode = RandomString.make(64);

				User newUser = new User();
				newUser.setPasswordHash(hashPwd);
				newUser.setUsername(formUserAdmin.getUsername());
				newUser.setRole(formUserAdmin.getRole());
				newUser.setFirstname(formUserAdmin.getFirstname());
				newUser.setLastname(formUserAdmin.getLastname());
				newUser.setIsOut(!formUserAdmin.getIsCompetitor());
				newUser.setIsCompetitor(formUserAdmin.getIsCompetitor());
				newUser.setStage(srepository.findByStage("No").get(0));
				newUser.setEmail(formUserAdmin.getEmail());
				newUser.setAccountVerified(false);
				newUser.setVerificationCode(randomCode);

				if (repository.findByUsername(formUserAdmin.getUsername()) == null) { // Check if user exists
					if (repository.findByEmail(formUserAdmin.getEmail()) == null) { // check if email already exists
						repository.save(newUser);
						this.sendVerificationEmail(newUser);
					} else {
						bindingResult.rejectValue("email", "err.email", "Email is already in use");
						return "signup";
					}
				} else {
					bindingResult.rejectValue("username", "err.username", "Username already exists");
					return "adduser";
				}
			} else {
				bindingResult.rejectValue("passwordCheck", "err.passCheck", "Passwords does not match");
				return "adduser";
			}
		} else {
			return "adduser";
		}
		return "redirect:/competitors";
	}

	// edit user functionality for user
	@RequestMapping(value = "/edituser", method = RequestMethod.POST)
	@PreAuthorize("isAuthenticated()")
	public String editUser(@ModelAttribute("form") User user, BindingResult bindingResult) {
		if (!bindingResult.hasErrors()) { // validation errors
			user.setIsOut(!user.getIsCompetitor());
			repository.save(user);
		} else {
			return "edituser";
		}
		if (user.getUsername().equals(SecurityContextHolder.getContext().getAuthentication().getName())) {
			return "redirect:/competitors/" + user.getUsername();
		}
		return "redirect:logoutme";
	}

	// edit user functionality for admin
	@RequestMapping(value = "admin/edituser", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('ADMIN')")
	public String editAdminUser(@ModelAttribute("form") User user, BindingResult bindingResult) {
		if (!bindingResult.hasErrors()) { // validation errors
			user.setIsOut(!user.getIsCompetitor());
			repository.save(user);
		} else {
			return "edituser";
		}
		return "redirect:/competitors";
	}

	// logout functionality for me
	@RequestMapping(value = "/logoutme")
	@PreAuthorize("isAuthenticated()")
	public String logoutUser() {
		SecurityContextHolder.getContext().setAuthentication(null);
		return "redirect:/home";
	}

	// verification method
	private boolean verify(String verificationCode) {
		User user = repository.findByVerificationCode(verificationCode);

		if (user == null || user.isAccountVerified()) {
			return false;
		} else {
			user.setVerificationCode(null);
			user.setAccountVerified(true);
			if (rrepository.findAll().size() > 0) {
				user.setIsCompetitor(false);
				user.setIsOut(true);
			}
			repository.save(user);

			return true;
		}
	}

	// Email sending method for password reset
	private void sendPasswordEmail(User user, String password) throws MessagingException, UnsupportedEncodingException {
		String toAddress = user.getEmail();
		String fromAddress = "aleksei.application.noreply@gmail.com";
		String senderName = "No reply";
		String subject = "Reset password";
		String content = "Dear [[name]],<br>" + "Here is your new password:<br>" + "<h3>[[PASSWORD]]</h3>"
				+ "Thank you,<br>" + "AXOS inc.";

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
