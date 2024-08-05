package com.site.digitalBook.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.site.digitalBook.controller.payload.Payload;
import com.site.digitalBook.entity.User;
import com.site.digitalBook.exception.UserNotFoundException;
import com.site.digitalBook.service.UserService;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<Payload> registerUser(@RequestBody User user) {
        try {
            User newUser = userService.addUser(user);
            Payload payload = new Payload("New user added", newUser);
            return new ResponseEntity<>(payload, HttpStatus.CREATED);
        } catch (Exception e) {
            Payload payload = new Payload("Registration failed: " + e.getMessage());
            return new ResponseEntity<>(payload, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Payload> loginUser(@RequestBody User user) {
        try {
            User authenticatedUser = userService.authenticateUser(user.getEmail(), user.getMdp());
            Payload payload = new Payload("User authenticated", authenticatedUser);
            return new ResponseEntity<>(payload, HttpStatus.OK);
        } catch (UserNotFoundException e) {
            Payload payload = new Payload("Login failed: " + e.getMessage());
            return new ResponseEntity<>(payload, HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/user")
    public ResponseEntity<Payload> getAllUsers() {
        Payload payload = new Payload("All users", userService.getAllUsers());
        return new ResponseEntity<>(payload, HttpStatus.OK);
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<Payload> getUserById(@PathVariable int id) {
        try {
            User user = userService.getUserById(id);
            Payload payload = new Payload("User found", user);
            return new ResponseEntity<>(payload, HttpStatus.OK);
        } catch (UserNotFoundException ex) {
            Payload payload = new Payload(ex.getMessage());
            return new ResponseEntity<>(payload, HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/user/{id}")
    public ResponseEntity<Payload> deleteUser(@PathVariable int id) {
        userService.deleteUser(id);
        return new ResponseEntity<>(new Payload("User deleted"), HttpStatus.NO_CONTENT);
    }
}