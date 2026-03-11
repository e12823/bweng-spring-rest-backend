package at.technikum.springrestbackend.service;

import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String SECRET = "0123456789012345678901234567890123456789";

    @Test
    void token_shouldContainExpectedEmail_andBeValid() {
        JwtService jwtService = new JwtService(SECRET, 60_000L);

        String token = jwtService.generateToken("user@example.com");

        assertNotNull(token);
        assertTrue(jwtService.isTokenValid(token, "user@example.com"));
        assertFalse(jwtService.isTokenValid(token, "other@example.com"));
    }

    @Test
    void token_shouldBeExpired_whenExpirationIsInPast() {
        JwtService jwtService = new JwtService(SECRET, -1L);

        String token = jwtService.generateToken("user@example.com");

        assertThrows(ExpiredJwtException.class,
                () -> jwtService.isTokenValid(token, "user@example.com"));
    }

    @Test
    void extractExpiration_shouldReturnFutureDate_forFreshToken() {
        JwtService jwtService = new JwtService(SECRET, 120_000L);

        String token = jwtService.generateToken("user@example.com");
        Date expiration = jwtService.extractExpiration(token);

        assertTrue(expiration.after(new Date()));
    }
}
