package com.nhamparsomia.libraryapi.model.repository;

import com.nhamparsomia.libraryapi.model.entity.Book;
import com.nhamparsomia.libraryapi.model.entity.Loan;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    boolean verifyIfBookHasAlreadyBeenTaken(Book book);
}
