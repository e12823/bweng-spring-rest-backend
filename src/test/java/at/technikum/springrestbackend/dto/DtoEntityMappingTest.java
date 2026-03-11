package at.technikum.springrestbackend.dto;

import at.technikum.springrestbackend.entity.BlogPost;
import at.technikum.springrestbackend.entity.Comment;
import at.technikum.springrestbackend.entity.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DtoEntityMappingTest {

    @Test
    void userDto_shouldMapFromUser_andHandleNull() {
        User user = new User("u@example.com", "user1", "pw", "AT");
        user.setProfileImageUrl("img.jpg");
        user.setRole("ADMIN");

        UserDto dto = UserDto.fromEntity(user);

        assertEquals("u@example.com", dto.getEmail());
        assertEquals("user1", dto.getUsername());
        assertEquals("AT", dto.getCountry());
        assertEquals("img.jpg", dto.getProfileImageUrl());
        assertEquals("ADMIN", dto.getRole());
        assertNull(UserDto.fromEntity(null));
    }

    @Test
    void blogPostDto_shouldMapFromEntity_withAndWithoutUser() {
        BlogPost post = new BlogPost();
        post.setTitle("Title");
        post.setContent("Body");
        post.setFileUrl("file");
        post.setFileType("image/jpeg");

        User user = new User("u@example.com", "user1", "pw", "AT");
        post.setUser(user);

        BlogPostDto dto = BlogPostDto.fromEntity(post);
        assertEquals("Title", dto.getTitle());
        assertEquals("Body", dto.getContent());
        assertEquals("file", dto.getFileUrl());
        assertEquals("image/jpeg", dto.getFileType());
        assertEquals("user1", dto.getUsername());

        BlogPost postWithoutUser = new BlogPost();
        postWithoutUser.setTitle("No User");
        BlogPostDto dtoWithoutUser = BlogPostDto.fromEntity(postWithoutUser);
        assertEquals("No User", dtoWithoutUser.getTitle());
        assertNull(dtoWithoutUser.getUserId());

        assertNull(BlogPostDto.fromEntity(null));
    }

    @Test
    void commentDto_shouldMapFromEntity_withAndWithoutRelations() {
        Comment comment = new Comment();
        comment.setContent("Text");

        User user = new User("u@example.com", "user1", "pw", "AT");
        BlogPost post = new BlogPost();
        comment.setUser(user);
        comment.setPost(post);

        CommentDto dto = CommentDto.fromEntity(comment);
        assertEquals("Text", dto.getContent());
        assertEquals("user1", dto.getUsername());

        Comment commentNoRelation = new Comment();
        commentNoRelation.setContent("Simple");
        CommentDto dtoNoRelation = CommentDto.fromEntity(commentNoRelation);
        assertEquals("Simple", dtoNoRelation.getContent());
        assertNull(dtoNoRelation.getUserId());
        assertNull(dtoNoRelation.getPostId());

        assertNull(CommentDto.fromEntity(null));
    }

    @Test
    void dtoRequestAndResponse_shouldSupportGettersAndSetters() {
        LoginRequest login = new LoginRequest();
        login.setEmail("a@example.com");
        login.setPassword("Secure123");
        assertEquals("a@example.com", login.getEmail());
        assertEquals("Secure123", login.getPassword());

        RegisterRequest register = new RegisterRequest();
        register.setEmail("b@example.com");
        register.setUsername("user99");
        register.setPassword("Secure123");
        register.setCountry("AT");
        register.setProfileImageUrl("img.png");
        assertEquals("user99", register.getUsername());
        assertEquals("AT", register.getCountry());

        CreatePostRequest createPost = new CreatePostRequest();
        createPost.setTitle("T");
        createPost.setContent("C");
        assertEquals("T", createPost.getTitle());
        assertEquals("C", createPost.getContent());

        UpdatePostRequest updatePost = new UpdatePostRequest();
        updatePost.setTitle("TT");
        updatePost.setContent("CC");
        assertEquals("TT", updatePost.getTitle());
        assertEquals("CC", updatePost.getContent());

        CreateCommentRequest createComment = new CreateCommentRequest();
        createComment.setContent("Nice");
        assertEquals("Nice", createComment.getContent());

        UpdateUserRequest updateUser = new UpdateUserRequest();
        updateUser.setUsername("johnny");
        updateUser.setCountry("DE");
        updateUser.setProfileImageUrl("new.png");
        updateUser.setPassword("Secure123");
        assertEquals("johnny", updateUser.getUsername());
        assertEquals("DE", updateUser.getCountry());
        assertEquals("new.png", updateUser.getProfileImageUrl());
        assertEquals("Secure123", updateUser.getPassword());

        JwtResponseDto jwt = new JwtResponseDto("token", 7L, "u", "u@example.com", "USER");
        assertEquals("token", jwt.getToken());
        assertEquals("Bearer", jwt.getType());
        assertEquals(7L, jwt.getId());
        assertEquals("u", jwt.getUsername());
        assertEquals("u@example.com", jwt.getEmail());
        assertEquals("USER", jwt.getRole());

        jwt.setToken("token-2");
        jwt.setType("Custom");
        jwt.setId(9L);
        jwt.setUsername("u2");
        jwt.setEmail("u2@example.com");
        jwt.setRole("ADMIN");
        assertEquals("token-2", jwt.getToken());
        assertEquals("Custom", jwt.getType());
        assertEquals(9L, jwt.getId());
        assertEquals("u2", jwt.getUsername());
        assertEquals("u2@example.com", jwt.getEmail());
        assertEquals("ADMIN", jwt.getRole());

        JwtResponseDto emptyJwt = new JwtResponseDto();
        emptyJwt.setType("Bearer");
        assertEquals("Bearer", emptyJwt.getType());
    }

    @Test
    void entities_shouldSupportConstructorsAndMutators() {
        User user = new User("u@example.com", "name1", "pw", "AT");
        user.setEmail("new@example.com");
        user.setUsername("name2");
        user.setPassword("pw2");
        user.setCountry("DE");
        user.setProfileImageUrl("img.jpg");
        user.setRole("ADMIN");
        assertEquals("new@example.com", user.getEmail());
        assertEquals("name2", user.getUsername());
        assertEquals("pw2", user.getPassword());
        assertEquals("DE", user.getCountry());
        assertEquals("img.jpg", user.getProfileImageUrl());
        assertEquals("ADMIN", user.getRole());

        BlogPost post = new BlogPost("Title", "Content", user);
        post.setTitle("T2");
        post.setContent("C2");
        post.setUser(user);
        post.setFileUrl("f");
        post.setFileType("image/png");
        assertEquals("T2", post.getTitle());
        assertEquals("C2", post.getContent());
        assertEquals("f", post.getFileUrl());
        assertEquals("image/png", post.getFileType());
        assertEquals(user, post.getUser());

        Comment comment = new Comment("hello", user, post);
        comment.setContent("updated");
        comment.setUser(user);
        comment.setPost(post);
        assertEquals("updated", comment.getContent());
        assertEquals(user, comment.getUser());
        assertEquals(post, comment.getPost());
    }
}
