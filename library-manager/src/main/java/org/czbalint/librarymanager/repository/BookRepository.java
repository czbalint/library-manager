package org.czbalint.librarymanager.repository;
import org.czbalint.librarymanager.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long>, BookRepositoryCustom {

    boolean existsByIsbn(String isbn);
}
