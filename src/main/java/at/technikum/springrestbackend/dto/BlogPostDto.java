package at.technikum.springrestbackend.dto;

import at.technikum.springrestbackend.entity.BlogPost;

import java.time.LocalDateTime;

public class BlogPostDto {
    private Long id;
    private String title;
    private String content;
    private String fileUrl;
    private String fileType;
    private Long userId;
    private String username;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public BlogPostDto() {}

    // getters and setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public String getFileUrl() {
        return fileUrl;
    }
    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }
    public String getFileType() {
        return fileType;
    }
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // Maps BlogPost entity to BlogPostDto
    public static BlogPostDto fromEntity(BlogPost post) {
        if (post == null) return null;

        BlogPostDto dto = new BlogPostDto();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setFileUrl(post.getFileUrl());
        dto.setFileType(post.getFileType());

        if (post.getUser() != null) {
            dto.setUserId(post.getUser().getId());
            dto.setUsername(post.getUser().getUsername());
        }

        dto.setCreatedAt(post.getCreatedAt());
        dto.setUpdatedAt(post.getUpdatedAt());
        
        return dto;

    }
}