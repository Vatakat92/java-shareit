package ru.practicum.shareit.request.client;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import ru.practicum.shareit.request.ItemRequestClient;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@TestPropertySource(properties = {
        "shareit-server.url=http://localhost:9090"
})
class ItemRequestClientConfigurationTest {

    @Autowired
    private ItemRequestClient itemRequestClient;

    @Test
    void contextLoads() {
        assertNotNull(itemRequestClient);
    }

    @Test
    void clientShouldBeProperlyConfigured() {
        assertNotNull(itemRequestClient);
    }
}