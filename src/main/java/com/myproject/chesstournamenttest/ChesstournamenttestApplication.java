package com.myproject.chesstournamenttest;

import java.sql.Date;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.myproject.chesstournamenttest.model.RoundRepository;
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
	public CommandLineRunner demo(UserRepository urepository,
			RoundRepository rrepository, StageRepository srepository) {
		return (args) -> {
			srepository.save(new Stage("No", Date.valueOf("2020-01-01"), Date.valueOf("2032-01-01"), true));
			
			User user1 = new User("Me", "user", "usero", "$2a$06$3jYRJrg0ghaaypjZ/.g4SethoeA51ph3UD4kZi9oPkeMTpjKU5uo6", "USER", true, false, srepository.findByStage("No").get(0), "mymail@gmail.com", true);
			User user2 = new User("Aleksei", "Admin", "admino", "$2a$10$0MMwY.IQqpsVc1jC8u7IJ.2rT8b0Cd3b3sfIBGV2zfgnPGtT4r0.C", "ADMIN", true, false, srepository.findByStage("No").get(0), "mymailss@gmail.com", true);
			urepository.save(user1);
			urepository.save(user2);
			
			urepository.save(new User("Aleksei", "Shevelenkov", "alexonthespot", "$2a$10$0MMwY.IQqpsVc1jC8u7IJ.2rT8b0Cd3b3sfIBGV2zfgnPGtT4r0.C", "ADMIN", false, true, srepository.findByStage("No").get(0), "mymssail@gmail.com", true));
			urepository.save(new User("Maksim", "Minenko", "madara", "$2a$06$3jYRJrg0ghaaypjZ/.g4SethoeA51ph3UD4kZi9oPkeMTpjKU5uo6", "USER", false, true, srepository.findByStage("No").get(0), "mymasl@gmail.com", true));
			urepository.save(new User("Egor", "Minenko", "EgoMin", "$2a$06$3jYRJrg0ghaaypjZ/.g4SethoeA51ph3UD4kZi9oPkeMTpjKU5uo6", "USER", false, true, srepository.findByStage("No").get(0), "mysdmail@gmail.com", true));
			urepository.save(new User("Tin", "Tran", "TrIn", "$2a$06$3jYRJrg0ghaaypjZ/.g4SethoeA51ph3UD4kZi9oPkeMTpjKU5uo6", "USER", false, true, srepository.findByStage("No").get(0), "mymaadwil@gmail.com", true));
			urepository.save(new User("Minh", "Nguyen", "Mikko", "$2a$06$3jYRJrg0ghaaypjZ/.g4SethoeA51ph3UD4kZi9oPkeMTpjKU5uo6", "USER", false, true, srepository.findByStage("No").get(0), "mymaidwqrl@gmail.com", true));
			urepository.save(new User("Who", "Am", "I", "$2a$10$0MMwY.IQqpsVc1jC8u7IJ.2rT8b0Cd3b3sfIBGV2zfgnPGtT4r0.C", "ADMIN", true, false, srepository.findByStage("No").get(0), "myma23il@gmail.com", true));
			urepository.save(new User("Maksimsss", "ss", "aaaeee", "$2a$06$3jYRJrg0ghaaypjZ/.g4SethoeA51ph3UD4kZi9oPkeMTpjKU5uo6", "USER", false, true, srepository.findByStage("No").get(0), "mymai21wl@gmail.com", true));
			urepository.save(new User("AAd", "Asdsd", "aAsdw", "$2a$06$3jYRJrg0ghaaypjZ/.g4SethoeA51ph3UD4kZi9oPkeMTpjKU5uo6", "USER", false, true, srepository.findByStage("No").get(0), "my23mwail@gmail.com", true));
			urepository.save(new User("AA", "AASdsd", "DDSSS", "$2a$06$3jYRJrg0ghaaypjZ/.g4SethoeA51ph3UD4kZi9oPkeMTpjKU5uo6", "USER", false, true, srepository.findByStage("No").get(0), "mym32qail@gmail.com", true));
			
		};
	}
}
