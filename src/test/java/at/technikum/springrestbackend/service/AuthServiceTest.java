package at.technikum.springrestbackend.service;

import at.technikum.springrestbackend.dto.JwtResponseDto;
import at.technikum.springrestbackend.dto.LoginRequest;
import at.technikum.springrestbackend.dto.RegisterRequest;
import at.technikum.springrestbackend.dto.UserDto;
import at.technikum.springrestbackend.entity.User;
import at.technikum.springrestbackend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_shouldCreateUserWithPlaceholderImage_whenProfileImageMissing() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("max@example.com");
        request.setUsername("maxpower");
        request.setPassword("Secure123");
        request.setCountry("AT");
        request.setProfileImageUrl(" ");

        when(userRepository.existsByEmail("max@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("maxpower")).thenReturn(false);
        when(passwordEncoder.encode("Secure123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setRole("USER");
            user.setProfileImageUrl("placeholder.jpg");
            return user;
        });

        UserDto result = authService.register(request);

        assertEquals("max@example.com", result.getEmail());
        assertEquals("maxpower", result.getUsername());
        assertEquals("placeholder.jpg", result.getProfileImageUrl());
        assertEquals("USER", result.getRole());
    }

    @Test
    void register_shouldUseProvidedProfileImage_whenPresent() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("eva@example.com");
        request.setUsername("evastar");
        request.setPassword("Secure123");
        request.setCountry("DE");
        request.setProfileImageUrl("https://img.example/avatar.png");

        when(userRepository.existsByEmail("eva@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("evastar")).thenReturn(false);
        when(passwordEncoder.encode("Secure123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDto result = authService.register(request);

        assertEquals("https://img.example/avatar.png", result.getProfileImageUrl());
    }

    @Test
    void register_shouldThrow_whenEmailExists() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("taken@example.com");

        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.register(request));

        assertEquals("Email already exists", ex.getMessage());
    }

    @Test
    void register_shouldThrow_whenUsernameExists() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("x@example.com");
        request.setUsername("takenuser");

        when(userRepository.existsByEmail("x@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("takenuser")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.register(request));

        assertEquals("Username already exists", ex.getMessage());
    }

    @Test
    void login_shouldReturnJwtResponse_whenCredentialsAreValid() {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("Secure123");

        User user = new User("user@example.com", "myuser", "hashed", "AT");
        user.setRole("ADMIN");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Secure123", "hashed")).thenReturn(true);
        when(jwtService.generateToken("user@example.com")).thenReturn("jwt-token");

        JwtResponseDto response = authService.login(request);

        assertEquals("jwt-token", response.getToken());
        assertEquals("myuser", response.getUsername());
        assertEquals("user@example.com", response.getEmail());
        assertEquals("ADMIN", response.getRole());
    }

    @Test
    void login_shouldThrow_whenUserNotFound() {
        LoginRequest request = new LoginRequest();
        request.setEmail("missing@example.com");
        request.setPassword("Secure123");

        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.login(request));

        assertTrue(ex.getMessage().contains("Invalid email or password"));
    }

    @Test
    void login_shouldThrow_whenPasswordIsInvalid() {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("bad");

        User user = new User("user@example.com", "myuser", "hashed", "AT");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("bad", "hashed")).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.login(request));

        assertTrue(ex.getMessage().contains("Invalid email or password"));
    }
}
