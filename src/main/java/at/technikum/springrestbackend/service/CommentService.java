package at.technikum.springrestbackend.service;

import at.technikum.springrestbackend.dto.CreateCommentRequest;
import at.technikum.springrestbackend.entity.BlogPost;
import at.technikum.springrestbackend.entity.Comment;
import at.technikum.springrestbackend.entity.User;
import at.technikum.springrestbackend.repository.BlogPostRepository;
import at.technikum.springrestbackend.repository.CommentRepository;
import at.technikum.springrestbackend.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final BlogPostRepository blogPostRepository;
    private final UserRepository userRepository;

    public CommentService(CommentRepository commentRepository,
                          BlogPostRepository blogPostRepository,
                          UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.blogPostRepository = blogPostRepository;
        this.userRepository = userRepository;
    }

    public List<Comment> getCommentsByPostId(Long postId) {
        if (!blogPostRepository.existsById(postId)) {
            throw new IllegalArgumentException("Post not found");
        }

        return commentRepository.findByPostIdOrderByCreatedAtAsc(postId);
    }

    public Comment createComment(Long postId, CreateCommentRequest request) {
        User user = getCurrentUser().orElseThrow(() ->
                new AccessDeniedException("Authentication required"));

        BlogPost post = blogPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setPost(post);
        comment.setUser(user);

        return commentRepository.save(comment);
    }

    public Comment createComment(Long postId, Long userId, CreateCommentRequest request) {

        BlogPost post = blogPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setPost(post);
        comment.setUser(user);

        return commentRepository.save(comment);
    }

    public Comment updateComment(Long commentId, CreateCommentRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

        ensureOwnerOrAdmin(comment.getUser() != null ? comment.getUser().getId() : null);

        if (request.getContent() != null && !request.getContent().isBlank()) {
            comment.setContent(request.getContent());
        }

        return commentRepository.save(comment);
    }

    public void deleteComment(Long commentId) {
        Optional<User> currentUser = getCurrentUser();
        if (currentUser.isEmpty()) {
            if (!commentRepository.existsById(commentId)) {
                throw new IllegalArgumentException("Comment not found");
            }
            commentRepository.deleteById(commentId);
            return;
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

        ensureOwnerOrAdmin(comment.getUser() != null ? comment.getUser().getId() : null);

        commentRepository.deleteById(commentId);
    }

    private Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            return Optional.empty();
        }
        return Optional.of(user);
    }

    private void ensureOwnerOrAdmin(Long ownerId) {
        Optional<User> currentUser = getCurrentUser();
        if (currentUser.isEmpty()) {
            return;
        }

        User user = currentUser.get();
        boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole());
        boolean isOwner = ownerId != null && ownerId.equals(user.getId());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("You are not allowed to modify this comment");
        }
    }
}