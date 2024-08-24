package com.site.digitalBook.controller;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.site.digitalBook.controller.payload.Payload;
import com.site.digitalBook.entity.User;
import com.site.digitalBook.exception.UserNotFoundException;
import com.site.digitalBook.repository.UserRepository;
import com.site.digitalBook.service.UserService;

public class UserControllerTest {

	   @Mock
	    private UserRepository userRepository;

	    @Mock
	    private BCryptPasswordEncoder passwordEncoder;

	    @InjectMocks
	    private UserService userService;

	    @BeforeEach
	    public void setUp() {
	        MockitoAnnotations.openMocks(this);
	    }

	    @Test
	    public void testAddUser() {
	        User user = new User("john.doe@example.com", "password");
	        String encodedPassword = "encodedPassword";

	        when(passwordEncoder.encode(user.getMdp())).thenReturn(encodedPassword);
	        when(userRepository.findByEmail(user.getEmail())).thenReturn(null);
	        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
	            User savedUser = invocation.getArgument(0);
	            savedUser.setMdp(encodedPassword);
	            return savedUser;
	        });

	        User savedUser = userService.addUser(user);
	        assertNotNull(savedUser);
	        assertEquals(encodedPassword, savedUser.getMdp());
	    }

	    @Test
	    public void testAuthenticateUser() {
	        User user = new User("m.pecheur09@gmail.com", "Mickael09@");
	        String encodedPassword = "Mickael09@";

	        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
	        when(passwordEncoder.matches(user.getMdp(), encodedPassword)).thenReturn(true);

	        User authenticatedUser = userService.authenticateUser(user.getEmail(), user.getMdp());
	        assertNotNull(authenticatedUser);
	        assertEquals(user.getEmail(), authenticatedUser.getEmail());
	    }
	}