package com.myproject.tournamentapp;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.myproject.tournamentapp.model.Round;
import com.myproject.tournamentapp.model.RoundRepository;
import com.myproject.tournamentapp.model.Stage;
import com.myproject.tournamentapp.model.StageRepository;

@ExtendWith(SpringExtension.class)
@DataJpaTest
public class RoundRepositoryTests {
	@Autowired
	private RoundRepository rrepository;

	@Autowired
	private StageRepository srepository;

	// creation functionality checking for the round repo
	@Test
	public void creationTestsRounds() {
		rrepository.deleteAll(); // deleting all hard-coded rounds

		Stage stageNo = srepository.findCurrentStage();
		Round round = new Round("No", stageNo);

		rrepository.save(round);
		assertThat(rrepository.findAll()).isNotEmpty();
	}

	// check round repository findAll function
	@Test
	public void testFindAllRounds() {
		rrepository.deleteAll(); // deleting all hard-coded rounds

		List<Round> allRounds = rrepository.findAll();
		assertThat(allRounds).hasSize(0);

		Stage stageNo = srepository.findCurrentStage();
		Round round1 = new Round("No", stageNo);
		Round round2 = new Round("No", stageNo);
		rrepository.save(round1);
		rrepository.save(round2);
		
		allRounds = rrepository.findAll();
		assertThat(allRounds).hasSize(2);
	}

	// check round repository findAllCurrentAndPlayed function: 
	// reading rounds that either were already played or the rounds of the current stage 
	@Test
	public void testFindAllCurrentAndPlayedRounds() {
		rrepository.deleteAll(); // deleting all hard-coded rounds

		List<Round> allCurrentAndPlayedRounds = rrepository.findAllCurrentAndPlayed();
		assertThat(allCurrentAndPlayedRounds).hasSize(0);
		
		Stage stageNo = srepository.findCurrentStage();
		stageNo.setIsCurrent(false);
		srepository.save(stageNo);
		
		Stage stageSemiFinal = new Stage("1/2", false);
		Stage stageFinal = new Stage("final", true);
		srepository.save(stageSemiFinal);
		srepository.save(stageFinal);

		Round round1 = new Round("Messi win", stageSemiFinal);
		Round round2 = new Round("Leo win", stageSemiFinal);
		Round round3 = new Round("No", stageFinal);
		rrepository.save(round1);
		rrepository.save(round2);
		rrepository.save(round3);
		
		allCurrentAndPlayedRounds = rrepository.findAllCurrentAndPlayed();
		assertThat(allCurrentAndPlayedRounds).hasSize(3);
	}
	
	// Test find round by id functionality:
	@Test
	public void testFindRoundById() {
		rrepository.deleteAll(); // deleting all hard-coded rounds

		Round round = rrepository.findRoundById(Long.valueOf(1));
		assertThat(round).isNull();
		
		Stage stageNo = srepository.findCurrentStage();
		Round newRound = new Round("No", stageNo);
		rrepository.save(newRound);
		
		Round foundRound = rrepository.findRoundById(newRound.getRoundid());
		assertThat(foundRound).isNotNull();
	}
	
	// Test findCurrentRounds functionality: searches for the rounds in current stage;
	@Test
	public void testFindCurrentRounds() {
		rrepository.deleteAll(); // deleting all hard-coded rounds

		List<Round> currentRoundsNotFound = rrepository.findCurrentRounds();
		assertThat(currentRoundsNotFound).hasSize(0);
		
		Stage stageNo = srepository.findCurrentStage();
		Round round1 = new Round("No", stageNo);
		Round round2 = new Round("No", stageNo);
		rrepository.save(round1);
		rrepository.save(round2);
		
		List<Round> currentRoundsFound = rrepository.findCurrentRounds();
		assertThat(currentRoundsFound).hasSize(2);
	}
	
