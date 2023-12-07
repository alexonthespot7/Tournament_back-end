package com.myproject.tournamentapp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myproject.tournamentapp.forms.ChangePasswordForm;
import com.myproject.tournamentapp.forms.LoginForm;
import com.myproject.tournamentapp.forms.PersonalInfo;
import com.myproject.tournamentapp.model.Round;
import com.myproject.tournamentapp.model.RoundRepository;
import com.myproject.tournamentapp.model.Stage;
import com.myproject.tournamentapp.model.StageRepository;
import com.myproject.tournamentapp.model.User;
import com.myproject.tournamentapp.model.UserRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RestUserControllerTest {
	private static final String END_POINT_PATH = "/api";

	private String jwtToken;

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

	@BeforeEach
	public void loginAndRetrieveToken() throws Exception {
		String requestURI = END_POINT_PATH + "/login";
		Stage stageNo = this.resetStageUserAndRoundRepos();

		// creating 5 users: 1 admin, 1 unverified user, 3 verified competitors;
		User userAdmin = new User("admin", "$2a$12$0Mu/91y.kvDE7rj0ZXrWkOxUISfqEuQcXyU.luDJIe7DW2W/eqUYq", "ADMIN",
				true, false, stageNo, "admin.mail@test.com", true, null);

		User user1 = new User("user1", "$2a$12$0Mu/91y.kvDE7rj0ZXrWkOxUISfqEuQcXyU.luDJIe7DW2W/eqUYq", "USER", false,
				true, stageNo, "user1.mail@test.com", true, null);
		User user2 = new User("user2", "$2a$12$0Mu/91y.kvDE7rj0ZXrWkOxUISfqEuQcXyU.luDJIe7DW2W/eqUYq", "USER", false,
				true, stageNo, "user2.mail@test.com", true, null);
		User user3 = new User("user3", "$2a$12$0Mu/91y.kvDE7rj0ZXrWkOxUISfqEuQcXyU.luDJIe7DW2W/eqUYq", "USER", false,
				true, stageNo, "user3.mail@test.com", true, null);
		User user4 = new User("unverified", "$2a$12$0Mu/91y.kvDE7rj0ZXrWkOxUISfqEuQcXyU.luDJIe7DW2W/eqUYq", "USER",
				true, false, stageNo, "user4.mail@test.com", false, "example_code");

		urepository.save(userAdmin);
		urepository.save(user1);
		urepository.save(user2);
		urepository.save(user3);
		urepository.save(user4);

		// Create the login request body
		LoginForm loginForm = new LoginForm("user1", "asas2233");
		String requestBody = objectMapper.writeValueAsString(loginForm);

		// Perform the login request and retrieve the token
		MvcResult result = mockMvc
				.perform(post(requestURI).contentType(MediaType.APPLICATION_JSON).content(requestBody))
				.andExpect(status().isOk()).andReturn();

		// Retrieve the JWT token from the login response
		jwtToken = result.getResponse().getHeader("Authorization");
	}

	@Test
	@Order(1)
	public void testGetCompetitors() throws Exception {
		String requestURI = END_POINT_PATH + "/competitors";

		// good request case:
		// here I use the amount of hard-coded competitors to check the size of
		// competitors array
		mockMvc.perform(get(requestURI).header("Authorization", jwtToken)).andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(3));
	}

	@Test
	@Order(2)
	public void testGetPersonalInfoById() throws Exception {
		String requestURI = END_POINT_PATH + "/competitors/";

		User user1 = urepository.findByUsername("user1");
		Long user1Id = user1.getId();
		// wrong userid in path case:
		String requestURIWrongId = requestURI + user1Id + "1";
		mockMvc.perform(get(requestURIWrongId).header("Authorization", jwtToken)).andExpect(status().isForbidden());

		// good request case:
		String requestURIGood = requestURI + user1Id;
		mockMvc.perform(get(requestURIGood).header("Authorization", jwtToken)).andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.username").value("user1"));
	}

	// test the method to update participants status;
	@Test
	@Order(3)
	public void testUpdateUser() throws Exception {
		String requestURI = END_POINT_PATH + "/updateuser/";

		User user1 = urepository.findByUsername("user1");
		assertThat(user1.getIsCompetitor()).isTrue();

		Long user1Id = user1.getId();

		PersonalInfo personalInfoGood = new PersonalInfo(user1.getUsername(), user1.getEmail(), false,
				user1.getStage().getStage(), false, 0, null);
		String requestBodyGood = objectMapper.writeValueAsString(personalInfoGood);

		// wrong userid in path case:
		String requestURIWrongUserId = requestURI + user1Id + "1";

		mockMvc.perform(put(requestURIWrongUserId).header("Authorization", jwtToken)
				.contentType(MediaType.APPLICATION_JSON).content(requestBodyGood)).andExpect(status().isForbidden());

		// good request case:
		String requestURIGood = requestURI + user1Id;

		mockMvc.perform(put(requestURIGood).header("Authorization", jwtToken).contentType(MediaType.APPLICATION_JSON)
				.content(requestBodyGood)).andExpect(status().isOk());

		User userAfterUpdate = urepository.findByUsername("user1");
		assertThat(userAfterUpdate.getIsCompetitor()).isFalse();
	}

	@Test
	@Order(4)
	public void testGetPublicInfoOfAllRounds() throws Exception {
		String requestURI = END_POINT_PATH + "/rounds";

		// case, when the bracket wasn't made yet
		mockMvc.perform(get(requestURI).header("Authorization", jwtToken)).andExpect(status().isAccepted());

		// good case (first let's add some rounds to repo first):
		this.createBracketDraft();

		mockMvc.perform(get(requestURI).header("Authorization", jwtToken)).andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(2))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].result").value("No"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].stage").value("1/2"));

	}

	@Test
	@Order(5)
	public void testGetBracketInfo() throws Exception {
		String requestURI = END_POINT_PATH + "/bracket";

		// bracket wasn't made yet case:
		mockMvc.perform(get(requestURI).header("Authorization", jwtToken)).andExpect(status().isAccepted());

		
		
		// good case (first let's add some rounds to repo first):
		this.createBracketDraft();

		mockMvc.perform(get(requestURI).header("Authorization", jwtToken)).andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.winner").value(""))
				.andExpect(MockMvcResultMatchers.jsonPath("$.stages.size()").value(2))
				.andExpect(MockMvcResultMatchers.jsonPath("$.stages[0].stage").value("1/2"));

	}

	@Test
	@Order(6)
	public void testCahngePassword() throws Exception {
		String requestURI = END_POINT_PATH + "/changepassword";

		// wrong old password case:
		ChangePasswordForm changePasswordFormWrongPwd = new ChangePasswordForm("awaw2233", "awaw2233");
		String requestBodyWrongPwd = objectMapper.writeValueAsString(changePasswordFormWrongPwd);
		mockMvc.perform(put(requestURI).header("Authorization", jwtToken).contentType(MediaType.APPLICATION_JSON)
				.content(requestBodyWrongPwd)).andExpect(status().isForbidden());

		// good request case:
		// here I use the hard-coded user1 with intial password "asas2233";
		ChangePasswordForm changePasswordFormGood = new ChangePasswordForm("asas2233", "awaw2233");
		String requestBodyGood = objectMapper.writeValueAsString(changePasswordFormGood);
		mockMvc.perform(put(requestURI).header("Authorization", jwtToken).contentType(MediaType.APPLICATION_JSON)
				.content(requestBodyGood)).andExpect(status().isOk());

		// Create the login request body
		LoginForm loginForm = new LoginForm("user1", "awaw2233");
		String requestBody = objectMapper.writeValueAsString(loginForm);

		// Perform the login request and retrieve the token
		mockMvc.perform(post("/api/login").contentType(MediaType.APPLICATION_JSON).content(requestBody))
				.andExpect(status().isOk());
	}

	private Stage resetStageUserAndRoundRepos() {
		rrepository.deleteAll();
		urepository.deleteAll();
		srepository.deleteAll();
		List<Stage> stageNullNo = srepository.findByStage("No");
		assertThat(stageNullNo).hasSize(0);

		Stage stageNo = new Stage("No", true);
		srepository.save(stageNo);

		return stageNo;
	}
	
	private void createBracketDraft() {
		Stage stageSemiFinal = new Stage("1/2", true);
		Stage stageFinal = new Stage("final");
		Stage stageNo = srepository.findCurrentStage();
		stageNo.setIsCurrent(false);
		srepository.save(stageSemiFinal);
		srepository.save(stageFinal);
		srepository.save(stageNo);

		Round round1 = new Round("No", stageSemiFinal);
		Round round2 = new Round("No", stageSemiFinal);
		Round round3 = new Round("No", stageFinal);
		rrepository.save(round1);
		rrepository.save(round2);
		rrepository.save(round3);
	}

}
