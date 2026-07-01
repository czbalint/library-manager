package org.czbalint.librarymanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.czbalint.librarymanager.config.LoanProperties;
import org.czbalint.librarymanager.entity.Book;
import org.czbalint.librarymanager.entity.Loan;
import org.czbalint.librarymanager.entity.Reader;
import org.czbalint.librarymanager.repository.BookRepository;
import org.czbalint.librarymanager.repository.LoanRepository;
import org.czbalint.librarymanager.repository.ReaderRepository;
import org.czbalint.librarymanager.web.error.BookNotAvailableException;
import org.czbalint.librarymanager.web.error.InvalidLoanStateException;
import org.czbalint.librarymanager.web.error.LoanLimitExceededException;
import org.czbalint.librarymanager.web.error.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private ReaderRepository readerRepository;

    private LoanService loanService;

    @BeforeEach
    void setup() {
        LoanProperties loanProperties = new LoanProperties();
        loanProperties.setPeriodDays(14);
        loanProperties.setMaxActiveLoansPerReader(5);
        loanService = new LoanService(loanRepository, bookRepository, readerRepository, loanProperties);
    }

    @Test
    void startLoan_createsLoanAndDecrementsAvailableCopies() {
        Reader reader = new Reader("Ann", "ann@x.com", "1");
        Book book = new Book("A", "X", "1", 2);
        when(readerRepository.findById(1L)).thenReturn(Optional.of(reader));
        when(bookRepository.findById(2L)).thenReturn(Optional.of(book));
        when(loanRepository.countByReaderIdAndReturnDateIsNull(1L)).thenReturn(0L);
        when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));

        Loan loan = loanService.startLoan(1L, 2L, 7);

        assertThat(loan.getBook()).isSameAs(book);
        assertThat(loan.getReader()).isSameAs(reader);
        assertThat(loan.getDueDate()).isEqualTo(loan.getLoanDate().plusDays(7));
        assertThat(book.getAvailableCopies()).isEqualTo(1);
        assertThat(loan.isActive()).isTrue();
    }

    @Test
    void startLoan_usesDefaultPeriodWhenDaysIsNull() {
        Reader reader = new Reader("Ann", "ann@x.com", "1");
        Book book = new Book("A", "X", "1", 2);
        when(readerRepository.findById(1L)).thenReturn(Optional.of(reader));
        when(bookRepository.findById(2L)).thenReturn(Optional.of(book));
        when(loanRepository.countByReaderIdAndReturnDateIsNull(1L)).thenReturn(0L);
        when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));

        Loan loan = loanService.startLoan(1L, 2L, null);

        assertThat(loan.getDueDate()).isEqualTo(loan.getLoanDate().plusDays(14));
    }

    @Test
    void startLoan_throwsWhenReaderMissing() {
        when(readerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanService.startLoan(1L, 2L, 7))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Reader");
    }

    @Test
    void startLoan_throwsWhenBookMissing() {
        when(readerRepository.findById(1L)).thenReturn(Optional.of(new Reader("Ann", "a@x.com", "1")));
        when(bookRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanService.startLoan(1L, 2L, 7))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Book");
    }

    @Test
    void startLoan_throwsWhenLoanLimitReached() {
        when(readerRepository.findById(1L)).thenReturn(Optional.of(new Reader("Ann", "a@x.com", "1")));
        when(bookRepository.findById(2L)).thenReturn(Optional.of(new Book("A", "X", "1", 2)));
        when(loanRepository.countByReaderIdAndReturnDateIsNull(1L)).thenReturn(5L);

        assertThatThrownBy(() -> loanService.startLoan(1L, 2L, 7))
                .isInstanceOf(LoanLimitExceededException.class);
        verify(loanRepository, never()).save(any());
    }

    @Test
    void startLoan_throwsWhenNoCopiesAvailable() {
        Book book = new Book("A", "X", "1", 1, 0);
        when(readerRepository.findById(1L)).thenReturn(Optional.of(new Reader("Ann", "a@x.com", "1")));
        when(bookRepository.findById(2L)).thenReturn(Optional.of(book));
        when(loanRepository.countByReaderIdAndReturnDateIsNull(1L)).thenReturn(0L);

        assertThatThrownBy(() -> loanService.startLoan(1L, 2L, 7))
                .isInstanceOf(BookNotAvailableException.class);
        verify(loanRepository, never()).save(any());
    }

    @Test
    void returnLoan_marksReturnedAndIncrementsCopies() {
        Book book = new Book("A", "X", "1", 2, 1);
        Reader reader = new Reader("Ann", "a@x.com", "1");
        Loan loan = new Loan(book, reader, LocalDate.now(), LocalDate.now().plusDays(7));
        when(loanRepository.findById(10L)).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));

        Loan result = loanService.returnLoan(10L);

        assertThat(result.getReturnDate()).isEqualTo(LocalDate.now());
        assertThat(result.isActive()).isFalse();
        assertThat(book.getAvailableCopies()).isEqualTo(2);
    }

    @Test
    void returnLoan_throwsWhenLoanMissing() {
        when(loanRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanService.returnLoan(10L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void returnLoan_throwsWhenAlreadyReturned() {
        Book book = new Book("A", "X", "1", 2, 1);
        Loan loan = new Loan(book, new Reader("Ann", "a@x.com", "1"),
                LocalDate.now(), LocalDate.now().plusDays(7));
        loan.setReturnDate(LocalDate.now());
        when(loanRepository.findById(10L)).thenReturn(Optional.of(loan));

        assertThatThrownBy(() -> loanService.returnLoan(10L))
                .isInstanceOf(InvalidLoanStateException.class);
        verify(loanRepository, never()).save(any());
    }

    @Test
    void listExpired_queriesByTodayAndReturnDateNull() {
        List<Loan> expired = List.of(new Loan());
        when(loanRepository.findByReturnDateIsNullAndDueDateBefore(LocalDate.now())).thenReturn(expired);

        assertThat(loanService.listExpired()).isEqualTo(expired);
    }
}