	// Test findRoundsByStage functionality:
	@Test
	public void testFindRoundsByStage() {
		rrepository.deleteAll(); // deleting all hard-coded rounds
		
		Stage stageNo = srepository.findCurrentStage();
		
		List<Round> roundsByStageNotFound = rrepository.findRoundsByStage(stageNo.getStageid());
		assertThat(roundsByStageNotFound).hasSize(0);
		
		Round round1 = new Round("No", stageNo);
		Round round2 = new Round("No", stageNo);
		rrepository.save(round1);
		rrepository.save(round2);
		
		List<Round> roundsByStageFound = rrepository.findRoundsByStage(stageNo.getStageid());
		assertThat(roundsByStageFound).hasSize(2);
	}
	
	// Test findQuantityOfGamesInCurrentStage functionality:
	@Test
	public void testFindQuantityOfGamesInCurrentStage() {
		rrepository.deleteAll(); // deleting all hard-coded rounds

		int quantityOfGamesInCurrentStageZero = rrepository.findQuantityOfGamesInCurrentStage();
		assertThat(quantityOfGamesInCurrentStageZero).isEqualTo(0);
		
		Stage stageNo = srepository.findCurrentStage();
		Round round1 = new Round("No", stageNo);
		Round round2 = new Round("No", stageNo);
		rrepository.save(round1);
		rrepository.save(round2);
		
		int quantityOfGamesInCurrentStageNotZero = rrepository.findQuantityOfGamesInCurrentStage();
		assertThat(quantityOfGamesInCurrentStageNotZero).isEqualTo(2);
	}
	
	// Test quantityOfPlayedInCurrentStage functionality:
	@Test
	public void testQuantityOfPlayedInCurrentStage() {
		rrepository.deleteAll(); // deleting all hard-coded rounds

		int quantityOfPlayedInCurrentStageZero = rrepository.quantityOfPlayedInCurrentStage();
		assertThat(quantityOfPlayedInCurrentStageZero).isEqualTo(0);
		
		Stage stageNo = srepository.findCurrentStage();
		Round round1 = new Round("No", stageNo);
		Round round2 = new Round("Messi win", stageNo);
		rrepository.save(round1);
		rrepository.save(round2);
		
		int quantityOfPlayedInCurrentStageNotZero = rrepository.quantityOfPlayedInCurrentStage();
		assertThat(quantityOfPlayedInCurrentStageNotZero).isEqualTo(1);
	}
	
	// Test findFinal functionality:
	@Test
	public void testFindFinal() {
		rrepository.deleteAll(); // deleting all hard-coded rounds
		
		Round finalNotFound = rrepository.findFinal();
		assertThat(finalNotFound).isNull();
		
		Stage stageFinal = new Stage("final", false);
		srepository.save(stageFinal);
		Round roundFinal = new Round("No", stageFinal);
		rrepository.save(roundFinal);
		
		Round finalFound = rrepository.findFinal();
		assertThat(finalFound).isNotNull();
	}

	// Test update functionality:
	@Test
	public void testUpdateRound() {
		rrepository.deleteAll(); // deleting all hard-coded rounds
		
		Stage stageNo = srepository.findCurrentStage();
		Round round = new Round("No", stageNo);
		rrepository.save(round);
		
		Round roundCheckResult = rrepository.findCurrentRounds().get(0);
		assertThat(roundCheckResult.getResult()).isEqualTo("No");
		
		roundCheckResult.setResult("Alex win");
		rrepository.save(roundCheckResult);
		
		Round roundResult = rrepository.findCurrentRounds().get(0);
		assertThat(roundResult.getResult()).isEqualTo("Alex win");
	}

	// Test delete functionality for round repo:
	@Test
	public void testDeleteRound() {
		Stage stageNo = srepository.findCurrentStage();
		Round round1 = new Round("No", stageNo);
		Round round2 = new Round("No", stageNo);

		rrepository.save(round1);
		rrepository.save(round2);

		rrepository.deleteAll(); // deleting all functionality testing
		assertThat(rrepository.findAll()).isEmpty();

		Round round3 = new Round("No", stageNo);

		rrepository.save(round3);

		Round round = rrepository.findRoundsByStage(stageNo.getStageid()).get(0);
		rrepository.delete(round);
		assertThat(rrepository.findAll()).isEmpty();
	}
}
