package at.technikum.springrestbackend.security;

import at.technikum.springrestbackend.entity.User;
import at.technikum.springrestbackend.repository.UserRepository;
import at.technikum.springrestbackend.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    private static final String SECRET = "0123456789012345678901234567890123456789";

    @Mock
    private UserRepository userRepository;

    @Mock
    private FilterChain filterChain;

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_shouldSkipPublicEndpoint() throws ServletException, IOException {
        JwtService jwtService = new JwtService(SECRET, 60_000L);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, userRepository);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/posts");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_shouldSkipWhenHeaderMissing() throws ServletException, IOException {
        JwtService jwtService = new JwtService(SECRET, 60_000L);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, userRepository);

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/posts");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_shouldSetAuthenticationForValidToken() throws ServletException, IOException {
        JwtService jwtService = new JwtService(SECRET, 60_000L);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, userRepository);

        User user = new User("user@example.com", "user1", "pw", "AT");
        user.setRole("ADMIN");

        String token = jwtService.generateToken("user@example.com");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/posts");
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
