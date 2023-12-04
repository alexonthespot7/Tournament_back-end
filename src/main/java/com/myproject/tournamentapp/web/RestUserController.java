package com.myproject.tournamentapp.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.myproject.tournamentapp.forms.BracketPageInfo;
import com.myproject.tournamentapp.forms.ChangePasswordForm;
import com.myproject.tournamentapp.forms.CompetitorPublicInfo;
import com.myproject.tournamentapp.forms.PersonalInfo;
import com.myproject.tournamentapp.forms.RoundPublicInfo;
import com.myproject.tournamentapp.service.BracketService;
import com.myproject.tournamentapp.service.RoundService;
import com.myproject.tournamentapp.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;


@RestController
@RequestMapping("/api")
@Tag(name="Authenticated Methods", description="Methods that can be fetched by all authenticated users (ADMIN and USER). These methods require sending the jwt token in the request's Authorization header")
@ApiResponses(value = {
		@ApiResponse(responseCode = "401", description = "Error: Full authentication is required to access this resource", content = {
				@Content(mediaType = "application/json", schema = @Schema(implementation = ResponseStatusException.class)) 
		}) 
})
@PreAuthorize("isAuthenticated()")
public class RestUserController {

	@Autowired
	private UserService userService;

	@Autowired
	private RoundService roundsService;

	@Autowired
	private BracketService bracketService;
	
	// method to display competitors on competitors page for authorized user
	@Operation(summary = "Gets the list of competitors", description = "The method to fetch the list of all competitors. Competitor record doesn't contain any sensitive data, thus can be perceived by users")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "OK", content = {
			        @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = CompetitorPublicInfo.class)))
			})
	})
	@RequestMapping(value = "/competitors", method = RequestMethod.GET)
	public @ResponseBody List<CompetitorPublicInfo> listCompetitorsPublicInfo() {

		return userService.listCompetitorsPublicInfo();

	}

	// method to display user's personal info on the user page
	@Operation(summary = "Gets user's personal info", description = "The method to fetch user's personal info by user id, provided as path variable. The id in the path must match id of the user's authentication instance")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "OK", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = PersonalInfo.class))
			}), 
			@ApiResponse(responseCode = "403", description = "FORBIDDEN, provided id doesn't match authentication instance's id.", content = {
					@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class), examples = {
							@ExampleObject(value = "You don't have an access to this page")
					})
			})
	})
	@RequestMapping(value = "/competitors/{userid}", method = RequestMethod.GET)
	public @ResponseBody PersonalInfo getPersonalInfoById(@PathVariable("userid") Long userId, Authentication auth) {

		return userService.getPersonalInfoById(userId, auth);

	}

	// method to change user's competitor status
	@Operation(summary = "Updates user's competitor status", description = "If the bracket wasn't made yet, users can update their participant status. The username and email can only be updated by administrator.")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "OK", content = {
					@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class), examples = {
							@ExampleObject(value = "User info was updated successfully")
					})
			}), 
			@ApiResponse(responseCode = "409", description = "CONFLICT, some authentication error", content = {
					@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class), examples = {
							@ExampleObject(value = "Something wrong with authentication")
					})
			})
	})
	@RequestMapping(value = "/updateuser/{userid}", method = RequestMethod.PUT)
	@PreAuthorize("authentication.getPrincipal().getId() == #userId")
	public ResponseEntity<?> updateUser(@PathVariable("userid") Long userId, @RequestBody PersonalInfo personalInfo) {

		return userService.updateUser(userId, personalInfo);

	}

	// method to change personal's password
	@Operation(summary = "Changes user's password", description = "The method allows users to change their password by providing the old one and the new one. The corresponding user in database is found by provided jwt token.")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "OK", content = {
					@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class), examples = {
							@ExampleObject(value = "The password was successfully changed")
					})
			}), 
			@ApiResponse(responseCode = "403", description = "FORBIDDEN, provided old password is wrong", content = {
					@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class), examples = {
							@ExampleObject(value = "The old password is incorrect")
					})
			})
	})
	@RequestMapping(value = "/changepassword", method = RequestMethod.PUT)
	public ResponseEntity<?> changePassword(@RequestBody ChangePasswordForm changePasswordForm, Authentication auth) {
		
		return userService.changePassword(changePasswordForm, auth);
		
	}

	// Method to display all rounds on the rounds page
	@Operation(summary = "Gets rounds", description = "The method fetches rounds without exposing sensitive data.")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "OK", content = {
			        @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = RoundPublicInfo.class)))
			}), 
			@ApiResponse(responseCode = "202", description = "ACCEPTED, can't complete method, as the bracket wasn't made yet", content = {
					@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class), examples = {
							@ExampleObject(value = "The bracket wasn't made yet")
					})
			})
	})
	@RequestMapping(value = "/rounds", method = RequestMethod.GET)
	public @ResponseBody List<RoundPublicInfo> getPublicInfoOfAllRounds() {

		return roundsService.getPublicInfoOfAllRounds();

	}

	// method to send stages and rounds public info and winner (if exists) for the
	// bracket page: each stage has the list of its rounds;
	@Operation(summary = "Gets bracket page info", description = "The method fetches all rounds grouped by stages with no sensitive data.")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "OK", content = {
			        @Content(mediaType = "application/json", schema = @Schema(implementation = BracketPageInfo.class))
			}), 
			@ApiResponse(responseCode = "202", description = "ACCEPTED, can't complete method, as the bracket wasn't made yet", content = {
					@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class), examples = {
							@ExampleObject(value = "The bracket wasn't made yet")
					})
			})
	})
	@RequestMapping(value = "/bracket", method = RequestMethod.GET)
	public @ResponseBody BracketPageInfo getBracketInfo() {

		return bracketService.getBracketInfo();

	}

}
