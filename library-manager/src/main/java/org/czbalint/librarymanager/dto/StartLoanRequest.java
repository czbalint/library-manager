package org.czbalint.librarymanager.dto;

import jakarta.validation.constraints.NotNull;

public record StartLoanRequest(
        @NotNull Long readerId,
        @NotNull Long bookId,
        Integer days) {
}
