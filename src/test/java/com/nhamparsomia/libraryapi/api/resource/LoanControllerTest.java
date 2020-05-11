package com.nhamparsomia.libraryapi.api.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhamparsomia.libraryapi.api.dto.LoanDTO;
import com.nhamparsomia.libraryapi.api.dto.ReturnedLoanDTO;
import com.nhamparsomia.libraryapi.exception.BusinessException;
import com.nhamparsomia.libraryapi.model.entity.Book;
import com.nhamparsomia.libraryapi.model.entity.Loan;
import com.nhamparsomia.libraryapi.service.BookService;
import com.nhamparsomia.libraryapi.service.LoanService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.Optional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@WebMvcTest(controllers = LoanController.class)
public class LoanControllerTest {
    static final String LOAN_API = "/api/loans";

    @Autowired
    MockMvc mvc;

    @MockBean
    private BookService bookService;

    @MockBean
    private LoanService loanService;

    @Test
    @DisplayName("Deve realizar um emprestimo de livro")
    public void createLoanTest() throws Exception {
        LoanDTO dto = LoanDTO.builder()
                .isbn("123")
                .customer("Pessoa")
                .build();

        String json = new ObjectMapper().writeValueAsString(dto);

        Book book = Book.builder().id(11L).isbn("123").build();

        Loan loan = Loan.builder()
                .id(11L)
                .customer("Pessoa")
                .book(book)
                .loanDate(LocalDate.now())
                .build();

        BDDMockito.given(bookService.getBookByIsbn("123"))
                .willReturn(Optional.of(book));

        BDDMockito.given(loanService.save(Mockito.any(Loan.class)))
                .willReturn(loan);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(content().string("11"));
    }

    @Test
    @DisplayName("Deve retornar erro ao tentar realizar emprestimo de um livro inexistente")
    public void loanWithInvalidIsbnTest() throws Exception {

        LoanDTO dto = LoanDTO.builder()
                .isbn("123")
                .customer("Pessoa")
                .build();

        String json = new ObjectMapper().writeValueAsString(dto);

        BDDMockito.given(bookService.getBookByIsbn("123"))
                .willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", Matchers.hasSize(1)))
                .andExpect(jsonPath("errors[0]").value("Book not found for given isbn"));
    }

    @Test
    @DisplayName("Deve retornar erro ao tentar realizar emprestimo de um livro não disponivel")
    public void loanFromUnavailableBookTest() throws Exception {
        LoanDTO dto = LoanDTO.builder()
                .isbn("123")
                .customer("Pessoa")
                .build();

        String json = new ObjectMapper().writeValueAsString(dto);

        Book book = Book.builder().id(11L).isbn("123").build();

        BDDMockito.given(bookService.getBookByIsbn("123"))
                .willReturn(Optional.of(book));

        BDDMockito.given(loanService.save(Mockito.any(Loan.class)))
                .willThrow(new BusinessException("Book has already been taken by another customer"));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", Matchers.hasSize(1)))
                .andExpect(jsonPath("errors[0]").value("Book has already been taken by another customer"));
    }

    @Test
    @DisplayName("Deve devolver um livro emprestado")
    public void giveBackBookTest() throws Exception {
        ReturnedLoanDTO dto = ReturnedLoanDTO.builder()
                .returned(true)
                .build();

        String json = new ObjectMapper().writeValueAsString(dto);

        Loan loan = Loan.builder().id(1L).build();

        BDDMockito.given(loanService.getById(Mockito.anyLong()))
                .willReturn(Optional.of(loan));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.patch(LOAN_API.concat("/1"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(status().isOk());

        Mockito.verify(loanService, Mockito.times(1)).update(loan);
    }

    @Test
    @DisplayName("Deve retornar erro 404 ao tentar devolver empréstimo de um livro inexistente")
    public void tryToGiveBackNotFoundBookTest() throws Exception {
        ReturnedLoanDTO dto = ReturnedLoanDTO.builder()
                .returned(true)
                .build();

        String json = new ObjectMapper().writeValueAsString(dto);

        BDDMockito.given(loanService.getById(Mockito.anyLong()))
                .willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.patch(LOAN_API.concat("/1"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(status().isNotFound());

        Mockito.verify(loanService, Mockito.never()).update(Mockito.any(Loan.class));
    }
}
