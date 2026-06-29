package org.czbalint.librarymanager.repository;

import org.czbalint.librarymanager.entity.Book;

import java.util.List;

public interface BookRepositoryCustom {

    List<Book> findBookByTitleAndAuthorAndIsbn(String title, String author, String isbn);
}
