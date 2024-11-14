package org.lab.service;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import org.lab.model.User;
import org.lab.repository.UserRepository;
import org.lab.utils.JwtUtils;
import org.lab.utils.PasswordUtils;

import java.util.List;

@Stateless
public class UserService {

    @Inject
    private UserRepository userRepository;

    public String register(User user) {
        user.setPassword(PasswordUtils.hashPassword(user.getPassword()));
        User createdUser = userRepository.save(user);
        return JwtUtils.generateToken(createdUser);
    }


    public String login(User user) {
        User existingUser = userRepository.findByUsername(user.getUsername());
        if (existingUser != null && existingUser.getPassword().equals(PasswordUtils.hashPassword(user.getPassword()))) {
            return JwtUtils.generateToken(existingUser);
        }
        return null;
    }

    public void logout() {

    }

    public User getUserById(Integer id) {
        return userRepository.findById(id);
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateUser(Integer id, User user) {
        user.setId(id); // Ensure the ID is set for the update
        return userRepository.update(user);
    }

    public void deleteUser(Integer id) {
        userRepository.delete(id);
    }
}
