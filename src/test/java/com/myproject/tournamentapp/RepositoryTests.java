package com.myproject.tournamentapp;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.myproject.tournamentapp.model.Round;
import com.myproject.tournamentapp.model.RoundRepository;
import com.myproject.tournamentapp.model.Stage;
import com.myproject.tournamentapp.model.StageRepository;
import com.myproject.tournamentapp.model.User;
import com.myproject.tournamentapp.model.UserRepository;

@ExtendWith(SpringExtension.class)
@DataJpaTest
class RepositoryTests {
	@Autowired
	private UserRepository urepository;

	@Autowired
	private RoundRepository rrepository;

	@Autowired
	private StageRepository srepository;

	// test Create for User repo:
	@Test
	public void testUserCreation() {
		User user = new User("usersdso", "$2a$06$3jYRJrg0ghaaypjZ/.g4SethoeA51ph3UD4kZi9oPkeMTpjKU5uo6",
				"USER", true, false, srepository.findByStage("No").get(0), "123mymail@gmail.com", true, null);
		urepository.save(user);
		assertThat(user.getId()).isNotNull();
	}

	// Test find by username and by email for the user repo
	@Test
	public void testUserByUsernameAndEmail() {
		urepository.deleteAll(); // deleting all hardcoded users
		assertThat(urepository.findAll()).isEmpty();

		User user0 = urepository.findByUsername("usero");
		assertThat(user0).isNull();

		User user00 = urepository.findByEmail("mymailss@gmail.com");
		assertThat(user00).isNull();

		User userCr1 = new User("usero", "$2a$06$3jYRJrg0ghaaypjZ/.g4SethoeA51ph3UD4kZi9oPkeMTpjKU5uo6",
				"USER", true, false, srepository.findByStage("No").get(0), "123mymail@gmail.com", true, null);
		urepository.save(userCr1);

		User user = urepository.findByUsername("usero");
		assertThat(user).isNotNull();
		assertThat(user.getRole()).isEqualTo("USER");

		User userCr2 = new User("usersdso",
				"$2a$06$3jYRJrg0ghaaypjZ/.g4SethoeA51ph3UD4kZi9oPkeMTpjKU5uo6", "ADMIN", true, false,
				srepository.findByStage("No").get(0), "mymailss@gmail.com", true, null);
		urepository.save(userCr2);

		User user2 = urepository.findByEmail("mymailss@gmail.com");
		assertThat(user2).isNotNull();
		assertThat(user2.getRole()).isEqualTo("ADMIN");
	}

	// Test searching for verified users and competitors functionality for user repo
	@Test
	public void testVerifiedUsersSearch() {
		urepository.deleteAll(); // deleting the hard-coded users
		assertThat(urepository.findAll()).isEmpty();

		User userCr1 = new User("usero", "$2a$06$3jYRJrg0ghaaypjZ/.g4SethoeA51ph3UD4kZi9oPkeMTpjKU5uo6",
				"USER", true, false, srepository.findByStage("No").get(0), "12323mymail@gmail.com", true, null);
		urepository.save(userCr1);
		User userCr2 = new User("usero2",
				"$2a$06$3jYRJrg0ghaaypjZ/.g4SethoeA51ph3UD4kZi9oPkeMTpjKU5uo6", "USER", false, true,
				srepository.findByStage("No").get(0), "12332mymail@gmail.com", false, "SomeCode");
		User userCr3 = new User("usero3",
				"$2a$06$3jYRJrg0ghaaypjZ/.g4SethoeA51ph3UD4kZi9oPkeMTpjKU5uo6", "ADMIN", false, true,
				srepository.findByStage("No").get(0), "12342mymail@gmail.com", true, null);
		User userCr4 = new User("usero43",
				"$2a$06$3jYRJrg0ghaaypjZ/.g4SethoeA51ph3UD4kZi9oPkeMTpjKU5uo6", "USER", false, true,
				srepository.findByStage("No").get(0), "1234212mymail@gmail.com", true, null);

		urepository.save(userCr1);
		urepository.save(userCr2);
		urepository.save(userCr3);
		urepository.save(userCr4);

		List<User> verifiedUsers = urepository.findAllVerifiedUsers();
		assertThat(verifiedUsers).hasSize(2);

		for (User user : verifiedUsers) {
			assertThat(user.isAccountVerified()).isEqualTo(true);
		}

		List<User> competitors = urepository.findAllCompetitors();
		assertThat(competitors).hasSize(1);

		for (User user : competitors) {
			assertThat(user.getIsCompetitor()).isEqualTo(true);
			assertThat(user.isAccountVerified()).isEqualTo(true);
		}
	}

