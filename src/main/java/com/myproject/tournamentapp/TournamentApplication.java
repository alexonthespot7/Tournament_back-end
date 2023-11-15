package com.myproject.tournamentapp;

import java.sql.Date;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.myproject.tournamentapp.model.Stage;
import com.myproject.tournamentapp.model.StageRepository;
import com.myproject.tournamentapp.model.User;
import com.myproject.tournamentapp.model.UserRepository;

@SpringBootApplication
public class TournamentApplication {

	public static void main(String[] args) {
		SpringApplication.run(TournamentApplication.class, args);
	}
	
	@Bean
	public CommandLineRunner tournamentDemo(UserRepository uRepository, StageRepository stageRepository) {
		return (args) -> {
			Stage stageNo = new Stage("No", Date.valueOf("2023-01-01"), Date.valueOf("2023-01-10"), true);
			stageRepository.save(stageNo);
			
			User userAdmin = new User("Aleksei", "Shevelenkov", "axosinc", "$2a$12$0Mu/91y.kvDE7rj0ZXrWkOxUISfqEuQcXyU.luDJIe7DW2W/eqUYq", "ADMIN", true, false, stageRepository.findByStage("No").get(0), "aleksei2.shevelenkov@gmail.com", true, null);
			User user1 = new User("Dan", "Reynolds", "danrey", "$2a$12$0Mu/91y.kvDE7rj0ZXrWkOxUISfqEuQcXyU.luDJIe7DW2W/eqUYq", "USER", false, true, stageRepository.findByStage("No").get(0), "bgw2595@myy.haaga-helia.fi", true, null);
			User user2 = new User("Wayne", "Sermon", "wanyeser", "$2a$12$0Mu/91y.kvDE7rj0ZXrWkOxUISfqEuQcXyU.luDJIe7DW2W/eqUYq", "USER", false, true, stageRepository.findByStage("No").get(0), "mrbudach2@mail.ru", true, null);
			uRepository.save(userAdmin);
			uRepository.save(user1);
			uRepository.save(user2);
			
		};
	}
}
