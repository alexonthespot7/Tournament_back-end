package com.myproject.tournamentapp.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.myproject.tournamentapp.forms.RoundPublicInfo;
import com.myproject.tournamentapp.model.Round;

@Service
public class MakeRoundsPublicService {
	// method for restricting the list of rounds, which by default contains user's
	// info
	public List<RoundPublicInfo> makeRoundsPublic(List<Round> rounds) {
		List<RoundPublicInfo> publicRounds = new ArrayList<>();
		RoundPublicInfo publicRound;
		// In round entity one of the competitor can be null, so I need to handle it as
		// well
		String username1;
		String username2;

		for (Round round : rounds) {
			username1 = round.getUser1() == null ? null : round.getUser1().getUsername();
			username2 = round.getUser2() == null ? null : round.getUser2().getUsername();
			publicRound = new RoundPublicInfo(username1, username2, round.getStage().getStage(), round.getResult());
			publicRounds.add(publicRound);
		}

		return publicRounds;
	}
}
