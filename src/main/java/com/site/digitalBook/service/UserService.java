package com.site.digitalBook.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.site.digitalBook.entity.User;
import com.site.digitalBook.exception.EmailAlreadyExistsException;
import com.site.digitalBook.exception.UnauthorizedException;
import com.site.digitalBook.exception.UserNotFoundException;
import com.site.digitalBook.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // Utilisation de PasswordEncoder générique
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    private static final int MAX_ATTEMPTS = 3;
    private static final long LOCK_TIME_DURATION = 20 * 60 * 1000; // 20 minutes

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User addUser(User user) {
        logger.info("Ajout d'un utilisateur avec l'email : {}", user.getEmail());

        if (userRepository.findByEmail(user.getEmail()) != null) {
            logger.warn("L'email existe déjà : {}", user.getEmail());
            throw new EmailAlreadyExistsException("L'email existe déjà");
        }

        String rawPassword = user.getMdp();
        String encryptedPassword = passwordEncoder.encode(rawPassword);

        logger.debug("Mot de passe avant l'encodage : {}", rawPassword);
        logger.debug("Mot de passe après l'encodage : {}", encryptedPassword);

        user.setMdp(encryptedPassword);
        User savedUser = userRepository.save(user);

        logger.info("Utilisateur ajouté avec l'email : {}", savedUser.getEmail());

        return savedUser;
    }

    public List<User> getAllUsers() {
        logger.info("Récupération de tous les utilisateurs");
        return userRepository.findAll();
    }

    public User getUserById(int id) {
        logger.info("Récupération de l'utilisateur par id : {}", id);
        return userRepository.findById(id);
    }

    public User getUserByEmail(String email) {
        logger.info("Récupération de l'utilisateur par email : {}", email);
        return userRepository.findByEmail(email); 
    }

    public void deleteUser(int id) {
        logger.info("Suppression de l'utilisateur avec l'id : {}", id);

        userRepository.deleteById(id);
    }

    public User authenticateUser(String email, String password) {
        logger.info("Authentification de l'utilisateur avec l'email : {}", email);
        User user = userRepository.findByEmail(email);

        if (user == null) {
            logger.warn("Utilisateur non trouvé avec l'email : {}", email);
            throw new UnauthorizedException("Email ou mot de passe invalide");
        }

        if (user.isLocked() && user.getLastFailedLogin() != null &&
            Duration.between(user.getLastFailedLogin(), LocalDateTime.now()).toMillis() < LOCK_TIME_DURATION) {
            logger.warn("Compte verrouillé pour l'email : {}", email);
            throw new UnauthorizedException("Compte verrouillé. Essayez plus tard.");
        }

        boolean passwordMatches = passwordEncoder.matches(password, user.getMdp());

        if (!passwordMatches) {
            handleFailedLogin(user);
            logger.warn("Mot de passe invalide pour l'email : {}", email);
            throw new UnauthorizedException("Email ou mot de passe invalide");
        }

        if (user.getFailedLoginAttempts() > 0 || user.isLocked()) {
            user.setFailedLoginAttempts(0);
            user.setLastFailedLogin(null);
            user.setLocked(false);
            userRepository.save(user);
            logger.info("Tentatives de connexion échouées réinitialisées et compte déverrouillé pour l'email : {}", email);
        }

        return user;
    }

    private void handleFailedLogin(User user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        logger.info("Tentative de connexion échouée : {}", attempts);
        user.setFailedLoginAttempts(attempts);
        user.setLastFailedLogin(LocalDateTime.now());

        if (attempts >= MAX_ATTEMPTS) {
            user.setLocked(true);
            logger.warn("Compte utilisateur verrouillé en raison de trop nombreuses tentatives de connexion échouées : {}", user.getEmail());
        }

        userRepository.save(user);
    }

    public void resetPassword(String email, String newPassword) {
        logger.info("Réinitialisation du mot de passe pour l'email : {}", email);
        User user = userRepository.findByEmail(email);

        if (user == null) {
            logger.error("Utilisateur non trouvé avec l'email : {}", email);
            throw new RuntimeException("Utilisateur non trouvé.");
        }

        String encodedNewPassword = passwordEncoder.encode(newPassword);
        List<String> lastPasswords = getLastPasswordsList(user);

        for (String lastPassword : lastPasswords) {
            if (passwordEncoder.matches(newPassword, lastPassword)) {
                logger.warn("Le nouveau mot de passe correspond à l'un des derniers mots de passe pour l'email : {}", email);
                throw new RuntimeException("Le nouveau mot de passe ne peut pas être l'un des 5 derniers mots de passe.");
            }
        }

        user.setMdp(encodedNewPassword);
        addPasswordToHistory(user, encodedNewPassword);

        userRepository.save(user);
        logger.info("Mot de passe réinitialisé avec succès pour l'email : {}", email);
    }

    private List<String> getLastPasswordsList(User user) {
        String lastPasswords = user.getAnciensMotsDePasse();
        if (lastPasswords != null && !lastPasswords.isEmpty()) {
            return new ArrayList<>(Arrays.asList(lastPasswords.split(",")));
        }
        return new ArrayList<>();
    }

    private void addPasswordToHistory(User user, String newPassword) {
        List<String> passwords = getLastPasswordsList(user);

        if (passwords.size() >= 5) {
            passwords.remove(0);
        }
        passwords.add(newPassword);

        user.setAnciensMotsDePasse(String.join(",", passwords));
    }

    public User updateUser(User updatedUser) {
        logger.info("Mise à jour de l'utilisateur avec l'id : {}", updatedUser.getId());
        User existingUser = userRepository.findById(updatedUser.getId())
                .orElseThrow(() -> {
                    logger.error("Utilisateur non trouvé avec l'id : {}", updatedUser.getId());
                    return new UserNotFoundException("Utilisateur non trouvé");
                });

        existingUser.setNom(updatedUser.getNom());
        existingUser.setPrenom(updatedUser.getPrenom());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setTel(updatedUser.getTel());
        existingUser.setDateNaissance(updatedUser.getDateNaissance());

        if (updatedUser.getMdp() != null && !updatedUser.getMdp().isEmpty()) {
            existingUser.setMdp(passwordEncoder.encode(updatedUser.getMdp()));
            logger.info("Mot de passe mis à jour pour l'utilisateur avec l'id : {}", updatedUser.getId());
        }

        return userRepository.save(existingUser);
    }

    public void activateUser(String email) throws UserNotFoundException {
        logger.info("Activation de l'utilisateur avec l'email : {}", email);
        User user = getUserByEmail(email);
        if (user == null) {
            logger.error("Utilisateur non trouvé avec l'email : {}", email);
            throw new UserNotFoundException("Utilisateur non trouvé.");
        }

        user.setEstActif(true);
        userRepository.save(user);
        logger.info("Utilisateur activé avec l'email : {}", email);
    }

    public User findById(Integer id) {
        logger.info("Recherche de l'utilisateur par id : {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Utilisateur non trouvé avec l'id : {}", id);
                    return new RuntimeException("Utilisateur non trouvé avec l'id " + id);
                });
    }
}
