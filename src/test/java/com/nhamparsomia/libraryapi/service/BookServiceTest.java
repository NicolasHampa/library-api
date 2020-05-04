package com.nhamparsomia.libraryapi.service;

import com.nhamparsomia.libraryapi.exception.BusinessException;
import com.nhamparsomia.libraryapi.model.entity.Book;
import com.nhamparsomia.libraryapi.model.repository.BookRepository;
import com.nhamparsomia.libraryapi.service.impl.BookServiceImpl;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mockito;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class BookServiceTest {

    BookService service;

    @MockBean
    BookRepository repository;

    @BeforeEach
    public void setUp() {
        this.service = new BookServiceImpl(repository);
    }

    @Test
    @DisplayName("Deve salvar um livro")
    public void saveBookTest() {
        Book book = createBook();

        Mockito.when(repository.save(book)).thenReturn(
            Book.builder()
                .id(Long.valueOf(111))
                .isbn("123")
                .author("John Doe")
                .title("Java World")
                .build()
        );

        Book savedBook = service.save(book);

        assertThat(savedBook.getId()).isNotNull();
        assertThat(savedBook.getIsbn()).isEqualTo("123");
        assertThat(savedBook.getAuthor()).isEqualTo("John Doe");
        assertThat(savedBook.getTitle()).isEqualTo("Java World");
    }

    @Test
    @DisplayName("Deve lançar erro de negócio ao tentar salvar um livro com isbn duplicado")
    public void shouldNotSaveABookWithDuplicatedIsbnTest() {
        Book book = createBook();

        Mockito.when(repository.existsByIsbn(Mockito.anyString())).thenReturn(true);

        Throwable exception = Assertions.catchThrowable(() -> service.save(book));

        assertThat(exception)
                .isInstanceOf(BusinessException.class)
                .hasMessage("Isbn já cadastrado.");

        Mockito.verify(repository, Mockito.never()).save(book);
    }

    @Test
    @DisplayName("Deve obter um livro por Id")
    public void getBookByIdTest() {
        Long id = 11L;

        Book book = createBook();
        book.setId(id);

        Mockito.when(repository.findById(id))
                .thenReturn(Optional.of(book));

        Optional<Book> foundBook = service.getById(id);

        assertThat(foundBook.isPresent()).isTrue();
        assertThat(foundBook.get().getId()).isEqualTo(id);
        assertThat(foundBook.get().getIsbn()).isEqualTo(book.getIsbn());
        assertThat(foundBook.get().getAuthor()).isEqualTo(book.getAuthor());
        assertThat(foundBook.get().getTitle()).isEqualTo(book.getTitle());
    }

    @Test
    @DisplayName("Deve retornar vazio ao obter um livro por Id quando inexistente na base")
    public void bookNotFoundByIdTest() {
        Long id = 11L;

        Mockito.when(repository.findById(id))
                .thenReturn(Optional.empty());

        Optional<Book> book = service.getById(id);

        assertThat(book.isPresent()).isFalse();
    }

    @Test
    @DisplayName("Deve remover um livro por Id")
    public void deleteBookTest() {
        Book book = createBook();
        book.setId(11L);

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> service.delete(book));

        Mockito.verify(repository, Mockito.times(1)).delete(book);
    }

    @Test
    @DisplayName("Deve ocorrer erro ao tentar remover livro inexistente")
    public void deleteInvalidBookTest() {
        Book book = new Book();

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> service.delete(book));

        Mockito.verify(repository, Mockito.never()).delete(book);
    }

    @Test
    @DisplayName("Deve atualizar um livro por Id")
    public void updateBookTest() {
        long id = 11;

        Book book = Book.builder().id(id).build();

        Book updatedBook = createBook();
        updatedBook.setId(id);

        Mockito.when(repository.save(book))
                .thenReturn(updatedBook);

        Book updatedBookReturn = service.update(book);

        assertThat(updatedBookReturn.getId()).isEqualTo(updatedBook.getId());
        assertThat(updatedBookReturn.getIsbn()).isEqualTo(updatedBook.getIsbn());
        assertThat(updatedBookReturn.getAuthor()).isEqualTo(updatedBook.getAuthor());
        assertThat(updatedBookReturn.getTitle()).isEqualTo(updatedBook.getTitle());
    }

    @Test
    @DisplayName("Deve ocorrer erro ao tentar atualizar um livro inexistente")
    public void updateInvalidBookTest() {
        Book book = new Book();

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> service.delete(book));

        Mockito.verify(repository, Mockito.never()).save(book);
    }

    private Book createBook() {
        return Book.builder()
                .isbn("123")
                .author("John Doe")
                .title("Java World")
                .build();
    }
}
