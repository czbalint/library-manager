package org.czbalint.librarymanager.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.czbalint.librarymanager.entity.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class BookRepositoryCustomImplTest {

    @Autowired
    private BookRepository bookRepository;

    @BeforeEach
    void setup() {
        bookRepository.save(new Book("Clean Code", "Robert Martin", "ISBN-1", 3));
        bookRepository.save(new Book("Clean Architecture", "Robert Martin", "ISBN-2", 2));
        bookRepository.save(new Book("Refactoring", "Martin Fowler", "ISBN-3", 1));
    }

    @Test
    void matchesTitleCaseInsensitivelyAsSubstring() {
        List<Book> result = bookRepository.findBookByTitleAndAuthorAndIsbn("clean", null, null);

        assertThat(result).extracting(Book::getTitle)
                .containsExactlyInAnyOrder("Clean Code", "Clean Architecture");
    }

    @Test
    void matchesAuthorCaseInsensitivelyAsExactValue() {
        List<Book> result = bookRepository.findBookByTitleAndAuthorAndIsbn(null, "ROBERT MARTIN", null);

        assertThat(result).hasSize(2);
    }

    @Test
    void matchesIsbnCaseInsensitively() {
        List<Book> result = bookRepository.findBookByTitleAndAuthorAndIsbn(null, null, "isbn-3");

        assertThat(result).extracting(Book::getTitle).containsExactly("Refactoring");
    }

    @Test
    void combinesFiltersWithAnd() {
        List<Book> result = bookRepository.findBookByTitleAndAuthorAndIsbn("clean", "Martin Fowler", null);

        assertThat(result).isEmpty();
    }
}
