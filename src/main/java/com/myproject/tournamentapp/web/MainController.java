package com.myproject.tournamentapp.web;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.ui.Model;
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
import com.myproject.tournamentapp.forms.UsersPageAdminForm;
import com.myproject.tournamentapp.model.Round;
import com.myproject.tournamentapp.model.RoundRepository;
import com.myproject.tournamentapp.model.Stage;
import com.myproject.tournamentapp.model.StageRepository;
import com.myproject.tournamentapp.model.User;
import com.myproject.tournamentapp.model.UserRepository;

@RestController
public class MainController {
	private static final Logger log = LoggerFactory.getLogger(MainController.class);

	@Autowired
	private UserRepository urepository;

	@Autowired
	private StageRepository srepository;

	@Autowired
	private RoundRepository rrepository;

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

		PersonalInfo personalInfoInstance = new PersonalInfo(user.getFirstname(), user.getLastname(),
				user.getUsername(), user.getEmail(), user.getIsOut(), user.getStage().getStage(),
				user.getIsCompetitor(), allRounds);

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

		user.setFirstname(personalInfo.getFirstname());
		user.setLastname(personalInfo.getLastname());
		user.setUsername(personalInfo.getUsername());

		if (!(rrepository.findAll().size() > 0))
			user.setIsCompetitor(personalInfo.isCompetitor());

		urepository.save(user);

