package org.czbalint.librarymanager.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.czbalint.librarymanager.entity.Book;
import org.czbalint.librarymanager.repository.BookRepositoryCustom;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class BookRepositoryCustomImpl implements BookRepositoryCustom {

    private final EntityManager entityManager;

    @Override
    public List<Book> findBookByTitleAndAuthorAndIsbn(String title, String author, String isbn) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Book> cq = cb.createQuery(Book.class);
        Root<Book> book = cq.from(Book.class);

        Predicate finalPredicate = cb.conjunction();
        if (author != null) {
            Predicate authorPredicate = cb.equal(cb.lower(book.get("author")), author.toLowerCase());
            finalPredicate = cb.and(finalPredicate, authorPredicate);
        }
        if (title != null) {
            Predicate titlePredicate = cb.like(cb.lower(book.get("title")), "%" + title.toLowerCase() + "%");
            finalPredicate = cb.and(finalPredicate, titlePredicate);
        }
        if (isbn != null) {
            Predicate isbnPredicate = cb.equal(cb.lower(book.get("isbn")), isbn.toLowerCase());
            finalPredicate = cb.and(finalPredicate, isbnPredicate);
        }

        cq.where(finalPredicate);

        TypedQuery<Book> query = entityManager.createQuery(cq);
        return query.getResultList();
    }
}
