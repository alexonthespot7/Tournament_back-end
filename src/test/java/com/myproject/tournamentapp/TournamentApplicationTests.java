package com.myproject.tournamentapp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.myproject.tournamentapp.web.MainController;
import com.myproject.tournamentapp.web.UserController;


//smoke testing of Chess application controller and User controller
@SpringBootTest
class TournamentApplicationTests {

	@Autowired
	private MainController chessController;
	
	@Autowired
	private UserController userController;
	
	@Test
	void contextLoads() throws Exception {
		assertThat(chessController).isNotNull();
	}
	
	@Test
	void contextLoadsTwo() throws Exception {
		assertThat(userController).isNotNull();
	}


}
