package com.myproject.tournamentapp;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.myproject.tournamentapp.model.Stage;
import com.myproject.tournamentapp.model.StageRepository;
import com.myproject.tournamentapp.model.User;
import com.myproject.tournamentapp.model.UserRepository;

@ExtendWith(SpringExtension.class)
@DataJpaTest
public class UserRepositoryTests {
	@Autowired
	private UserRepository urepository;

	@Autowired
	private StageRepository srepository;

	// test Create for User repo:
	@Test
	public void testUserCreation() {
		Stage stageNo = this.resetStageAndUserRepos();

		User user = new User("usersdso", "$2a$06$3jYRJrg0ghaaypjZ/.g4SethoeA51ph3UD4kZi9oPkeMTpjKU5uo6", "USER", true,
				false, stageNo, "test1@gmail.com", true, null);
		urepository.save(user);
		assertThat(user.getId()).isNotNull();

		List<User> users = urepository.findAll();
		assertThat(users).hasSize(1);
	}

	// Test find by username
	@Test
	public void testFindByUsername() {
		Stage stageNo = this.resetStageAndUserRepos();

		String username = "usero";

		User userNotFound = urepository.findByUsername(username);
		assertThat(userNotFound).isNull();

		User user = new User(username, "$2a$06$3jYRJrg0ghaaypjZ/.g4SethoeA51ph3UD4kZi9oPkeMTpjKU5uo6", "USER", true,
				false, stageNo, "test1@gmail.com", true, null);
		urepository.save(user);

		User userFound = urepository.findByUsername(username);
		assertThat(userFound).isNotNull();
	}

	// Test findAllAdmins functionality:
	@Test
	public void testFindAllAdmins() {
		Stage stageNo = this.resetStageAndUserRepos();

		List<User> allAdminsEmpty = urepository.findAllAdmins();
		assertThat(allAdminsEmpty).hasSize(0);

		User newAdmin = new User("admino", "$2a$06$3jYRJrg0ghaaypjZ/.g4SethoeA51ph3UD4kZi9oPkeMTpjKU5uo6", "ADMIN",
				true, false, stageNo, "test1@gmail.com", true, null);
		urepository.save(newAdmin);

		List<User> allAdminsNotEmpty = urepository.findAllAdmins();
		assertThat(allAdminsNotEmpty).hasSize(1);
	}

	// Test findAllVerifiedUsers:
	@Test
	public void testFindAllVerifiedUsers() {
		Stage stageNo = this.resetStageAndUserRepos();

		List<User> allVerifiedUsersEmpty = urepository.findAllVerifiedUsers();
		assertThat(allVerifiedUsersEmpty).hasSize(0);

		User newAdmin = new User("admino", "$2a$06$3jYRJrg0ghaaypjZ/.g4SethoeA51ph3UD4kZi9oPkeMTpjKU5uo6", "ADMIN",
				true, false, stageNo, "test1@gmail.com", true, null);
		User user = new User("usero1", "$2a$06$3jYRJrg0ghaaypjZ/.g4SethoeA51ph3UD4kZi9oPkeMTpjKU5uo6", "USER", true,
				false, stageNo, "test2@gmail.com", true, null);
		User userUnverified = new User("usero2", "$2a$06$3jYRJrg0ghaaypjZ/.g4SethoeA51ph3UD4kZi9oPkeMTpjKU5uo6", "USER",
				true, false, stageNo, "test3@gmail.com", false, "SomeVerificationCode");
		urepository.save(newAdmin);
		urepository.save(user);
		urepository.save(userUnverified);

		List<User> allVerifiedUsersNotEmpty = urepository.findAllVerifiedUsers();
		assertThat(allVerifiedUsersNotEmpty).hasSize(1);
	}

	// Test findAllCompetitors:
	@Test
	public void testFindAllCompetitors() {
		Stage stageNo = this.resetStageAndUserRepos();

		List<User> allCompetitorsEmpty = urepository.findAllCompetitors();
		assertThat(allCompetitorsEmpty).hasSize(0);

		User newAdmin = new User("admino", "$2a$06$3jYRJrg0ghaaypjZ/.g4SethoeA51ph3UD4kZi9oPkeMTpjKU5uo6", "ADMIN",
				true, false, stageNo, "test1@gmail.com", true, null);
		User user = new User("usero1", "$2a$06$3jYRJrg0ghaaypjZ/.g4SethoeA51ph3UD4kZi9oPkeMTpjKU5uo6", "USER", true,
				false, stageNo, "test2@gmail.com", true, null);
		User userUnverifiedCompetitor = new User("usero2",
				"$2a$06$3jYRJrg0ghaaypjZ/.g4SethoeA51ph3UD4kZi9oPkeMTpjKU5uo6", "USER", false, true, stageNo,
				"test3@gmail.com", false, "SomeVerificationCode");
		User competitor = new User("usero3", "$2a$06$3jYRJrg0ghaaypjZ/.g4SethoeA51ph3UD4kZi9oPkeMTpjKU5uo6", "USER",
				false, true, stageNo, "test4@gmail.com", true, null);
		urepository.save(newAdmin);
		urepository.save(user);
		urepository.save(userUnverifiedCompetitor);
		urepository.save(competitor);

		List<User> allCompetitors = urepository.findAllCompetitors();
		assertThat(allCompetitors).hasSize(1);
	}

