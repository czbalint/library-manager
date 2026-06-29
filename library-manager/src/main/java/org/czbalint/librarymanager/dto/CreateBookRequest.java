package org.czbalint.librarymanager.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateBookRequest(
        @NotBlank String title,
        @NotBlank String author,
        @NotBlank String isbn,
        @Min(1) int totalCopies) {
}
