package org.czbalint.librarymanager.service;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.czbalint.librarymanager.entity.Book;
import org.czbalint.librarymanager.repository.BookRepository;
import org.czbalint.librarymanager.dto.CreateBookRequest;
import org.czbalint.librarymanager.error.DuplicateResourceException;
import org.czbalint.librarymanager.error.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;

    @Transactional
    public Book addBook(CreateBookRequest request) {
        if (bookRepository.existsByIsbn(request.isbn())) {
            throw new DuplicateResourceException("A book with ISBN " + request.isbn() + " already exists");
        }
        return bookRepository.save(
                new Book(request.title(), request.author(), request.isbn(), request.totalCopies()));
    }

    public List<Book> listBooks() {
        return bookRepository.findAll();
    }

    public Book getBook(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Book", id));
    }

    public List<Book> search(String title, String author, String isbn) {
        if (title == null && author == null && isbn == null) {
            return bookRepository.findAll();
        }
        
        return bookRepository.findBookByTitleAndAuthorAndIsbn(title, author, isbn);
    }
}
