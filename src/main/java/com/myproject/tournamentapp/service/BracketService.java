package com.myproject.tournamentapp.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.myproject.tournamentapp.model.Round;
import com.myproject.tournamentapp.model.RoundRepository;
import com.myproject.tournamentapp.model.Stage;
import com.myproject.tournamentapp.model.StageRepository;
import com.myproject.tournamentapp.model.User;
import com.myproject.tournamentapp.model.UserRepository;

@Service
public class BracketService {
	@Autowired
	private UserRepository urepository;

	@Autowired
	private RoundRepository rrepository;

	@Autowired
	private StageRepository srepository;

	@Transactional
	public ResponseEntity<?> makeBracket() {
		// check if the bracket wasn't made yet, and there are more than two competitors
		if (!(rrepository.findAll().size() == 0 && urepository.findAllCompetitors().size() > 2))
			return new ResponseEntity<>("The bracket was already made or there are less than 3 competitors",
					HttpStatus.NOT_ACCEPTABLE);

		// making sure that all admins are not competitors
		excludeAdminsFromCompetitors();

		// deleting all unverified users
		deleteUnverifiedUsers();

		// Retrieving list of competitors from user table (only verified users)
		List<User> competitors = urepository.findAllCompetitors();

		int firstStageRoundsQuantity = findAmountOfRoundsInFirstStage(competitors);

		Stage firstStage = createStages(firstStageRoundsQuantity);

		List<User[]> pairs = assignUsersToPairsOfFirstStage(firstStageRoundsQuantity, competitors, firstStage);

		// creating rounds for the first stage and populating them with the users
		createRounds(firstStageRoundsQuantity, pairs, firstStage);

		return new ResponseEntity<>("Play-off bracket was made successfully", HttpStatus.OK);
	}

	private void excludeAdminsFromCompetitors() {
		List<User> admins = urepository.findAllAdmins();

		for (User admin : admins) {
			if (!admin.getIsCompetitor()) {
				continue;
			}
			admin.setIsCompetitor(false);
			admin.setIsOut(true);
			urepository.save(admin);
		}
	}

	private void deleteUnverifiedUsers() {
		List<User> users = urepository.findAll();
		for (User user : users) {
			if (!user.isAccountVerified()) {
				urepository.delete(user);
			}
		}
	}

	private int findAmountOfRoundsInFirstStage(List<User> competitors) {
		int numberOfCompetitors = competitors.size();

		// find the largest power of 2 that is less than or equal to one less than the
		// number of competitors. This number equals to the amount of the rounds of the
		// first stage
		int firstStageRoundsQuantity = Integer.highestOneBit(numberOfCompetitors - 1);

		return firstStageRoundsQuantity;
	}

	private Stage createStages(int firstStageRoundsQuantity) {
		// the amount of total stages = log2 (firstStageRoundsQuantity * 2)
		int totalStages = (int) (Math.log(2 * firstStageRoundsQuantity) / Math.log(2));

		// making the No stage not the current one, because it was by default before
		Stage noStage = srepository.findCurrentStage();
		noStage.setIsCurrent(false);
		srepository.save(noStage);

		// creating stages in accordance with quantity of rounds and competitors

		// first stage needs to be current now
		String firstStageName = "1/" + firstStageRoundsQuantity;
		Stage firstStage = new Stage(firstStageName, true);
		srepository.save(firstStage);

		// Create and save the remaining stages
		for (int stageNumber = totalStages - 1; stageNumber > 1; stageNumber--) {
			String stageName = "1/" + (int) Math.pow(2, stageNumber - 1);
			srepository.save(new Stage(stageName));
		}

		// the final stage is always final;
		srepository.save(new Stage("final"));

		return firstStage;
	}

	private List<User[]> assignUsersToPairsOfFirstStage(int firstStageRoundsQuantity, List<User> competitors,
			Stage firstStage) {
		// creating list for adding couples there
		List<User[]> pairs = new ArrayList<>();
		for (int i = 0; i < firstStageRoundsQuantity; i++) {
			pairs.add(new User[] { null, null });
		}

		// Shuffle the competitors to randomize their positions
		Collections.shuffle(competitors);

		// creating rounds and assigning users to the rounds: making sure that each
		// couple will have at least one user
		User competitor1;
		for (int i = 0; i < firstStageRoundsQuantity; i++) {
			competitor1 = competitors.get(0);
			competitor1.setStage(firstStage);
			pairs.get(i)[0] = competitor1;

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
			competitor2.setStage(firstStage);

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

			pairs.get(coupleIndex)[1] = competitor2;

			urepository.save(competitor2);
			competitors.remove(0);
		}

		return pairs;
	}

	private void createRounds(int firstStageRoundsQuantity, List<User[]> pairs, Stage firstStage) {
		for (int i = 0; i < firstStageRoundsQuantity; i++) {
			if (pairs.get(i)[1] == null) {
				rrepository
						.save(new Round(pairs.get(i)[0].getUsername() + " autowin", pairs.get(i)[0], null, firstStage));
				continue;
			}
			rrepository.save(new Round("No", pairs.get(i)[0], pairs.get(i)[1], firstStage));
		}

		// creating all the rounds until final with null instead of competitors
		int roundCount = firstStageRoundsQuantity / 2;
		while (roundCount > 1) {
			for (int i = 0; i < roundCount; i++) {
				rrepository.save(new Round("No", null, null, srepository.findByStage("1/" + roundCount).get(0)));
			}
			roundCount /= 2;
		}
		rrepository.save(new Round("No", null, null, srepository.findByStage("final").get(0)));
	}

	@Transactional
	public ResponseEntity<?> resetAll() {
		if (rrepository.findAll().size() == 0)
			return new ResponseEntity<>("There is nothing to reset yet", HttpStatus.CONFLICT);

		resetUsers();

		rrepository.deleteAll();

		resetStages();

		return new ResponseEntity<>("Everything was reset successfully", HttpStatus.OK);
	}

	private void resetUsers() {
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
	}

	private void resetStages() {
		// deleting all stages, except 'no' stage
		srepository.deleteAllStages();

		//making the 'no' stage current
		Stage noStage = srepository.findByStage("No").get(0);
		noStage.setIsCurrent(true);
		srepository.save(noStage);
	}

}
