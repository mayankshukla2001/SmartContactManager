package com.Smart.Contact.Manager.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.Smart.Contact.Manager.dao.UserRepository;
import com.Smart.Contact.Manager.entities.User;

public class UserDetailsServiceImpl implements UserDetailsService {
	
	@Autowired
	private UserRepository userRepository;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		// Fetching user from database
		User user = userRepository.getUserByUserName(username);
		if(user == null)
		{
			throw new UsernameNotFoundException("Could not found user !!");
		}
		
		CustomUserDetails customUserDetails = new CustomUserDetails(user);
		return customUserDetails;
	}
	
}
