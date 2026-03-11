package at.technikum.springrestbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdatePostRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @NotBlank(message = "Content is required")
    @Size(max = 3000, message = "Content must not exceed 3000 characters")    
    private String content;

    public UpdatePostRequest() {
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
}