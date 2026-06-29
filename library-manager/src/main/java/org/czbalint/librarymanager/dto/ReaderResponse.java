package org.czbalint.librarymanager.dto;

import java.time.LocalDate;
import org.czbalint.librarymanager.entity.Reader;

public record ReaderResponse(
        Long id,
        String name,
        String email,
        String phone) {

    public static ReaderResponse from(Reader reader) {
        return new ReaderResponse(reader.getId(), reader.getName(), reader.getEmail(), reader.getPhone());
    }
}
