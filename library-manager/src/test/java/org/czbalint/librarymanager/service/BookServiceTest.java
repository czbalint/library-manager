package org.czbalint.librarymanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.czbalint.librarymanager.dto.CreateBookRequest;
import org.czbalint.librarymanager.entity.Book;
import org.czbalint.librarymanager.repository.BookRepository;
import org.czbalint.librarymanager.error.DuplicateResourceException;
import org.czbalint.librarymanager.error.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    @Test
    void addBook_savesWhenIsbnIsNew() {
        CreateBookRequest request = new CreateBookRequest("Clean Code", "Robert Martin", "111", 3);
        when(bookRepository.existsByIsbn("111")).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        Book result = bookService.addBook(request);

        assertThat(result.getTitle()).isEqualTo("Clean Code");
        assertThat(result.getAuthor()).isEqualTo("Robert Martin");
        assertThat(result.getIsbn()).isEqualTo("111");
        assertThat(result.getTotalCopies()).isEqualTo(3);
        assertThat(result.getAvailableCopies()).isEqualTo(3);
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void addBook_throwsWhenIsbnAlreadyExists() {
        CreateBookRequest request = new CreateBookRequest("Clean Code", "Robert Martin", "111", 3);
        when(bookRepository.existsByIsbn("111")).thenReturn(true);

        assertThatThrownBy(() -> bookService.addBook(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("111");
        verify(bookRepository, never()).save(any());
    }

    @Test
    void getBook_throwsWhenMissing() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getBook(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void search_returnsAllWhenNoFiltersGiven() {
        List<Book> all = List.of(new Book("A", "X", "1", 1));
        when(bookRepository.findAll()).thenReturn(all);

        assertThat(bookService.search(null, null, null)).isEqualTo(all);
        verify(bookRepository, never()).findBookByTitleAndAuthorAndIsbn(any(), any(), any());
    }

    @Test
    void search_delegatesToCustomQueryWhenFilterGiven() {
        List<Book> matches = List.of(new Book("Clean Code", "Martin", "111", 1));
        when(bookRepository.findBookByTitleAndAuthorAndIsbn("Clean", null, null)).thenReturn(matches);

        assertThat(bookService.search("Clean", null, null)).isEqualTo(matches);
        verify(bookRepository, never()).findAll();
    }
}
