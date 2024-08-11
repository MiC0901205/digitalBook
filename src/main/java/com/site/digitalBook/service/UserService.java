package com.site.digitalBook.service;

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
                .orElseThrow(() -> new UserNotFoundException("User Not Found"));
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
}