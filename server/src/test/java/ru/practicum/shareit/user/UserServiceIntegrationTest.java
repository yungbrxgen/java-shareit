package ru.practicum.shareit.user;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserServiceIntegrationTest {

    private final UserService userService;
    private final EntityManager em;

    @Test
    void getById_shouldReturnUserFromDb() {
        User user = new User(null, "IntegrationUser", "integration@mail.ru");
        em.persist(user);
        em.flush();

        UserDto retrievedUser = userService.getById(user.getId());

        assertNotNull(retrievedUser);
        assertEquals(user.getId(), retrievedUser.getId());
        assertEquals("IntegrationUser", retrievedUser.getName());
        assertEquals("integration@mail.ru", retrievedUser.getEmail());
    }
}