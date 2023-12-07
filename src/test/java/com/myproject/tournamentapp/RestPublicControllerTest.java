package com.myproject.tournamentapp;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myproject.tournamentapp.forms.EmailForm;
import com.myproject.tournamentapp.forms.LoginForm;
import com.myproject.tournamentapp.forms.SignupForm;
import com.myproject.tournamentapp.forms.VerificationCodeForm;
import com.myproject.tournamentapp.model.Round;
import com.myproject.tournamentapp.model.RoundRepository;
import com.myproject.tournamentapp.model.Stage;
import com.myproject.tournamentapp.model.StageRepository;
import com.myproject.tournamentapp.model.User;
import com.myproject.tournamentapp.model.UserRepository;

import jakarta.transaction.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.hamcrest.Matchers;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(Lifecycle.PER_CLASS)
@Transactional
public class RestPublicControllerTest {
	private static final String END_POINT_PATH = "/api";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserRepository urepository;

	@Autowired
	private StageRepository srepository;

	@Autowired
	private RoundRepository rrepository;

	@BeforeAll
	public void resetRepos() throws Exception {
		rrepository.deleteAll();
		urepository.deleteAll();
		srepository.deleteAll();

		List<Stage> allStages = srepository.findAll();
		assertThat(allStages).hasSize(0);

		List<Round> allRounds = rrepository.findAll();
		assertThat(allRounds).hasSize(0);

		List<User> allUsers = urepository.findAll();
		assertThat(allUsers).hasSize(0);

		Stage stageNo = new Stage("No", true);
		srepository.save(stageNo);
	}

	@Test
	@Rollback 
	public void testGetRoundsQuantityMethod() throws Exception {
		String requestURI = END_POINT_PATH + "/roundsquantity";

		Stage stageNo = srepository.findCurrentStage();

		mockMvc.perform(get(requestURI)).andExpect(status().isOk()).andExpect(content().string("0"));

		Round round1 = new Round("No", stageNo);
		Round round2 = new Round("No", stageNo);
		rrepository.save(round1);
		rrepository.save(round2);

		mockMvc.perform(get(requestURI)).andExpect(status().isOk()).andExpect(content().string("2"));
	}

	@Test
	@Rollback 
	public void testLogin() throws Exception {
		String requestURI = END_POINT_PATH + "/login";

		Stage stageNo = srepository.findCurrentStage();

		User user1 = new User("user1", "$2a$12$0Mu/91y.kvDE7rj0ZXrWkOxUISfqEuQcXyU.luDJIe7DW2W/eqUYq", "USER", false,
				true, stageNo, "user1.mail@test.com", true, null);
		urepository.save(user1);
		Long user1Id = user1.getId();

		User unverified = new User("unverified", "$2a$12$0Mu/91y.kvDE7rj0ZXrWkOxUISfqEuQcXyU.luDJIe7DW2W/eqUYq", "USER",
				false, true, stageNo, "user4.mail@test.com", false, "example_code");
		urepository.save(unverified);

		// wrong username case
		LoginForm loginFormWrongUsername = new LoginForm("messi", "asas2233");
		String requestBodyWrongUsername = objectMapper.writeValueAsString(loginFormWrongUsername);

		mockMvc.perform(post(requestURI).contentType(MediaType.APPLICATION_JSON).content(requestBodyWrongUsername))
				.andExpect(status().isBadRequest());

		// wrong password case
		LoginForm loginFormWrongPassword = new LoginForm("user1", "wrong");
		String requestBodyWrongPassword = objectMapper.writeValueAsString(loginFormWrongPassword);
		mockMvc.perform(post(requestURI).contentType(MediaType.APPLICATION_JSON).content(requestBodyWrongPassword))
				.andExpect(status().isUnauthorized());

		// unverified user case:
		LoginForm loginFormUnverified = new LoginForm("unverified", "asas2233");
		String requestBodyUnverified = objectMapper.writeValueAsString(loginFormUnverified);
		mockMvc.perform(post(requestURI).contentType(MediaType.APPLICATION_JSON).content(requestBodyUnverified))
				.andExpect(status().isConflict());

		// good request case:
		LoginForm loginFormGood = new LoginForm("user1", "asas2233");
		String requestBodyGood = objectMapper.writeValueAsString(loginFormGood);
		mockMvc.perform(post(requestURI).contentType(MediaType.APPLICATION_JSON).content(requestBodyGood))
				.andExpect(status().isOk()).andExpect(header().exists("Authorization"))
				.andExpect(header().string("Authorization", Matchers.containsString("Bearer")))
				.andExpect(header().exists("Host"))
				.andExpect(header().string("Host", Matchers.equalTo(user1Id.toString())))
				.andExpect(header().exists("Origin")).andExpect(header().string("Origin", Matchers.equalTo("user1")))
				.andExpect(header().exists("Allow")).andExpect(header().string("Allow", Matchers.equalTo("USER")));
	}

