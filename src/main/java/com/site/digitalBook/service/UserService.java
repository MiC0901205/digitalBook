package com.site.digitalBook.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.site.digitalBook.entity.User;
import com.site.digitalBook.exception.EmailAlreadyExistsException;
import com.site.digitalBook.exception.UnauthorizedException;
import com.site.digitalBook.exception.UserNotFoundException;
import com.site.digitalBook.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

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
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé"));
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

    public User authenticateUser(String email, String password) {
        User user = userRepository.findByEmail(email);
        
        if (user == null) {
            throw new UserNotFoundException("Utilisateur non trouvé");
        }

        if (!passwordEncoder.matches(password, user.getMdp())) {
            throw new UserNotFoundException("Email ou mot de passe invalide");
        }

        Boolean estActif = userRepository.findEstActifByEmail(email);
        if (estActif == null || estActif != true) {
            throw new UnauthorizedException("Le compte est inactif. Authentification non autorisée.");
        }

        return user;
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
            passwords.remove(0); 
        }
        passwords.add(newPassword);

        user.setAnciensMotsDePasse(String.join(",", passwords));
    }
}