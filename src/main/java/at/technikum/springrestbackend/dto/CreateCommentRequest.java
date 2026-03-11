package at.technikum.springrestbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateCommentRequest {

    @NotBlank(message = "Content is required")
    @Size(max = 2000, message = "Comment must be under 2000 characters")
    private String content;

    public CreateCommentRequest() {
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}