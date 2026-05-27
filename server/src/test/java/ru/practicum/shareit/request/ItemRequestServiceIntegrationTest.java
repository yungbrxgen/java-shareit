package ru.practicum.shareit.request;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestServiceIntegrationTest {

    private final ItemRequestService requestService;
    private final EntityManager em;

    @Test
    void createRequest_shouldSaveAndReturnRequest() {
        User user = new User(null, "Requester", "req@mail.ru");
        em.persist(user);
        em.flush();

        ItemRequestDto savedRequest = requestService.createRequest(user.getId(), "Нужна лестница");

        assertNotNull(savedRequest.getId());
        assertEquals("Нужна лестница", savedRequest.getDescription());
    }
}