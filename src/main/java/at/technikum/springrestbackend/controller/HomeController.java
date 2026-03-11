package at.technikum.springrestbackend.controller;

import at.technikum.springrestbackend.dto.BlogPostDto;
import at.technikum.springrestbackend.service.BlogPostService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/")
public class HomeController {

    private final BlogPostService blogPostService;

    public HomeController(BlogPostService blogPostService) {
        this.blogPostService = blogPostService;
    }

    @GetMapping
    public ResponseEntity<List<BlogPostDto>> home(
            @RequestParam(defaultValue = "5") int limit
    ) {

        List<BlogPostDto> posts = blogPostService.getLatestPosts(limit)
                .stream()
                .map(BlogPostDto::fromEntity)
                .toList();

        return ResponseEntity.ok(posts);
    }
}