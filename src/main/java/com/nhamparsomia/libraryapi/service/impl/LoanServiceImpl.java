package com.nhamparsomia.libraryapi.service.impl;

import com.nhamparsomia.libraryapi.exception.BusinessException;
import com.nhamparsomia.libraryapi.model.entity.Book;
import com.nhamparsomia.libraryapi.model.entity.Loan;
import com.nhamparsomia.libraryapi.model.repository.LoanRepository;
import com.nhamparsomia.libraryapi.service.LoanService;

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
}
