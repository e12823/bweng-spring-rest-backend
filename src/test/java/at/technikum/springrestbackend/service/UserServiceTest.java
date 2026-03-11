package at.technikum.springrestbackend.service;

import at.technikum.springrestbackend.dto.UpdateUserRequest;
import at.technikum.springrestbackend.dto.UserDto;
import at.technikum.springrestbackend.entity.User;
import at.technikum.springrestbackend.repository.BlogPostRepository;
import at.technikum.springrestbackend.repository.CommentRepository;
import at.technikum.springrestbackend.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private BlogPostRepository blogPostRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    private void authenticate(Long id, String role) {
        User user = new User("auth@example.com", "authuser", "pw", "AT");
        user.setRole(role);
        try {
            java.lang.reflect.Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, java.util.List.of())
        );
    }

    @Test
    void getAllUsers_shouldMapEntitiesToDtos() {
        User user = new User("a@example.com", "alice1", "pw", "AT");
        user.setRole("USER");
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> page = new PageImpl<>(List.of(user), pageable, 1);

        when(userRepository.findAll(pageable)).thenReturn(page);

        Page<UserDto> result = userService.getAllUsers(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("a@example.com", result.getContent().get(0).getEmail());
    }

    @Test
    void getUserById_shouldReturnDto_whenUserExists() {
        User user = new User("a@example.com", "alice1", "pw", "AT");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDto result = userService.getUserById(1L);

        assertEquals("alice1", result.getUsername());
    }

    @Test
    void getUserById_shouldThrow_whenMissing() {
        when(userRepository.findById(5L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.getUserById(5L));

        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void updateUser_shouldUpdateAllMutableFields() {
        User user = new User("u@example.com", "oldname", "oldpw", "AT");

        UpdateUserRequest request = new UpdateUserRequest();
        request.setUsername("newname");
        request.setCountry("DE");
        request.setProfileImageUrl("new.jpg");
        request.setPassword("Secure123");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("newname")).thenReturn(false);
        when(passwordEncoder.encode("Secure123")).thenReturn("encoded-pw");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDto result = userService.updateUser(1L, request);

        assertEquals("newname", result.getUsername());
        assertEquals("DE", result.getCountry());
        assertEquals("new.jpg", result.getProfileImageUrl());
    }

    @Test
    void updateUser_shouldThrow_whenNewUsernameAlreadyExists() {
        User user = new User("u@example.com", "oldname", "oldpw", "AT");

        UpdateUserRequest request = new UpdateUserRequest();
        request.setUsername("takenname");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("takenname")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.updateUser(1L, request));

        assertEquals("Username already exists", ex.getMessage());
    }

    @Test
    void updateUser_shouldThrow_whenUserMissing() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.updateUser(1L, new UpdateUserRequest()));

        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void updateUser_shouldIgnoreUsernameCheck_whenUsernameUnchanged() {
        User user = new User("u@example.com", "sameuser", "oldpw", "AT");
        UpdateUserRequest request = new UpdateUserRequest();
        request.setUsername("sameuser");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDto result = userService.updateUser(1L, request);

        assertEquals("sameuser", result.getUsername());
    }

    @Test
    void updateUser_shouldThrowForbidden_whenAuthenticatedUserIsNotSelfOrAdmin() {
        authenticate(99L, "USER");

        assertThrows(AccessDeniedException.class,
                () -> userService.updateUser(1L, new UpdateUserRequest()));
    }

    @Test
    void deleteUser_shouldDeleteRelatedDataAndUser_whenExists() {
        when(userRepository.existsById(10L)).thenReturn(true);

        userService.deleteUser(10L);

        verify(commentRepository).deleteByUserId(10L);
        verify(commentRepository).deleteByPostUserId(10L);
        verify(blogPostRepository).deleteByUserId(10L);
        verify(userRepository).deleteById(10L);
    }

    @Test
    void deleteUser_shouldThrow_whenMissing() {
        when(userRepository.existsById(10L)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.deleteUser(10L));

        assertEquals("User not found", ex.getMessage());
    }
}
