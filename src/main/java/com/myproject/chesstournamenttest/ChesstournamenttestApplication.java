package com.myproject.chesstournamenttest;

import java.sql.Date;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.myproject.chesstournamenttest.model.Stage;
import com.myproject.chesstournamenttest.model.StageRepository;
import com.myproject.chesstournamenttest.model.User;
import com.myproject.chesstournamenttest.model.UserRepository;

@SpringBootApplication
public class ChesstournamenttestApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChesstournamenttestApplication.class, args);
	}
	
	@Bean
	public CommandLineRunner tournamentDemo(UserRepository uRepository, StageRepository stageRepository) {
		return (args) -> {
			Stage stageNo = new Stage("No", Date.valueOf("2023-01-01"), Date.valueOf("2023-01-10"), true);
			stageRepository.save(stageNo);
			
			User userAdmin = new User("Aleksei", "Shevelenkov", "axosinc", "$2a$12$0Mu/91y.kvDE7rj0ZXrWkOxUISfqEuQcXyU.luDJIe7DW2W/eqUYq", "ADMIN", true, false, stageRepository.findByStage("No").get(0), "aleksei2.shevelenkov@gmail.com", true);
			User user1 = new User("Dan1", "Reynolds1", "danrey1", "$2a$12$0Mu/91y.kvDE7rj0ZXrWkOxUISfqEuQcXyU.luDJIe7DW2W/eqUYq", "USER", false, true, stageRepository.findByStage("No").get(0), "bgw12595@myy.haaga-helia.fi", true);
			User user2 = new User("Dan2", "Reynolds2", "danrey2", "$2a$12$0Mu/91y.kvDE7rj0ZXrWkOxUISfqEuQcXyU.luDJIe7DW2W/eqUYq", "USER", false, true, stageRepository.findByStage("No").get(0), "bgw2533395@myy.haaga-helia.fi", true);
			User user3 = new User("Dan3", "Reynolds3", "danrey3", "$2a$12$0Mu/91y.kvDE7rj0ZXrWkOxUISfqEuQcXyU.luDJIe7DW2W/eqUYq", "USER", false, true, stageRepository.findByStage("No").get(0), "bgw2533595@myy.haaga-helia.fi", true);
			User user4 = new User("Dan4", "Reynolds5", "danrey4", "$2a$12$0Mu/91y.kvDE7rj0ZXrWkOxUISfqEuQcXyU.luDJIe7DW2W/eqUYq", "USER", false, true, stageRepository.findByStage("No").get(0), "bgw25334495@myy.haaga-helia.fi", true);
			User user6 = new User("Wayne", "Sermon", "wanyeser", "$2a$12$0Mu/91y.kvDE7rj0ZXrWkOxUISfqEuQcXyU.luDJIe7DW2W/eqUYq", "USER", false, true, stageRepository.findByStage("No").get(0), "mrbudach25@mail.ru", true);
			uRepository.save(userAdmin);
			uRepository.save(user1);
			uRepository.save(user2);
			uRepository.save(user3);
			uRepository.save(user6);
			uRepository.save(user4);

		};
	}
}
