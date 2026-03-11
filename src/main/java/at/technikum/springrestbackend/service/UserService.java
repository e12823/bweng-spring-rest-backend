package at.technikum.springrestbackend.service;

import at.technikum.springrestbackend.dto.UpdateUserRequest;
import at.technikum.springrestbackend.dto.UserDto;
import at.technikum.springrestbackend.entity.User;
import at.technikum.springrestbackend.repository.BlogPostRepository;
import at.technikum.springrestbackend.repository.CommentRepository;
import at.technikum.springrestbackend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final BlogPostRepository blogPostRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       CommentRepository commentRepository,
                       BlogPostRepository blogPostRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.blogPostRepository = blogPostRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Page<UserDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(UserDto::fromEntity);
    }

    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return UserDto.fromEntity(user);
    }

    public UserDto updateUser(Long id, UpdateUserRequest request) {
        ensureSelfOrAdmin(id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (request.getUsername() != null
                && !request.getUsername().isBlank()
                && !request.getUsername().equals(user.getUsername())) {

            if (userRepository.existsByUsername(request.getUsername())) {
                throw new IllegalArgumentException("Username already exists");
            }

            user.setUsername(request.getUsername());
        }

        if (request.getCountry() != null && !request.getCountry().isBlank()) {
            user.setCountry(request.getCountry());
        }

        if (request.getProfileImageUrl() != null && !request.getProfileImageUrl().isBlank()) {
            user.setProfileImageUrl(request.getProfileImageUrl());
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        User savedUser = userRepository.save(user);
        return UserDto.fromEntity(savedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        ensureSelfOrAdmin(id);

        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found");
        }

        commentRepository.deleteByUserId(id);
        commentRepository.deleteByPostUserId(id);
        blogPostRepository.deleteByUserId(id);
        userRepository.deleteById(id);
    }

    private Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            return Optional.empty();
        }
        return Optional.of(user);
    }

    private void ensureSelfOrAdmin(Long targetUserId) {
        Optional<User> currentUser = getCurrentUser();
        if (currentUser.isEmpty()) {
            return;
        }

        User user = currentUser.get();
        boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole());
        boolean isSelf = targetUserId != null && targetUserId.equals(user.getId());

        if (!isAdmin && !isSelf) {
            throw new AccessDeniedException("You are not allowed to modify this user");
        }
    }
}