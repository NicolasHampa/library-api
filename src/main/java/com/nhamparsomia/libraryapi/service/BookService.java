package com.nhamparsomia.libraryapi.service;

import com.nhamparsomia.libraryapi.model.entity.Book;

import java.util.Optional;

public interface BookService {
    Book save(Book any);

    Optional<Book> getById(Long id);
}
