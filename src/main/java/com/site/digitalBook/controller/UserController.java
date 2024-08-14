package com.site.digitalBook.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.site.digitalBook.controller.payload.Payload;
import com.site.digitalBook.entity.User;
import com.site.digitalBook.exception.EmailAlreadyExistsException;
import com.site.digitalBook.exception.UnauthorizedException;
import com.site.digitalBook.exception.UserNotFoundException;
import com.site.digitalBook.service.EmailService;
import com.site.digitalBook.service.TokenBlacklistService;
import com.site.digitalBook.service.VerificationCodeStorageService;
import com.site.digitalBook.util.JwtUtil;

import jakarta.mail.MessagingException;

import com.site.digitalBook.service.UserService;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;
    private final EmailService emailService;
    private final VerificationCodeStorageService verificationCodeService;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

    // Constructor Injection
    public UserController(UserService userService, EmailService emailService, VerificationCodeStorageService verificationCodeService, JwtUtil jwtUtil, TokenBlacklistService tokenBlacklistService) {
        this.userService = userService;
        this.emailService = emailService;
        this.verificationCodeService = verificationCodeService;
        this.jwtUtil = jwtUtil;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @PostMapping("/register")
    public ResponseEntity<Payload> registerUser(@RequestBody User user) {

        if (user.getEmail() == null || user.getMdp() == null) {
            Payload payload = new Payload("Email or password cannot be null");
            return new ResponseEntity<>(payload, HttpStatus.BAD_REQUEST);
        }

        try {
            User newUser = userService.addUser(user);
            Payload payload = new Payload("New user added", newUser);
            return new ResponseEntity<>(payload, HttpStatus.CREATED);
        } catch (EmailAlreadyExistsException e) {
            Payload payload = new Payload(e.getMessage());
            return new ResponseEntity<>(payload, HttpStatus.BAD_REQUEST);
        } catch (UnauthorizedException e) {
            Payload payload = new Payload("Login failed: " + e.getMessage());
            return new ResponseEntity<>(payload, HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            Payload payload = new Payload("Registration failed: " + e.getMessage());
            return new ResponseEntity<>(payload, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Payload> loginUser(@RequestBody User user) {
        try {
            User authenticatedUser = userService.authenticateUser(user.getEmail(), user.getMdp());
            String code = emailService.generateVerificationCode();
            emailService.sendConfirmationEmail(user.getEmail(), code);
            emailService.saveCode(user.getEmail(), code);

            Payload payload = new Payload("User authenticated. Please check your email for the confirmation code.", authenticatedUser);
            return new ResponseEntity<>(payload, HttpStatus.OK);
        } catch (UserNotFoundException e) {
            Payload payload = new Payload("Login failed: " + e.getMessage());
            return new ResponseEntity<>(payload, HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            Payload payload = new Payload("Login failed: " + e.getMessage());
            return new ResponseEntity<>(payload, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/verify-code")
    public ResponseEntity<Payload> verifyCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");

        try {
            boolean isValid = emailService.validateCode(email, code);
            String token = null;

            if (isValid) {
                token = jwtUtil.generateToken(email);
                Payload payload = new Payload("Code vérifié avec succès!", email, token);
                return ResponseEntity.ok(payload);
            } else {
                Payload payload = new Payload("Code invalide ou expiré.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(payload);
            }
        } catch (Exception e) {
            Payload payload = new Payload("Une erreur est survenue.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(payload);
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Payload> forgotPassword(@RequestBody User user) {
        try {
            String email = user.getEmail();

            try {
                userService.getUserByEmail(email);
            } catch (UserNotFoundException e) {
                Payload payload = new Payload("Adresse e-mail non enregistrée.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(payload);
            }

            String resetLink = "http://localhost:4200/reset-password?email=" + email;
            emailService.sendResetPasswordEmail(email, resetLink);

            Payload payload = new Payload("Email de réinitialisation envoyé.", resetLink);
            return ResponseEntity.ok(payload);

        } catch (MessagingException e) {
            System.err.println("Erreur lors de l'envoi de l'email: " + e.getMessage());
            Payload payload = new Payload("Erreur lors de l'envoi de l'email.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(payload);
        } catch (Exception e) {
            System.err.println("Erreur inattendue lors de l'envoi de l'email: " + e.getMessage());
            Payload payload = new Payload("Erreur inattendue lors de l'envoi de l'email.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(payload);
        }
    }



    @PostMapping("/reset-password")
    public ResponseEntity<Payload> resetPassword(@RequestParam("email") String email, @RequestBody Map<String, String> requestBody) {
        try {
            String newPassword = requestBody.get("newPassword");
            
            userService.resetPassword(email, newPassword);
            
            emailService.sendPasswordChangeConfirmationEmail(email);
            
            Payload payload = new Payload("Mot de passe réinitialisé avec succès.", null, null);
            return ResponseEntity.ok(payload);
        } catch (Exception e) {
            Payload payload = new Payload(e.getMessage(), null, null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(payload);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = "Authorization") String authorizationHeader) {
        String token = authorizationHeader.substring(7); // Supposer "Bearer " préfixé

        tokenBlacklistService.addToBlacklist(token);

        return ResponseEntity.ok().build(); // Retourne HTTP 200 OK
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
