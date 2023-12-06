package com.myproject.tournamentapp;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.myproject.tournamentapp.model.Stage;
import com.myproject.tournamentapp.model.StageRepository;

@ExtendWith(SpringExtension.class)
@DataJpaTest
public class StageRepositoryTests {
	@Autowired
	private StageRepository srepository;

	// Test Create Functionality for stage repository
	@Test
	public void testCreationStage() {
		srepository.deleteAll(); // deleting all hard-coded stages

		Stage stage = new Stage("1/4", true);
		srepository.save(stage);
		assertThat(stage.getStageid()).isNotNull();
	}

	// Test findAll functions of stage repository:
	@Test
	public void testfindAll() {
		srepository.deleteAll(); // deleting all hard-coded stages
		
		List<Stage> stagesEmpty = srepository.findAll();
		assertThat(stagesEmpty).isEmpty();

		Stage stageNo = new Stage("No", true);
		srepository.save(stageNo);
		List<Stage> stages = srepository.findAll();
		assertThat(stages).hasSize(1);
	}

	// Test findAllStages functions of stage repository: find all except 'no' stage
	@Test
	public void testFindAllStages() {
		srepository.deleteAll(); // deleting all hard-coded stages
		
		List<Stage> allStagesEmpty = srepository.findAllStages();
		assertThat(allStagesEmpty).isEmpty();

		Stage stageNo = new Stage("No", true);
		Stage stageFinal = new Stage("final", false);
		srepository.save(stageNo);
		srepository.save(stageFinal);
		List<Stage> stagesExceptNo = srepository.findAllStages();
		assertThat(stagesExceptNo).hasSize(1);
	}

	// Test findCurrentStage functions of stage repository:
	@Test
	public void testFindCurrentStage() {
		srepository.deleteAll(); // deleting all hard-coded stages
		
		Stage currentStageNotFound = srepository.findCurrentStage();
		assertThat(currentStageNotFound).isNull();

		Stage stageNo = new Stage("No", true);
		Stage stageFinal = new Stage("final", false);
		srepository.save(stageNo);
		srepository.save(stageFinal);
		Stage currentStage = srepository.findCurrentStage();
		assertThat(currentStage).isNotNull();
		assertThat(currentStage.getStage()).isEqualTo("No");
	}
	
	// Test stage update functionality:
	@Test
	public void testUpdateStage() {		
		Stage stageNo = srepository.findCurrentStage();
		assertThat(stageNo.getIsCurrent()).isTrue();
		
		stageNo.setIsCurrent(false);
		srepository.save(stageNo);

		Stage stageNull = srepository.findCurrentStage();
		assertThat(stageNull).isNull();
		
		Stage updatedStage = srepository.findByStage("No").get(0);
		assertThat(updatedStage.getIsCurrent()).isFalse();
	}

	// Test delete functionality for stage repository
	@Test
	public void testDeletionStage() {
		srepository.deleteAll(); // deleting all hard-coded stages
		
		List<Stage> stagesEmpty = srepository.findAll();
		assertThat(stagesEmpty).isEmpty();

		Stage stageNo = new Stage("No", true);
		srepository.save(stageNo);		
		srepository.delete(stageNo);
		stagesEmpty = srepository.findByStage("No");
		assertThat(stagesEmpty).hasSize(0);

		stageNo = new Stage("No", true);
		Stage stage1 = new Stage("1/2", false);
		Stage stage2 = new Stage("1/4", true);
		srepository.save(stageNo);
		srepository.save(stage1);
		srepository.save(stage2);
		
		//testing method that deletes all stages except 'no' stage
		srepository.deleteAllStages();
		List<Stage> stagesWithNoOnly = srepository.findAll();
		assertThat(stagesWithNoOnly).hasSize(1);
	}

}
