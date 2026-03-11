package at.technikum.springrestbackend.config;

import at.technikum.springrestbackend.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class SecurityConfigTest {

    @Test
    void passwordEncoder_shouldEncodeAndMatch() {
        JwtAuthenticationFilter filter = mock(JwtAuthenticationFilter.class);

        SecurityConfig config = new SecurityConfig(filter);
        PasswordEncoder encoder = config.passwordEncoder();

        String encoded = encoder.encode("Secure123");

        assertNotNull(encoded);
        assertTrue(encoder.matches("Secure123", encoded));
    }
}
