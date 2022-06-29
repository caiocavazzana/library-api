package com.example.libraryapi.service;

import com.example.libraryapi.api.model.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface BookService {
    Book save(Book book);

    Optional<Book> getById(long id);

    void delete(Book book);

    Book update(Book book);

    Page<Book> find(Book book, Pageable pageable);

    Optional<Book> getBookByIsbn(String s);
}
