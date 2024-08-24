package com.site.digitalBook.controller;

import java.util.Map;
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
import com.site.digitalBook.service.UserService;
import com.site.digitalBook.service.VerificationCodeStorageService;
import com.site.digitalBook.util.JwtUtil;

import jakarta.mail.MessagingException;

/**
 * Contrôleur pour la gestion des utilisateurs.
 * Fournit des endpoints pour l'inscription, l'authentification, la réinitialisation de mot de passe, et d'autres opérations liées aux utilisateurs.
 */
@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;
    private final EmailService emailService;
    private final VerificationCodeStorageService verificationCodeService;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

    /**
     * Constructeur pour l'injection des dépendances.
     *
     * @param userService Le service pour gérer les opérations des utilisateurs.
     * @param emailService Le service pour gérer les opérations liées aux emails.
     * @param verificationCodeService Le service pour stocker et vérifier les codes de vérification.
     * @param jwtUtil L'utilitaire pour gérer les opérations de jetons JWT.
     * @param tokenBlacklistService Le service pour gérer la liste noire des jetons.
     */
    public UserController(UserService userService, EmailService emailService, VerificationCodeStorageService verificationCodeService, JwtUtil jwtUtil, TokenBlacklistService tokenBlacklistService) {
        this.userService = userService;
        this.emailService = emailService;
        this.verificationCodeService = verificationCodeService;
        this.jwtUtil = jwtUtil;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    /**
     * Enregistre un nouvel utilisateur.
     *
     * @param user L'utilisateur à enregistrer.
     * @return Une ResponseEntity avec une charge utile contenant le résultat de l'inscription.
     */
    @PostMapping("/register")
    public ResponseEntity<Payload> registerUser(@RequestBody User user) {
        if (user.getEmail() == null || user.getMdp() == null) {
            Payload payload = new Payload("Email ou mot de passe ne peut pas être nul");
            return new ResponseEntity<>(payload, HttpStatus.BAD_REQUEST);
        }

        try {
            User newUser = userService.addUser(user);
            Payload payload = new Payload("Nouvel utilisateur ajouté", newUser);
            return new ResponseEntity<>(payload, HttpStatus.CREATED);
        } catch (EmailAlreadyExistsException e) {
            Payload payload = new Payload(e.getMessage());
            return new ResponseEntity<>(payload, HttpStatus.BAD_REQUEST);
        } catch (UnauthorizedException e) {
            Payload payload = new Payload("Échec de la connexion : " + e.getMessage());
            return new ResponseEntity<>(payload, HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            Payload payload = new Payload("Échec de l'inscription : " + e.getMessage());
            return new ResponseEntity<>(payload, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Authentifie un utilisateur et envoie un code de confirmation par email.
     *
     * @param user L'utilisateur essayant de se connecter.
     * @return Une ResponseEntity avec une charge utile contenant le résultat de la connexion.
     */
    @PostMapping("/login")
    public ResponseEntity<Payload> loginUser(@RequestBody User user) {
        try {
            User authenticatedUser = userService.authenticateUser(user.getEmail(), user.getMdp());
            String code = emailService.generateVerificationCode();
            emailService.sendConfirmationEmail(user.getEmail(), code);
            emailService.saveCode(user.getEmail(), code);

            Payload payload = new Payload("Utilisateur authentifié. Veuillez vérifier votre email pour le code de confirmation.", authenticatedUser);
            return new ResponseEntity<>(payload, HttpStatus.OK);
        } catch (UnauthorizedException e) {
            Payload payload = new Payload("Échec de la connexion : " + e.getMessage());
            return new ResponseEntity<>(payload, HttpStatus.FORBIDDEN);
        } catch (UserNotFoundException e) {
            Payload payload = new Payload("Échec de la connexion : " + e.getMessage());
            return new ResponseEntity<>(payload, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            Payload payload = new Payload("Échec de la connexion : " + e.getMessage());
            return new ResponseEntity<>(payload, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Vérifie le code envoyé à l'email de l'utilisateur.
     *
     * @param request Une carte contenant l'email et le code.
     * @return Une ResponseEntity avec une charge utile contenant le résultat de la vérification.
     */
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

    /**
     * Initie le processus de réinitialisation du mot de passe en envoyant un lien de réinitialisation à l'email de l'utilisateur.
     *
     * @param user L'utilisateur demandant la réinitialisation du mot de passe.
     * @return Une ResponseEntity avec une charge utile contenant le résultat de la réinitialisation.
     */
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
            Payload payload = new Payload("Erreur lors de l'envoi de l'email.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(payload);
        } catch (Exception e) {
            Payload payload = new Payload("Erreur inattendue lors de l'envoi de l'email.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(payload);
        }
    }

    /**
     * Réinitialise le mot de passe de l'utilisateur.
     *
     * @param email L'email de l'utilisateur.
     * @param requestBody Une carte contenant le nouveau mot de passe.
     * @return Une ResponseEntity avec une charge utile contenant le résultat de la réinitialisation.
     */
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

    /**
     * Déconnecte l'utilisateur en ajoutant le jeton à la liste noire.
     *
     * @param authorizationHeader Le header d'autorisation contenant le jeton.
     * @return Une ResponseEntity indiquant le résultat de la déconnexion.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = "Authorization") String authorizationHeader) {
        String token = authorizationHeader.substring(7); // Supposer "Bearer " préfixé

        tokenBlacklistService.addToBlacklist(token);

        return ResponseEntity.ok().build(); // Retourne HTTP 200 OK
    }

    /**
     * Met à jour le profil de l'utilisateur.
     *
     * @param id L'ID de l'utilisateur à mettre à jour.
     * @param updatedUser Les informations mises à jour de l'utilisateur.
     * @return Une ResponseEntity avec une charge utile contenant le résultat de la mise à jour.
     */
    @PutMapping("/user/{id}")
    public ResponseEntity<Payload> updateUserProfile(@PathVariable int id, @RequestBody User updatedUser) {
        try {
           User user = userService.getUserById(id);
           user.setNom(updatedUser.getNom());
           user.setPrenom(updatedUser.getPrenom());
           user.setEmail(updatedUser.getEmail());
           user.setTel(updatedUser.getTel());
           user.setDateNaissance(updatedUser.getDateNaissance());
           user.setQuestionSecrete(updatedUser.getQuestionSecrete());
           user.setReponseSecrete(updatedUser.getReponseSecrete());

            userService.updateUser(user);

            Payload payload = new Payload("Utilisateur mis à jour avec succès", user);
            return ResponseEntity.ok(payload);
        } catch (UserNotFoundException e) {
            Payload payload = new Payload(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(payload);
        } catch (Exception e) {
            Payload payload = new Payload("Erreur lors de la mise à jour du profil utilisateur : " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(payload);
        }
    }

    /**
     * Obtient l'utilisateur authentifié actuel par email.
     *
     * @param email L'email de l'utilisateur actuel.
     * @return Une ResponseEntity avec une charge utile contenant les informations de l'utilisateur.
     */
    @GetMapping("/current-user")
    public ResponseEntity<Payload> getCurrentUser(@RequestHeader("Email") String email) {
        try {
            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Payload("Email non fourni"));
            }

            User user = userService.getUserByEmail(email);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Payload("Utilisateur non trouvé"));
            }

            Payload payload = new Payload("Utilisateur trouvé", user);
            return ResponseEntity.ok(payload);

        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Payload("Erreur : Utilisateur non trouvé. Détails : " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Payload("Erreur : L'email fourni est invalide. Détails : " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Payload("Erreur interne du serveur. Détails : " + e.getMessage()));
        }
    }
}
