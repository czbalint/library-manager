package org.czbalint.librarymanager.dto;

import java.time.LocalDate;
import org.czbalint.librarymanager.entity.Loan;

public record LoanResponse(
        Long id,
        Long bookId,
        String bookTitle,
        Long readerId,
        String readerName,
        LocalDate loanDate,
        LocalDate dueDate,
        LocalDate returnDate,
        boolean active) {

    public static LoanResponse from(Loan loan) {
        return new LoanResponse(
                loan.getId(),
                loan.getBook().getId(),
                loan.getBook().getTitle(),
                loan.getReader().getId(),
                loan.getReader().getName(),
                loan.getLoanDate(),
                loan.getDueDate(),
                loan.getReturnDate(),
                loan.isActive());
    }
}
