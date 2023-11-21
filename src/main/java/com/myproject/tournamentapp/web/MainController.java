package com.myproject.tournamentapp.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
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

import com.myproject.tournamentapp.MyUser;
import com.myproject.tournamentapp.forms.BracketPageInfo;
import com.myproject.tournamentapp.forms.ChangePasswordForm;
import com.myproject.tournamentapp.forms.CompetitorPublicInfo;
import com.myproject.tournamentapp.forms.PersonalInfo;
import com.myproject.tournamentapp.forms.RoundPublicInfo;
import com.myproject.tournamentapp.forms.RoundsForAdminForm;
import com.myproject.tournamentapp.forms.UsersPageAdminForm;
import com.myproject.tournamentapp.model.Round;
import com.myproject.tournamentapp.model.RoundRepository;
import com.myproject.tournamentapp.model.Stage;
import com.myproject.tournamentapp.model.StageRepository;
import com.myproject.tournamentapp.model.User;
import com.myproject.tournamentapp.model.UserRepository;

@RestController
public class MainController {
//	private static final Logger log = LoggerFactory.getLogger(MainController.class);

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
	@PreAuthorize("isAuthenticated()")
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
			return null;

		MyUser myUserInstance = (MyUser) auth.getPrincipal();
		User user = urepository.findByUsername(myUserInstance.getUsername());

		if (user == null || user.getId() != userId)
			return null;

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

		user.setUsername(personalInfo.getUsername());

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
			return null; // return null if the bracket wasn't made yet

		List<RoundPublicInfo> allPublicRounds = makeRoundsPublic(allRounds);
		
