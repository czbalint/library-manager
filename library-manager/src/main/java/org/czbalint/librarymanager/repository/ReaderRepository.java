package org.czbalint.librarymanager.repository;

import org.czbalint.librarymanager.entity.Reader;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReaderRepository extends JpaRepository<Reader, Long> {

    boolean existsByEmail(String email);
}
