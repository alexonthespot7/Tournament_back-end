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

import com.myproject.tournamentapp.forms.BracketPageInfo;
import com.myproject.tournamentapp.forms.ChangePasswordForm;
import com.myproject.tournamentapp.forms.CompetitorPublicInfo;
import com.myproject.tournamentapp.forms.PersonalInfo;
import com.myproject.tournamentapp.forms.RoundPublicInfo;
import com.myproject.tournamentapp.service.BracketService;
import com.myproject.tournamentapp.service.RoundService;
import com.myproject.tournamentapp.service.UserService;

@RestController
@RequestMapping("/api")
@PreAuthorize("isAuthenticated()")
public class RestUserController {

	@Autowired
	private UserService userService;

	@Autowired
	private RoundService roundsService;

	@Autowired
	private BracketService bracketService;
	

	// method to display competitors on competitors page for authorized user
	@RequestMapping(value = "/competitors", method = RequestMethod.GET)
	public @ResponseBody List<CompetitorPublicInfo> listCompetitorsPublicInfo() {

		return userService.listCompetitorsPublicInfo();

	}

	// method to display user's personal info on the user page
	@RequestMapping(value = "/competitors/{userid}", method = RequestMethod.GET)
	public @ResponseBody PersonalInfo getPersonalInfoById(@PathVariable("userid") Long userId, Authentication auth) {

		return userService.getPersonalInfoById(userId, auth);

	}

	// method to change user's competitor status
	@RequestMapping(value = "/updateuser/{userid}", method = RequestMethod.PUT)
	@PreAuthorize("authentication.getPrincipal().getId() == #userId")
	public ResponseEntity<?> updateUser(@PathVariable("userid") Long userId, @RequestBody PersonalInfo personalInfo) {

		return userService.updateUser(userId, personalInfo);

	}

	// method to change personal's password
	@RequestMapping(value = "/changepassword", method = RequestMethod.PUT)
	public ResponseEntity<?> changePassword(@RequestBody ChangePasswordForm changePasswordForm, Authentication auth) {
		
		return userService.changePassword(changePasswordForm, auth);
		
	}

	// Method to display all rounds on the rounds page
	@RequestMapping(value = "/rounds", method = RequestMethod.GET)
	public @ResponseBody List<RoundPublicInfo> getPublicInfoOfAllRounds() {

		return roundsService.getPublicInfoOfAllRounds();

	}

	// method to send stages and rounds public info and winner (if exists) for the
	// bracket page: each stage has the list of its rounds;
	@RequestMapping(value = "/bracket", method = RequestMethod.GET)
	public @ResponseBody BracketPageInfo getBracketInfo() {

		return bracketService.getBracketInfo();

	}

}
