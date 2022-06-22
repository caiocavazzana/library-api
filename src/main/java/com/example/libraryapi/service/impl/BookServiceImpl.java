package com.example.libraryapi.service.impl;

import com.example.libraryapi.api.model.entity.Book;
import com.example.libraryapi.api.model.repository.BookRepository;
import com.example.libraryapi.exception.BusinessException;
import com.example.libraryapi.service.BookService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

}
