package com.nhamparsomia.libraryapi.model.repository;

import com.nhamparsomia.libraryapi.model.entity.Book;

import com.nhamparsomia.libraryapi.model.entity.Loan;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;

import static com.nhamparsomia.libraryapi.model.repository.BookRepositoryTest.createNewBook;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class LoanRepositoryTest {

    @Autowired
    private LoanRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Deve verificar se o livro consultado já esta emprestado")
    public void verifyIfBookHasAlreadyBeenTaken(){
        Book book = createNewBook();
        entityManager.persist(book);

        Loan loan = Loan.builder()
                .book(book)
                .customer("Pessoa")
                .loanDate(LocalDate.now())
                .build();
        entityManager.persist(loan);

        boolean bookIsTaken = repository.verifyIfBookHasAlreadyBeenTaken(book);
        assertThat(bookIsTaken).isTrue();
    }
}
