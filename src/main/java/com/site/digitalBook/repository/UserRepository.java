package com.site.digitalBook.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.site.digitalBook.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    User findByEmail(String email);
    
    @Query("SELECT u.estActif FROM User u WHERE u.email = :email")
    Boolean findEstActifByEmail(@Param("email") String email);
}
