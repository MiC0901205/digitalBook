package com.site.digitalBook.repository;

import com.site.digitalBook.entity.Panier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Panier, Integer> {
    Optional<Panier> findByUserId(int userId);
}

