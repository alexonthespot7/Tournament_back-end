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

import java.sql.Date;

@ExtendWith(SpringExtension.class)
@DataJpaTest
class RepositoryTests {
	@Autowired
	private UserRepository urepository;
	
	@Autowired
	private RoundRepository rrepository;
	
	@Autowired
	private StageRepository srepository;
	
	//test Create for User repo:
	@Test
	public void testUserCreation() {
		User user = new User("Me", "user", "usersdso", "$2a$06$3jYRJrg0ghaaypjZ/.g4SethoeA51ph3UD4kZi9oPkeMTpjKU5uo6", "USER", true, false, srepository.findByStage("No").get(0), "123mymail@gmail.com", true);
		urepository.save(user);
		assertThat(user.getId()).isNotNull();
	}
	
	//Test find by username and by email
	@Test
	public void testUserByUsernameAndEmail() {
		User user0 = urepository.findByUsername("usero");
		assertThat(user0).isNull();
		
		User user00 = urepository.findByEmail("mymailss@gmail.com");
		assertThat(user00).isNull();

		User userCr1 = new User("Me", "user", "usero", "$2a$06$3jYRJrg0ghaaypjZ/.g4SethoeA51ph3UD4kZi9oPkeMTpjKU5uo6", "USER", true, false, srepository.findByStage("No").get(0), "123mymail@gmail.com", true);
		urepository.save(userCr1);

		User user = urepository.findByUsername("usero");
		assertThat(user).isNotNull();
		assertThat(user.getRole()).isEqualTo("USER");
		
		User userCr2 = new User("Me", "user", "usersdso", "$2a$06$3jYRJrg0ghaaypjZ/.g4SethoeA51ph3UD4kZi9oPkeMTpjKU5uo6", "ADMIN", true, false, srepository.findByStage("No").get(0), "mymailss@gmail.com", true);
		urepository.save(userCr2);

		
		User user2 = urepository.findByEmail("mymailss@gmail.com");
		assertThat(user2).isNotNull();
		assertThat(user2.getRole()).isEqualTo("ADMIN");
	}
	
	//Test searching for verified and competitors functionality
	@Test
	public void testVerifiedUsersSearch() {
		List<User> verifiedUsers = urepository.findAllVerified();
		assertThat(verifiedUsers.size()).isNotZero();
		
		for (User user : verifiedUsers) {
			assertThat(user.isAccountVerified()).isEqualTo(true);
		}
		
		List<User> competitors = urepository.findAllCompetitors();
		assertThat(competitors.size()).isNotZero();
		
		for (User user : competitors) {
			assertThat(user.getIsCompetitor()).isEqualTo(true);
			assertThat(user.isAccountVerified()).isEqualTo(true);
		}
	}
	
	//Test deletion for user repo
	@Test
	public void deleteUserTest() {
		User userCr1 = new User("Me", "user", "usero", "$2a$06$3jYRJrg0ghaaypjZ/.g4SethoeA51ph3UD4kZi9oPkeMTpjKU5uo6", "USER", true, false, srepository.findByStage("No").get(0), "123mymail@gmail.com", true);
		urepository.save(userCr1);
		
		User user = urepository.findByUsername("usero");
		urepository.delete(user);
		assertThat(urepository.findByUsername("usero")).isNull();
	}
	
	//Test Create Functionality for stage repository
	@Test
	public void testCreationStage() {
		Stage stage = new Stage("1/4", Date.valueOf("2020-01-01"), Date.valueOf("2032-01-01"), true);
		srepository.save(stage);
		assertThat(stage.getStageid()).isNotNull();
	}
	
	//Test delete functionality for stage repository
	@Test
	public void testDeletionStage() {
		Stage stage = srepository.findByStage("No").get(0);
		srepository.delete(stage);
		List<Stage> stages = srepository.findByStage("No");
		assertThat(stages).hasSize(0);
		
		Stage stage1 = new Stage("1/2", Date.valueOf("2020-01-01"), Date.valueOf("2032-01-01"), false);
		Stage stage2 = new Stage("1/4", Date.valueOf("2020-01-01"), Date.valueOf("2032-01-01"), true);
		srepository.save(stage1);
		srepository.save(stage2);
		srepository.deleteAllStages();
		assertThat(srepository.findAllStages()).hasSize(0);
	}
	
	//Test seacrhing functions of stage repository
	@Test
	public void testSearchStage() {
		List<Stage> stages = srepository.findByStage("No");
		assertThat(stages).hasSize(1);
	}
	
	//check round repository findAll function
	@Test
	public void testFindAllRounds() {
		List<Round> allRounds = rrepository.findAll();
		assertThat(allRounds).hasSize(0);
		rrepository.save(new Round("No", srepository.findByStage("No").get(0)));
		allRounds = rrepository.findAll();
		assertThat(allRounds).hasSize(1);
	}
	
	//Test delete and creation functions for round repo:
	@Test
	public void testDeleteCreateRound() { 
		rrepository.save(new Round("No", srepository.findByStage("No").get(0)));
		assertThat(rrepository.findAll().size()).isNotZero();
		Round round = rrepository.findRoundsByStage(srepository.findByStage("No").get(0).getStageid()).get(0);
		rrepository.delete(round);
		assertThat(rrepository.findAll()).hasSize(0);
	}
}
