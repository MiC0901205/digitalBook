package com.site.digitalBook.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.site.digitalBook.entity.User;
import com.site.digitalBook.exception.EmailAlreadyExistsException;
import com.site.digitalBook.exception.UnauthorizedException;
import com.site.digitalBook.exception.UserNotFoundException;
import com.site.digitalBook.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    
    private static final int MAX_ATTEMPTS = 3;
    private static final long LOCK_TIME_DURATION = 20 * 60 * 1000; // 15 minutes

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User addUser(User user) {
        if (userRepository.findByEmail(user.getEmail()) != null) {
            throw new EmailAlreadyExistsException("Email already exists");
        }
        String encryptedPassword = passwordEncoder.encode(user.getMdp());
        user.setMdp(encryptedPassword);
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(int id) {
        User user = userRepository.findById(id);
        if (user == null) {
            throw new UserNotFoundException("Utilisateur non trouvé");
        }
        return user;
    }

    public User getUserByEmail(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UserNotFoundException("Utilisateur non trouvé");
        }
        return user;
    }

    public void deleteUser(int id) {
        userRepository.deleteById(id);
    }

    /**
     * Authentifie un utilisateur à partir de son email et mot de passe.
     * Vérifie également si le compte est verrouillé en raison de tentatives de connexion échouées.
     *
     * @param email    l'adresse email de l'utilisateur
     * @param password le mot de passe de l'utilisateur
     * @return l'utilisateur authentifié s'il est trouvé et les informations d'authentification sont correctes
     * @throws UnauthorizedException si les informations d'identification sont incorrectes ou si le compte est verrouillé
     */
    public User authenticateUser(String email, String password) {
        // Récupération de l'utilisateur à partir de son email
        User user = userRepository.findByEmail(email);

        // Vérification si le compte est verrouillé
        if (user.isLocked() && user.getLastFailedLogin() != null &&
            Duration.between(user.getLastFailedLogin(), LocalDateTime.now()).toMillis() < LOCK_TIME_DURATION) {
            throw new UnauthorizedException("Compte verrouillé. Essayez plus tard.");
        }

        // Vérification de la correspondance du mot de passe
        if (!passwordEncoder.matches(password, user.getMdp())) {
            handleFailedLogin(user); // Gestion des tentatives échouées
            throw new UnauthorizedException("Email ou mot de passe invalide");
        }

        // Réinitialisation des tentatives échouées et du verrouillage si l'authentification réussit
        if (user.getFailedLoginAttempts() > 0 || user.isLocked()) {
            user.setFailedLoginAttempts(0);
            user.setLastFailedLogin(null);
            user.setLocked(false);
            userRepository.save(user);
        }

        return user; // Retourne l'utilisateur authentifié
    }

    /**
     * Gère les tentatives de connexion échouées.
     * Incrémente le compteur de tentatives échouées et verrouille le compte si le nombre maximal est atteint.
     *
     * @param user l'utilisateur pour lequel la tentative a échoué
     */
    private void handleFailedLogin(User user) {
        int attempts = user.getFailedLoginAttempts() + 1; // Incrémenter le nombre de tentatives échouées
        System.out.println("Failed login attempt: " + attempts);
        user.setFailedLoginAttempts(attempts);
        user.setLastFailedLogin(LocalDateTime.now());

        // Verrouiller le compte si le nombre maximal de tentatives échouées est atteint
        if (attempts >= MAX_ATTEMPTS) {
            user.setLocked(true);
            System.out.println("Account is locked due to too many failed login attempts.");
        }

        userRepository.save(user); // Sauvegarde des modifications de l'utilisateur
    }

    public void resetPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("Utilisateur non trouvé.");
        }

        String encodedNewPassword = passwordEncoder.encode(newPassword);

        List<String> lastPasswords = getLastPasswordsList(user);
        for (String lastPassword : lastPasswords) {
            if (passwordEncoder.matches(newPassword, lastPassword)) {
                throw new RuntimeException("Le nouveau mot de passe ne peut pas être l'un des 5 derniers mots de passe.");
            }
        }

        user.setMdp(encodedNewPassword);
        addPasswordToHistory(user, encodedNewPassword);

        userRepository.save(user);
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
            passwords.remove(0); // Enlever le plus ancien mot de passe
        }
        passwords.add(newPassword);

        user.setAnciensMotsDePasse(String.join(",", passwords));
    }

    public User updateUser(User updatedUser) {
        User existingUser = userRepository.findById(updatedUser.getId());
        if (existingUser == null) {
            throw new UserNotFoundException("Utilisateur non trouvé");
        }

        existingUser.setNom(updatedUser.getNom());
        existingUser.setPrenom(updatedUser.getPrenom());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setTel(updatedUser.getTel());
        existingUser.setDateNaissance(updatedUser.getDateNaissance());

        if (updatedUser.getMdp() != null && !updatedUser.getMdp().isEmpty()) {
            existingUser.setMdp(passwordEncoder.encode(updatedUser.getMdp()));
        }

        return userRepository.save(existingUser);
    }
    
    public void activateUser(String email) throws UserNotFoundException {
        User user = getUserByEmail(email);
        if (user == null) {
            throw new UserNotFoundException("Utilisateur non trouvé.");
        }

        user.setEstActif(true);
        updateUser(user); // Enregistrer les modifications
    }
}
