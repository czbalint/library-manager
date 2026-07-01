package org.czbalint.librarymanager.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.czbalint.librarymanager.dto.StartLoanRequest;
import org.czbalint.librarymanager.entity.Book;
import org.czbalint.librarymanager.entity.Loan;
import org.czbalint.librarymanager.entity.Reader;
import org.czbalint.librarymanager.service.LoanService;
import org.czbalint.librarymanager.error.InvalidLoanStateException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(LoanController.class)
class LoanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LoanService loanService;

    private static Loan loan(Long id) {
        Book book = new Book("Clean Code", "Robert Martin", "111", 3);
        book.setId(2L);
        Reader reader = new Reader("Ann", "ann@example.com", "123");
        reader.setId(1L);
        Loan loan = new Loan(book, reader, LocalDate.now(), LocalDate.now().plusDays(14));
        loan.setId(id);
        return loan;
    }

    @Test
    void start_returns201() throws Exception {
        when(loanService.startLoan(any(), any(), any())).thenReturn(loan(10L));

        mockMvc.perform(post("/api/v1/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new StartLoanRequest(1L, 2L, 7))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.bookId").value(2))
                .andExpect(jsonPath("$.readerId").value(1))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void start_returns400WhenIdsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new StartLoanRequest(null, null, 7))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnLoan_returnsOk() throws Exception {
        Loan returned = loan(10L);
        returned.setReturnDate(LocalDate.now());
        when(loanService.returnLoan(10L)).thenReturn(returned);

        mockMvc.perform(patch("/api/v1/loans/10/return"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void returnLoan_returns409WhenAlreadyReturned() throws Exception {
        when(loanService.returnLoan(10L))
                .thenThrow(new InvalidLoanStateException("Loan 10 has already been returned"));

        mockMvc.perform(patch("/api/v1/loans/10/return"))
                .andExpect(status().isConflict());
    }

    @Test
    void expired_returnsExpiredLoans() throws Exception {
        when(loanService.listExpired()).thenReturn(List.of(loan(11L)));

        mockMvc.perform(get("/api/v1/loans/expired"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(11));
    }
}
