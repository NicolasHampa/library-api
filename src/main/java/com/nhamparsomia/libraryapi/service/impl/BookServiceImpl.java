package com.nhamparsomia.libraryapi.service.impl;

import com.nhamparsomia.libraryapi.exception.BusinessException;
import com.nhamparsomia.libraryapi.model.entity.Book;
import com.nhamparsomia.libraryapi.model.repository.BookRepository;
import com.nhamparsomia.libraryapi.service.BookService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BookServiceImpl implements BookService {

    private BookRepository repository;

    public BookServiceImpl(BookRepository repository) {
        this.repository = repository;
    }

    @Override
    public Book save(Book book) {
        if (repository.existsByIsbn(book.getIsbn())) {
            throw new BusinessException("Isbn já cadastrado.");
        }

        return repository.save(book);
    }

    @Override
    public Optional<Book> getById(Long id) {
        return this.repository.findById(id);
    }

    @Override
    public void delete(Book book) {
        if(book.getId() == null) {
            throw new IllegalArgumentException("Book id cant be null");
        }

        this.repository.delete(book);
    }

    @Override
    public Book update(Book book) {
        if(book.getId() == null) {
            throw new IllegalArgumentException("Book id cant be null");
        }

        return this.repository.save(book);
    }

    @Override
    public Page<Book> find(Book filter, Pageable pageRequest) {
        return null;
    }
}
