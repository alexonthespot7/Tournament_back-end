package com.myproject.chesstournamenttest.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.myproject.chesstournamenttest.model.User;
import com.myproject.chesstournamenttest.model.UserRepository;

/**
 * This class is used by spring security to authenticate and authorize user
 **/

@Service
public class UserDetailServiceImpl implements UserDetailsService {
	private final UserRepository repository;
	
	@Autowired
	public UserDetailServiceImpl(UserRepository userRepository) {
		this.repository = userRepository;
	}
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User curruser = repository.findByUsername(username);
		if (curruser == null) {
			throw new UsernameNotFoundException(username); //check if user with such username exists
		}
		
		boolean enabled = !curruser.isAccountVerified();
		UserDetails user = org.springframework.security.core.userdetails.User.withUsername(username)
				.password(curruser.getPasswordHash())
				.disabled(enabled)
				.authorities(AuthorityUtils.createAuthorityList(curruser.getRole()))
				.build();
		return user;
	}
	
}


