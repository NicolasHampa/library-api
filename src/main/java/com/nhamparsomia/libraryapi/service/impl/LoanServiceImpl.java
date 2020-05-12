package com.nhamparsomia.libraryapi.service.impl;

import com.nhamparsomia.libraryapi.api.dto.LoanFilterDTO;
import com.nhamparsomia.libraryapi.exception.BusinessException;
import com.nhamparsomia.libraryapi.model.entity.Book;
import com.nhamparsomia.libraryapi.model.entity.Loan;
import com.nhamparsomia.libraryapi.model.repository.LoanRepository;
import com.nhamparsomia.libraryapi.service.LoanService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public class LoanServiceImpl implements LoanService {

    private LoanRepository repository;

    public LoanServiceImpl(LoanRepository repository) {
        this.repository = repository;
    }

    @Override
    public Loan save(Loan loan) {
        Book book = loan.getBook();

        if(repository.verifyIfBookHasAlreadyBeenTaken(book)) {
            throw new BusinessException("Book has already been taken by another customer");
        }

        return repository.save(loan);
    }

    @Override
    public Optional<Loan> getById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Loan update(Loan loan) {
        return repository.save(loan);
    }

    @Override
    public Page<Loan> find(LoanFilterDTO filter, Pageable pageable) {
        return repository.findBookByIsbnOrCustomer(filter.getIsbn(), filter.getCustomer(), pageable);
    }
}
