package org.czbalint.librarymanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.czbalint.librarymanager.dto.CreateReaderRequest;
import org.czbalint.librarymanager.dto.UpdateReaderRequest;
import org.czbalint.librarymanager.entity.Reader;
import org.czbalint.librarymanager.repository.ReaderRepository;
import org.czbalint.librarymanager.error.DuplicateResourceException;
import org.czbalint.librarymanager.error.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReaderServiceTest {

    @Mock
    private ReaderRepository readerRepository;

    @InjectMocks
    private ReaderService readerService;

    @Test
    void register_savesWhenEmailIsNew() {
        CreateReaderRequest request = new CreateReaderRequest("Ann", "ann@example.com", "123");
        when(readerRepository.existsByEmail("ann@example.com")).thenReturn(false);
        when(readerRepository.save(any(Reader.class))).thenAnswer(inv -> inv.getArgument(0));

        Reader result = readerService.register(request);

        assertThat(result.getName()).isEqualTo("Ann");
        assertThat(result.getEmail()).isEqualTo("ann@example.com");
        assertThat(result.getPhone()).isEqualTo("123");
        verify(readerRepository).save(any(Reader.class));
    }

    @Test
    void register_throwsWhenEmailExists() {
        CreateReaderRequest request = new CreateReaderRequest("Ann", "ann@example.com", "123");
        when(readerRepository.existsByEmail("ann@example.com")).thenReturn(true);

        assertThatThrownBy(() -> readerService.register(request))
                .isInstanceOf(DuplicateResourceException.class);
        verify(readerRepository, never()).save(any());
    }

    @Test
    void update_changesAllProvidedFields() {
        Reader reader = new Reader("Old", "old@example.com", "000");
        when(readerRepository.findById(1L)).thenReturn(Optional.of(reader));
        when(readerRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(readerRepository.save(any(Reader.class))).thenAnswer(inv -> inv.getArgument(0));

        Reader result = readerService.update(1L,
                new UpdateReaderRequest("New", "new@example.com", "999"));

        assertThat(result.getName()).isEqualTo("New");
        assertThat(result.getEmail()).isEqualTo("new@example.com");
        assertThat(result.getPhone()).isEqualTo("999");
    }

    @Test
    void update_allowsKeepingSameEmail() {
        Reader reader = new Reader("Old", "same@example.com", "000");
        when(readerRepository.findById(1L)).thenReturn(Optional.of(reader));
        when(readerRepository.save(any(Reader.class))).thenAnswer(inv -> inv.getArgument(0));

        Reader result = readerService.update(1L,
                new UpdateReaderRequest(null, "same@example.com", null));

        assertThat(result.getEmail()).isEqualTo("same@example.com");
        verify(readerRepository, never()).existsByEmail(any());
    }

    @Test
    void update_throwsWhenNewEmailBelongsToAnotherReader() {
        Reader reader = new Reader("Old", "old@example.com", "000");
        when(readerRepository.findById(1L)).thenReturn(Optional.of(reader));
        when(readerRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThatThrownBy(() -> readerService.update(1L,
                new UpdateReaderRequest(null, "taken@example.com", null)))
                .isInstanceOf(DuplicateResourceException.class);
        verify(readerRepository, never()).save(any());
    }

    @Test
    void update_throwsWhenReaderMissing() {
        when(readerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> readerService.update(99L, new UpdateReaderRequest("X", null, null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listReaders_returnsAll() {
        when(readerRepository.findAll()).thenReturn(List.of(new Reader("A", "a@x.com", "1")));

        assertThat(readerService.listReaders()).hasSize(1);
    }

    @Test
    void getReader_returnsWhenFound() {
        Reader reader = new Reader("A", "a@x.com", "1");
        when(readerRepository.findById(1L)).thenReturn(Optional.of(reader));

        assertThat(readerService.getReader(1L)).isSameAs(reader);
    }
}
