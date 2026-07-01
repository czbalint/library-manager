package org.czbalint.librarymanager.controller;

import jakarta.validation.Valid;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.czbalint.librarymanager.service.ReaderService;
import org.czbalint.librarymanager.dto.CreateReaderRequest;
import org.czbalint.librarymanager.dto.ReaderResponse;
import org.czbalint.librarymanager.dto.UpdateReaderRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/readers")
@RequiredArgsConstructor
public class ReaderController {

    private final ReaderService readerService;

    @PostMapping
    public ResponseEntity<ReaderResponse> create(@Valid @RequestBody CreateReaderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ReaderResponse.from(readerService.register(request)));
    }

    @GetMapping
    public ResponseEntity<List<ReaderResponse>> list() {
        return ResponseEntity.ok(readerService.listReaders().stream().map(ReaderResponse::from).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReaderResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(ReaderResponse.from(readerService.getReader(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReaderResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateReaderRequest request) {
        return ResponseEntity.ok(ReaderResponse.from(readerService.update(id, request)));
    }
}
