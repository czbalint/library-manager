package org.czbalint.librarymanager.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "loans")
@Getter
@Setter
@NoArgsConstructor
public class Loan extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "BOOK_ID", nullable = false)
    private Book book;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "READER_ID", nullable = false)
    private Reader reader;

    @Column(name = "LOAN_DATE", nullable = false)
    private LocalDate loanDate;

    @Column(name = "DUE_DATE", nullable = false)
    private LocalDate dueDate;

    /** Null while the loan is active; set when the book is taken back. */
    @Column(name = "RETURN_DATE")
    private LocalDate returnDate;

    public Loan(Book book, Reader reader, LocalDate loanDate, LocalDate dueDate) {
        this.book = book;
        this.reader = reader;
        this.loanDate = loanDate;
        this.dueDate = dueDate;
    }

    public boolean isActive() {
        return returnDate == null;
    }
}
