package com.myproject.tournamentapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.myproject.tournamentapp.MyUser;
import com.myproject.tournamentapp.model.User;
import com.myproject.tournamentapp.model.UserRepository;


@Service
public class UserDetailServiceImpl implements UserDetailsService {
	@Autowired
	private UserRepository urepository;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = urepository.findByUsername(username);
		
		MyUser myUser = null;
		
		if (user != null) {
			
			boolean enabled = user.isAccountVerified();
			
			myUser = new MyUser(user.getId(), username,
					user.getPasswordHash(), enabled, true, true, true,
					AuthorityUtils.createAuthorityList(user.getRole()));
		} else {
			throw new UsernameNotFoundException("User (" + username + ") not found.");
		}
		
		return myUser;
	}
}


