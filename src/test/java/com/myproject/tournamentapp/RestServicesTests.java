package com.myproject.tournamentapp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.myproject.tournamentapp.model.RoundRepository;
import com.myproject.tournamentapp.model.StageRepository;
import com.myproject.tournamentapp.model.User;
import com.myproject.tournamentapp.model.UserRepository;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class RestServicesTests {
	public static final String HOST = "http://localhost:8080";
	
	@Autowired
	private UserRepository urepository;

	@Autowired
	private RoundRepository rrepository;

	@Autowired
	private StageRepository srepository;
	
	@Test
    public void testLoginRequest() {
		
		String requestBody = "{\"username\": \"loginTest\", \"password\": \"taylorswift\"}"; //testing with the hard-coded user info
		
		Response response = RestAssured.given()
				.contentType(ContentType.JSON)
				.body(requestBody)
				.post(HOST + "/login");
		
		assertThat(response.getStatusCode()).isEqualTo(200);
		
		String authorizationHeader = response.getHeader("Authorization"); // jwt token
		String allowHeader = response.getHeader("Allow"); //role of the user
		String hostHeader = response.getHeader("Host"); //id of the user
		
		assertThat(authorizationHeader).isNotNull();
		assertThat(authorizationHeader).contains("Bearer ");
		
		assertThat(allowHeader).isNotNull();
		assertThat(allowHeader).isEqualTo("USER");
		
		assertThat(hostHeader).isNotNull();
		
		requestBody = "{\"username\": \"loginTest\", \"password\": \"taylor2wrongswift\"}";
		
		response = RestAssured.given()
				.contentType(ContentType.JSON)
				.body(requestBody)
				.post(HOST + "/login");
		
		assertThat(response.getStatusCode()).isEqualTo(401);
		
		authorizationHeader = response.getHeader("Authorization"); // jwt token
		allowHeader = response.getHeader("Allow"); //role of the user
		hostHeader = response.getHeader("Host"); //id of the user
		
		assertThat(authorizationHeader).isNull();
		
		assertThat(allowHeader).isNull();
		
		assertThat(hostHeader).isNull();
	}
	
	@Test
    public void testSignupRequest() {
		
		String requestBody = "{"
                + "\"firstname\": \"John\", "
                + "\"lastname\": \"Doe\", "
                + "\"isCompetitor\": true, "
                + "\"username\": \"john_doe\", "
                + "\"password\": \"taylorswift\", "
                + "\"email\": \"aleksei.shevelenkov@gmail.com\""
                + "}";
		
		Response response = RestAssured.given()
				.contentType(ContentType.JSON)
				.body(requestBody)
				.post(HOST + "/signup");
		
		assertThat(response.getStatusCode()).isEqualTo(200);
    }
	
}
