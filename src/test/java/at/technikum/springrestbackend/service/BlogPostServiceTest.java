package at.technikum.springrestbackend.service;

import at.technikum.springrestbackend.dto.CreatePostRequest;
import at.technikum.springrestbackend.dto.UpdatePostRequest;
import at.technikum.springrestbackend.entity.BlogPost;
import at.technikum.springrestbackend.entity.User;
import at.technikum.springrestbackend.repository.BlogPostRepository;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlogPostServiceTest {

    @Mock
    private BlogPostRepository blogPostRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BlogPostService blogPostService;

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    private User authUser(Long id, String role) {
        User user = new User("auth@example.com", "authuser", "pw", "AT");
        user.setRole(role);
        if (id != null) {
            try {
                java.lang.reflect.Field idField = User.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(user, id);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, java.util.List.of())
        );
        return user;
    }

    @Test
    void getAllPosts_shouldReturnRepositoryPage() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<BlogPost> page = new PageImpl<>(List.of(new BlogPost()), pageable, 1);
        when(blogPostRepository.findAll(pageable)).thenReturn(page);

        Page<BlogPost> result = blogPostService.getAllPosts(pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getPostById_shouldReturnPost_whenFound() {
        BlogPost post = new BlogPost();
        when(blogPostRepository.findById(2L)).thenReturn(Optional.of(post));

        BlogPost result = blogPostService.getPostById(2L);

        assertEquals(post, result);
    }

    @Test
    void getPostById_shouldThrow_whenMissing() {
        when(blogPostRepository.findById(2L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> blogPostService.getPostById(2L));

        assertEquals("Post not found", ex.getMessage());
    }

    @Test
    void getLatestPosts_shouldLimitResult() {
        List<BlogPost> posts = List.of(new BlogPost(), new BlogPost(), new BlogPost());
        when(blogPostRepository.findAllByOrderByCreatedAtDesc()).thenReturn(posts);

        List<BlogPost> result = blogPostService.getLatestPosts(2);

        assertEquals(2, result.size());
    }

    @Test
    void searchPosts_shouldDelegateToRepository() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<BlogPost> page = new PageImpl<>(List.of(new BlogPost()), pageable, 1);
        when(blogPostRepository.findByTitleContainingIgnoreCase("java", pageable)).thenReturn(page);

        Page<BlogPost> result = blogPostService.searchPosts("java", pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void createPost_shouldCreateAndSavePost() {
        User user = new User("u@example.com", "user1", "pw", "AT");
        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("Title");
        request.setContent("Content");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(blogPostRepository.save(any(BlogPost.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BlogPost result = blogPostService.createPost(1L, request);

        assertEquals("Title", result.getTitle());
        assertEquals("Content", result.getContent());
        assertEquals(user, result.getUser());
    }

    @Test
    void createPost_shouldThrow_whenUserMissing() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> blogPostService.createPost(1L, new CreatePostRequest()));

        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void createPost_shouldUseAuthenticatedUser() {
        User current = authUser(20L, "USER");
        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("Title");
        request.setContent("Content");

        when(blogPostRepository.save(any(BlogPost.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BlogPost result = blogPostService.createPost(request);

        assertEquals("Title", result.getTitle());
        assertEquals(current, result.getUser());
    }

    @Test
    void createPost_shouldThrow_whenUnauthenticated() {
        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("Title");
        request.setContent("Content");

        assertThrows(AccessDeniedException.class, () -> blogPostService.createPost(request));
    }

    @Test
    void updatePost_shouldOnlyUpdateNonBlankFields() {
        BlogPost post = new BlogPost();
        post.setUser(new User("owner@example.com", "owner1", "pw", "AT"));
        post.setTitle("Old");
        post.setContent("Old Content");

        UpdatePostRequest request = new UpdatePostRequest();
        request.setTitle("New");
        request.setContent(" ");

        when(blogPostRepository.findById(7L)).thenReturn(Optional.of(post));
        when(blogPostRepository.save(any(BlogPost.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BlogPost result = blogPostService.updatePost(7L, request);

        assertEquals("New", result.getTitle());
        assertEquals("Old Content", result.getContent());
    }

    @Test
    void updatePost_shouldUpdateContent_whenTitleBlankButContentPresent() {
        BlogPost post = new BlogPost();
        post.setUser(new User("owner@example.com", "owner1", "pw", "AT"));
        post.setTitle("Old");
        post.setContent("Old Content");

        UpdatePostRequest request = new UpdatePostRequest();
        request.setTitle(" ");
        request.setContent("New Content");

        when(blogPostRepository.findById(7L)).thenReturn(Optional.of(post));
        when(blogPostRepository.save(any(BlogPost.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BlogPost result = blogPostService.updatePost(7L, request);

        assertEquals("Old", result.getTitle());
        assertEquals("New Content", result.getContent());
    }

    @Test
    void updatePost_shouldThrow_whenPostMissing() {
        when(blogPostRepository.findById(7L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> blogPostService.updatePost(7L, new UpdatePostRequest()));

        assertEquals("Post not found", ex.getMessage());
    }

    @Test
    void attachFile_shouldSetFileDataAndSave() {
        BlogPost post = new BlogPost();
        post.setUser(new User("owner@example.com", "owner1", "pw", "AT"));
        when(blogPostRepository.findById(3L)).thenReturn(Optional.of(post));
        when(blogPostRepository.save(any(BlogPost.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BlogPost result = blogPostService.attachFile(3L, "file-url", "image/png");

        assertEquals("file-url", result.getFileUrl());
        assertEquals("image/png", result.getFileType());
    }

    @Test
    void attachFile_shouldThrow_whenPostMissing() {
        when(blogPostRepository.findById(3L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> blogPostService.attachFile(3L, "file-url", "image/png"));

        assertEquals("Post not found", ex.getMessage());
    }

    @Test
    void updatePost_shouldThrowForbidden_whenAuthenticatedUserIsNotOwner() {
        BlogPost post = new BlogPost();
        User owner = new User("owner@example.com", "owner1", "pw", "AT");
        try {
            java.lang.reflect.Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(owner, 30L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        post.setUser(owner);

        authUser(31L, "USER");

        when(blogPostRepository.findById(7L)).thenReturn(Optional.of(post));

        assertThrows(AccessDeniedException.class,
                () -> blogPostService.updatePost(7L, new UpdatePostRequest()));
    }

    @Test
    void deletePost_shouldAllow_whenAuthenticatedAdmin() {
        BlogPost post = new BlogPost();
        User owner = new User("owner@example.com", "owner1", "pw", "AT");
        post.setUser(owner);

        authUser(99L, "ADMIN");
        when(blogPostRepository.findById(9L)).thenReturn(Optional.of(post));

        blogPostService.deletePost(9L);

        verify(blogPostRepository).deleteById(9L);
    }

    @Test
    void deletePost_shouldDelete_whenExists() {
        when(blogPostRepository.existsById(9L)).thenReturn(true);

        blogPostService.deletePost(9L);

        verify(blogPostRepository).deleteById(9L);
    }

    @Test
    void deletePost_shouldThrow_whenMissing() {
        when(blogPostRepository.existsById(9L)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> blogPostService.deletePost(9L));

        assertEquals("Post not found", ex.getMessage());
    }
}
