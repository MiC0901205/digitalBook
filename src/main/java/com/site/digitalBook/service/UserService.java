package com.site.digitalBook.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.site.digitalBook.entity.User;
import com.site.digitalBook.exception.EmailAlreadyExistsException;
import com.site.digitalBook.exception.UnauthorizedException;
import com.site.digitalBook.exception.UserNotFoundException;
import com.site.digitalBook.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // Utilisation de PasswordEncoder générique
    
    private static final int MAX_ATTEMPTS = 3;
    private static final long LOCK_TIME_DURATION = 20 * 60 * 1000; // 20 minutes

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User addUser(User user) {
        if (userRepository.findByEmail(user.getEmail()) != null) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        String rawPassword = user.getMdp();
        String encryptedPassword = passwordEncoder.encode(rawPassword);

        System.out.println("Mot de passe avant encodage : " + rawPassword);
        System.out.println("Mot de passe après encodage : " + encryptedPassword);

        user.setMdp(encryptedPassword);
        System.out.println("user : " + user);

        
        User savedUser = userRepository.save(user);

        System.out.println("Utilisateur ajouté : " + savedUser.getEmail());

        return savedUser;
    }


    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(int id) {
        return userRepository.findById(id);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email); 
    }

    public void deleteUser(int id) {
        userRepository.deleteById(id);
    }

    public User authenticateUser(String email, String password) {
        User user = userRepository.findByEmail(email);
        		
		 if(user == null) {
	        	throw new UnauthorizedException("Email ou mot de passe invalide");
	     }

        if (user.isLocked() && user.getLastFailedLogin() != null &&
            Duration.between(user.getLastFailedLogin(), LocalDateTime.now()).toMillis() < LOCK_TIME_DURATION) {
            throw new UnauthorizedException("Compte verrouillé. Essayez plus tard.");
        }

        boolean passwordMatches = passwordEncoder.matches(password, user.getMdp());


        if (!passwordMatches) {
            handleFailedLogin(user);
            throw new UnauthorizedException("Email ou mot de passe invalide");
        }

        if (user.getFailedLoginAttempts() > 0 || user.isLocked()) {
            user.setFailedLoginAttempts(0);
            user.setLastFailedLogin(null);
            user.setLocked(false);
            userRepository.save(user);
        }

        return user;
    }

    private void handleFailedLogin(User user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        System.out.println("Failed login attempt: " + attempts);
        user.setFailedLoginAttempts(attempts);
        user.setLastFailedLogin(LocalDateTime.now());

        if (attempts >= MAX_ATTEMPTS) {
            user.setLocked(true);
        }

        userRepository.save(user);
    }

    public void resetPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email);
        
        if(user == null) {
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

    public User updateUser(User updatedUser) {
        User existingUser = userRepository.findById(updatedUser.getId())
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé"));

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
        userRepository.save(user);
    }

    
    public User findById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id " + id));
    }
}
