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

import io.swagger.v3.oas.annotations.tags.Tag;


@RestController
@RequestMapping("/api/admin")
@Tag(name="Admin Methods", description="Methods that can be fetched by ADMINs only. These methods require sending the jwt token in the request's Authorization header")
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

	@RequestMapping(value = "/users", method = RequestMethod.GET)
	public @ResponseBody UsersPageAdminForm getUsersForAdmin() {

		return userService.getUsersForAdmin();

	}

	// method to save new user created by admin
	@RequestMapping(value = "/adduser", method = RequestMethod.POST)
	public ResponseEntity<?> addNewUserByAdmin(@RequestBody AddUserFormForAdmin userForm)
			throws UnsupportedEncodingException, MessagingException {
		
		return userService.addNewUserByAdmin(userForm);
	
	}

	// method to edit user's info by admin
	@RequestMapping(value = "/updateuser/{userid}", method = RequestMethod.PUT)
	public ResponseEntity<?> updateUserByAdmin(@PathVariable("userid") Long userId, @RequestBody User updatedUser) {

		return userService.updateUserByAdmin(userId, updatedUser);

	}

	// method to delete the user for admin
	@RequestMapping(value = "/deleteuser/{userid}", method = RequestMethod.DELETE)
	public ResponseEntity<?> deleteUserForAdmin(@PathVariable("userid") Long userId) {

		return userService.deleteUserForAdmin(userId);

	}

	// method to display stages for admin
	@RequestMapping(value = "/stages", method = RequestMethod.GET)
	public @ResponseBody List<Stage> getStagesForAdmin() {
		return srepository.findAll();
	}

	// method to display rounds for the admin
	@RequestMapping(value = "/rounds", method = RequestMethod.GET)
	public @ResponseBody RoundsForAdminForm getRoundsInfoForAdmin() {
		return roundService.getRoundsInfoForAdmin();
	}

	// method to set the result of the round for admin
	@RequestMapping(value = "/setresult/{roundid}", method = RequestMethod.PUT)
	public ResponseEntity<?> setRoundResultForAdmin(@PathVariable("roundid") Long roundId, @RequestBody Round round) {

		return roundService.setRoundResultForAdmin(roundId, round);

	}

	// Method to make all users participants (admin)
	@RequestMapping(value = "/makeallcompetitors", method = RequestMethod.PUT)
	public ResponseEntity<?> makeAllCompetitors() {

		return userService.makeAllCompetitors();

	}

	// method with admin's reset functionality: all users become non-participants
	// and rounds are cleared + deleting unverified accounts
	@RequestMapping(value = "/reset", method = RequestMethod.PUT)
	public ResponseEntity<?> resetAll() {

		return bracketService.resetAll();

	}

	// method to make draw and create bracket: stages, rounds for the admin
	@RequestMapping(value = "/makebracket", method = RequestMethod.PUT)
	public ResponseEntity<?> makeBracket() {

		return bracketService.makeBracket();

	}

	// Confirm current stage results functionality for admin and activate the new
	// stage
	@RequestMapping(value = "/confirmresults", method = RequestMethod.PUT)
	public ResponseEntity<?> confirmStageResults() {

		return roundService.confirmStageResults();

	}
}
