package com.myproject.tournamentapp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.myproject.tournamentapp.web.RestAdminController;
import com.myproject.tournamentapp.web.RestPublicController;
import com.myproject.tournamentapp.web.RestUserController;


//smoke testing of Chess application controller and User controller
@SpringBootTest
class ControllersTests {

	@Autowired
	private RestAdminController restAdminController;
	
	@Autowired
	private RestUserController restUserController;
	
	@Autowired
	private RestPublicController restPublicController;
	
	@Test
	void contextLoads() throws Exception {
		assertThat(restAdminController).isNotNull();
	}
	
	@Test
	void contextLoadsTwo() throws Exception {
		assertThat(restUserController).isNotNull();
	}
	
	@Test
	void contextLoadsThree() throws Exception {
		assertThat(restPublicController).isNotNull();
	}


}
