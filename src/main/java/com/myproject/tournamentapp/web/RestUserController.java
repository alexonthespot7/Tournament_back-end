package com.myproject.tournamentapp.web;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.myproject.tournamentapp.forms.CompetitorPublicInfo;
import com.myproject.tournamentapp.model.User;
import com.myproject.tournamentapp.service.UserService;

@RestController
@RequestMapping("/api")
@PreAuthorize("isAuthenticated()")
public class RestUserController {
	@Autowired
	private UserService userService;
	
	// method to display competitors on competitors page for authorized user
	@RequestMapping(value = "/competitors", method = RequestMethod.GET)
	public @ResponseBody List<CompetitorPublicInfo> listCompetitorsPublicInfo() {
		return userService.listCompetitorsPublicInfo();
	}
}
