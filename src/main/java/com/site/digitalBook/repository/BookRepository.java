package com.site.digitalBook.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.site.digitalBook.entity.Livre;

@Repository
public interface BookRepository extends JpaRepository<Livre, Long> {
	 List<Livre> findByCategoriesName(String categoryName);
}