		return new ResponseEntity<>("User info was updated successfully", HttpStatus.OK);
	}

	// Method to display all rounds on the rounds page
	@RequestMapping(value = "/rounds", method = RequestMethod.GET)
	@PreAuthorize("isAuthenticated()")
	public List<RoundPublicInfo> getPublicInfoOfAllRounds() {
		List<Round> allRounds = rrepository.findAll();
		if (allRounds.isEmpty())
			return null; // return null if the bracket wasn't made yet

		List<RoundPublicInfo> allPublicRounds = new ArrayList<>();
		RoundPublicInfo publicRound;
		// In round entity one of the competitor can be null, so I need to handle it as
		// well
		String username1;
		String username2;

		for (Round round : allRounds) {
			username1 = round.getUser1() == null ? null : round.getUser1().getUsername();
			username2 = round.getUser2() == null ? null : round.getUser2().getUsername();
			publicRound = new RoundPublicInfo(username1, username2, round.getStage().getStage(), round.getResult());
			allPublicRounds.add(publicRound);
		}

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

		List<RoundPublicInfo> allPublicRounds = new ArrayList<>();
		RoundPublicInfo publicRound;
		for (Round round : allRounds) {
			publicRound = new RoundPublicInfo(round.getUser1().getUsername(), round.getUser2().getUsername(),
					round.getStage().getStage(), round.getResult());
			allPublicRounds.add(publicRound);
		}

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
	@RequestMapping(value = "/users", method = RequestMethod.GET)
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

	// functionality to make all users participants (admin)
	@RequestMapping("/makeallcompetitors")
	@PreAuthorize("hasAuthority('ADMIN')")
	public String makeAllCompetitors() {
		/**
		 * making it possible to conduct this method only if there are no rounds and
		 * there is at least one non-competitor
		 */
		if (rrepository.findAll().size() == 0
				& urepository.findAllVerifiedUsers().size() != urepository.findAllCompetitors().size()) {
			List<User> users = urepository.findAllVerifiedUsers();
			for (User user : users) {
				user.setIsCompetitor(true);
				user.setIsOut(false);
				urepository.save(user);
			}
		}
		return "redirect:competitors";
	}

	// Delete user (admin)
	@RequestMapping("/deleteuser/{id}")
	@PreAuthorize("hasAuthority('ADMIN')")
	public String deleteUserId(@PathVariable("id") Long userId, Model model) {
		// making it possible to delete competitors only before draw;
		Optional<User> user = urepository.findById(userId);
		user.ifPresent(userIn -> {
			if (rrepository.findAll().size() == 0 || !userIn.getIsCompetitor()) {
				urepository.deleteById(userId);
			}
		});
		return "redirect:../competitors";
	}

	// Set result for round (admin)
	@RequestMapping("/setresult/{roundid}")
	@PreAuthorize("hasAuthority('ADMIN')")
	public String setResult(@PathVariable("roundid") Long roundId, Model model) {
		Optional<Round> round = rrepository.findById(roundId);

		round.ifPresent(roundIn -> {
			model.addAttribute("round", roundIn);
			model.addAttribute("user1", roundIn.getUser1());
			model.addAttribute("user2", roundIn.getUser2());
			model.addAttribute("stage", roundIn.getStage());
		});
		if (!round.isPresent()) {
			model.addAttribute("round", null);
			model.addAttribute("user1", null);
			model.addAttribute("user2", null);
			model.addAttribute("stage", null);
		}
		// results can be set only for current stage rounds
		return rrepository.findRoundById(roundId).getStage() == srepository.findCurrentStage() ? "setresult"
				: "redirect:../competitors";
	}

	// Save new round (admin)
	@RequestMapping(value = "/saveround", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('ADMIN')")
	public String saveRound(Round round) {

		// making it possible to save rounds of current stage only;
		if (round.getStage() == srepository.findCurrentStage()) {
			rrepository.save(round);

			// changing the status of looser
			String result = round.getResult();
			if (result.indexOf(" ") != -1) {
				result = result.substring(0, result.indexOf(" "));
				User looser;
				if (round.getUser1().getUsername().equals(result)) {
					looser = urepository.findByUsername(round.getUser2().getUsername());
				} else {
					looser = urepository.findByUsername(round.getUser1().getUsername());
				}

				looser.setIsOut(true);
				urepository.save(looser);
			}
		}
		return "redirect:rounds";
	}

	// Show all stages (admin)
	@RequestMapping("/stages")
	@PreAuthorize("hasAuthority('ADMIN')")
	public String stageList(Model model) {
		model.addAttribute("stages", srepository.findAll());
		return "stagelist";
	}

	// Confirm current stage results (ADMIN)
	@RequestMapping("/nextstage")
	@PreAuthorize("hasAuthority('ADMIN')")
	public String nextStage() {
		// checking if if is the situation to confirm stage results
		int playedRoundshere = rrepository.quantityOfPlayed();
		int allCurrRoundshere = rrepository.findQuantityOfCurrent();
		boolean stageStatus = playedRoundshere == allCurrRoundshere
				&& !srepository.findCurrentStage().getStage().equals("No");
		if (stageStatus && rrepository.findAll().size() > 0) {
			int playedRounds = rrepository.quantityOfPlayed();
			int allCurrRounds = rrepository.findQuantityOfCurrent();
			if (playedRounds == allCurrRounds) {
				Stage currStageBefore = playedRounds > 1 ? srepository.findByStage("1/" + playedRounds).get(0)
						: srepository.findByStage("final").get(0);
				Stage currStage;
				if (playedRounds > 2) {
					currStage = srepository.findByStage("1/" + playedRounds / 2).get(0);
				} else if (playedRounds == 2) {
					currStage = srepository.findByStage("final").get(0);
				} else {
					currStage = srepository.findByStage("No").get(0);
				}
				currStageBefore.setIsCurrent(false);
				currStage.setIsCurrent(true);
				srepository.save(currStageBefore);
				srepository.save(currStage);

				// populating games of next stage with winners of current stage:
				if (playedRounds > 1) {
					List<Round> currentRounds = rrepository.findPlayedCurrentRounds();
					List<Round> previousRounds = rrepository.findRoundsByStage(currStageBefore.getStageid());

					Round currRound;
					User player1;
					User player2;
					for (int i = 0; i < currentRounds.size(); i++) {
						currRound = currentRounds.get(i);
						player1 = urepository.findByUsername(previousRounds.get(i * 2).getResult().substring(0,
								previousRounds.get(i * 2).getResult().indexOf(" ")));
						player2 = urepository.findByUsername(previousRounds.get(i * 2 + 1).getResult().substring(0,
								previousRounds.get(i * 2 + 1).getResult().indexOf(" ")));

						currRound.setUser1(player1);
						player1.setStage(currStage);

						currRound.setUser2(player2);
						player2.setStage(currStage);

						rrepository.save(currRound);
						urepository.save(player1);
						urepository.save(player2);
					}
				}
			}
		}
		return "redirect:rounds";
	}

	// -------------------------------------
	// Making play-off bracket (ADMIN)
	/**
	 * 
	 * 
	 */

	// IMPORTANT NB: don't forget to make all unverified users non-competitors
	/**
	 * 
	 * 
	 */
	@RequestMapping(value = "/makedraw", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('ADMIN')")
	public String makeBracket() {
		// checking if draw wasn't made before and there are more than 2 competitors.
		if (rrepository.findAll().size() == 0 & urepository.findAllCompetitors().size() > 2) {
			// Retrieving list of competitors from user table
			List<User> competitors = urepository.findAllCompetitors();

			// variable of extent
			int x = 1;

			// finding amount of rounds
			while (competitors.size() > Math.round(Math.pow(2, x))) {
				x++;
			}
			Long roundsQuantity = (Math.round(Math.pow(2, x - 1)));

			// creating stages in accordance with quantity of rounds and competitors
			boolean firstStage = true;
			Stage no = srepository.findCurrentStage();
			no.setIsCurrent(false);
			srepository.save(no);

			while (x > 1) {
				String stage = "1/" + Math.round(Math.pow(2, x - 1));
				if (firstStage) {
					srepository.save(new Stage(stage, Date.valueOf("2022-01-01"), Date.valueOf("2022-12-31"), true));
					firstStage = false;
				} else {
					srepository.save(new Stage(stage, Date.valueOf("2022-01-01"), Date.valueOf("2022-12-31")));
				}
				x--;
			}
			srepository.save(new Stage("final", Date.valueOf("2022-01-01"), Date.valueOf("2022-12-31")));

			// creating list for adding couples there
			List<User[]> couples = new ArrayList<>();
			for (int i = 0; i < roundsQuantity; i++) {
				couples.add(new User[] { null, null });
			}

			// creating rounds and making draw using random method of Math class
			int index;
			for (int i = 0; i < roundsQuantity; i++) {
				index = (int) (Math.random() * (competitors.size()));
				competitors.get(index).setStage(srepository.findByStage("1/" + roundsQuantity).get(0));
				couples.get(i)[0] = competitors.get(index);
				competitors.remove(index);
			}
			// creating variable for holding competitors size
			int cycleLength = competitors.size();

			for (int i = 0; i < cycleLength; i++) {
				index = (int) (Math.random() * (competitors.size()));
				competitors.get(index).setStage(srepository.findByStage("1/" + roundsQuantity).get(0));
				couples.get((int) ((i % 2) * roundsQuantity - (i - i / 2) * (2 * (i % 2) - 1)))[1] = competitors
						.get(index);
				competitors.remove(index);
			}

			for (int i = 0; i < roundsQuantity; i++) {
				if (couples.get(i)[0] != null && couples.get(i)[1] != null) {
					rrepository.save(new Round("No", couples.get(i)[0], couples.get(i)[1],
							srepository.findByStage("1/" + roundsQuantity).get(0)));
				} else if (couples.get(i)[0] == null) {
					rrepository.save(new Round(couples.get(i)[1].getUsername() + " autowin", couples.get(i)[0],
							couples.get(i)[1], srepository.findByStage("1/" + roundsQuantity).get(0)));
				} else {
					rrepository.save(new Round(couples.get(i)[0].getUsername() + " autowin", couples.get(i)[0],
							couples.get(i)[1], srepository.findByStage("1/" + roundsQuantity).get(0)));
				}
			}

			// creating all the rounds until final with null competitors
			Long xx = roundsQuantity / 2;
			while (xx > 1) {
				for (int i = 0; i < xx; i++) {
					rrepository.save(new Round("No", null, null, srepository.findByStage("1/" + xx).get(0)));
				}
				xx /= 2;
			}
			rrepository.save(new Round("No", null, null, srepository.findByStage("final").get(0)));
		}

		return "redirect:competitors";
	}

	// Reset functionality: all users become non-participants and rounds are cleared
	// (admin)
	@Transactional
	@RequestMapping("/reset")
	@PreAuthorize("hasAuthority('ADMIN')")
	public String resetAll() {
		if (!(rrepository.findAll().size() == 0)) {
			List<User> users = urepository.findAll();
			for (User user : users) {
				user.setStage(srepository.findByStage("No").get(0));
				user.setIsCompetitor(false);
				user.setIsOut(true);
				if (!user.isAccountVerified()) {
					urepository.delete(user);
				} else {
					urepository.save(user);
				}
			}

			rrepository.deleteAll();

			srepository.deleteAllStages();
			Stage noStage = srepository.findByStage("No").get(0);
			noStage.setIsCurrent(true);
			srepository.save(noStage);
		}

		return "redirect:competitors";
	}

}
