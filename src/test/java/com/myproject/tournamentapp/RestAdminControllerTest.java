package com.myproject.tournamentapp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myproject.tournamentapp.forms.AddUserFormForAdmin;
import com.myproject.tournamentapp.forms.LoginForm;
import com.myproject.tournamentapp.model.Round;
import com.myproject.tournamentapp.model.RoundRepository;
import com.myproject.tournamentapp.model.Stage;
import com.myproject.tournamentapp.model.StageRepository;
import com.myproject.tournamentapp.model.User;
import com.myproject.tournamentapp.model.UserRepository;

import jakarta.transaction.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
public class RestAdminControllerTest {
	private static final String END_POINT_PATH = "/api/admin";
	private static final String MAKE_BRACKET_END_POINT = END_POINT_PATH + "/makebracket";
	private static final String RESET_END_POINT = END_POINT_PATH + "/reset";
	private static final String DELETE_END_POINT = END_POINT_PATH + "/deleteuser/";

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

	// authenticate as admin
	@BeforeAll
	public void loginAndRetrieveToken() throws Exception {
		String requestURI = "/api/login";

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
		LoginForm loginForm = new LoginForm("admin", "asas2233");
		String requestBody = objectMapper.writeValueAsString(loginForm);

		// Perform the login request and retrieve the token
		MvcResult result = mockMvc
				.perform(post(requestURI).contentType(MediaType.APPLICATION_JSON).content(requestBody))
				.andExpect(status().isOk()).andReturn();

		// Retrieve the JWT token from the login response
		jwtToken = result.getResponse().getHeader("Authorization");
	}

	@Test
	@Rollback
	public void testMakeAllCompetitorsAllVerifiedAreCompetitorsCase() throws Exception {
		String requestURI = END_POINT_PATH + "/makeallcompetitors";

		// All verified users are already competitors case:
		mockMvc.perform(put(requestURI).header("Authorization", jwtToken)).andExpect(status().isNotAcceptable());
	}

	@Test
	@Rollback
	public void testGetUsersForAdminAllCases() throws Exception {
		String requestURI = END_POINT_PATH + "/users";

		// good case:
		mockMvc.perform(get(requestURI).header("Authorization", jwtToken)).andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.users.size()").value(5))
				.andExpect(MockMvcResultMatchers.jsonPath("$.bracketMade").value(false));
	}

