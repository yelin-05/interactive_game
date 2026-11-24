package com.example.moshi.service;

import com.example.moshi.TestDatabase;
import com.example.moshi.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthServiceTest extends TestDatabase {

    private final AuthService auth = new AuthService();

    @BeforeEach
    void clean() throws Exception {
        clearData();
    }

    @Test
    void registersAndAuthenticatesWithBcrypt() throws Exception {
        auth.register("student01", "secret6", "secret6", "学生");
        User user = auth.authenticate("student01", "secret6");
        assertNotNull(user);
        assertEquals("PLAYER", user.getRole());
        assertNull(auth.authenticate("student01", "wrong-password"));
    }

    @Test
    void rejectsInvalidAndDuplicateRegistration() throws Exception {
        assertThrows(IllegalArgumentException.class,
                () -> auth.register("bad name", "secret6", "secret6", null));
        assertThrows(IllegalArgumentException.class,
                () -> auth.register("student01", "short", "short", null));
        auth.register("student01", "secret6", "secret6", null);
        assertThrows(IllegalArgumentException.class,
                () -> auth.register("student01", "secret6", "secret6", null));
    }
}
