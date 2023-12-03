package com.myproject.tournamentapp.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.myproject.tournamentapp.MyUser;
import com.myproject.tournamentapp.forms.BracketPageInfo;
import com.myproject.tournamentapp.forms.ChangePasswordForm;
import com.myproject.tournamentapp.forms.CompetitorPublicInfo;
import com.myproject.tournamentapp.forms.PersonalInfo;
import com.myproject.tournamentapp.forms.RoundPublicInfo;
import com.myproject.tournamentapp.forms.RoundsForAdminForm;
import com.myproject.tournamentapp.forms.StageForBracketInfo;
import com.myproject.tournamentapp.forms.UsersPageAdminForm;
import com.myproject.tournamentapp.model.Round;
import com.myproject.tournamentapp.model.RoundRepository;
import com.myproject.tournamentapp.model.Stage;
import com.myproject.tournamentapp.model.StageRepository;
import com.myproject.tournamentapp.model.User;
import com.myproject.tournamentapp.model.UserRepository;
import com.myproject.tournamentapp.service.BracketService;

@RestController
public class MainController {
	private static final Logger log = LoggerFactory.getLogger(MainController.class);

	@Autowired
	private BracketService bracketService;
	
	@Autowired
	private UserRepository urepository;

	@Autowired
	private StageRepository srepository;

	@Autowired
	private RoundRepository rrepository;
	
	//method to test testing mechaniques:
	@RequestMapping(value = "/roundsquantitytest", method = RequestMethod.GET)
	public @ResponseBody String getRoundsPublicQuantity() {
		int roundQuantity = rrepository.findAll().size();
		return String.valueOf(roundQuantity);
	}

	// Method to send the quantity of the rounds to the main page to conditionally
	// render buttons
	@RequestMapping(value = "/roundsquantity", method = RequestMethod.GET)
	public @ResponseBody String getRoundsQuantity() {
		int roundQuantity = rrepository.findAll().size();

		return String.valueOf(roundQuantity);
	}

	// method to display competitors on competitors page for authorized user
	@RequestMapping(value = "/competitors", method = RequestMethod.GET)
	@PreAuthorize("isAuthenticated()")
	public @ResponseBody List<CompetitorPublicInfo> listCompetitorsPublicInfo() {
		List<CompetitorPublicInfo> allCompetitors = new ArrayList<>();
		CompetitorPublicInfo competitor;

		List<User> allUsers = urepository.findAll();
		for (User user : allUsers) {
			if (user.getIsCompetitor()) {
				competitor = new CompetitorPublicInfo(user.getUsername(), user.getIsOut(), user.getStage().getStage());
				allCompetitors.add(competitor);
			}
		}

		return allCompetitors;
	}

	// method to display user's personal info on the user page
	@RequestMapping(value = "/competitors/{userid}", method = RequestMethod.GET)
	@PreAuthorize("isAuthenticated()")
	public @ResponseBody PersonalInfo getPersonalInfoById(@PathVariable("userid") Long userId, Authentication auth) {

		// double check authentication
		if (!auth.getPrincipal().getClass().toString().equals("class com.myproject.tournamentapp.MyUser"))
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You are not authorized");

		MyUser myUserInstance = (MyUser) auth.getPrincipal();
		User user = urepository.findByUsername(myUserInstance.getUsername());

		if (user == null || user.getId() != userId)
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have an access to this page");

		List<Round> allRounds = user.getRounds1();
		allRounds.addAll(user.getRounds2());
		
		List<RoundPublicInfo> publicUserRounds = makeRoundsPublic(allRounds);
		
		int roundsQuantity = rrepository.findAll().size();

		PersonalInfo personalInfoInstance = new PersonalInfo(user.getUsername(), user.getEmail(), user.getIsOut(), user.getStage().getStage(),
				user.getIsCompetitor(),roundsQuantity, publicUserRounds);

		return personalInfoInstance;
	}

	// method to change user's personal info: firstname, lastname, username. Email
	// changing requires email verification, so it will either be implemented later,
	// or will not be implemented at all
	@RequestMapping(value = "/updateuser/{userid}", method = RequestMethod.POST)
	@PreAuthorize("authentication.getPrincipal().getId() == #userId")
	public ResponseEntity<?> updateUser(@PathVariable("userid") Long userId, @RequestBody PersonalInfo personalInfo) {
		Optional<User> optionalUser = urepository.findById(userId);

		if (!optionalUser.isPresent())
			return new ResponseEntity<>("Something wrong with authentication", HttpStatus.CONFLICT);

		User user = optionalUser.get();

		if (rrepository.findAll().size() == 0) {
			user.setIsCompetitor(personalInfo.isCompetitor());
			user.setIsOut(!personalInfo.isCompetitor());
		}
		urepository.save(user);

		return new ResponseEntity<>("User info was updated successfully", HttpStatus.OK);
	}

