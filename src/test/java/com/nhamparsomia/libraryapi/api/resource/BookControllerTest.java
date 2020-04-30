package com.nhamparsomia.libraryapi.api.resource;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.nhamparsomia.libraryapi.api.dto.BookDTO;
import com.nhamparsomia.libraryapi.exception.BusinessException;
import com.nhamparsomia.libraryapi.model.entity.Book;
import com.nhamparsomia.libraryapi.service.BookService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.BDDMockito;
import org.mockito.Mockito;

import static org.hamcrest.Matchers.hasSize;

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

import java.util.Optional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest
@AutoConfigureMockMvc
public class BookControllerTest {

    static String BOOK_API = "/api/books";

    @Autowired
    MockMvc mvc;

    @MockBean
    BookService service;

    @Test
    @DisplayName("Deve criar um livro com sucesso.")
    public void createBookTest() throws Exception {
        BookDTO dto = createNewBook();

        Book savedBook = Book
                .builder()
                .id(Long.valueOf(101))
                .author("Joao")
                .title("Mundo Java")
                .isbn("001")
                .build();

        BDDMockito
                .given(service.save(Mockito.any(Book.class)))
                .willReturn(savedBook);

        String json = new ObjectMapper().writeValueAsString(dto);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc
            .perform(request)
            .andExpect( status().isCreated() )
            .andExpect( jsonPath("id").value(101) )
            .andExpect( jsonPath("title").value(savedBook.getTitle()) )
            .andExpect( jsonPath("author").value(savedBook.getAuthor()) )
            .andExpect( jsonPath("isbn").value(savedBook.getIsbn()) );
    }

    @Test
    @DisplayName("Deve lançar erro de validação quando não houver dados suficientes para criação do livro.")
    public void createInvalidBookTest() throws Exception {
        String json = new ObjectMapper().writeValueAsString(new BookDTO());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", hasSize(3)));
    }

    @Test
    @DisplayName("Deve lançar erro ao tentar cadastrar um livro com isbn em uso por outro livro")
    public void createBookWithDuplicatedIsbn() throws Exception {
        BookDTO dto = createNewBook();
        String json = new ObjectMapper().writeValueAsString(dto);
        String errorMessage = "Isbn já cadastrado.";

        BDDMockito
                .given(service.save(Mockito.any(Book.class)))
                .willThrow(new BusinessException(errorMessage));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", hasSize(1)))
                .andExpect(jsonPath("errors[0]").value(errorMessage));
    }

    @Test
    @DisplayName("Deve obter informações de um livro")
    public void getBookDetailsTest() throws Exception {
        Long id = Long.valueOf(11);

        Book book = Book.builder()
                .id(id)
                .author("Joao")
                .title("Mundo Java")
                .isbn("001")
                .build();

        BDDMockito
                .given(service.getById(id))
                .willReturn(Optional.of(book));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat("/" + id))
                .accept(MediaType.APPLICATION_JSON);

        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect( jsonPath("id").value(id) )
                .andExpect( jsonPath("title").value(book.getTitle()) )
                .andExpect( jsonPath("author").value(book.getAuthor()) )
                .andExpect( jsonPath("isbn").value(book.getIsbn()) );
    }

    @Test
    @DisplayName("Deve retornar resource not found quando o livro procurado não existir")
    public void bookNotFoundTest() throws Exception {
        BDDMockito
                .given(service.getById(Mockito.anyLong()))
                .willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat("/" + 1))
                .accept(MediaType.APPLICATION_JSON);

        mvc.perform(request)
                .andExpect(status().isNotFound());
    }

    private BookDTO createNewBook() {
        return BookDTO
                .builder()
                .author("Joao")
                .title("Mundo Java")
                .isbn("001")
                .build();
    }
}
