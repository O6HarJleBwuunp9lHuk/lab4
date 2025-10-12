package org.example.service;

import org.example.dto.CreateUserRequest;
import org.example.dto.UpdateUserRequest;
import org.example.dto.UserDto;
import org.example.entity.User;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDto createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalStateException("User with email " + request.getEmail() + " already exists");
        }

        User user = new User(request.getName(), request.getEmail(), request.getAge());
        User savedUser = userRepository.save(user);

        return convertToDto(savedUser);
    }

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userRepository.findAll()
            .stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalStateException("User not found with ID: " + id));

        return convertToDto(user);
    }

    public UserDto updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalStateException("User not found with ID: " + id));

        if (request.getName() != null) {
            user.setName(request.getName());
        }

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
                throw new IllegalStateException("Email " + request.getEmail() + " is already taken by another user");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getAge() != null) {
            user.setAge(request.getAge());
        }

        User updatedUser = userRepository.save(user);
        return convertToDto(updatedUser);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalStateException("User not found with ID: " + id);
        }
        userRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public boolean userExists(String email) {
        return userRepository.existsByEmail(email);
    }

    private UserDto convertToDto(User user) {
        return new UserDto(user.getId(), user.getName(), user.getEmail(), user.getAge());
    }
}
