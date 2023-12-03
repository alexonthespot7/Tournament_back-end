package com.myproject.tournamentapp;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.myproject.tournamentapp.model.RoundRepository;
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
	public CommandLineRunner tournamentDemo(UserRepository uRepository, StageRepository stageRepository, RoundRepository rrepository) {
		return (args) -> {
			Stage stageNo = new Stage("No", true);
			stageRepository.save(stageNo);

			//admin with login: admin and password: asas2233
			
			User userAdmin = new User("admin", "$2a$12$0Mu/91y.kvDE7rj0ZXrWkOxUISfqEuQcXyU.luDJIe7DW2W/eqUYq",
					"ADMIN", true, false, stageRepository.findByStage("No").get(0), "admin.mail@test.com",
					true, null);
			
			
			
			uRepository.save(userAdmin);
		};
	}
}
