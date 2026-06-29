package org.czbalint.librarymanager.dto;

import jakarta.validation.constraints.Email;

public record UpdateReaderRequest(
        String name,
        @Email String email,
        String phone) {
}
