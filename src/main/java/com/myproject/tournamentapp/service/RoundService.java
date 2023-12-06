package com.myproject.tournamentapp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.myproject.tournamentapp.forms.RoundPublicInfo;
import com.myproject.tournamentapp.forms.RoundsForAdminForm;
import com.myproject.tournamentapp.model.Round;
import com.myproject.tournamentapp.model.RoundRepository;
import com.myproject.tournamentapp.model.Stage;
import com.myproject.tournamentapp.model.StageRepository;
import com.myproject.tournamentapp.model.User;
import com.myproject.tournamentapp.model.UserRepository;

@Service
public class RoundService {
	@Autowired
	private RoundRepository rrepository;

	@Autowired
	private StageRepository srepository;

	@Autowired
	private UserRepository urepository;

	@Autowired
	private QuantityService quantityService;

	@Autowired
	private MakeRoundsPublicService makeRoundsPublicService;

	public String getRoundsQuantity() {
		List<Round> rounds = rrepository.findAll();
		int roundsQuantity = rounds.size();

		return String.valueOf(roundsQuantity);
	}

	public List<RoundPublicInfo> getPublicInfoOfAllRounds() {
		List<Round> allRounds = rrepository.findAllCurrentAndPlayed();

		if (allRounds.isEmpty())
			throw new ResponseStatusException(HttpStatus.ACCEPTED, "The bracket wasn't made yet");

		List<RoundPublicInfo> allPublicRounds = makeRoundsPublicService.makeRoundsPublic(allRounds);

		return allPublicRounds;
	}

	public RoundsForAdminForm getRoundsInfoForAdmin() {

		List<Round> allRounds = rrepository.findAll();

		if (allRounds.isEmpty())
			throw new ResponseStatusException(HttpStatus.ACCEPTED, "The bracket wasn't made yet");

		boolean isCurrentStageFinished = this.findIsCurrentStageFinished();

		boolean isCurrentStageNoStage = this.findIsCurrentStageNoStage();
		int roundsQuantity = quantityService.findRoundsQuantity();

		// this flag indicates whether to show set result column or not
		boolean doesWinnerExist = isCurrentStageNoStage && roundsQuantity > 0;

		RoundsForAdminForm roundsFormAdmin = new RoundsForAdminForm(allRounds, doesWinnerExist, isCurrentStageFinished);

		return roundsFormAdmin;
	}

	private boolean findIsCurrentStageNoStage() {
		Stage currentStage = srepository.findCurrentStage();
		String currentStageValue = currentStage.getStage();
		boolean isCurrentStageNoStage = currentStageValue.equals("No");

		return isCurrentStageNoStage;
	}

	private boolean findIsCurrentStageFinished() {
		// Checking, if all the games in a current stage were played to allow admin to
		// confirm stage results

		boolean allRoundsInCurrentStageArePlayed = this.findAllRoundsInCurrentStageArePlayed();

		// This flag indicates whether to show confirm stage results button or not;
		boolean isCurrentStageFinished = allRoundsInCurrentStageArePlayed && !this.findIsCurrentStageNoStage();

		return isCurrentStageFinished;
	}

	private boolean findAllRoundsInCurrentStageArePlayed() {
		int playedInCurrentStageRounds = rrepository.quantityOfPlayedInCurrentStage();
		int allCurrentStageRounds = rrepository.findQuantityOfGamesInCurrentStage();

		boolean allRoundsInCurrentStageArePlayed = playedInCurrentStageRounds == allCurrentStageRounds;

		return allRoundsInCurrentStageArePlayed;
	}

	public ResponseEntity<?> setRoundResultForAdmin(Long roundId, Round round) {
		Round localRound = rrepository.findRoundById(roundId);

		if (localRound == null || roundId != round.getRoundid())
			return new ResponseEntity<>("Round id missmatch with the one in request body and the one in path",
					HttpStatus.BAD_REQUEST);

		if (localRound.getStage() != srepository.findCurrentStage())
			return new ResponseEntity<>("You cannot change the status of the round out of the current stage",
					HttpStatus.NOT_ACCEPTABLE);

		if (localRound.getUser1() == null || localRound.getUser2() == null)
			return new ResponseEntity<>("The rounds with only one user shouldn't be handled by admin",
					HttpStatus.CONFLICT);

		String result = this.setResult(round, localRound);

		// now as we are saving the results to the database and everyone is able to see
		// the results, we should update the competitors status as well
		this.updateWinnerAndLooser(result, localRound);

		return new ResponseEntity<>("The round result was set successfully", HttpStatus.OK);
	}

