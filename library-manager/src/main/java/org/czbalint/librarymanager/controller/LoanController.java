package org.czbalint.librarymanager.controller;

import jakarta.validation.Valid;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.czbalint.librarymanager.service.LoanService;
import org.czbalint.librarymanager.dto.LoanResponse;
import org.czbalint.librarymanager.dto.StartLoanRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @PostMapping
    public ResponseEntity<LoanResponse> start(@Valid @RequestBody StartLoanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(LoanResponse.from(loanService.startLoan(request.readerId(), request.bookId(), request.days())));
    }

    @PatchMapping("/{id}/return")
    public ResponseEntity<LoanResponse> returnLoan(@PathVariable Long id) {
        return ResponseEntity.ok(LoanResponse.from(loanService.returnLoan(id)));
    }

    @GetMapping("/active")
    public ResponseEntity<List<LoanResponse>> active() {
        return ResponseEntity.ok(loanService.listActive().stream().map(LoanResponse::from).toList());
    }

    @GetMapping("/expired")
    public ResponseEntity<List<LoanResponse>> expired() {
        return ResponseEntity.ok(loanService.listExpired().stream().map(LoanResponse::from).toList());
    }
}
