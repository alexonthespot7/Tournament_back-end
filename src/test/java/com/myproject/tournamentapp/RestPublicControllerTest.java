package com.myproject.tournamentapp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myproject.tournamentapp.forms.EmailForm;
import com.myproject.tournamentapp.forms.LoginForm;
import com.myproject.tournamentapp.forms.SignupForm;
import com.myproject.tournamentapp.forms.VerificationCodeForm;
import com.myproject.tournamentapp.model.Stage;
import com.myproject.tournamentapp.model.StageRepository;
import com.myproject.tournamentapp.model.User;
import com.myproject.tournamentapp.model.UserRepository;
import com.myproject.tournamentapp.service.RoundService;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@AutoConfigureMockMvc
public class RestPublicControllerTest {
	private static final String END_POINT_PATH = "/api";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private RoundService roundService;

	@Autowired
	private UserRepository urepository;
	
	@Autowired
	private StageRepository srepository;

	@Test
	public void testGetRoundsQuantityMethod() throws Exception {
		String requestURI = END_POINT_PATH + "/roundsquantity";

		String roundQuantity = "0";

		when(roundService.getRoundsQuantity()).thenReturn(roundQuantity);

		mockMvc.perform(get(requestURI)).andExpect(status().isOk()).andExpect(content().string(roundQuantity));
	}

	@Test
	public void testLogin() throws Exception {
		String requestURI = END_POINT_PATH + "/login";

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
				.andExpect(header().exists("Host")).andExpect(header().string("Host", Matchers.equalTo("2")))
				.andExpect(header().exists("Origin")).andExpect(header().string("Origin", Matchers.equalTo("user1")))
				.andExpect(header().exists("Allow")).andExpect(header().string("Allow", Matchers.equalTo("USER")));
	}

	@Test
	public void testSignup() throws Exception {
		String requestURI = END_POINT_PATH + "/signup";

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
	public void testVerify() throws Exception {
		String requestURI = END_POINT_PATH + "/verify";

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
	public void testResetPassword() throws Exception {
		String requestURI = END_POINT_PATH + "/resetpassword";

		// User not found by email case
		EmailForm emailFormUserNotFound = new EmailForm("wrong@email.com");
		String requestBodyUserNotFound = objectMapper.writeValueAsString(emailFormUserNotFound);

		mockMvc.perform(put(requestURI).contentType(MediaType.APPLICATION_JSON).content(requestBodyUserNotFound))
				.andExpect(status().isBadRequest());

		// User is not verified case
		Stage stageNo = srepository.findCurrentStage();
		User unverifiedUser = new User("usero", "hashpwd", "USER", false, true, stageNo, "unverified@mail.com", false, "some_code_");
		urepository.save(unverifiedUser); 
		
		EmailForm emailFormUnverified = new EmailForm("unverified@mail.com");
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
