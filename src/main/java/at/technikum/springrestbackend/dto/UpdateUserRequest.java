package at.technikum.springrestbackend.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UpdateUserRequest {

    @Size(min = 5, max = 50)
    private String username;

    @Pattern(regexp = "^[A-Z]{2}$", message = "Invalid country code")
    private String country;

    private String profileImageUrl;

    @Size(min = 8, max = 100, message = "Password must be at least 8 characters")
    @Pattern(
        regexp = "^(?=.[a-z])(?=.[A-Z])(?=.*\\d).+$",
        message = "Password must contain uppercase, lowercase and a number"
    )
    private String password;

    public UpdateUserRequest() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}