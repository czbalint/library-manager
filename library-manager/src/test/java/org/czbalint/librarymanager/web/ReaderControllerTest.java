package org.czbalint.librarymanager.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.czbalint.librarymanager.dto.CreateReaderRequest;
import org.czbalint.librarymanager.dto.UpdateReaderRequest;
import org.czbalint.librarymanager.entity.Reader;
import org.czbalint.librarymanager.service.ReaderService;
import org.czbalint.librarymanager.web.error.DuplicateResourceException;
import org.czbalint.librarymanager.web.error.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ReaderController.class)
class ReaderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReaderService readerService;

    private static Reader reader() {
        Reader reader = new Reader("Ann", "ann@example.com", "123");
        reader.setId(1L);
        return reader;
    }

    @Test
    void create_returns201WithBody() throws Exception {
        when(readerService.register(any())).thenReturn(reader());

        mockMvc.perform(post("/api/v1/readers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateReaderRequest("Ann", "ann@example.com", "123"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("ann@example.com"));
    }

    @Test
    void create_returns400WhenInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/readers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateReaderRequest("", "not-an-email", "123"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void create_returns409WhenEmailDuplicate() throws Exception {
        when(readerService.register(any()))
                .thenThrow(new DuplicateResourceException("A reader with email ann@example.com already exists"));

        mockMvc.perform(post("/api/v1/readers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateReaderRequest("Ann", "ann@example.com", "123"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void list_returnsReaders() throws Exception {
        when(readerService.listReaders()).thenReturn(List.of(reader()));

        mockMvc.perform(get("/api/v1/readers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void get_returns404WhenMissing() throws Exception {
        when(readerService.getReader(99L)).thenThrow(ResourceNotFoundException.of("Reader", 99L));

        mockMvc.perform(get("/api/v1/readers/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void update_returnsUpdatedReader() throws Exception {
        Reader updated = reader();
        updated.setName("New");
        when(readerService.update(eq(1L), any())).thenReturn(updated);

        mockMvc.perform(put("/api/v1/readers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UpdateReaderRequest("New", null, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New"));
    }

    @Test
    void update_returns400WhenEmailInvalid() throws Exception {
        mockMvc.perform(put("/api/v1/readers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UpdateReaderRequest(null, "bad-email", null))))
                .andExpect(status().isBadRequest());
    }
}
