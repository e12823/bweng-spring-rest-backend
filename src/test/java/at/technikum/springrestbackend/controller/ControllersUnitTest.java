package at.technikum.springrestbackend.controller;

import at.technikum.springrestbackend.dto.BlogPostDto;
import at.technikum.springrestbackend.dto.CreateCommentRequest;
import at.technikum.springrestbackend.dto.CreatePostRequest;
import at.technikum.springrestbackend.dto.JwtResponseDto;
import at.technikum.springrestbackend.dto.LoginRequest;
import at.technikum.springrestbackend.dto.RegisterRequest;
import at.technikum.springrestbackend.dto.UpdatePostRequest;
import at.technikum.springrestbackend.dto.UpdateUserRequest;
import at.technikum.springrestbackend.dto.UserDto;
import at.technikum.springrestbackend.entity.BlogPost;
import at.technikum.springrestbackend.entity.Comment;
import at.technikum.springrestbackend.entity.User;
import at.technikum.springrestbackend.service.AuthService;
import at.technikum.springrestbackend.service.BlogPostService;
import at.technikum.springrestbackend.service.CommentService;
import at.technikum.springrestbackend.service.FileStorageService;
import at.technikum.springrestbackend.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ControllersUnitTest {

    @Mock
    private AuthService authService;

    @Mock
    private BlogPostService blogPostService;

    @Mock
    private CommentService commentService;

    @Mock
    private UserService userService;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private AuthController authController;

    @InjectMocks
    private BlogPostController blogPostController;

    @InjectMocks
    private CommentController commentController;

    @InjectMocks
    private UserController userController;

    @InjectMocks
    private AdminController adminController;

    @InjectMocks
    private HomeController homeController;

    @InjectMocks
    private FileController fileController;

    @Test
    void authController_shouldRegisterAndLogin() {
        RegisterRequest registerRequest = new RegisterRequest();
        UserDto userDto = new UserDto();
        userDto.setUsername("user1");
        when(authService.register(registerRequest)).thenReturn(userDto);

        JwtResponseDto jwt = new JwtResponseDto("token", 1L, "user1", "u@example.com", "USER");
        LoginRequest loginRequest = new LoginRequest();
        when(authService.login(loginRequest)).thenReturn(jwt);

        assertEquals(HttpStatus.CREATED, authController.register(registerRequest).getStatusCode());
        assertEquals("user1", authController.register(registerRequest).getBody().getUsername());
        assertEquals(HttpStatus.OK, authController.login(loginRequest).getStatusCode());
        assertEquals("token", authController.login(loginRequest).getBody().getToken());
    }

    @Test
    void blogPostController_shouldHandleEndpoints() {
        BlogPost post = new BlogPost();
        post.setTitle("Title");
        post.setContent("Content");

        Pageable pageable = PageRequest.of(0, 5);
        Page<BlogPost> page = new PageImpl<>(List.of(post), pageable, 1);

        when(blogPostService.getAllPosts(pageable)).thenReturn(page);
        when(blogPostService.getLatestPosts(2)).thenReturn(List.of(post));
        when(blogPostService.searchPosts("ti", pageable)).thenReturn(page);
        when(blogPostService.getPostById(1L)).thenReturn(post);
        when(blogPostService.createPost(any(CreatePostRequest.class))).thenReturn(post);
        when(blogPostService.updatePost(any(Long.class), any(UpdatePostRequest.class))).thenReturn(post);

        assertEquals(HttpStatus.OK, blogPostController.getAllPosts(pageable).getStatusCode());
        assertEquals(HttpStatus.OK, blogPostController.getLatestPosts(2).getStatusCode());
        assertEquals(HttpStatus.OK, blogPostController.searchPosts("ti", pageable).getStatusCode());
        assertEquals(HttpStatus.OK, blogPostController.getPostById(1L).getStatusCode());
        assertEquals(HttpStatus.CREATED,
            blogPostController.createPost(new CreatePostRequest()).getStatusCode());
        assertEquals(HttpStatus.OK,
                blogPostController.updatePost(1L, new UpdatePostRequest()).getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, blogPostController.deletePost(1L).getStatusCode());
    }

    @Test
    void commentController_shouldHandleEndpoints() {
        Comment comment = new Comment();
        comment.setContent("Hello");
        when(commentService.getCommentsByPostId(1L)).thenReturn(List.of(comment));
        when(commentService.createComment(any(Long.class), any(CreateCommentRequest.class)))
                .thenReturn(comment);
        when(commentService.updateComment(any(Long.class), any(CreateCommentRequest.class))).thenReturn(comment);

        assertEquals(HttpStatus.OK, commentController.getCommentsByPostId(1L).getStatusCode());
        assertEquals(HttpStatus.CREATED,
            commentController.createComment(1L, new CreateCommentRequest()).getStatusCode());
        assertEquals(HttpStatus.OK,
                commentController.updateComment(1L, new CreateCommentRequest()).getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, commentController.deleteComment(1L).getStatusCode());
    }

    @Test
    void userAndAdminAndHomeAndFileControllers_shouldHandleEndpoints() {
        UserDto user = new UserDto();
        user.setUsername("admin");
        BlogPost post = new BlogPost();
        post.setTitle("Post");

        Pageable pageable = PageRequest.of(0, 5);
        Page<UserDto> userPage = new PageImpl<>(List.of(user), pageable, 1);
        Page<BlogPost> postPage = new PageImpl<>(List.of(post), pageable, 1);

        when(userService.getAllUsers(pageable)).thenReturn(userPage);
        when(userService.getUserById(1L)).thenReturn(user);
        when(userService.updateUser(any(Long.class), any(UpdateUserRequest.class))).thenReturn(user);

        when(blogPostService.getAllPosts(pageable)).thenReturn(postPage);
        when(blogPostService.getPostById(1L)).thenReturn(post);
        when(blogPostService.getLatestPosts(3)).thenReturn(List.of(post));

        MockMultipartFile file = new MockMultipartFile("file", "a.jpg", "image/jpeg", new byte[]{1});
        when(fileStorageService.storeFile(file)).thenReturn("http://cdn/file");
        when(blogPostService.attachFile(1L, "http://cdn/file", "image/jpeg")).thenReturn(post);

        assertEquals(HttpStatus.OK, userController.getAllUsers(pageable).getStatusCode());
        assertEquals(HttpStatus.OK, userController.getUserById(1L).getStatusCode());
        assertEquals(HttpStatus.OK, userController.updateUser(1L, new UpdateUserRequest()).getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, userController.deleteUser(1L).getStatusCode());

        assertEquals(HttpStatus.OK, adminController.getAllUsers(pageable).getStatusCode());
        assertEquals(HttpStatus.OK, adminController.getAllPosts(pageable).getStatusCode());
        assertEquals(HttpStatus.OK, adminController.getUserById(1L).getStatusCode());
        assertEquals(HttpStatus.OK, adminController.getPostById(1L).getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, adminController.deleteUser(1L).getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, adminController.deletePost(1L).getStatusCode());

        assertEquals(HttpStatus.OK, homeController.home(3).getStatusCode());
        assertEquals(HttpStatus.OK, fileController.uploadPostFile(1L, file).getStatusCode());

        verify(userService, times(2)).deleteUser(1L);
        verify(blogPostService).deletePost(1L);
    }
}
