package com.site.digitalBook.controller;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.site.digitalBook.controller.payload.Payload;
import com.site.digitalBook.entity.User;
import com.site.digitalBook.exception.UserNotFoundException;
import com.site.digitalBook.service.UserService;

public class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testRegisterUser() {
        User user = new User("john.doe@example.com", "password");

        when(userService.addUser(any())).thenReturn(user);

        ResponseEntity<Payload> response = userController.registerUser(user);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("New user added", response.getBody().getMessage());
        assertEquals(user, response.getBody().getData());

        verify(userService, times(1)).addUser(any());
    }

    @Test
    public void testLoginUser() throws UserNotFoundException {
        User user = new User("john.doe@example.com", "password");

        when(userService.authenticateUser(user.getEmail(), user.getMdp())).thenReturn(user);

        ResponseEntity<Payload> response = userController.loginUser(user);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User authenticated", response.getBody().getMessage());
        assertEquals(user, response.getBody().getData());

        verify(userService, times(1)).authenticateUser(user.getEmail(), user.getMdp());
    }

    @Test
    public void testGetAllUsers() {
        User user1 = new User("john.doe@example.com", "password");
        User user2 = new User("gon.mic@example.com", "pass");
        List<User> userList = Arrays.asList(user1, user2);

        when(userService.getAllUsers()).thenReturn(userList);

        ResponseEntity<Payload> response = userController.getAllUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("All users", response.getBody().getMessage());
        assertEquals(userList, response.getBody().getData());

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    public void testGetUserById() throws UserNotFoundException {
        int userId = 1;
        User user = new User("john.doe@example.com", "password");

        when(userService.getUserById(userId)).thenReturn(user);

        ResponseEntity<Payload> response = userController.getUserById(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User found", response.getBody().getMessage());
        assertEquals(user, response.getBody().getData());

        verify(userService, times(1)).getUserById(userId);
    }

    @Test
    public void testGetUserById_UserNotFoundException() throws UserNotFoundException {
        int userId = 1;
        String errorMessage = "User not found";

        when(userService.getUserById(userId)).thenThrow(new UserNotFoundException(errorMessage));

        ResponseEntity<Payload> response = userController.getUserById(userId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(errorMessage, response.getBody().getMessage());

        verify(userService, times(1)).getUserById(userId);
    }

    @Test
    public void testDeleteUser() {
        int userId = 1;

        ResponseEntity<Payload> response = userController.deleteUser(userId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertEquals("User deleted", response.getBody().getMessage());

        verify(userService, times(1)).deleteUser(userId);
    }
}