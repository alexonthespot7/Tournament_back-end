package com.myproject.chesstournamenttest.model;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {
	User findByUsername(String username);
	
	@Query(value="SELECT * FROM users WHERE account_verified = true", nativeQuery = true)
	List<User> findAllVerified();
	
	@Query(value="SELECT * FROM users WHERE is_competitor = true AND account_verified = true", nativeQuery = true)
	List<User> findAllCompetitors();
	
	@Override
	@Query(value="SELECT * FROM users ORDER BY account_verified DESC, is_competitor DESC, role, id", nativeQuery = true)
	List<User> findAll();
	
	@Query(value="SELECT * FROM users WHERE is_competitor = true AND is_out = false AND account_verified = true", nativeQuery = true)
	List<User> findAllCurrentCompetitors();
	
	@Query(value="SELECT * FROM users WHERE verification_code = ?1", nativeQuery=true)
	User findByVerificationCode(String code);
	
	@Query(value="SELECT * FROM users WHERE email = ?1", nativeQuery=true)
	User findByEmail(String email);
	
}