	// test findAll functionality:
	@Test
	public void testFindAll() {
		Stage stageNo = this.resetStageAndUserRepos();

		List<User> allUsersEmpty = urepository.findAll();
		assertThat(allUsersEmpty).hasSize(0);

		User newAdmin = new User("admino", "$2a$06$3jYRJrg0ghaaypjZ/.g4SethoeA51ph3UD4kZi9oPkeMTpjKU5uo6", "ADMIN",
				true, false, stageNo, "test1@gmail.com", true, null);
		User user = new User("usero1", "$2a$06$3jYRJrg0ghaaypjZ/.g4SethoeA51ph3UD4kZi9oPkeMTpjKU5uo6", "USER", true,
				false, stageNo, "test2@gmail.com", true, null);
		User userUnverifiedCompetitor = new User("usero2",
				"$2a$06$3jYRJrg0ghaaypjZ/.g4SethoeA51ph3UD4kZi9oPkeMTpjKU5uo6", "USER", false, true, stageNo,
				"test3@gmail.com", false, "SomeVerificationCode");
		User competitor = new User("usero3", "$2a$06$3jYRJrg0ghaaypjZ/.g4SethoeA51ph3UD4kZi9oPkeMTpjKU5uo6", "USER",
				false, true, stageNo, "test4@gmail.com", true, null);
		urepository.save(newAdmin);
		urepository.save(user);
		urepository.save(userUnverifiedCompetitor);
		urepository.save(competitor);

		List<User> allUsers = urepository.findAll();
		assertThat(allUsers).hasSize(4);
	}

	// test findAllCurrentCompetitors
	@Test
	public void testFindAllCurrentCompetitors() {
		Stage stageNo = this.resetStageAndUserRepos();

		List<User> allCurrentCompetitorsEmpty = urepository.findAllCurrentCompetitors();
		assertThat(allCurrentCompetitorsEmpty).hasSize(0);

		User newAdmin = new User("admino", "$2a$06$3jYRJrg0ghaaypjZ/.g4SethoeA51ph3UD4kZi9oPkeMTpjKU5uo6", "ADMIN",
				true, false, stageNo, "test1@gmail.com", true, null);
		User user = new User("usero1", "$2a$06$3jYRJrg0ghaaypjZ/.g4SethoeA51ph3UD4kZi9oPkeMTpjKU5uo6", "USER", true,
				false, stageNo, "test2@gmail.com", true, null);
		User userUnverifiedCompetitor = new User("usero2",
				"$2a$06$3jYRJrg0ghaaypjZ/.g4SethoeA51ph3UD4kZi9oPkeMTpjKU5uo6", "USER", false, true, stageNo,
				"test3@gmail.com", false, "SomeVerificationCode");
		User competitorIn = new User("usero3", "$2a$06$3jYRJrg0ghaaypjZ/.g4SethoeA51ph3UD4kZi9oPkeMTpjKU5uo6", "USER",
				false, true, stageNo, "test4@gmail.com", true, null);
		User competitorOut = new User("usero4", "$2a$06$3jYRJrg0ghaaypjZ/.g4SethoeA51ph3UD4kZi9oPkeMTpjKU5uo6", "USER",
				true, true, stageNo, "test5@gmail.com", true, null);
		urepository.save(newAdmin);
		urepository.save(user);
		urepository.save(userUnverifiedCompetitor);
		urepository.save(competitorIn);
		urepository.save(competitorOut);

		List<User> allCurrentCompetitors = urepository.findAllCurrentCompetitors();
		assertThat(allCurrentCompetitors).hasSize(1);
	}

	// Test findByVerificationCode:
	@Test
	public void testFindByVerificationCode() {
		Stage stageNo = this.resetStageAndUserRepos();

		String verificationCode = "TestCode";

		User userNull = urepository.findByVerificationCode(verificationCode);
		assertThat(userNull).isNull();

		User user = new User("usero1", "$2a$06$3jYRJrg0ghaaypjZ/.g4SethoeA51ph3UD4kZi9oPkeMTpjKU5uo6", "USER", true,
				false, stageNo, "test2@gmail.com", false, verificationCode);
		urepository.save(user);

		User userNotNull = urepository.findByVerificationCode(verificationCode);
		assertThat(userNotNull).isNotNull();
	}

