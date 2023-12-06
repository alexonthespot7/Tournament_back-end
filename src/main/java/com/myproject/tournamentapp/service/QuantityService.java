package com.myproject.tournamentapp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.myproject.tournamentapp.model.Round;
import com.myproject.tournamentapp.model.RoundRepository;
import com.myproject.tournamentapp.model.User;
import com.myproject.tournamentapp.model.UserRepository;

@Service
public class QuantityService {
	@Autowired
	private RoundRepository rrepository;
	
	@Autowired
	private UserRepository urepository;
	
	public int findRoundsQuantity() {
		List<Round> allRounds = rrepository.findAll();
		int roundsQuantity = allRounds.size();
		
		return roundsQuantity;
	}
	
	public int findCompetitorsQuantity() {
		List<User> allCompetitors = urepository.findAllCompetitors();
		int competitorsQuantity = allCompetitors.size();
		
		return competitorsQuantity;
	}
}
