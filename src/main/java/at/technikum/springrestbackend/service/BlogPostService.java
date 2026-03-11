package at.technikum.springrestbackend.service;

import at.technikum.springrestbackend.dto.CreatePostRequest;
import at.technikum.springrestbackend.dto.UpdatePostRequest;
import at.technikum.springrestbackend.entity.BlogPost;
import at.technikum.springrestbackend.entity.User;
import at.technikum.springrestbackend.repository.BlogPostRepository;
import at.technikum.springrestbackend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BlogPostService {

    private final BlogPostRepository blogPostRepository;
    private final UserRepository userRepository;

    public BlogPostService(BlogPostRepository blogPostRepository,
                           UserRepository userRepository) {
        this.blogPostRepository = blogPostRepository;
        this.userRepository = userRepository;
    }

    public Page<BlogPost> getAllPosts(Pageable pageable) {
        return blogPostRepository.findAll(pageable);
    }

    public BlogPost getPostById(Long id) {
        return blogPostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
    }

    public List<BlogPost> getLatestPosts(int limit) {
        return blogPostRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .limit(limit)
                .toList();
    }

    public Page<BlogPost> searchPosts(String title, Pageable pageable) {
        return blogPostRepository.findByTitleContainingIgnoreCase(title, pageable);
    }

    public BlogPost createPost(Long userId, CreatePostRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        BlogPost post = new BlogPost();
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setUser(user);

        return blogPostRepository.save(post);
    }

    public BlogPost updatePost(Long postId, UpdatePostRequest request) {
        BlogPost post = blogPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            post.setTitle(request.getTitle());
        }

        if (request.getContent() != null && !request.getContent().isBlank()) {
            post.setContent(request.getContent());
        }

        return blogPostRepository.save(post);
    }

    public BlogPost attachFile(Long postId, String fileUrl, String fileType) {
        BlogPost post = blogPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        post.setFileUrl(fileUrl);
        post.setFileType(fileType);

        return blogPostRepository.save(post);
    }

    public void deletePost(Long postId) {
        if (!blogPostRepository.existsById(postId)) {
            throw new IllegalArgumentException("Post not found");
        }

        blogPostRepository.deleteById(postId);
    }
}