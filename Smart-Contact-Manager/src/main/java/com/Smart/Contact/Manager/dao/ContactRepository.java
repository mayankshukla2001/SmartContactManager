package com.Smart.Contact.Manager.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.Smart.Contact.Manager.entities.Contact;

public interface ContactRepository extends JpaRepository<Contact, Integer> {
    // Pagination....
    @Query("select c from Contact c where c.user.id = :userId")
    Page<Contact> findContactsByUser(@Param("userId") int userId, Pageable pageable);
}
