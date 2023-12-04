package com.myproject.tournamentapp.web;

import java.io.UnsupportedEncodingException;
import java.util.List;

import jakarta.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.myproject.tournamentapp.forms.AddUserFormForAdmin;
import com.myproject.tournamentapp.forms.RoundsForAdminForm;
import com.myproject.tournamentapp.forms.UsersPageAdminForm;
import com.myproject.tournamentapp.model.Round;
import com.myproject.tournamentapp.model.Stage;
import com.myproject.tournamentapp.model.StageRepository;
import com.myproject.tournamentapp.model.User;
import com.myproject.tournamentapp.service.BracketService;
import com.myproject.tournamentapp.service.RoundService;
import com.myproject.tournamentapp.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;


@RestController
@RequestMapping("/api/admin")
@Tag(name="Admin Methods", description="Methods that can be fetched by ADMINs only. These methods require sending the jwt token in the request's Authorization header")
@ApiResponses(value = {
		@ApiResponse(responseCode = "401", description = "Error: Full authentication is required to access this resource. Either wrong jwt token or user doesn't have permission.", content = {
				@Content(mediaType = "application/json", schema = @Schema(implementation = ResponseStatusException.class)) 
		}) 
})
@PreAuthorize("hasAuthority('ADMIN')")
public class RestAdminController {
	@Autowired
	private UserService userService;

	@Autowired
	private RoundService roundService;

	@Autowired
	private BracketService bracketService;

