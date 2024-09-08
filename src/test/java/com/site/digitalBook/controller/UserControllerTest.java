package com.site.digitalBook.controller;

import com.site.digitalBook.controller.payload.Payload;
import com.site.digitalBook.entity.User;
import com.site.digitalBook.exception.EmailAlreadyExistsException;
import com.site.digitalBook.exception.UnauthorizedException;
import com.site.digitalBook.exception.UserNotFoundException;
import com.site.digitalBook.service.EmailService;
import com.site.digitalBook.service.TokenBlacklistService;
import com.site.digitalBook.service.UserService;
import com.site.digitalBook.service.VerificationCodeService;
import com.site.digitalBook.service.VerificationCodeStorageService;
import com.site.digitalBook.util.JwtUtil;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private UserService userService;

    @Mock
    private EmailService emailService;

    @Mock
    private VerificationCodeService verifService;

    @Mock
    private VerificationCodeStorageService verificationCodeService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testRegisterUserSuccess() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setMdp("password");

        when(userService.addUser(any(User.class))).thenReturn(user);
        when(verifService.generateVerificationCode()).thenReturn("123456");
        try {
			doNothing().when(emailService).sendConfirmationEmail(anyString(), anyString());
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        doNothing().when(verifService).saveCode(anyString(), anyString());

        ResponseEntity<Payload> response = userController.registerUser(user);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Nouvel utilisateur ajouté. Veuillez vérifier votre email pour le code de confirmation.", response.getBody().getMessage());
    }

    @Test
    public void testRegisterUserEmailAlreadyExists() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setMdp("password");

        when(userService.addUser(any(User.class))).thenThrow(new EmailAlreadyExistsException("Email déjà utilisé"));

        ResponseEntity<Payload> response = userController.registerUser(user);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Email déjà utilisé", response.getBody().getMessage());
    }

    @Test
    public void testLoginUserSuccess() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setMdp("password");
        user.setEstActif(true);
        user.setProfil("USER");

        when(userService.authenticateUser(anyString(), anyString())).thenReturn(user);
        when(jwtUtil.generateToken(anyString())).thenReturn("jwt-token");

        ResponseEntity<Payload> response = userController.loginUser(user);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Utilisateur authentifié avec succès.", response.getBody().getMessage());
        assertEquals("jwt-token", response.getBody().getToken());
        assertEquals("USER", response.getBody().getRole());
    }

    @Test
    public void testLoginUserUnauthorized() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setMdp("wrongpassword");

        when(userService.authenticateUser(anyString(), anyString())).thenThrow(new UnauthorizedException("Identifiants incorrects"));

        ResponseEntity<Payload> response = userController.loginUser(user);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Échec de la connexion : Identifiants incorrects", response.getBody().getMessage());
    }

    @Test
    public void testVerifyCodeSuccess() {
        Map<String, String> request = new HashMap<>();
        request.put("email", "test@example.com");
        request.put("code", "123456");

        when(verifService.validateCode(anyString(), anyString())).thenReturn(true);
        doNothing().when(userService).activateUser(anyString());
        when(jwtUtil.generateToken(anyString())).thenReturn("jwt-token");

        ResponseEntity<Payload> response = userController.verifyCode(request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Code vérifié avec succès! Utilisateur activé.", response.getBody().getMessage());
        assertEquals("jwt-token", response.getBody().getToken());
    }

    @Test
    public void testVerifyCodeFailure() {
        Map<String, String> request = new HashMap<>();
        request.put("email", "test@example.com");
        request.put("code", "wrong-code");

        when(verifService.validateCode(anyString(), anyString())).thenReturn(false);

        ResponseEntity<Payload> response = userController.verifyCode(request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Code invalide ou expiré.", response.getBody().getMessage());
    }

    @Test
    public void testForgotPasswordSuccess() throws MessagingException {
        User user = new User();
        user.setEmail("test@example.com");

        when(userService.getUserByEmail(anyString())).thenReturn(user);
        doNothing().when(emailService).sendResetPasswordEmail(anyString(), anyString());

        ResponseEntity<Payload> response = userController.forgotPassword(user);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Email de réinitialisation envoyé.", response.getBody().getMessage());
    }

    @Test
    public void testForgotPasswordEmailNotFound() {
        User user = new User();
        user.setEmail("test@example.com");

        when(userService.getUserByEmail(anyString())).thenThrow(new UserNotFoundException("Utilisateur non trouvé"));

        ResponseEntity<Payload> response = userController.forgotPassword(user);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Adresse e-mail non enregistrée.", response.getBody().getMessage());
    }

    @Test
    public void testResetPasswordSuccess() {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("newPassword", "new-password");

        doNothing().when(userService).resetPassword(anyString(), anyString());
        try {
			doNothing().when(emailService).sendPasswordChangeConfirmationEmail(anyString());
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        ResponseEntity<Payload> response = userController.resetPassword("test@example.com", requestBody);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Mot de passe réinitialisé avec succès.", response.getBody().getMessage());
    }

    @Test
    public void testLogoutSuccess() {
        doNothing().when(tokenBlacklistService).addToBlacklist(anyString());

        ResponseEntity<Void> response = userController.logout("Bearer jwt-token");
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testUpdateUserProfileSuccess() {
        User user = new User();
        user.setId(1);
        user.setEmail("test@example.com");

        User updatedUser = new User();
        updatedUser.setNom("New Name");

        when(userService.getUserById(anyInt())).thenReturn(user);
        doNothing().when(userService).updateUser(any(User.class));

        ResponseEntity<Payload> response = userController.updateUserProfile(1, updatedUser);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Utilisateur mis à jour avec succès", response.getBody().getMessage());
        assertEquals("New Name", ((User) response.getBody().getData()).getNom());
    }

    @Test
    public void testUpdateUserProfileNotFound() {
        User updatedUser = new User();
        updatedUser.setNom("New Name");

        when(userService.getUserById(anyInt())).thenThrow(new UserNotFoundException("Utilisateur non trouvé"));

        ResponseEntity<Payload> response = userController.updateUserProfile(1, updatedUser);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Utilisateur non trouvé : Utilisateur non trouvé", response.getBody().getMessage());
    }

    @Test
    public void testGetCurrentUserSuccess() {
        User user = new User();
        user.setEmail("test@example.com");

        when(userService.getUserByEmail(anyString())).thenReturn(user);

        ResponseEntity<Payload> response = userController.getCurrentUser("test@example.com");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Utilisateur trouvé", response.getBody().getMessage());
    }

    @Test
    public void testGetCurrentUserNotFound() {
        when(userService.getUserByEmail(anyString())).thenThrow(new UserNotFoundException("Utilisateur non trouvé"));

        ResponseEntity<Payload> response = userController.getCurrentUser("test@example.com");
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Erreur : Utilisateur non trouvé. Détails : Utilisateur non trouvé", response.getBody().getMessage());
    }

    @Test
    public void testContactSuccess() throws MessagingException {
        doNothing().when(emailService).sendSimpleEmail(anyString(), anyString(), anyString());

        ResponseEntity<Payload> response = userController.contact("test@example.com", "Subject", "Message");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Message envoyé avec succès.", response.getBody().getMessage());
    }

    @Test
    public void testContactFailure() throws MessagingException {
        doThrow(new MessagingException("Erreur d'envoi d'email")).when(emailService).sendSimpleEmail(anyString(), anyString(), anyString());

        ResponseEntity<Payload> response = userController.contact("test@example.com", "Subject", "Message");
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Erreur lors de l'envoi du message : Erreur d'envoi d'email", response.getBody().getMessage());
    }
    
    @Test
    public void testRegisterUserMissingEmail() {
        User user = new User();
        user.setMdp("password");

        ResponseEntity<Payload> response = userController.registerUser(user);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Email ou mot de passe ne peut pas être nul", response.getBody().getMessage());
    }

    @Test
    public void testRegisterUserMissingPassword() {
        User user = new User();
        user.setEmail("test@example.com");

        ResponseEntity<Payload> response = userController.registerUser(user);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Email ou mot de passe ne peut pas être nul", response.getBody().getMessage());
    }

}
