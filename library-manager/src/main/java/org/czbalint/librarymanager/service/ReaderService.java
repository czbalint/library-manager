package org.czbalint.librarymanager.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.czbalint.librarymanager.entity.Reader;
import org.czbalint.librarymanager.repository.ReaderRepository;
import org.czbalint.librarymanager.dto.CreateReaderRequest;
import org.czbalint.librarymanager.dto.UpdateReaderRequest;
import org.czbalint.librarymanager.error.DuplicateResourceException;
import org.czbalint.librarymanager.error.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReaderService {

    private final ReaderRepository readerRepository;

    @Transactional
    public Reader register(CreateReaderRequest request) {
        if (readerRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("A reader with email " + request.email() + " already exists");
        }
        Reader reader = new Reader(request.name(), request.email(), request.phone());
        return readerRepository.save(reader);
    }

    @Transactional
    public Reader update(Long id, UpdateReaderRequest request) {
        Reader reader = getReader(id);

        if (request.email() != null) {
            if (!reader.getEmail().equals(request.email()) && readerRepository.existsByEmail(request.email())) {
                throw new DuplicateResourceException("A reader with email " + request.email() + " already exists");
            }
            reader.setEmail(request.email());
        }
        if (request.name() != null) {
            reader.setName(request.name());
        }
        if (request.phone() != null) {
            reader.setPhone(request.phone());
        }
        return readerRepository.save(reader);
    }

    public List<Reader> listReaders() {
        return readerRepository.findAll();
    }

    public Reader getReader(Long id) {
        return readerRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Reader", id));
    }
}
