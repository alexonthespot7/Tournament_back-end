package com.myproject.tournamentapp.model;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface StageRepository extends CrudRepository<Stage, Long> {
	List<Stage> findByStage(String stage);
	
	@Query(value="SELECT stage.* FROM stage ORDER BY stageid", nativeQuery = true)
	List<Stage> findAll();
	
	@Query(value="SELECT stage.* FROM stage WHERE stage <> 'No' ORDER BY stageid", nativeQuery = true)
	List<Stage> findAllStages();
	
	@Query(value="SELECT stage.* FROM stage WHERE is_current = true", nativeQuery = true)
	Stage findCurrentStage();
	
	@Modifying
	@Query(value="DELETE FROM stage WHERE stage <> 'No'", nativeQuery = true)
	void deleteAllStages();
}
