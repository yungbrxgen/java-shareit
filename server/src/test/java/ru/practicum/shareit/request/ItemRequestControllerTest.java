package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
public class ItemRequestControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemRequestService requestService;

    private ItemRequestDto requestDto;

    @BeforeEach
    void setUp() {
        requestDto = ItemRequestDto.builder()
                .id(1L)
                .description("Нужен мощный перфоратор")
                .created(LocalDateTime.now())
                .build();
    }

    @Test
    void createRequest_success() throws Exception {
        when(requestService.createRequest(anyLong(), anyString()))
                .thenReturn(requestDto);

        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(requestDto.getId()))
                .andExpect(jsonPath("$.description").value(requestDto.getDescription()));
    }

    @Test
    void getMyRequests_success() throws Exception {
        when(requestService.getMyRequests(anyLong(), any()))
                .thenReturn(List.of(requestDto));

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].description").value(requestDto.getDescription()));
    }

    @Test
    void getAllOtherRequests_success() throws Exception {
        when(requestService.getAllOtherRequests(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(requestDto));

        mvc.perform(get("/requests/all")
                        .param("from", "0")
                        .param("size", "10")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getRequestById_success() throws Exception {
        when(requestService.getRequestById(anyLong(), anyLong()))
                .thenReturn(requestDto);

        mvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Нужен мощный перфоратор"));
    }

    @Test
    void createRequest_withoutHeader_shouldReturnBadRequest() throws Exception {
        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }
}