		return allPublicRounds;
	}

	// method to send stages and rounds public info and winner (if exists) for the
	// bracket page
	@RequestMapping(value = "/bracket", method = RequestMethod.GET)
	@PreAuthorize("isAuthenticated()")
	public @ResponseBody BracketPageInfo getBracketInfo() {
		List<Round> allRounds = rrepository.findAll();
		if (allRounds.isEmpty())
			return null; // return null if the bracket wasn't made yet

		List<RoundPublicInfo> allPublicRounds = makeRoundsPublic(allRounds);

		List<Stage> allStages = srepository.findAllStages();

		String winner = "";

		if (srepository.findCurrentStage().getStage().equals("No") & rrepository.findAll().size() > 0) {
			Round finalOf = rrepository.findFinal();

			String result = finalOf.getResult();
			if (result.indexOf(" ") != -1) {
				winner = result.substring(0, result.indexOf(" "));
			}
		}

		BracketPageInfo bracketInfo = new BracketPageInfo(allStages, allPublicRounds, winner);

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

	// Show all users
	@RequestMapping(value = "/admin/users", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('ADMIN')")
	public @ResponseBody UsersPageAdminForm getUsersForAdmin() {
		boolean isBracketMade = rrepository.findAll().size() != 0;

		List<User> users = urepository.findAll();

		// the flag to indicate whether to show make bracket button for admin or not.
		// The bracket can be made if it wasn't made before and there are more than 2
		// competitors
		boolean showMakeBracket = !isBracketMade && urepository.findAllCompetitors().size() > 2;

		// the flag to indicate whether to show make all competitors button for admin.
		// Admin can make all users competitors only if the bracket was not made yet and
		// there are more verified users than there are already competitors
		boolean showMakeAllCompetitors = !isBracketMade
				&& urepository.findAllVerifiedUsers().size() != urepository.findAllCompetitors().size();

		// flag to indicate whether to show reset button on admin's users page. The
		// reset can be activated only if there was a bracket made already
		boolean showReset = isBracketMade;

		return new UsersPageAdminForm(users, showMakeBracket, showMakeAllCompetitors, showReset);
	}

	// method to edit user's info by admin
	@RequestMapping(value = "/admin/updateuser/{userid}", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('ADMIN')")
	public ResponseEntity<?> updateUserByAdmin(@PathVariable("userid") long userId, @RequestBody User updatedUser) {
		Optional<User> optionalCurrentUser = urepository.findById(userId);

		if (!optionalCurrentUser.isPresent())
			return new ResponseEntity<>("User cannot be find by the specified user id", HttpStatus.BAD_REQUEST);

		User currentUser = optionalCurrentUser.get();

		if (updatedUser.getId() != currentUser.getId())
			return new ResponseEntity<>("User id missmatch in request bady and path", HttpStatus.CONFLICT);

		currentUser.setUsername(updatedUser.getUsername());
		currentUser.setRole(updatedUser.getRole());
		if (updatedUser.isAccountVerified() && !currentUser.isAccountVerified()) {
			currentUser.setAccountVerified(true);
			currentUser.setVerificationCode(null);
		}
		if (rrepository.findAll().size() == 0) {
			currentUser.setIsOut(!updatedUser.getIsCompetitor());
			currentUser.setIsCompetitor(updatedUser.getIsCompetitor());
		}
		urepository.save(currentUser);
		return new ResponseEntity<>("User was updated successfully", HttpStatus.OK);
	}

	// method to delete the user for admin
	@RequestMapping(value = "/admin/deleteuser/{userid}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('ADMIN')")
	public ResponseEntity<?> deleteUserForAdmin(@PathVariable("userid") Long userId) {
		Optional<User> optionalUser = urepository.findById(userId);

		if (!optionalUser.isPresent())
			return new ResponseEntity<>("Cannot find user with specified id", HttpStatus.BAD_REQUEST);

		User user = optionalUser.get();

		// user can be deleted only if the bracket wasn't made yet or the user is not a
		// competitor and the role of the user is not ADMIN
		if (user.getRole() == "ADMIN")
			return new ResponseEntity<>("You cannot delete ADMIN", HttpStatus.UNAUTHORIZED);

		if (rrepository.findAll().size() > 0 && user.getIsCompetitor())
			return new ResponseEntity<>("The competitor cannot be deleted after the competition has started",
					HttpStatus.CONFLICT);

		urepository.deleteById(userId);

		return new ResponseEntity<>("The user was deleted successfully", HttpStatus.OK);
	}

	// method to display stages for admin
	@RequestMapping(value = "/admin/stages", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('ADMIN')")
	public @ResponseBody List<Stage> getStagesForAdmin() {
		return srepository.findAll();
	}

	// method to display rounds for the admin
	@RequestMapping(value = "/admin/rounds", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('ADMIN')")
	public @ResponseBody RoundsForAdminForm getRoundsInfoForAdmin() {
		List<Round> allRounds = rrepository.findAll();

		if (allRounds.isEmpty())
			return null;

		// Checking, if all the games in a current stage were played to allow admin to
		// confirm stage results
		int playedInCurrentStageRounds = rrepository.quantityOfPlayedInCurrentStage();
		int allCurrentStageRounds = rrepository.findQuantityOfGamesInCurrentStage();

		// This flag indicates whether to show confirm stage results button or not;
		boolean isCurrentStageFinished = playedInCurrentStageRounds == allCurrentStageRounds
				&& !srepository.findCurrentStage().getStage().equals("No");

		// this flag indicates whether to show set result column or not
		boolean doesWinnerExist = srepository.findCurrentStage().getStage().equals("No")
				&& rrepository.findAll().size() > 0;

		RoundsForAdminForm roundsFormAdmin = new RoundsForAdminForm(allRounds, doesWinnerExist, isCurrentStageFinished);

		return roundsFormAdmin;
	}

	// method to set the result of the round for admin
	@RequestMapping(value = "/admin/setresult/{roundid}", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('ADMIN')")
	public ResponseEntity<?> setRoundResultForAdmin(@PathVariable("roundid") Long roundId, @RequestBody Round round) {
		Round localRound = rrepository.findRoundById(roundId);

		if (localRound == null || roundId != round.getRoundid())
			return new ResponseEntity<>("Round id missmatch with the one in request body and the one in path",
					HttpStatus.BAD_REQUEST);

		if (localRound.getStage() != srepository.findCurrentStage())
			return new ResponseEntity<>("You cannot change the status of the round out of the current stage",
					HttpStatus.NOT_ACCEPTABLE);

		if (localRound.getUser1() == null || localRound.getUser2() == null)
			return new ResponseEntity<>("The rounds with only one user shouldn't be handled by admin",
					HttpStatus.UNAUTHORIZED);

		localRound.setResult(round.getResult());
		rrepository.save(localRound);

		// now as we are saving the results to the database and everyone is able to see
		// the results, we should update the competitors status as well
		String result = localRound.getResult();
		User winner;
		User looser;

		// in case of No result there is no winner;
		if (result.indexOf(" ") == -1) {
			winner = localRound.getUser1();
			looser = localRound.getUser2();
			looser.setIsOut(false);
		} else {
			// the results are stored in the following pattern: <winner_username> win. E.g.
			// "alex win", so the winner's username is right before the whitespace
			String winnerUsername = result.substring(0, result.indexOf(" "));
			if (localRound.getUser1().getUsername().equals(winnerUsername)) {
				winner = localRound.getUser1();
				looser = localRound.getUser2();
			} else {
				winner = localRound.getUser2();
				looser = localRound.getUser1();
			}
			looser.setIsOut(true);
		}
		winner.setIsOut(false);

		urepository.save(looser);
		urepository.save(winner);
		return new ResponseEntity<>("The round result was set successfully", HttpStatus.OK);
	}

	// Method to make all users participants (admin)
	@RequestMapping(value = "/admin/makeallcompetitors", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('ADMIN')")
	public ResponseEntity<?> makeAllCompetitors() {
		// check if the bracket was already made and if there are some verified users
		// who are not competitors yet, assuming only users are participants;
		if (rrepository.findAll().size() != 0
				|| urepository.findAllVerifiedUsers().size() == urepository.findAllCompetitors().size())
			return new ResponseEntity<>(
					"It's not allowed to use this method once the bracket is made or all verified users are competitors",
					HttpStatus.NOT_ACCEPTABLE);

		List<User> users = urepository.findAllVerifiedUsers();
		for (User user : users) {
			user.setIsCompetitor(true);
			user.setIsOut(false);
			urepository.save(user);
		}
		return new ResponseEntity<>("The method was invoked successfully", HttpStatus.OK);
	}

	// method with admin's reset functionality: all users become non-participants
	// and rounds are cleared + deleting unverified accounts
	@Transactional
	@RequestMapping(value = "/admin/reset", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('ADMIN')")
	public ResponseEntity<?> resetAll() {
		if (rrepository.findAll().size() == 0)
			return new ResponseEntity<>("There is nothing to reset yet", HttpStatus.CONFLICT);

		List<User> users = urepository.findAll();
		for (User user : users) {
			if (!user.isAccountVerified()) {
				urepository.delete(user);
				continue;
			}

			user.setStage(srepository.findByStage("No").get(0));
			user.setIsCompetitor(false);
			user.setIsOut(true);
			urepository.save(user);
		}

		rrepository.deleteAll();

		srepository.deleteAllStages();
		Stage noStage = srepository.findByStage("No").get(0);
		noStage.setIsCurrent(true);
		srepository.save(noStage);

		return new ResponseEntity<>("Everything was reset successfully", HttpStatus.OK);
	}

	// method to make draw and create bracket: stages, rounds for the admin
	@Transactional
	@RequestMapping(value = "/admin/makebracket", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('ADMIN')")
	public ResponseEntity<?> makeBracket() {
		// check if the bracket wasn't made yet, and there are more than two competitors
		if (rrepository.findAll().size() != 0 || urepository.findAllCompetitors().size() <= 2)
			return new ResponseEntity<>("The bracket was already made or there are less than 3 competitors",
					HttpStatus.NOT_ACCEPTABLE);

		// making sure that all admins are not competitors
		List<User> admins = urepository.findAllAdmins();

		for (User admin : admins) {
			if (!admin.getIsCompetitor()) {
				continue;
			}
			admin.setIsCompetitor(false);
			admin.setIsOut(true);
			urepository.save(admin);
		}

		// deleting all unverified users
		List<User> users = urepository.findAll();
		for (User user : users) {
			if (!user.isAccountVerified()) {
				urepository.delete(user);
			}
		}

		// Retrieving list of competitors from user table (only verified users)
		List<User> competitors = urepository.findAllCompetitors();

		int numberOfCompetitors = competitors.size();

		// find the largest power of 2 that is less than or equal to one less than the
		// number of competitors. This number equals to the amount of the rounds of the
		// first stage
		int firstStageRoundsQuantity = Integer.highestOneBit(numberOfCompetitors - 1);

		// the amount of total stages = log2 (firstStageRoundsQuantity * 2)
		int totalStages = (int) (Math.log(2 * firstStageRoundsQuantity) / Math.log(2));

		// making the No stage not the current one, because it was by default before
		Stage noStage = srepository.findCurrentStage();
		noStage.setIsCurrent(false);
		srepository.save(noStage);

		// creating stages in accordance with quantity of rounds and competitors

		// first stage needs to be current now
		String firstStageName = "1/" + firstStageRoundsQuantity;
		Stage currentStage = new Stage(firstStageName, true);
		srepository.save(currentStage);

		// Create and save the remaining stages
		for (int stageNumber = totalStages - 1; stageNumber > 0; stageNumber--) {
			String stageName = "1/" + (int) Math.pow(2, stageNumber - 1);
			srepository.save(new Stage(stageName));
		}

		// the final stage is always final;
		srepository.save(new Stage("final"));

		// creating list for adding couples there
		List<User[]> couples = new ArrayList<>();
		for (int i = 0; i < firstStageRoundsQuantity; i++) {
			couples.add(new User[] { null, null });
		}

		// Shuffle the competitors to randomize their positions
		Collections.shuffle(competitors);

		// creating rounds and assigning users to the rounds: making sure that each
		// couple will have at least one user
		User competitor1;
		for (int i = 0; i < firstStageRoundsQuantity; i++) {
			competitor1 = competitors.get(0);
			competitor1.setStage(currentStage);
			couples.get(i)[0] = competitor1;

			urepository.save(competitor1);
			competitors.remove(0);
		}

		// creating variable for holding left competitors size
		int leftCompetitorsNumber = competitors.size();

		User competitor2;
		// populating the couples with the second competitor till there are no more
		// competitors left
		for (int i = 0; i < leftCompetitorsNumber; i++) {
			competitor2 = competitors.get(0);
			competitor2.setStage(currentStage);

			// Assigns remaining users to different edges of the bracket:
			// - For even 'i', divides 'i' by 2 for even-indexed positions.
			// - For odd 'i', adjusts position by subtracting from
			// 'firstStageRoundsQuantity'.
			int coupleIndex;
			if (i % 2 == 0) {
				coupleIndex = i / 2;
			} else {
				coupleIndex = (firstStageRoundsQuantity - (i + 1) / 2);
			}

			couples.get(coupleIndex)[1] = competitor2;

			urepository.save(competitor2);
			competitors.remove(0);
		}

		// creating rounds for the first stage and populating them with the users
		for (int i = 0; i < firstStageRoundsQuantity; i++) {
			if (couples.get(i)[1] == null) {
				rrepository.save(
						new Round(couples.get(i)[0].getUsername() + " autowin", couples.get(i)[0], null, currentStage));
				continue;
			}
			rrepository.save(new Round("No", couples.get(i)[0], couples.get(i)[1], currentStage));
		}

		// creating all the rounds until final with null instead of competitors
		int roundCount  = firstStageRoundsQuantity / 2;
		while (roundCount  > 1) {
			for (int i = 0; i < roundCount ; i++) {
				rrepository.save(new Round("No", null, null, srepository.findByStage("1/" + roundCount).get(0)));
			}
			roundCount /= 2;
		}
		rrepository.save(new Round("No", null, null, srepository.findByStage("final").get(0)));

		return new ResponseEntity<>("Play-off bracket was made successfully", HttpStatus.OK);
	}

	// Confirm current stage results functionality for admin and activate the new
	// stage
	@RequestMapping(value = "/admin/confirmresults", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('ADMIN')")
	public ResponseEntity<?> confirmStageResults() {
		int playedRoundsInCurrent = rrepository.quantityOfPlayedInCurrentStage();
		int allCurrRoundsInCurrent = rrepository.findQuantityOfGamesInCurrentStage();	
		
		// If the played rounds in the current stage amount equals to the all current stage rounds and the current stage is not 'No' stage
		boolean isCurrentStageFinished = playedRoundsInCurrent == allCurrRoundsInCurrent
						&& !srepository.findCurrentStage().getStage().equals("No");
		
		if (!isCurrentStageFinished) return new ResponseEntity<>("Cannot confirm stage results, untill all the rounds results are saved", HttpStatus.NOT_ACCEPTABLE);
		
		//finding the current stage, that should be processed and making this stage no more current
		Stage stageToFinnish = srepository.findCurrentStage();
		
		//finding the next stage based on the played rounds quantity and making this stage current
		Stage newCurrentStage;
		
		if (playedRoundsInCurrent > 2) {
			newCurrentStage = srepository.findByStage("1/" + playedRoundsInCurrent / 2).get(0);
		} else if (playedRoundsInCurrent == 2) {
			newCurrentStage = srepository.findByStage("final").get(0);
		} else {
			newCurrentStage = srepository.findByStage("No").get(0);
		}
		stageToFinnish.setIsCurrent(false);
		newCurrentStage.setIsCurrent(true);
		srepository.save(stageToFinnish);
		srepository.save(newCurrentStage);

		// if the stage was a final one, there is no need to populate any more rounds with the users
		if (playedRoundsInCurrent == 1) return new ResponseEntity<>("The final stage resluts were successfully confirmed", HttpStatus.OK);
			
		// populating rounds of the next stage with winners of current stage (if it wasn't a final round):
		
		// receiving the list of the rounds of the new stage (to populate them)
		List<Round> currentRounds = rrepository.findCurrentRounds();
		//receiving the list of the rounds of the previous stage to get winners;
		List<Round> previousRounds = rrepository.findRoundsByStage(stageToFinnish.getStageid());

		//declaring the variables to be handled in the cycle
		Round currentRound;
		String result1;
		String result2;
		
		String usernameOfWinner1;
		String usernameOfWinner2;
		
		User player1;
		User player2;
		
		for (int i = 0; i < currentRounds.size(); i++) {
			currentRound = currentRounds.get(i);
			
			//receiving the results of the previous rounds
			result1 = previousRounds.get(i * 2).getResult();
			result2 = previousRounds.get(i * 2 + 1).getResult();
			
			//Assuming the result is stored in the format of <Username> <'win'/'autowin'>, the username can be received by spliting the string with whitespace
			usernameOfWinner1 = result1.substring(0, result1.indexOf(" "));
			usernameOfWinner2 = result2.substring(0, result2.indexOf(" "));
			
			player1 = urepository.findByUsername(usernameOfWinner1);
			player2 = urepository.findByUsername(usernameOfWinner2);

			//assigning players to the round and changing the current stage of the player;
			currentRound.setUser1(player1);
			player1.setStage(newCurrentStage);

			currentRound.setUser2(player2);
			player2.setStage(newCurrentStage);

			rrepository.save(currentRound);
			urepository.save(player1);
			urepository.save(player2);
		}
		
		return new ResponseEntity<>("The current stage results were successfully confirmed", HttpStatus.OK);
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
