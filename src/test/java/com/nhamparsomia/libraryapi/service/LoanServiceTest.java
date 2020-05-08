package com.nhamparsomia.libraryapi.service;

import com.nhamparsomia.libraryapi.exception.BusinessException;
import com.nhamparsomia.libraryapi.model.entity.Book;
import com.nhamparsomia.libraryapi.model.entity.Loan;
import com.nhamparsomia.libraryapi.model.repository.LoanRepository;

import com.nhamparsomia.libraryapi.service.impl.LoanServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mockito;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class LoanServiceTest {

    private LoanService service;

    @MockBean
    private LoanRepository repository;

    @BeforeEach
    public void setUp() {
        this.service = new LoanServiceImpl(repository);
    }

    @Test
    @DisplayName("Deve salvar um empréstimo")
    public void saveLoanTest() {

        Loan loanReceivedByRequest = createLoan();

        Loan savedLoan = createLoan();
        savedLoan.setId(11L);

        Mockito.when(repository.save(loanReceivedByRequest))
                .thenReturn(savedLoan);

        Loan loan = service.save(loanReceivedByRequest);

        assertThat(loan.getId()).isEqualTo(savedLoan.getId());
        assertThat(loan.getBook().getId()).isEqualTo(savedLoan.getBook().getId());
        assertThat(loan.getCustomer()).isEqualTo(savedLoan.getCustomer());
        assertThat(loan.getLoanDate()).isEqualTo(savedLoan.getLoanDate());
    }

    @Test
    @DisplayName("Deve lançar erro de negócio ao tentar realizar emprestimo de um livro não disponivel")
    public void loanUnavailableBookTest() {

        Loan loanReceivedByRequest = createLoan();

        Mockito.when(repository.verifyIfBookHasAlreadyBeenTaken(loanReceivedByRequest.getBook()))
                .thenReturn(true);

        Throwable exception = catchThrowable(() -> service.save(loanReceivedByRequest));

        assertThat(exception)
                .isInstanceOf(BusinessException.class)
                .hasMessage("Book has already been taken by another customer");

        Mockito.verify(repository, Mockito.never()).save(loanReceivedByRequest);
    }

    private Loan createLoan() {
        Book book = Book.builder().id(11L).build();

        return Loan.builder()
                .book(book)
                .customer("Pessoa")
                .loanDate(LocalDate.now())
                .build();
    }
}
