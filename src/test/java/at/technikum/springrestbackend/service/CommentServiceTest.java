package at.technikum.springrestbackend.service;

import at.technikum.springrestbackend.dto.CreateCommentRequest;
import at.technikum.springrestbackend.entity.BlogPost;
import at.technikum.springrestbackend.entity.Comment;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private BlogPostRepository blogPostRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommentService commentService;

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
    void getCommentsByPostId_shouldReturnComments_whenPostExists() {
        List<Comment> comments = List.of(new Comment(), new Comment());
        when(blogPostRepository.existsById(1L)).thenReturn(true);
        when(commentRepository.findByPostIdOrderByCreatedAtAsc(1L)).thenReturn(comments);

        List<Comment> result = commentService.getCommentsByPostId(1L);

        assertEquals(2, result.size());
    }

    @Test
    void getCommentsByPostId_shouldThrow_whenPostMissing() {
        when(blogPostRepository.existsById(1L)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> commentService.getCommentsByPostId(1L));

        assertEquals("Post not found", ex.getMessage());
    }

    @Test
    void createComment_shouldCreateAndSaveComment() {
        BlogPost post = new BlogPost();
        User user = new User("x@example.com", "user1", "pw", "AT");

        CreateCommentRequest request = new CreateCommentRequest();
        request.setContent("Nice post");

        when(blogPostRepository.findById(2L)).thenReturn(Optional.of(post));
        when(userRepository.findById(3L)).thenReturn(Optional.of(user));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Comment result = commentService.createComment(2L, 3L, request);

        assertEquals("Nice post", result.getContent());
        assertEquals(post, result.getPost());
        assertEquals(user, result.getUser());
    }

    @Test
    void createComment_shouldThrow_whenPostMissing() {
        when(blogPostRepository.findById(2L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> commentService.createComment(2L, 3L, new CreateCommentRequest()));

        assertEquals("Post not found", ex.getMessage());
    }

    @Test
    void createComment_shouldThrow_whenUserMissing() {
        when(blogPostRepository.findById(2L)).thenReturn(Optional.of(new BlogPost()));
        when(userRepository.findById(3L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> commentService.createComment(2L, 3L, new CreateCommentRequest()));

        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void createComment_shouldUseAuthenticatedUser() {
        User current = authUser(42L, "USER");
        BlogPost post = new BlogPost();
        CreateCommentRequest request = new CreateCommentRequest();
        request.setContent("Hello");

        when(blogPostRepository.findById(2L)).thenReturn(Optional.of(post));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Comment result = commentService.createComment(2L, request);

        assertEquals("Hello", result.getContent());
        assertEquals(current, result.getUser());
    }

    @Test
    void createComment_shouldThrow_whenUnauthenticated() {
        assertThrows(AccessDeniedException.class,
                () -> commentService.createComment(2L, new CreateCommentRequest()));
    }

    @Test
    void updateComment_shouldUpdateContent_whenNonBlank() {
        Comment comment = new Comment();
        comment.setContent("Old");
        comment.setUser(new User("owner@example.com", "owner1", "pw", "AT"));

        CreateCommentRequest request = new CreateCommentRequest();
        request.setContent("New");

        when(commentRepository.findById(4L)).thenReturn(Optional.of(comment));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Comment result = commentService.updateComment(4L, request);

        assertEquals("New", result.getContent());
    }

    @Test
    void updateComment_shouldKeepContent_whenBlank() {
        Comment comment = new Comment();
        comment.setContent("Old");
        comment.setUser(new User("owner@example.com", "owner1", "pw", "AT"));

        CreateCommentRequest request = new CreateCommentRequest();
        request.setContent(" ");

        when(commentRepository.findById(4L)).thenReturn(Optional.of(comment));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Comment result = commentService.updateComment(4L, request);

        assertEquals("Old", result.getContent());
    }

    @Test
    void updateComment_shouldThrow_whenCommentMissing() {
        when(commentRepository.findById(4L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> commentService.updateComment(4L, new CreateCommentRequest()));

        assertEquals("Comment not found", ex.getMessage());
    }

    @Test
    void deleteComment_shouldDelete_whenExists() {
        when(commentRepository.existsById(4L)).thenReturn(true);

        commentService.deleteComment(4L);

        verify(commentRepository).deleteById(4L);
    }

    @Test
    void updateComment_shouldThrowForbidden_whenAuthenticatedUserIsNotOwner() {
        Comment comment = new Comment();
        User owner = new User("owner@example.com", "owner1", "pw", "AT");
        try {
            java.lang.reflect.Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(owner, 10L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        comment.setUser(owner);
        authUser(11L, "USER");

        when(commentRepository.findById(4L)).thenReturn(Optional.of(comment));

        assertThrows(AccessDeniedException.class,
                () -> commentService.updateComment(4L, new CreateCommentRequest()));
    }

    @Test
    void deleteComment_shouldThrow_whenMissing() {
        when(commentRepository.existsById(4L)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> commentService.deleteComment(4L));

        assertEquals("Comment not found", ex.getMessage());
    }
}
