package com.nhamparsomia.libraryapi.api.resource;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.nhamparsomia.libraryapi.api.dto.BookDTO;
import com.nhamparsomia.libraryapi.model.entity.Book;
import com.nhamparsomia.libraryapi.service.BookService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.BDDMockito;
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

        String json = new ObjectMapper().writeValueAsString(savedBook);

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
    public void createInvalidBookTest() {

    }
}
