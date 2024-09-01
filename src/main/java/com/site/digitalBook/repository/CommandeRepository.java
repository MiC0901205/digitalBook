package com.site.digitalBook.repository;

import com.site.digitalBook.entity.Commande;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommandeRepository extends JpaRepository<Commande, Integer> {
    List<Commande> findByUserId(Integer userId);
}