	// Method to display all rounds on the rounds page
	@RequestMapping(value = "/rounds", method = RequestMethod.GET)
	@PreAuthorize("isAuthenticated()")
	public List<RoundPublicInfo> getPublicInfoOfAllRounds() {
		List<Round> allRounds = rrepository.findAllCurrentAndPlayed();
		if (allRounds.isEmpty())
			throw new ResponseStatusException(HttpStatus.ACCEPTED, "The bracket wasn't made yet");

		List<RoundPublicInfo> allPublicRounds = makeRoundsPublic(allRounds);
		
		return allPublicRounds;
	}

	// method to send stages and rounds public info and winner (if exists) for the
	// bracket page: each stage has the list of its rounds;
	@RequestMapping(value = "/bracket", method = RequestMethod.GET)
	@PreAuthorize("isAuthenticated()")
	public @ResponseBody BracketPageInfo getBracketInfo() {
		List<Round> allRounds = rrepository.findAll();
		if (allRounds.isEmpty())
			throw new ResponseStatusException(HttpStatus.ACCEPTED, "The bracket wasn't made yet");

		//all stages except for 'no' stage
		List<Stage> allStages = srepository.findAllStages();
		
		// The list of stages which would include the stage's rounds
		List<StageForBracketInfo> stagesWithRounds = new ArrayList<>();
		StageForBracketInfo stageWithRounds;
		
		// The variable to hold the list of public rounds info:
		List<RoundPublicInfo> currentStagePublicRounds;
		
		for (Stage stage : allStages) {
			currentStagePublicRounds = makeRoundsPublic(rrepository.findRoundsByStage(stage.getStageid()));
			
			stageWithRounds = new StageForBracketInfo(stage.getStage(), stage.getIsCurrent(), currentStagePublicRounds);
			stagesWithRounds.add(stageWithRounds);
		}

		String winner = "";

		if (srepository.findCurrentStage().getStage().equals("No") & rrepository.findAll().size() > 0) {
			Round finalOf = rrepository.findFinal();

			String result = finalOf.getResult();
			if (result.indexOf(" ") != -1) {
				winner = result.substring(0, result.indexOf(" "));
			}
		}

		BracketPageInfo bracketInfo = new BracketPageInfo(stagesWithRounds, winner);

		return bracketInfo;
	}

	// method to change personal's password
	@RequestMapping(value = "/changepassword", method = RequestMethod.POST)
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<?> changePassword(@RequestBody ChangePasswordForm changePasswordForm, Authentication auth) {
		// check authentication;
		if (!auth.getPrincipal().getClass().toString().equals("class com.myproject.tournamentapp.MyUser"))
			return new ResponseEntity<>("Not authenticated", HttpStatus.UNAUTHORIZED);

		MyUser myUserInstance = (MyUser) auth.getPrincipal();
		User user = urepository.findByUsername(myUserInstance.getUsername());
		if (user == null)
			return new ResponseEntity<>("Not authenticated", HttpStatus.UNAUTHORIZED);

		BCryptPasswordEncoder bcEncoder = new BCryptPasswordEncoder();
		// check the old password provided by user
		if (!bcEncoder.matches(changePasswordForm.getOldPassword(), user.getPasswordHash()))
			return new ResponseEntity<>("The old password is incorrect", HttpStatus.FORBIDDEN);

		String newHashedPwd = bcEncoder.encode(changePasswordForm.getNewPassword());
		user.setPasswordHash(newHashedPwd);
		urepository.save(user);

		return new ResponseEntity<>("The password was successfully changed", HttpStatus.OK);
	}
	
	//method for restricting the list of rounds, which by default contains user's info 
	private List<RoundPublicInfo> makeRoundsPublic(List<Round> rounds) {
		List<RoundPublicInfo> publicRounds = new ArrayList<>();
		RoundPublicInfo publicRound;
		// In round entity one of the competitor can be null, so I need to handle it as
		// well
		String username1;
		String username2;

		for (Round round : rounds) {
			username1 = round.getUser1() == null ? null : round.getUser1().getUsername();
			username2 = round.getUser2() == null ? null : round.getUser2().getUsername();
			publicRound = new RoundPublicInfo(username1, username2, round.getStage().getStage(), round.getResult());
			publicRounds.add(publicRound);
		}
		
		return publicRounds;
	}
}
