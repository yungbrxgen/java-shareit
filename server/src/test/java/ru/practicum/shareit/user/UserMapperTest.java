package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class UserMapperTest {

    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    @Test
    void toUserDto_shouldMapFieldsCorrectly() {
        User user = new User(1L, "Ivan", "ivan@mail.ru");

        UserDto dto = userMapper.toUserDto(user);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("Ivan", dto.getName());
        assertEquals("ivan@mail.ru", dto.getEmail());
    }

    @Test
    void toUser_shouldMapFieldsCorrectly() {
        UserDto dto = UserDto.builder().id(1L).name("Ivan").email("ivan@mail.ru").build();

        User user = userMapper.toUser(dto);

        assertNotNull(user);
        assertEquals(1L, user.getId());
        assertEquals("Ivan", user.getName());
        assertEquals("ivan@mail.ru", user.getEmail());
    }
}