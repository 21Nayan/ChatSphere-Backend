package com.chatsphere.repository;

import com.chatsphere.model.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
    List<Contact>  findByOwnerName(String ownerName);
    boolean        existsByOwnerNameAndPhone(String ownerName, String phone);
}