package at.technikum.springrestbackend.dto;

import at.technikum.springrestbackend.entity.Comment;

import java.time.LocalDateTime;

public class CommentDto {
    private Long id;
    private String content;
    private Long userId;
    private String username;
    private Long postId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CommentDto() {}

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
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

    public Long getPostId() {
        return postId;
    }
    public void setPostId(Long postId) {
        this.postId = postId;
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

    public static CommentDto fromEntity(Comment c) {
        if (c == null) return null;
        CommentDto dto = new CommentDto();
        dto.setId(c.getId());
        dto.setContent(c.getContent());
        if (c.getUser() != null) {
            dto.setUserId(c.getUser().getId());
            dto.setUsername(c.getUser().getUsername());
        }
        if (c.getPost() != null) {
            dto.setPostId(c.getPost().getId());
        }
        dto.setCreatedAt(c.getCreatedAt());
        dto.setUpdatedAt(c.getUpdatedAt());
        return dto;
    }
}