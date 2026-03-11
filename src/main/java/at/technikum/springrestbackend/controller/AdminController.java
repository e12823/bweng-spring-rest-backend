package at.technikum.springrestbackend.controller;

import at.technikum.springrestbackend.dto.BlogPostDto;
import at.technikum.springrestbackend.dto.UserDto;
import at.technikum.springrestbackend.service.BlogPostService;
import at.technikum.springrestbackend.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final BlogPostService blogPostService;

    public AdminController(UserService userService, BlogPostService blogPostService) {
        this.userService = userService;
        this.blogPostService = blogPostService;
    }

    @GetMapping("/users")
    public ResponseEntity<Page<UserDto>> getAllUsers(Pageable pageable) {
        Page<UserDto> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/posts")
    public ResponseEntity<Page<BlogPostDto>> getAllPosts(Pageable pageable) {
        Page<BlogPostDto> posts = blogPostService.getAllPosts(pageable)
                .map(BlogPostDto::fromEntity);

        return ResponseEntity.ok(posts);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/posts/{id}")
    public ResponseEntity<BlogPostDto> getPostById(@PathVariable Long id) {
        return ResponseEntity.ok(BlogPostDto.fromEntity(blogPostService.getPostById(id)));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        blogPostService.deletePost(id);
        return ResponseEntity.noContent().build();
    }
}