package com.Smart.Contact.Manager.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import com.Smart.Contact.Manager.entities.User;

@Service
public interface UserRepository extends JpaRepository<User, Integer> {
	boolean existsByEmail(String email);
	
	@Query("select u from User u where u.email = :email")
	
	public User getUserByUserName(@Param("email") String email);

}
