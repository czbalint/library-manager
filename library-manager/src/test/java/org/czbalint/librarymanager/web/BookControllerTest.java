package org.czbalint.librarymanager.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.czbalint.librarymanager.dto.CreateBookRequest;
import org.czbalint.librarymanager.entity.Book;
import org.czbalint.librarymanager.service.BookService;
import org.czbalint.librarymanager.web.error.DuplicateResourceException;
import org.czbalint.librarymanager.web.error.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookService bookService;

    private static Book book() {
        Book book = new Book("Clean Code", "Robert Martin", "111", 3);
        book.setId(1L);
        return book;
    }

    @Test
    void create_returns201() throws Exception {
        when(bookService.addBook(any())).thenReturn(book());

        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateBookRequest("Clean Code", "Robert Martin", "111", 3))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.availableCopies").value(3));
    }

    @Test
    void create_returns400WhenBlankFieldsOrZeroCopies() throws Exception {
        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateBookRequest("", "", "", 0))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_returns409WhenIsbnDuplicate() throws Exception {
        when(bookService.addBook(any()))
                .thenThrow(new DuplicateResourceException("A book with ISBN 111 already exists"));

        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateBookRequest("Clean Code", "Robert Martin", "111", 3))))
                .andExpect(status().isConflict());
    }

    @Test
    void list_returnsBooks() throws Exception {
        when(bookService.listBooks()).thenReturn(List.of(book()));

        mockMvc.perform(get("/api/v1/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Clean Code"));
    }

    @Test
    void search_passesParamsToService() throws Exception {
        when(bookService.search("Clean", null, null)).thenReturn(List.of(book()));

        mockMvc.perform(get("/api/v1/books/search").param("title", "Clean"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].isbn").value("111"));
    }

    @Test
    void get_returnsBook() throws Exception {
        when(bookService.getBook(1L)).thenReturn(book());

        mockMvc.perform(get("/api/v1/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.author").value("Robert Martin"));
    }

    @Test
    void get_returns404WhenMissing() throws Exception {
        when(bookService.getBook(99L)).thenThrow(ResourceNotFoundException.of("Book", 99L));

        mockMvc.perform(get("/api/v1/books/99"))
                .andExpect(status().isNotFound());
    }
}
