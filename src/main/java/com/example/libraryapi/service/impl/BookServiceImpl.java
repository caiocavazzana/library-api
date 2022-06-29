package com.example.libraryapi.service.impl;

import com.example.libraryapi.api.model.entity.Book;
import com.example.libraryapi.api.model.repository.BookRepository;
import com.example.libraryapi.exception.BusinessException;
import com.example.libraryapi.service.BookService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
@AllArgsConstructor
public class BookServiceImpl implements BookService {

    @Autowired
    private final BookRepository repository;

    @Override
    public Book save(Book book) {
        if (repository.existsByIsbn(book.getIsbn())) {
            throw new BusinessException("ISBN já cadastrado");
        }

        return repository.save(book);
    }

    @Override
    public Optional<Book> getById(long id) {
        return repository.findById(id);
    }

    @Override
    public void delete(Book book) {
        if (Objects.isNull(book) || Objects.isNull(book.getId())) {
            throw new IllegalArgumentException("Id do livro não pode ser nulo.");
        }

        repository.delete(book);
    }

    @Override
    public Book update(Book book) {
        if (Objects.isNull(book) || Objects.isNull(book.getId())) {
            throw new IllegalArgumentException("Id do livro não pode ser nulo.");
        }

        return repository.save(book);
    }

    @Override
    public Page<Book> find(Book filter, Pageable pageable) {
        var example = Example.of(
                filter,
                ExampleMatcher
                        .matching()
                        .withIgnoreCase()
                        .withIgnoreNullValues()
                        .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING));

        return repository.findAll(example, pageable);
    }

    @Override
    public Optional<Book> getBookByIsbn(String s) {
        return null;
    }

}
