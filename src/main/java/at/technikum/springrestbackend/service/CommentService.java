package at.technikum.springrestbackend.service;

import at.technikum.springrestbackend.dto.CreateCommentRequest;
import at.technikum.springrestbackend.entity.BlogPost;
import at.technikum.springrestbackend.entity.Comment;
import at.technikum.springrestbackend.entity.User;
import at.technikum.springrestbackend.repository.BlogPostRepository;
import at.technikum.springrestbackend.repository.CommentRepository;
import at.technikum.springrestbackend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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

        if (request.getContent() != null && !request.getContent().isBlank()) {
            comment.setContent(request.getContent());
        }

        return commentRepository.save(comment);
    }

    public void deleteComment(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new IllegalArgumentException("Comment not found");
        }

        commentRepository.deleteById(commentId);
    }
}