	@Autowired
	private StageRepository srepository;
	
	
	@Operation(summary = "Gets the list of users", description = "The method to fetch the list of all users. List contains users' sensitive data, thus should be treated carefully.")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "OK", content = {
			        @Content(mediaType = "application/json", schema = @Schema(implementation = UsersPageAdminForm.class))
			})
	})
	@RequestMapping(value = "/users", method = RequestMethod.GET)
	public @ResponseBody UsersPageAdminForm getUsersForAdmin() {

		return userService.getUsersForAdmin();

	}

	// method to save new user created by admin
	@Operation(summary = "Add user", description = "The method allows admin to add new user.")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "The user was added to database. If user isn't verified the verification link is sent to email.", content = {
					@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class), examples = {
							@ExampleObject(value = "We sent verification link to your email address")
					}) 
			}), 
			@ApiResponse(responseCode = "406", description = "NOT_ACCEPTABLE, email is in use", content = {
					@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class), examples = {
							@ExampleObject(value = "Email is already in use")
					})
			}),
			@ApiResponse(responseCode = "409", description = "CONFLICT, Username is in use", content = {
					@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class), examples = {
							@ExampleObject(value = "Username is already in use")
					}) 
			}),
			@ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR, the user was added to database, but the smtp service credentials are incorrect or can't reach the service, therefore user should be verified by admin or try signup later.", content = {
					@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class), examples = {
							@ExampleObject(value = "The smtp service authentication fail, ask admin to verify account")
					})
			})
	})
	@RequestMapping(value = "/adduser", method = RequestMethod.POST)
	public ResponseEntity<?> addNewUserByAdmin(@RequestBody AddUserFormForAdmin userForm)
			throws UnsupportedEncodingException, MessagingException {
		
		return userService.addNewUserByAdmin(userForm);
	
	}

	// method to edit user's info by admin
	@Operation(summary = "Updates user's info", description = "This method allows admin to update user's username, email, role. Admin also can verify user and change participant status (only if the bracket wasn't made yet).")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "OK", content = {
					@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class), examples = {
							@ExampleObject(value = "User was updated successfully")
					})
			}), 
			@ApiResponse(responseCode = "400", description = "BAD_REQUEST, no user was find by provided as a path variable id.", content = {
					@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class), examples = {
							@ExampleObject(value = "User cannot be find by the specified user id")
					})
			}),
			@ApiResponse(responseCode = "409", description = "CONFLICT, user id doesn't match id of the user in request body.", content = {
					@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class), examples = {
							@ExampleObject(value = "There is a user id missmatch in request body and path")
					})
			})
	})
	@RequestMapping(value = "/updateuser/{userid}", method = RequestMethod.PUT)
	public ResponseEntity<?> updateUserByAdmin(@PathVariable("userid") Long userId, @RequestBody User updatedUser) {

		return userService.updateUserByAdmin(userId, updatedUser);

	}

	// method to delete the user for admin
	@Operation(summary = "Deletes user", description = "This method allows admin to delete user by id.")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "OK", content = {
					@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class), examples = {
							@ExampleObject(value = "The user was deleted successfully")
					})
			}), 
			@ApiResponse(responseCode = "400", description = "BAD_REQUEST, no user was find by provided as a path variable id.", content = {
					@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class), examples = {
							@ExampleObject(value = "Cannot find user with specified id")
					})
			}),
			@ApiResponse(responseCode = "409", description = "CONFLICT, either trying to delete user with role ADMIN or trying to delete the competitor, when bracket was already made.", content = {
					@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class), examples = {
							@ExampleObject(value = "The competitor cannot be deleted after the competition has started")
					})
			})
	})
	@RequestMapping(value = "/deleteuser/{userid}", method = RequestMethod.DELETE)
	public ResponseEntity<?> deleteUserForAdmin(@PathVariable("userid") Long userId) {

		return userService.deleteUserForAdmin(userId);

	}

	// method to display stages for admin
	@Operation(summary = "Gets stages", description = "The method to fetch the list of all stages for admin")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "OK", content = {
			        @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Stage.class)))
			})
	})
	@RequestMapping(value = "/stages", method = RequestMethod.GET)
	public @ResponseBody List<Stage> getStagesForAdmin() {
		return srepository.findAll();
	}

	// method to display rounds for the admin
	@Operation(summary = "Gets rounds", description = "The method to fetch the list of all rounds for admin, including sensitive data.")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "OK", content = {
			        @Content(mediaType = "application/json", schema = @Schema(implementation = RoundsForAdminForm.class))
			}), 
			@ApiResponse(responseCode = "202", description = "ACCEPTED, can't complete method, as the bracket wasn't made yet", content = {
					@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class), examples = {
							@ExampleObject(value = "The bracket wasn't made yet")
					})
			})
	})
	@RequestMapping(value = "/rounds", method = RequestMethod.GET)
	public @ResponseBody RoundsForAdminForm getRoundsInfoForAdmin() {
		return roundService.getRoundsInfoForAdmin();
	}

	// method to set the result of the round for admin
	@Operation(summary = "Sets round result", description = "This method allows admin to set the round's result by round id.")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "OK", content = {
					@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class), examples = {
							@ExampleObject(value = "The round result was set successfully")
					})
			}), 
			@ApiResponse(responseCode = "400", description = "BAD_REQUEST, path variable round id and request body round id missmatch.", content = {
					@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class), examples = {
							@ExampleObject(value = "Round id missmatch with the one in request body and the one in path")
					})
			}),
			@ApiResponse(responseCode = "406", description = "NOT_ACCEPTABLE, trying to change the round of other stage than the current one.", content = {
					@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class), examples = {
							@ExampleObject(value = "You cannot change the status of the round out of the current stage")
					})
			}),
			@ApiResponse(responseCode = "409", description = "CONFLICT, trying to set the result for rounds with one user, while such rounds are automatically handled.", content = {
					@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class), examples = {
							@ExampleObject(value = "The rounds with only one user shouldn't be handled by admin")
					})
			})
	})
	@RequestMapping(value = "/setresult/{roundid}", method = RequestMethod.PUT)
	public ResponseEntity<?> setRoundResultForAdmin(@PathVariable("roundid") Long roundId, @RequestBody Round round) {

		return roundService.setRoundResultForAdmin(roundId, round);

	}

	// Method to make all users participants (admin)
	@Operation(summary = "Makes all users competitors", description = "This method allows admin to make all verified users with USER role the competitors (Change their participant status).")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "OK", content = {
					@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class), examples = {
							@ExampleObject(value = "The method was invoked successfully")
					})
			}), 
			@ApiResponse(responseCode = "406", description = "NOT_ACCEPTABLE, trying to change the users' participant status, when the bracket was already made or all verified users are already competitors.", content = {
					@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class), examples = {
							@ExampleObject(value = "It's not allowed to use this method once the bracket is made or all verified users are competitors")
					})
			})
	})
	@RequestMapping(value = "/makeallcompetitors", method = RequestMethod.PUT)
	public ResponseEntity<?> makeAllCompetitors() {

		return userService.makeAllCompetitors();

	}

	// method with admin's reset functionality: all users become non-participants
	// and rounds are cleared + deleting unverified accounts
	@Operation(summary = "Resets the tournament progress", description = "This method allows admin to reset the tournament progress: delete unverified users, make all users non-competitors, set users' stage to 'No' stage, delete all rounds and stages.")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "OK", content = {
					@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class), examples = {
							@ExampleObject(value = "Everything was reset successfully")
					})
			}), 
			@ApiResponse(responseCode = "409", description = "CONFLICT, trying to reset before the bracket was made.", content = {
					@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class), examples = {
							@ExampleObject(value = "There is nothing to reset yet")
					})
			})
	})
	@RequestMapping(value = "/reset", method = RequestMethod.PUT)
	public ResponseEntity<?> resetAll() {

		return bracketService.resetAll();

	}

	// method to make draw and create bracket: stages, rounds for the admin
	@Operation(summary = "Makes bracket", description = "This method allows admin to make bracket: change participant status of admins (admins cannot be participants), delete all unverified users, create stages, create rounds.")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "OK", content = {
					@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class), examples = {
							@ExampleObject(value = "Play-off bracket was made successfully")
					})
			}), 
			@ApiResponse(responseCode = "406", description = "NOT_ACCEPTABLE, trying to make bracket, when the bracket was already made or there are less than 3 competitors.", content = {
					@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class), examples = {
							@ExampleObject(value = "The bracket was already made or there are less than 3 competitors")
					})
			})
	})
	@RequestMapping(value = "/makebracket", method = RequestMethod.PUT)
	public ResponseEntity<?> makeBracket() {

		return bracketService.makeBracket();

	}

	// Confirm current stage results functionality for admin and activate the new
	// stage
	@Operation(summary = "Confirms current stage results", description = "This method allows admin to confirm the results of the currrent stage: making the next stage current, assigning users to the rounds of the next stage, if applicable.")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "OK, either the final stage results are confirmed or the current stage results are confirmed.", content = {
					@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class), examples = {
							@ExampleObject(value = "The current stage results were successfully confirmed")
					})
			}), 
			@ApiResponse(responseCode = "406", description = "NOT_ACCEPTABLE, trying to confirm stage results, when not all the rounds have the results.", content = {
					@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class), examples = {
							@ExampleObject(value = "Cannot confirm stage results, untill all the rounds results are saved")
					})
			})
	})
	@RequestMapping(value = "/confirmresults", method = RequestMethod.PUT)
	public ResponseEntity<?> confirmStageResults() {

		return roundService.confirmStageResults();

	}
}
