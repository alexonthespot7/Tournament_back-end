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

			User userAdmin = new User("axosinc", "$2a$12$0Mu/91y.kvDE7rj0ZXrWkOxUISfqEuQcXyU.luDJIe7DW2W/eqUYq",
					"ADMIN", true, false, stageRepository.findByStage("No").get(0), "aleksei2.shevelenkov@gmail.com",
					true, null);
			User user1 = new User("danrey", "$2a$12$0Mu/91y.kvDE7rj0ZXrWkOxUISfqEuQcXyU.luDJIe7DW2W/eqUYq", "USER",
					false, true, stageRepository.findByStage("No").get(0), "bgw2595@myy.haaga-helia.fi", true, null);
			User user2 = new User("wanyeser", "$2a$12$0Mu/91y.kvDE7rj0ZXrWkOxUISfqEuQcXyU.luDJIe7DW2W/eqUYq", "USER",
					false, true, stageRepository.findByStage("No").get(0), "mrbudach2@mail.ru", true, null);

			// creating verified user with login loginTest and password taylorswift
			User user3 = new User("loginTest", "$2a$12$19lxeD0nHwNrMxnGhWFNoOLMC/xOxd81ug1D.fboYQeoRHjyR9hym", "USER",
					false, true, stageRepository.findByStage("No").get(0), "login.test@gmail.com", true, null);
			User user4 = new User("danrey2", "$2a$12$0Mu/91y.kvDE7rj0ZXrWkOxUISfqEuQcXyU.luDJIe7DW2W/eqUYq", "USER",
					false, true, stageRepository.findByStage("No").get(0), "bgw259d5@myy.haaga-helia.fi", true, null);
			User user5 = new User("wanyeser2", "$2a$12$0Mu/91y.kvDE7rj0ZXrWkOxUISfqEuQcXyU.luDJIe7DW2W/eqUYq", "USER",
					false, true, stageRepository.findByStage("No").get(0), "mdrbudach2@mail.ru", true, null);
			User user6 = new User("danrey32", "$2a$12$0Mu/91y.kvDE7rj0ZXrWkOxUISfqEuQcXyU.luDJIe7DW2W/eqUYq", "USER",
					false, true, stageRepository.findByStage("No").get(0), "bg1w259d5@myy.haaga-helia.fi", true, null);
			User user7 = new User("wanyes3er2", "$2a$12$0Mu/91y.kvDE7rj0ZXrWkOxUISfqEuQcXyU.luDJIe7DW2W/eqUYq", "USER",
					false, true, stageRepository.findByStage("No").get(0), "mdrbu3dach2@mail.ru", true, null);

			uRepository.save(userAdmin);
			uRepository.save(user1);
			uRepository.save(user2);
			uRepository.save(user3);
			uRepository.save(user4);
			uRepository.save(user5);
			uRepository.save(user6);
			uRepository.save(user7);

		};
	}
}
