package org.czbalint.librarymanager.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateReaderRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        String phone) {
}
