package com.myproject.tournamentapp.model;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface RoundRepository extends CrudRepository<Round, Long> {
	@Override
	@Query(value="SELECT round.* FROM round ORDER BY roundid", nativeQuery = true)
	List<Round> findAll();
	
	@Query(value="SELECT round.* FROM round JOIN stage ON (stage.stageid = round.stageid) WHERE stage.is_current = true OR result <> 'No' ORDER BY roundid", nativeQuery=true)
	List<Round> findAllCurrentAndPlayed();
	
	@Query(value="SELECT round.* FROM round WHERE roundid = ?1", nativeQuery=true)
	Round findRoundById(Long id);
	
	@Query(value="SELECT roundid, result, user1_id, user2_id, round.stageid FROM round JOIN stage ON (stage.stageid = round.stageid) WHERE stage.is_current = true ORDER BY roundid", nativeQuery = true)
	List<Round> findCurrentRounds();
	
	@Query(value="SELECT round.* FROM round WHERE stageid = ?1 ORDER BY roundid", nativeQuery = true)
	List<Round> findRoundsByStage(Long stageid);
	
	@Query(value="SELECT COUNT(roundid) FROM round JOIN stage ON (stage.stageid = round.stageid) WHERE stage.is_current = true", nativeQuery = true)
	int findQuantityOfGamesInCurrentStage();
	
	@Query(value="SELECT COUNT(roundid) FROM round JOIN stage ON (stage.stageid = round.stageid) WHERE stage.is_current = true AND result <> 'No'", nativeQuery = true)
	int quantityOfPlayedInCurrentStage();
	
	@Query(value="SELECT round.* FROM round JOIN stage ON (stage.stageid = round.stageid) WHERE stage.stage = 'final'", nativeQuery=true)
	Round findFinal();
}