	@Test
	@Rollback
	public void testGetStagesForAdminAllCases() throws Exception {
		String requestURI = END_POINT_PATH + "/stages";

		// good case:
		mockMvc.perform(get(requestURI).header("Authorization", jwtToken)).andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(1))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].stage").value("No"));

		Stage stageSemiFinal = new Stage("1/2");
		srepository.save(stageSemiFinal);

		// Good case 2:
		mockMvc.perform(get(requestURI).header("Authorization", jwtToken)).andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(2))
				.andExpect(MockMvcResultMatchers.jsonPath("$[1].stage").value("1/2"));
	}

	@Test
	@Rollback
	public void testGetRoundsBracketNotMadeCase() throws Exception {
		String requestURI = END_POINT_PATH + "/rounds";
		// bracket wasn't made yet case:
		mockMvc.perform(get(requestURI).header("Authorization", jwtToken)).andExpect(status().isAccepted());
	}

	@Test
	@Rollback
	public void testMakeBracketGoodCase() throws Exception {
		String requestURI = END_POINT_PATH + "/makebracket";

		mockMvc.perform(put(requestURI).header("Authorization", jwtToken)).andExpect(status().isOk());

		Stage currentStage = srepository.findCurrentStage();
		assertThat(currentStage.getStage()).isEqualTo("1/2");

		List<Round> allRounds = rrepository.findAll();
		assertThat(allRounds).hasSize(3);

		Round semiFinalRound1 = allRounds.get(0);
		Round semiFinalRound2 = allRounds.get(1);
		Round finalRound = allRounds.get(2);
		assertThat(semiFinalRound1.getStage().getStage()).isEqualTo("1/2");
		assertThat(semiFinalRound2.getStage().getStage()).isEqualTo("1/2");
		assertThat(finalRound.getStage().getStage()).isEqualTo("final");

		List<User> allCompetitors = urepository.findAllCompetitors();
		assertThat(allCompetitors).hasSize(3);

		for (User competitor : allCompetitors) {
			assertThat(competitor.getStage().getStage()).isEqualTo("1/2");
			assertThat(competitor.getIsOut()).isFalse();
		}
	}

	@Test
	@Rollback
	public void testMakeBracketAlreadyMadeCase() throws Exception {
		String requestURI = END_POINT_PATH + "/makebracket";

		// Making bracket first:
		mockMvc.perform(put(requestURI).header("Authorization", jwtToken)).andExpect(status().isOk());

		// Case bracket was already made:
		mockMvc.perform(put(requestURI).header("Authorization", jwtToken)).andExpect(status().isNotAcceptable());
	}

	@Test
	@Rollback
	public void testMakeAllCompetitorsBracketMadeCase() throws Exception {
		String requestURI = END_POINT_PATH + "/makeallcompetitors";
		this.verifyUser4();

		// Making bracket first:
		mockMvc.perform(put(MAKE_BRACKET_END_POINT).header("Authorization", jwtToken)).andExpect(status().isOk());

		// Trying to makeAll competitors when the bracket was already made case
		mockMvc.perform(put(requestURI).header("Authorization", jwtToken)).andExpect(status().isNotAcceptable());
	}

	@Test
	@Rollback
	public void testGetRoundsGoodCase() throws Exception {
		String requestURI = END_POINT_PATH + "/rounds";

		// Making bracket first:
		mockMvc.perform(put(MAKE_BRACKET_END_POINT).header("Authorization", jwtToken)).andExpect(status().isOk());

		mockMvc.perform(get(requestURI).header("Authorization", jwtToken)).andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.allRounds.size()").value(3))
				.andExpect(MockMvcResultMatchers.jsonPath("$.allRounds[2].stage.stage").value("final"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.doesWinnerExist").value(false));
	}

	@Test
	@Rollback
	public void testSetResultNotAllPlayedCase() throws Exception {
		String requestURI = END_POINT_PATH + "/confirmresults";

		// Making bracket first:
		mockMvc.perform(put(MAKE_BRACKET_END_POINT).header("Authorization", jwtToken)).andExpect(status().isOk());

		// Trying to confirm stage results when not all of the rounds are played case:
		mockMvc.perform(put(requestURI).header("Authorization", jwtToken)).andExpect(status().isNotAcceptable());
	}

	@Test
	@Rollback
	public void testSetResultGoodCase() throws Exception {

		// Making bracket first:
		mockMvc.perform(put(MAKE_BRACKET_END_POINT).header("Authorization", jwtToken)).andExpect(status().isOk());

		this.setResultForSemiFinalRound1();

		List<Round> allRounds = rrepository.findAll();
		Round updatedRound = allRounds.get(0);
		assertThat(updatedRound.getResult()).isNotEqualTo("No");

		List<User> allCompetitors = urepository.findAllCompetitors();
		boolean allCompetitorsIn = true;
		for (User competitor : allCompetitors) {
			if (competitor.getIsOut()) {
				allCompetitorsIn = false;
				break;
			}
		}
		assertThat(allCompetitorsIn).isFalse();
	}

	@Test
	@Rollback
	public void testSetResultNotCurrentStageCase() throws Exception {
		String requestURI = END_POINT_PATH + "/setresult/";

		// Making bracket first: (current stage is 1/2)
		mockMvc.perform(put(MAKE_BRACKET_END_POINT).header("Authorization", jwtToken)).andExpect(status().isOk());

		// trying to set result for round out of current stage case:
		Round finalRound = rrepository.findFinal();
		assertThat(finalRound.getStage().getIsCurrent()).isFalse();

		Long finalRoundRoundId = finalRound.getRoundid();

		String requestURINotInCurrentStage = requestURI + finalRoundRoundId;

		finalRound.setResult("someone win");
		String requestBodyNotInCurrentStage = objectMapper.writeValueAsString(finalRound);

		mockMvc.perform(put(requestURINotInCurrentStage).header("Authorization", jwtToken)
				.contentType(MediaType.APPLICATION_JSON).content(requestBodyNotInCurrentStage))
				.andExpect(status().isNotAcceptable());
	}

	@Test
	@Rollback
	public void testSetResultIdMissmatchCase() throws Exception {
		String requestURI = END_POINT_PATH + "/setresult/";

		// Making bracket first:
		mockMvc.perform(put(MAKE_BRACKET_END_POINT).header("Authorization", jwtToken)).andExpect(status().isOk());

		List<Round> allRounds = rrepository.findAll();
		Round roundToSetResult = allRounds.get(0);
		User roundWinner = roundToSetResult.getUser1();
		roundToSetResult.setResult(roundWinner.getUsername() + " win");

		String requestBody = objectMapper.writeValueAsString(roundToSetResult);
		String requestURImissmatch = requestURI + "11";

		mockMvc.perform(put(requestURImissmatch).header("Authorization", jwtToken)
				.contentType(MediaType.APPLICATION_JSON).content(requestBody)).andExpect(status().isBadRequest());
	}

	@Test
	@Rollback
	public void testSetResultAutoHandledRoundCase() throws Exception {
		String requestURI = END_POINT_PATH + "/setresult/";

		// Making bracket first:
		mockMvc.perform(put(MAKE_BRACKET_END_POINT).header("Authorization", jwtToken)).andExpect(status().isOk());

		List<Round> allRounds = rrepository.findAll();
		Round roundToSetResult = allRounds.get(1);
		assertThat(roundToSetResult.getResult()).isNotEqualTo("No");

		User roundWinner = roundToSetResult.getUser1();
		roundToSetResult.setResult(roundWinner.getUsername() + " win");

		String finalRequestUri = requestURI + roundToSetResult.getRoundid();

		String requestBody = objectMapper.writeValueAsString(roundToSetResult);

		mockMvc.perform(put(finalRequestUri).header("Authorization", jwtToken).contentType(MediaType.APPLICATION_JSON)
				.content(requestBody)).andExpect(status().isConflict());
	}

	@Test
	@Rollback
	public void testConfirmResultsGoodCases() throws Exception {
		String requestURI = END_POINT_PATH + "/confirmresults";

		// Making bracket first:
		mockMvc.perform(put(MAKE_BRACKET_END_POINT).header("Authorization", jwtToken)).andExpect(status().isOk());

		this.setResultForSemiFinalRound1();

		// Good case of confirming 1/2 stage:
		mockMvc.perform(put(requestURI).header("Authorization", jwtToken)).andExpect(status().isOk())
				.andExpect(content().string("The current stage results were successfully confirmed"));

		Stage currentStage = srepository.findCurrentStage();
		assertThat(currentStage.getStage()).isEqualTo("final");

		Stage semiFinal = srepository.findByStage("1/2").get(0);
		assertThat(semiFinal.getIsCurrent()).isFalse();

		// Good case of confirming final stage: (let's first set the final round result)
		Round finalRound = rrepository.findFinal();
		User finalWinner = finalRound.getUser1();
		finalRound.setResult(finalWinner + " win");
		Long finalRoundId = finalRound.getRoundid();

		String requestBody = objectMapper.writeValueAsString(finalRound);
		String setResultRequestURI = END_POINT_PATH + "/setresult/" + finalRoundId;

		mockMvc.perform(put(setResultRequestURI).header("Authorization", jwtToken)
				.contentType(MediaType.APPLICATION_JSON).content(requestBody)).andExpect(status().isOk());

		mockMvc.perform(put(requestURI).header("Authorization", jwtToken)).andExpect(status().isOk())
				.andExpect(content().string("The final stage results were successfully confirmed"));

		currentStage = srepository.findCurrentStage();
		assertThat(currentStage.getStage()).isEqualTo("No");
	}

	@Test
	@Rollback
	public void testDeleteUserTryingToDeleteCompetitorAndBracketMadeCase() throws Exception {
		String requestURI = END_POINT_PATH + "/deleteuser/";

		// Making bracket first:
		mockMvc.perform(put(MAKE_BRACKET_END_POINT).header("Authorization", jwtToken)).andExpect(status().isOk());

		// Trying to delete competitor when bracket was already made:
		User competitor1 = urepository.findByUsername("user1");
		Long competitor1Id = competitor1.getId();

		String requestURICompetitor = requestURI + competitor1Id;

		mockMvc.perform(delete(requestURICompetitor).header("Authorization", jwtToken))
				.andExpect(status().isConflict());
	}

	@Test
	@Rollback
	public void testResetAllCases() throws Exception {
		String requestURI = END_POINT_PATH + "/reset";

		// Making bracket first:
		mockMvc.perform(put(MAKE_BRACKET_END_POINT).header("Authorization", jwtToken)).andExpect(status().isOk());

		// Good case :
		mockMvc.perform(put(requestURI).header("Authorization", jwtToken)).andExpect(status().isOk());

		List<Round> allRounds = rrepository.findAll();
		assertThat(allRounds).hasSize(0);

		List<User> allUsers = urepository.findAll();
		assertThat(allUsers).hasSize(4);

		List<User> allCompetitors = urepository.findAllCompetitors();
		assertThat(allCompetitors).hasSize(0);

		List<Stage> allStages = srepository.findAll();
		assertThat(allStages).hasSize(1);

		// Nothing to reset case:
		mockMvc.perform(put(requestURI).header("Authorization", jwtToken)).andExpect(status().isConflict());
	}

	@Test
	@Rollback
	public void testMakeAllCompetitorsGoodCase() throws Exception {
		String requestURI = END_POINT_PATH + "/makeallcompetitors";
		// Making bracket first:
		mockMvc.perform(put(MAKE_BRACKET_END_POINT).header("Authorization", jwtToken)).andExpect(status().isOk());

		// Then resetting everything:
		mockMvc.perform(put(RESET_END_POINT).header("Authorization", jwtToken)).andExpect(status().isOk());

		List<User> allCompetitors = urepository.findAllCompetitors();
		assertThat(allCompetitors).hasSize(0);

		// Good case:
		mockMvc.perform(put(requestURI).header("Authorization", jwtToken)).andExpect(status().isOk());

		List<User> allUsers = urepository.findAll();
		assertThat(allUsers).hasSize(4);

		allCompetitors = urepository.findAllCompetitors();
		assertThat(allCompetitors).hasSize(3);
	}

	@Test
	@Rollback
	public void testDeleteUserNotFoundCase() throws Exception {
		String requestURI = END_POINT_PATH + "/deleteuser/";

		// User not found by id case:
		String requestURINotFound = requestURI + "11";

		mockMvc.perform(delete(requestURINotFound).header("Authorization", jwtToken))
				.andExpect(status().isBadRequest());
	}

	@Test
	@Rollback
	public void testDeleteUserTryingToDeleteAdminCase() throws Exception {
		String requestURI = END_POINT_PATH + "/deleteuser/";

		User admin = urepository.findByUsername("admin");
		Long adminId = admin.getId();

		// Trying to delete ADMIN case:
		String requestURIAdmin = requestURI + adminId;

		mockMvc.perform(delete(requestURIAdmin).header("Authorization", jwtToken)).andExpect(status().isConflict());
	}

	@Test
	@Rollback
	public void testDeleteUserGoodCase() throws Exception {
		String requestURI = END_POINT_PATH + "/deleteuser/";

		User userToDelete = urepository.findByUsername("user1");
		assertThat(userToDelete).isNotNull();

		// Good case:
		Long userToDeleteId = userToDelete.getId();
		String requestURIToDelete = requestURI + userToDeleteId;
		mockMvc.perform(delete(requestURIToDelete).header("Authorization", jwtToken)).andExpect(status().isOk());

		User deletedUser = urepository.findByUsername("user1");
		assertThat(deletedUser).isNull();
	}

	@Test
	@Rollback
	public void testMakeBracketLessThan3CompetitorsCase() throws Exception {
		String requestURI = END_POINT_PATH + "/makebracket";

		// Let's delete one user first, so there will be only 2 competitors;
		User userToDelete = urepository.findByUsername("user1");
		Long userToDeleteId = userToDelete.getId();
		String requestURIToDelete = DELETE_END_POINT + userToDeleteId;
		mockMvc.perform(delete(requestURIToDelete).header("Authorization", jwtToken)).andExpect(status().isOk());

		// Trying to makeBracket with less than 3 competitors case:
		mockMvc.perform(put(requestURI).header("Authorization", jwtToken)).andExpect(status().isNotAcceptable());
	}

	@Test
	@Rollback
	public void testUpdateUserByAdminAllCases() throws Exception {
		String requestURI = END_POINT_PATH + "/updateuser/";

		// fetching user to update and send to server:
		User user2 = urepository.findByUsername("user2");
		assertThat(user2.getEmail()).isNotEqualTo("new@mail.com");
		assertThat(user2.getRole()).isNotEqualTo("ADMIN");

		user2.setEmail("new@mail.com");
		user2.setRole("ADMIN");
		String requestBody = objectMapper.writeValueAsString(user2);

		// User not found by id case:
		String requestURINotFound = requestURI + "11";

		mockMvc.perform(put(requestURINotFound).header("Authorization", jwtToken)
				.contentType(MediaType.APPLICATION_JSON).content(requestBody)).andExpect(status().isBadRequest());

		// User id in path and in request body missmatch case:
		User wrongUser = urepository.findByUsername("user1");
		Long wrongId = wrongUser.getId();
		String requestURIIdMissmatch = requestURI + wrongId;

		mockMvc.perform(put(requestURIIdMissmatch).header("Authorization", jwtToken)
				.contentType(MediaType.APPLICATION_JSON).content(requestBody)).andExpect(status().isConflict());

		// Good case:
		Long user2Id = user2.getId();
		String requestURIGood = requestURI + user2Id;

		mockMvc.perform(put(requestURIGood).header("Authorization", jwtToken).contentType(MediaType.APPLICATION_JSON)
				.content(requestBody)).andExpect(status().isOk());

		User updatedUser = urepository.findByUsername("user2");
		assertThat(updatedUser.getEmail()).isEqualTo("new@mail.com");
		assertThat(updatedUser.getRole()).isEqualTo("ADMIN");
	}

	@Test
	@Rollback
	public void testAddNewUserByAdminAllCases() throws Exception {
		String requestURI = END_POINT_PATH + "/adduser";

		// email is already in use case
		AddUserFormForAdmin userFormEmailInUse = new AddUserFormForAdmin(true, "userNew", "asas2233",
				"user3.mail@test.com", "USER", true);
		String requestBodyEmailInUse = objectMapper.writeValueAsString(userFormEmailInUse);

		mockMvc.perform(post(requestURI).header("Authorization", jwtToken).contentType(MediaType.APPLICATION_JSON)
				.content(requestBodyEmailInUse)).andExpect(status().isNotAcceptable());

		// Username is already in use case
		AddUserFormForAdmin userFormUsernameInUse = new AddUserFormForAdmin(true, "user2", "asas2233", "new@mail.fi",
				"USER", true);
		String requestBodyUsernameInUse = objectMapper.writeValueAsString(userFormUsernameInUse);

		mockMvc.perform(post(requestURI).header("Authorization", jwtToken).contentType(MediaType.APPLICATION_JSON)
				.content(requestBodyUsernameInUse)).andExpect(status().isConflict());

		// Good case verified user:
		AddUserFormForAdmin userFormVerifiedGood = new AddUserFormForAdmin(true, "userNew", "asas2233",
				"aleksei.shevelenkov@gmail.com", "USER", true);
		String requestBodyGood = objectMapper.writeValueAsString(userFormVerifiedGood);

		mockMvc.perform(post(requestURI).header("Authorization", jwtToken).contentType(MediaType.APPLICATION_JSON)
				.content(requestBodyGood)).andExpect(status().isOk());

		// good request case unverified user:
		/**
		 * This piece of code is commented, as it automatically sends verification email
		 * everytime the test runs.
		 * 
		 * AddUserFormForAdmin userFormUnverifiedGood = new AddUserFormForAdmin(true,
		 * "userNew", "asas2233", "aleksei.shevelenkov@gmail.com", "USER", false);
		 * String requestBodyUnverifiedGood =
		 * objectMapper.writeValueAsString(userFormUnverifiedGood);
		 *
		 * mockMvc.perform(post(requestURI).header("Authorization",
		 * jwtToken).contentType(MediaType.APPLICATION_JSON).content(requestBodyUnverifiedGood))
		 * .andExpect(status().isOk());
		 */
	}

	private Stage resetStageUserAndRoundRepos() {
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

		return stageNo;
	}

	private void verifyUser4() {
		User user4 = urepository.findByUsername("unverified");
		user4.setAccountVerified(true);
		urepository.save(user4);
	}

	private void setResultForSemiFinalRound1() throws Exception {
		String requestURI = END_POINT_PATH + "/setresult/";

		List<Round> allRounds = rrepository.findAll();
		Round roundToSetResult = allRounds.get(0);
		User roundWinner = roundToSetResult.getUser1();
		roundToSetResult.setResult(roundWinner.getUsername() + " win");

		String finalRequestUri = requestURI + roundToSetResult.getRoundid();

		String requestBody = objectMapper.writeValueAsString(roundToSetResult);

		mockMvc.perform(put(finalRequestUri).header("Authorization", jwtToken).contentType(MediaType.APPLICATION_JSON)
				.content(requestBody)).andExpect(status().isOk());
	}

}
