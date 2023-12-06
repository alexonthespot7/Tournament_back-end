package com.myproject.tournamentapp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.myproject.tournamentapp.model.Stage;
import com.myproject.tournamentapp.model.StageRepository;

@Service
public class StageService {
	@Autowired
	private StageRepository srepository;
	
	public List<Stage> getStagesForAdmin() {
		
		return srepository.findAll();
	
	}
}