	private String setResult(Round round, Round localRound) {
		String result = round.getResult();

		localRound.setResult(result);
		rrepository.save(localRound);

		return result;
	}

	private void updateWinnerAndLooser(String result, Round localRound) {
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
			String winnerUsername = this.extractUsername(result);
			User localRoundUser1 = localRound.getUser1();
			String localRoundUser1Username = localRoundUser1.getUsername();

			boolean isUser1Winner = localRoundUser1Username.equals(winnerUsername);

			if (isUser1Winner) {
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
	}

	public ResponseEntity<?> confirmStageResults() {
		if (!this.canConfirmStageResults())
			return new ResponseEntity<>("Cannot confirm stage results, untill all the rounds results are saved",
					HttpStatus.NOT_ACCEPTABLE);

		// finding the current stage, that should be processed and making this stage no
		// more current
		Stage stageToFinish = srepository.findCurrentStage();

		int playedRoundsInCurrentStage = rrepository.quantityOfPlayedInCurrentStage();
		// finding the next stage based on the played rounds quantity and making this
		// stage current
		Stage newCurrentStage = this.determineNewCurrentStage(playedRoundsInCurrentStage);

		this.updateStagesStatus(stageToFinish, newCurrentStage);

		// populating rounds of the next stage with winners of current stage (if it
		// wasn't a final round):

		// receiving the list of the rounds of the new stage (to populate them)
		List<Round> currentRounds = rrepository.findCurrentRounds();

		// if the current stage is 'no' it means the tournament is finished and there is
		// no need to populate more rounds
		if (currentRounds.size() == 0) {
			return new ResponseEntity<>("The final stage results were successfully confirmed", HttpStatus.OK);
		}

		this.populateNextStageRounds(stageToFinish, newCurrentStage, currentRounds);

		return new ResponseEntity<>("The current stage results were successfully confirmed", HttpStatus.OK);
	}

	private boolean canConfirmStageResults() {
		boolean allRoundsInCurrentStageArePlayed = this.findAllRoundsInCurrentStageArePlayed();

		// If the played rounds in the current stage amount equals to the all current
		// stage rounds and the current stage is not 'No' stage
		boolean isCurrentStageFinished = allRoundsInCurrentStageArePlayed && !this.findIsCurrentStageNoStage();

		return isCurrentStageFinished;
	}

	private Stage determineNewCurrentStage(int playedRoundsInCurrentStage) {
		Stage newCurrentStage;
		
		if (playedRoundsInCurrentStage > 2) {
			newCurrentStage = srepository.findByStage("1/" + playedRoundsInCurrentStage / 2).get(0);
		} else if (playedRoundsInCurrentStage == 2) {
			newCurrentStage = srepository.findByStage("final").get(0);
		} else {
			newCurrentStage = srepository.findByStage("No").get(0);
		}
		
		return newCurrentStage;
	}

	private void updateStagesStatus(Stage stageToFinish, Stage newCurrentStage) {
		stageToFinish.setIsCurrent(false);
		newCurrentStage.setIsCurrent(true);
		srepository.save(stageToFinish);
		srepository.save(newCurrentStage);
	}

	private void populateNextStageRounds(Stage stageToFinish, Stage newCurrentStage, List<Round> currentRounds) {
		// receiving the list of the rounds of the previous stage to get winners;
		Long idOfPreviousStage = stageToFinish.getStageid();
		List<Round> previousRounds = rrepository.findRoundsByStage(idOfPreviousStage);

		// declaring the variables to be handled in the cycle
		Round currentRound;
		String result1;
		String result2;

		String usernameOfWinner1;
		String usernameOfWinner2;

		User player1;
		User player2;

		for (int i = 0; i < currentRounds.size(); i++) {
			currentRound = currentRounds.get(i);

			// receiving the results of the previous rounds
			result1 = previousRounds.get(i * 2).getResult();
			result2 = previousRounds.get(i * 2 + 1).getResult();

			usernameOfWinner1 = this.extractUsername(result1);
			usernameOfWinner2 = this.extractUsername(result2);

			player1 = urepository.findByUsername(usernameOfWinner1);
			player2 = urepository.findByUsername(usernameOfWinner2);

			// assigning players to the round and changing the current stage of the player;
			currentRound.setUser1(player1);
			player1.setStage(newCurrentStage);

			currentRound.setUser2(player2);
			player2.setStage(newCurrentStage);

			rrepository.save(currentRound);
			urepository.save(player1);
			urepository.save(player2);
		}
	}

	// Assuming the result is stored in the format of <Username> <'win'/'autowin'>,
	// the username can be received by spliting the string with whitespace
	private String extractUsername(String result) {
		return result.substring(0, result.indexOf(" "));
	}

}
