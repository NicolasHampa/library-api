package com.nhamparsomia.libraryapi.model.repository;

import com.nhamparsomia.libraryapi.model.entity.Book;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class BookRepositoryTest {
    @Autowired
    TestEntityManager entityManager;

    @Autowired
    BookRepository repository;

    @Test
    @DisplayName("Deve retornar verdadeiro quando existir um livro na base com o Isbn informado")
    public void returnTrueWhenIsbnExists() {
        String isbn = "123";

        Book book = createNewBook();

        entityManager.persist(book);

        boolean exists = repository.existsByIsbn(isbn);

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Deve retornar falso quando não existir um livro na base com o Isbn informado")
    public void returnFalseWhenIsbnNotExists() {
        String isbn = "123";

        boolean exists = repository.existsByIsbn(isbn);

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Deve obter um livro por Id")
    public void findByIdTest() {
        Book book = createNewBook();

        entityManager.persist(book);

        Optional<Book> foundBook = repository.findById(book.getId());

        assertThat(foundBook.isPresent()).isTrue();
    }

    @Test
    @DisplayName("Deve salvar um livro")
    public void saveBookTest() {
        Book book = createNewBook();

        Book savedBook = repository.save(book);

        assertThat(savedBook.getId()).isNotNull();
    }

    @Test
    @DisplayName("Deve remover um livro")
    public void deleteBookTest() {
        Book book = createNewBook();

        entityManager.persist(book);
        Book foundBook = entityManager.find(Book.class, book.getId());

        repository.delete(foundBook);
        Book deletedBook = entityManager.find(Book.class, book.getId());

        assertThat(deletedBook).isNull();
    }

    public static Book createNewBook() {
        return Book.builder()
                .isbn("123")
                .author("John Doe")
                .title("Java World")
                .build();
    }
}
