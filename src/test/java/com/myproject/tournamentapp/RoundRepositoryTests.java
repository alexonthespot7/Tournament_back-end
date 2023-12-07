package com.myproject.tournamentapp;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.myproject.tournamentapp.model.Round;
import com.myproject.tournamentapp.model.RoundRepository;
import com.myproject.tournamentapp.model.Stage;
import com.myproject.tournamentapp.model.StageRepository;

import jakarta.transaction.Transactional;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@TestInstance(Lifecycle.PER_CLASS)
@Transactional
public class RoundRepositoryTests {
	@Autowired
	private RoundRepository rrepository;

	@Autowired
	private StageRepository srepository;

	@BeforeAll
	public void resetStageAndRoundRepos() {
		rrepository.deleteAll(); // deleting all hard-coded rounds
		srepository.deleteAll();

		List<Stage> allStages = srepository.findAll();
		assertThat(allStages).hasSize(0);

		List<Round> allRounds = rrepository.findAll();
		assertThat(allRounds).hasSize(0);

		Stage stageNo = new Stage("No", true);
		srepository.save(stageNo);
	}

	// creation functionality checking for the round repo
	@Test
	@Rollback
	public void creationTestsRounds() {
		Stage stageNo = srepository.findCurrentStage();

		Round round = new Round("No", stageNo);

		rrepository.save(round);
		assertThat(rrepository.findAll()).isNotEmpty();
	}

	// check round repository findAll function
	@Test
	@Rollback
	public void testFindAllRounds() {
		Stage stageNo = srepository.findCurrentStage();

		List<Round> allRounds = rrepository.findAll();
		assertThat(allRounds).hasSize(0);

		Round round1 = new Round("No", stageNo);
		Round round2 = new Round("No", stageNo);
		rrepository.save(round1);
		rrepository.save(round2);

		allRounds = rrepository.findAll();
		assertThat(allRounds).hasSize(2);
	}

	// check round repository findAllCurrentAndPlayed function:
	// reading rounds that either were already played or the rounds of the current
	// stage
	@Test
	@Rollback
	public void testFindAllCurrentAndPlayedRounds() {
		Stage stageNo = srepository.findCurrentStage();

		List<Round> allCurrentAndPlayedRounds = rrepository.findAllCurrentAndPlayed();
		assertThat(allCurrentAndPlayedRounds).hasSize(0);

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
	@Rollback
	public void testFindRoundById() {
		Stage stageNo = srepository.findCurrentStage();

		Round round = rrepository.findRoundById(Long.valueOf(1));
		assertThat(round).isNull();

		Round newRound = new Round("No", stageNo);
		rrepository.save(newRound);

		Round foundRound = rrepository.findRoundById(newRound.getRoundid());
		assertThat(foundRound).isNotNull();
	}

	// Test findCurrentRounds functionality: searches for the rounds in current
	// stage;
	@Test
	@Rollback
	public void testFindCurrentRounds() {
		Stage stageNo = srepository.findCurrentStage();

		List<Round> currentRoundsNotFound = rrepository.findCurrentRounds();
		assertThat(currentRoundsNotFound).hasSize(0);

		Round round1 = new Round("No", stageNo);
		Round round2 = new Round("No", stageNo);
		rrepository.save(round1);
		rrepository.save(round2);

		List<Round> currentRoundsFound = rrepository.findCurrentRounds();
		assertThat(currentRoundsFound).hasSize(2);
	}

	// Test findRoundsByStage functionality:
	@Test
	@Rollback
	public void testFindRoundsByStage() {
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
	@Rollback
	public void testFindQuantityOfGamesInCurrentStage() {
		Stage stageNo = srepository.findCurrentStage();

		int quantityOfGamesInCurrentStageZero = rrepository.findQuantityOfGamesInCurrentStage();
		assertThat(quantityOfGamesInCurrentStageZero).isEqualTo(0);

		Round round1 = new Round("No", stageNo);
		Round round2 = new Round("No", stageNo);
		rrepository.save(round1);
		rrepository.save(round2);

		int quantityOfGamesInCurrentStageNotZero = rrepository.findQuantityOfGamesInCurrentStage();
		assertThat(quantityOfGamesInCurrentStageNotZero).isEqualTo(2);
	}

	// Test quantityOfPlayedInCurrentStage functionality:
	@Test
	@Rollback
	public void testQuantityOfPlayedInCurrentStage() {
		Stage stageNo = srepository.findCurrentStage();

		int quantityOfPlayedInCurrentStageZero = rrepository.quantityOfPlayedInCurrentStage();
		assertThat(quantityOfPlayedInCurrentStageZero).isEqualTo(0);

		Round round1 = new Round("No", stageNo);
		Round round2 = new Round("Messi win", stageNo);
		rrepository.save(round1);
		rrepository.save(round2);

		int quantityOfPlayedInCurrentStageNotZero = rrepository.quantityOfPlayedInCurrentStage();
		assertThat(quantityOfPlayedInCurrentStageNotZero).isEqualTo(1);
	}

	// Test findFinal functionality:
	@Test
	@Rollback
	public void testFindFinal() {
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
	@Rollback
	public void testUpdateRound() {
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
	@Rollback
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
