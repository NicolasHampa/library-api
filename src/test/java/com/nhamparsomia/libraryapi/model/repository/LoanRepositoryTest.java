package com.nhamparsomia.libraryapi.model.repository;

import com.nhamparsomia.libraryapi.model.entity.Book;

import com.nhamparsomia.libraryapi.model.entity.Loan;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
        Book book = createAndPersistLoan().getBook();

        boolean bookIsTaken = repository.verifyIfBookHasAlreadyBeenTaken(book);
        assertThat(bookIsTaken).isTrue();
    }

    @Test
    @DisplayName("Deve buscar empréstimo pelo isbn do livro ou pelo nome da pessoa que retirou o livro")
    public void findBookByIsbnOrCustomerTest() {
        Loan loan = createAndPersistLoan();

        Page<Loan> result = repository.findBookByIsbnOrCustomer(
                loan.getBook().getIsbn(),
                loan.getCustomer(),
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent()).contains(loan);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
    }

    private Loan createAndPersistLoan() {
        Book book = createNewBook();
        entityManager.persist(book);

        Loan loan = Loan.builder()
                .book(book)
                .customer("Pessoa")
                .loanDate(LocalDate.now())
                .build();
        entityManager.persist(loan);

        return loan;
    }
}
