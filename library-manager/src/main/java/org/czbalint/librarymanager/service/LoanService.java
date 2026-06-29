package org.czbalint.librarymanager.service;

import java.time.LocalDate;
import java.util.List;

import lombok.RequiredArgsConstructor;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final ReaderRepository readerRepository;
    private final LoanProperties loanProperties;

    @Transactional
    public Loan startLoan(Long readerId, Long bookId, Integer days) {
        Reader reader = readerRepository.findById(readerId)
                .orElseThrow(() -> ResourceNotFoundException.of("Reader", readerId));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> ResourceNotFoundException.of("Book", bookId));

        int maxActiveLoansPerReader = loanProperties.getMaxActiveLoansPerReader();
        if (loanRepository.countByReaderIdAndReturnDateIsNull(readerId) >= maxActiveLoansPerReader) {
            throw new LoanLimitExceededException(
                    "Reader " + readerId + " already holds the maximum of "
                            + maxActiveLoansPerReader + " active loans");
        }
        if (book.getAvailableCopies() <= 0) {
            throw new BookNotAvailableException("No available copies for book " + bookId);
        }

        LocalDate today = LocalDate.now();

        Loan loan = new Loan(book, reader, today, today.plusDays(days != null ? days : loanProperties.getPeriodDays()));
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        return loanRepository.save(loan);
    }

    @Transactional
    public Loan returnLoan(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> ResourceNotFoundException.of("Loan", loanId));
        if (!loan.isActive()) {
            throw new InvalidLoanStateException("Loan " + loanId + " has already been returned");
        }
        loan.setReturnDate(LocalDate.now());
        Book book = loan.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        return loanRepository.save(loan);
    }

    public List<Loan> listActive() {
        return loanRepository.findByReturnDateIsNull();
    }

    public List<Loan> listExpired() {
        return loanRepository.findByReturnDateIsNullAndDueDateBefore(LocalDate.now());
    }
}
