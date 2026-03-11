package at.technikum.springrestbackend.controller;

import at.technikum.springrestbackend.dto.BlogPostDto;
import at.technikum.springrestbackend.service.BlogPostService;
import at.technikum.springrestbackend.service.FileStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/files")
public class FileController {

    private final FileStorageService fileStorageService;
    private final BlogPostService blogPostService;

    public FileController(FileStorageService fileStorageService,
                          BlogPostService blogPostService) {
        this.fileStorageService = fileStorageService;
        this.blogPostService = blogPostService;
    }

    @PostMapping("/posts/{postId}")
    public ResponseEntity<BlogPostDto> uploadPostFile(
            @PathVariable Long postId,
            @RequestParam("file") MultipartFile file
    ) {
        String fileUrl = fileStorageService.storeFile(file);
        String fileType = file.getContentType();

        return ResponseEntity.ok(
                BlogPostDto.fromEntity(blogPostService.attachFile(postId, fileUrl, fileType))
        );
    }
}