	// Test deletion for user repo
	@Test
	public void deleteUserTest() {
		urepository.deleteAll(); // deleting all hard-coded users
		assertThat(urepository.findAll()).isEmpty();

		User userCr1 = new User("usero", "$2a$06$3jYRJrg0ghaaypjZ/.g4SethoeA51ph3UD4kZi9oPkeMTpjKU5uo6",
				"USER", true, false, srepository.findByStage("No").get(0), "123mymail@gmail.com", true, null);
		urepository.save(userCr1);

		User user = urepository.findByUsername("usero");
		urepository.delete(user);
		assertThat(urepository.findByUsername("usero")).isNull();
	}

	// Test Create Functionality for stage repository
	@Test
	public void testCreationStage() {
		srepository.deleteAll(); // deleting all hard-coded stages
		assertThat(srepository.findAll()).isEmpty();

		Stage stage = new Stage("1/4", true);
		srepository.save(stage);
		assertThat(stage.getStageid()).isNotNull();
	}

	// Test delete functionality for stage repository
	@Test
	public void testDeletionStage() {
		srepository.deleteAll(); // deleting all hard-coded stages
		assertThat(srepository.findAll()).isEmpty();

		Stage stageNo = new Stage("No", true);
		srepository.save(stageNo);
		srepository.delete(stageNo);
		List<Stage> stages = srepository.findByStage("No");
		assertThat(stages).hasSize(0);

		Stage stage1 = new Stage("1/2", false);
		Stage stage2 = new Stage("1/4", true);
		srepository.save(stage1);
		srepository.save(stage2);
		srepository.deleteAllStages();
		assertThat(srepository.findAllStages()).hasSize(0);
	}

	// Test seacrhing functions of stage repository
	@Test
	public void testSearchStage() {
		srepository.deleteAll(); // deleting all hard-coded stages
		assertThat(srepository.findAll()).isEmpty();

		Stage stageNo = new Stage("No", true);
		srepository.save(stageNo);
		List<Stage> stages = srepository.findByStage("No");
		assertThat(stages).hasSize(1);
		
		List<Stage> stages2 = srepository.findByStage("1/2");
		assertThat(stages2).hasSize(0);
	}
	
	//creation functionality checking for the round repo
	@Test
	public void creationTestsRounds() {
		rrepository.deleteAll(); //deleting all hard-coded rounds
		
		rrepository.save(new Round("No", srepository.findByStage("No").get(0)));
		assertThat(rrepository.findAll()).isNotEmpty();
	}

	// check round repository findAll function
	@Test
	public void testFindAllRounds() {
		rrepository.deleteAll(); //deleting all hard-coded rounds
		assertThat(rrepository.findAll()).isEmpty();

		List<Round> allRounds = rrepository.findAll();
		assertThat(allRounds).hasSize(0);
		rrepository.save(new Round("No", srepository.findByStage("No").get(0)));
		rrepository.save(new Round("No", srepository.findByStage("No").get(0)));
		allRounds = rrepository.findAll();
		assertThat(allRounds).hasSize(2);
	}

	// Test delete functionality for round repo:
	@Test
	public void testDeleteRound() {
		rrepository.deleteAll(); //deleting all hard-coded rounds
		assertThat(rrepository.findAll()).isEmpty();

		rrepository.save(new Round("No", srepository.findByStage("No").get(0)));
		assertThat(rrepository.findAll()).isNotEmpty();
		Round round = rrepository.findRoundsByStage(srepository.findByStage("No").get(0).getStageid()).get(0);
		rrepository.delete(round);
		assertThat(rrepository.findAll()).isEmpty();
	}
}
