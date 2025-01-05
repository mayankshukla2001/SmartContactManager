package com.Smart.Contact.Manager.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import com.Smart.Contact.Manager.entities.User;

@Service
public interface UserRepository extends JpaRepository<User, Integer> {
	boolean existsByEmail(String email);

}