	// Test findByEmail:
	@Test
	public void testFindByEmail() {
		Stage stageNo = this.resetStageAndUserRepos();

		String email = "test@mail.com";
		User userNull = urepository.findByEmail(email);
		assertThat(userNull).isNull();

		User user = new User("usero1", "$2a$06$3jYRJrg0ghaaypjZ/.g4SethoeA51ph3UD4kZi9oPkeMTpjKU5uo6", "USER", true,
				false, stageNo, email, true, null);
		urepository.save(user);

		User userNotNull = urepository.findByEmail(email);
		assertThat(userNotNull).isNotNull();
	}

	// Test update user functionality:
	@Test
	public void testUpdateUser() {
		Stage stageNo = this.resetStageAndUserRepos();

		String username = "usero";
		String email = "mail@test.com";
		boolean isCompetitor = true;
		boolean accountVerified = false;
		String verificationCode = "MaMa";

		User newUser = new User(username, "$2a$06$3jYRJrg0ghaaypjZ/.g4SethoeA51ph3UD4kZi9oPkeMTpjKU5uo6", "USER",
				!isCompetitor, isCompetitor, stageNo, email, accountVerified, verificationCode);
		urepository.save(newUser);

		assertThat(newUser.getUsername()).isEqualTo(username);
		assertThat(newUser.getEmail()).isEqualTo(email);
		assertThat(newUser.getVerificationCode()).isEqualTo(verificationCode);
		assertThat(newUser.getIsCompetitor()).isEqualTo(isCompetitor);
		assertThat(newUser.getIsOut()).isEqualTo(!isCompetitor);
		assertThat(newUser.isAccountVerified()).isEqualTo(accountVerified);

		newUser.setAccountVerified(!accountVerified);
		newUser.setIsCompetitor(!isCompetitor);
		newUser.setIsOut(isCompetitor);
		newUser.setVerificationCode(null);

		String updatedUsername = "usero2";
		String updatedEmail = "mail2@test.com";
		newUser.setUsername(updatedUsername);
		newUser.setEmail(updatedEmail);
		urepository.save(newUser);

		User updatedUser = urepository.findByUsername(updatedUsername);
		assertThat(updatedUser).isNotNull();

		assertThat(updatedUser.getVerificationCode()).isNull();
		assertThat(updatedUser.getIsCompetitor()).isEqualTo(!isCompetitor);
		assertThat(updatedUser.getIsOut()).isEqualTo(isCompetitor);
		assertThat(updatedUser.getUsername()).isEqualTo(updatedUsername);
		assertThat(updatedUser.getEmail()).isEqualTo(updatedEmail);
		assertThat(updatedUser.isAccountVerified()).isEqualTo(!accountVerified);
	}

	// Test delete functionalities:
	@Test
	public void testDeleteUser() {
		Stage stageNo = this.resetStageAndUserRepos();
		User user1 = new User("usero1", "$2a$06$3jYRJrg0ghaaypjZ/.g4SethoeA51ph3UD4kZi9oPkeMTpjKU5uo6", "USER", true,
				false, stageNo, "test1@gmail.com", true, null);
		User user2 = new User("usero2", "$2a$06$3jYRJrg0ghaaypjZ/.g4SethoeA51ph3UD4kZi9oPkeMTpjKU5uo6", "USER", true,
				false, stageNo, "test2@gmail.com", true, null);
		urepository.save(user1);
		urepository.save(user2);

		urepository.deleteAll(); // deleting all hardcoded users
		List<User> usersEmpty = urepository.findAll();
		assertThat(usersEmpty).hasSize(0);

		User newUser = new User("usero1", "$2a$06$3jYRJrg0ghaaypjZ/.g4SethoeA51ph3UD4kZi9oPkeMTpjKU5uo6", "USER", true,
				false, stageNo, "test2@gmail.com", true, null);
		urepository.save(newUser);
		urepository.delete(newUser);
		usersEmpty = urepository.findAll();
		assertThat(usersEmpty).hasSize(0);
	}

	private Stage resetStageAndUserRepos() {
		urepository.deleteAll(); // deleting all hardcoded users
		srepository.deleteAll();
		List<Stage> stageNullNo = srepository.findByStage("No");
		assertThat(stageNullNo).hasSize(0);

		Stage stageNo = new Stage("No", true);
		srepository.save(stageNo);

		return stageNo;
	}
}
