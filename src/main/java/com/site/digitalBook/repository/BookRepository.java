package com.site.digitalBook.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.site.digitalBook.entity.Book;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
}
