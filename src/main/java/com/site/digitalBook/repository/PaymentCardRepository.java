package com.site.digitalBook.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.site.digitalBook.entity.CarteDePaiement;

@Repository
public interface PaymentCardRepository extends JpaRepository<CarteDePaiement, Integer> {
    List<CarteDePaiement> findByUserId(int userId);
}

