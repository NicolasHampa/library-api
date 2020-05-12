package com.nhamparsomia.libraryapi.service;

import com.nhamparsomia.libraryapi.api.dto.LoanFilterDTO;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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

    @Test
    @DisplayName("Deve obter as informações de um empréstimo pelo seu Id")
    public void getLoanDetailsTest() {
        Long id = 1L;
        Loan loan = createLoan();
        loan.setId(id);

        Mockito.when(repository.findById(id))
                .thenReturn(Optional.of(loan));

        Optional<Loan> result = service.getById(id);

        assertThat(result.isPresent()).isTrue();
        assertThat(result.get().getId()).isEqualTo(id);
        assertThat(result.get().getCustomer()).isEqualTo(loan.getCustomer());
        assertThat(result.get().getBook()).isEqualTo(loan.getBook());
        assertThat(result.get().getLoanDate()).isEqualTo(loan.getLoanDate());

        Mockito.verify(repository, Mockito.times(1)).findById(id);
    }

    @Test
    @DisplayName("Deve atualizar um empréstimo")
    public void updateLoanTest() {
        Loan loan = createLoan();
        loan.setId(1L);
        loan.setReturned(true);

        Mockito.when(repository.save(loan))
                .thenReturn(loan);

        Loan updatedLoan = service.update(loan);

        assertThat(updatedLoan.getReturned()).isTrue();
        Mockito.verify(repository, Mockito.times(1)).save(loan);
    }

    @Test
    @DisplayName("Deve filtrar empréstimos pelas propriedades")
    public void findLoanTest() {
        LoanFilterDTO loanFilterDTO = LoanFilterDTO.builder()
                .customer("Pessoa")
                .isbn("321")
                .build();

        Loan loan = createLoan();
        loan.setId(1L);

        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Loan> loanList = Arrays.asList(loan);

        Page<Loan> page = new PageImpl<Loan>(
                loanList,
                pageRequest,
                1
        );

        Mockito.when(repository.findBookByIsbnOrCustomer(
                    Mockito.anyString(),
                    Mockito.anyString(),
                    Mockito.any(PageRequest.class)))
                .thenReturn(page);

        Page<Loan> result = service.find(loanFilterDTO, pageRequest);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).isEqualTo(loanList);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);
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
