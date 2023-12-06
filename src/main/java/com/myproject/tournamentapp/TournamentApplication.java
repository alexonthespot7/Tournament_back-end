package com.myproject.tournamentapp;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import com.myproject.tournamentapp.model.RoundRepository;
import com.myproject.tournamentapp.model.Stage;
import com.myproject.tournamentapp.model.StageRepository;
import com.myproject.tournamentapp.model.User;
import com.myproject.tournamentapp.model.UserRepository;

@SpringBootApplication
@EnableMethodSecurity
public class TournamentApplication {

	public static void main(String[] args) {
		SpringApplication.run(TournamentApplication.class, args);
	}
	
	@Bean
	public CommandLineRunner tournamentDemo(UserRepository uRepository, StageRepository stageRepository, RoundRepository rrepository) {
		return (args) -> {
			Stage stageNo = new Stage("No", true);
			stageRepository.save(stageNo);

			//admin with login: admin and password: asas2233
			
			User userAdmin = new User("admin", "$2a$12$0Mu/91y.kvDE7rj0ZXrWkOxUISfqEuQcXyU.luDJIe7DW2W/eqUYq",
					"ADMIN", true, false, stageRepository.findByStage("No").get(0), "admin.mail@test.com",
					true, null);
			
			User user1 = new User("user1", "$2a$12$0Mu/91y.kvDE7rj0ZXrWkOxUISfqEuQcXyU.luDJIe7DW2W/eqUYq",
					"USER", false, true, stageRepository.findByStage("No").get(0), "user1.mail@test.com",
					true, null);
			User user2 = new User("user2", "$2a$12$0Mu/91y.kvDE7rj0ZXrWkOxUISfqEuQcXyU.luDJIe7DW2W/eqUYq",
					"USER", false, true, stageRepository.findByStage("No").get(0), "user2.mail@test.com",
					true, null);
			User user3 = new User("user3", "$2a$12$0Mu/91y.kvDE7rj0ZXrWkOxUISfqEuQcXyU.luDJIe7DW2W/eqUYq",
					"USER", false, true, stageRepository.findByStage("No").get(0), "user3.mail@test.com",
					true, null);
			User user4 = new User("unverified", "$2a$12$0Mu/91y.kvDE7rj0ZXrWkOxUISfqEuQcXyU.luDJIe7DW2W/eqUYq",
					"USER", false, true, stageRepository.findByStage("No").get(0), "user4.mail@test.com",
					false, "example_code");
			
			
			
			
			uRepository.save(userAdmin);
			
			uRepository.save(user1);
			uRepository.save(user2);
			uRepository.save(user3);
			uRepository.save(user4);
		};
	}
}
