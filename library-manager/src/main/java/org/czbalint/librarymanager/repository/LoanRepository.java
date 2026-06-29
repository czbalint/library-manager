package org.czbalint.librarymanager.repository;

import java.time.LocalDate;
import java.util.List;
import org.czbalint.librarymanager.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    long countByReaderIdAndReturnDateIsNull(Long readerId);

    List<Loan> findByReturnDateIsNull();

    List<Loan> findByReturnDateIsNullAndDueDateBefore(LocalDate date);
}