	@Test
	@Rollback
	public void testSignup() throws Exception {
		String requestURI = END_POINT_PATH + "/signup";

		Stage stageNo = srepository.findCurrentStage();

		User user1 = new User("user1", "$2a$12$0Mu/91y.kvDE7rj0ZXrWkOxUISfqEuQcXyU.luDJIe7DW2W/eqUYq", "USER", false,
				true, stageNo, "user1.mail@test.com", true, null);
		urepository.save(user1);

		// email is already in use case
		SignupForm signupFormEmailInUse = new SignupForm(true, "userNew", "asas2233", "user1.mail@test.com");

		String requestBodyEmailInUse = objectMapper.writeValueAsString(signupFormEmailInUse);
		mockMvc.perform(post(requestURI).contentType(MediaType.APPLICATION_JSON).content(requestBodyEmailInUse))
				.andExpect(status().isNotAcceptable());

		// Username is already in use case
		SignupForm signupFormUsernameInUse = new SignupForm(true, "user1", "asas2233", "new@mail.fi");

		String requestBodyUsernameInUse = objectMapper.writeValueAsString(signupFormUsernameInUse);
		mockMvc.perform(post(requestURI).contentType(MediaType.APPLICATION_JSON).content(requestBodyUsernameInUse))
				.andExpect(status().isConflict());

		// good request case:
		/**
		 * This piece of code is commented, as it automatically sends verification email
		 * everytime the test runs.
		 * 
		 * SignupForm signupFormGood = new SignupForm(true, "userNew", "asas2233",
		 * "aleksei.shevelenkov@gmail.com");
		 *
		 * String requestBodyGood = objectMapper.writeValueAsString(signupFormGood);
		 * mockMvc.perform(post(requestURI).contentType(MediaType.APPLICATION_JSON).content(requestBodyGood))
		 * .andExpect(status().isOk());
		 */
	}

	@Test
	@Rollback 
	public void testVerify() throws Exception {
		String requestURI = END_POINT_PATH + "/verify";

		Stage stageNo = srepository.findCurrentStage();

		User unverified = new User("unverified", "$2a$12$0Mu/91y.kvDE7rj0ZXrWkOxUISfqEuQcXyU.luDJIe7DW2W/eqUYq", "USER",
				false, true, stageNo, "user4.mail@test.com", false, "example_code");
		urepository.save(unverified);

		// User not found by verification code case
		VerificationCodeForm verificationFormUserNotFound = new VerificationCodeForm("wrong_code");
		String requestBodyUserNotFound = objectMapper.writeValueAsString(verificationFormUserNotFound);

		mockMvc.perform(put(requestURI).contentType(MediaType.APPLICATION_JSON).content(requestBodyUserNotFound))
				.andExpect(status().isConflict());

		// Good case
		VerificationCodeForm verificationFormGood = new VerificationCodeForm("example_code");
		String requestBodyUserGood = objectMapper.writeValueAsString(verificationFormGood);

		mockMvc.perform(put(requestURI).contentType(MediaType.APPLICATION_JSON).content(requestBodyUserGood))
				.andExpect(status().isOk());
	}

	@Test
	@Rollback 
	public void testResetPassword() throws Exception {
		String requestURI = END_POINT_PATH + "/resetpassword";

		Stage stageNo = srepository.findCurrentStage();

		User user1 = new User("user1", "$2a$12$0Mu/91y.kvDE7rj0ZXrWkOxUISfqEuQcXyU.luDJIe7DW2W/eqUYq", "USER", false,
				true, stageNo, "user1.mail@test.com", true, null);
		urepository.save(user1);

		User unverified = new User("unverified", "$2a$12$0Mu/91y.kvDE7rj0ZXrWkOxUISfqEuQcXyU.luDJIe7DW2W/eqUYq", "USER",
				false, true, stageNo, "user4.mail@test.com", false, "example_code");
		urepository.save(unverified);

		// User not found by email case
		EmailForm emailFormUserNotFound = new EmailForm("wrong@email.com");
		String requestBodyUserNotFound = objectMapper.writeValueAsString(emailFormUserNotFound);

		mockMvc.perform(put(requestURI).contentType(MediaType.APPLICATION_JSON).content(requestBodyUserNotFound))
				.andExpect(status().isBadRequest());

		// User is not verified case
		EmailForm emailFormUnverified = new EmailForm("user4.mail@test.com");
		String requestBodyUnverified = objectMapper.writeValueAsString(emailFormUnverified);

		mockMvc.perform(put(requestURI).contentType(MediaType.APPLICATION_JSON).content(requestBodyUnverified))
				.andExpect(status().isConflict());

		// Good case
		/**
		 * EmailForm emaiLFormGood = new EmailForm("user1.mail@test.com"); String
		 * requestBodyGood = objectMapper.writeValueAsString(emaiLFormGood);
		 * 
		 * mockMvc.perform(put(requestURI).contentType(MediaType.APPLICATION_JSON).content(requestBodyGood))
		 * .andExpect(status().isOk());
		 */
	}
}
