package com.myproject.tournamentapp;


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
			Stage stageNo = new Stage("No", true);
			stageRepository.save(stageNo);
			
			User userAdmin = new User("axosinc", "$2a$12$0Mu/91y.kvDE7rj0ZXrWkOxUISfqEuQcXyU.luDJIe7DW2W/eqUYq", "ADMIN", true, false, stageRepository.findByStage("No").get(0), "aleksei2.shevelenkov@gmail.com", true, null);
			User user1 = new User("danrey", "$2a$12$0Mu/91y.kvDE7rj0ZXrWkOxUISfqEuQcXyU.luDJIe7DW2W/eqUYq", "USER", false, true, stageRepository.findByStage("No").get(0), "bgw2595@myy.haaga-helia.fi", true, null);
			User user2 = new User("wanyeser", "$2a$12$0Mu/91y.kvDE7rj0ZXrWkOxUISfqEuQcXyU.luDJIe7DW2W/eqUYq", "USER", false, true, stageRepository.findByStage("No").get(0), "mrbudach2@mail.ru", true, null);
			User user = new User("loginTest", "$2a$12$19lxeD0nHwNrMxnGhWFNoOLMC/xOxd81ug1D.fboYQeoRHjyR9hym",
					"USER", true, false, stageRepository.findByStage("No").get(0), "login.test@gmail.com", true, null); //creating verified user with login loginTest and password taylorswift
			uRepository.save(user);
			uRepository.save(userAdmin);
			uRepository.save(user1);
			uRepository.save(user2);
			
		};
	}
}
