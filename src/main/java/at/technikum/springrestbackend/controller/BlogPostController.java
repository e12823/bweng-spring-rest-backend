package at.technikum.springrestbackend.controller;

import at.technikum.springrestbackend.dto.BlogPostDto;
import at.technikum.springrestbackend.dto.CreatePostRequest;
import at.technikum.springrestbackend.dto.UpdatePostRequest;
import at.technikum.springrestbackend.service.BlogPostService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts")
public class BlogPostController {

    private final BlogPostService blogPostService;

    public BlogPostController(BlogPostService blogPostService) {
        this.blogPostService = blogPostService;
    }

    @GetMapping
    public ResponseEntity<Page<BlogPostDto>> getAllPosts(Pageable pageable) {
        Page<BlogPostDto> posts = blogPostService.getAllPosts(pageable)
                .map(BlogPostDto::fromEntity);

        return ResponseEntity.ok(posts);
    }

    @GetMapping("/latest")
    public ResponseEntity<java.util.List<BlogPostDto>> getLatestPosts(
            @RequestParam(defaultValue = "5") int limit
    ) {
        java.util.List<BlogPostDto> posts = blogPostService.getLatestPosts(limit)
                .stream()
                .map(BlogPostDto::fromEntity)
                .toList();
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<BlogPostDto>> searchPosts(
            @RequestParam String title,
            Pageable pageable
    ) {
        Page<BlogPostDto> posts = blogPostService.searchPosts(title, pageable)
                .map(BlogPostDto::fromEntity);

        return ResponseEntity.ok(posts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BlogPostDto> getPostById(@PathVariable Long id) {
        return ResponseEntity.ok(
                BlogPostDto.fromEntity(blogPostService.getPostById(id))
        );
    }
    
    @PostMapping
    public ResponseEntity<BlogPostDto> createPost(
            @Valid @RequestBody CreatePostRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                BlogPostDto.fromEntity(blogPostService.createPost(request))
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<BlogPostDto> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePostRequest request
    ) {
        return ResponseEntity.ok(
                BlogPostDto.fromEntity(blogPostService.updatePost(id, request))
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        blogPostService.deletePost(id);
        return ResponseEntity.noContent().build();
    }
}