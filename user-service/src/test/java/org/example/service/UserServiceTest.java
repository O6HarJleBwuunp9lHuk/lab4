package org.example.service;

import org.example.dto.CreateUserRequest;
import org.example.dto.UpdateUserRequest;
import org.example.dto.UserDto;
import org.example.entity.User;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_WithValidData_ShouldReturnUserDto() {
        // Given
        CreateUserRequest request = new CreateUserRequest("John Doe", "john@example.com", 30);
        User user = new User("John Doe", "john@example.com", 30);
        user.setId(1L);

        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        UserDto result = userService.createUser(request);

        // Then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("John Doe");
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        assertThat(result.getAge()).isEqualTo(30);

        verify(userRepository).existsByEmail("john@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_WithDuplicateEmail_ShouldThrowException() {
        // Given
        CreateUserRequest request = new CreateUserRequest("John Doe", "john@example.com", 30);

        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> userService.createUser(request));

        assertThat(exception.getMessage()).contains("already exists");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getAllUsers_ShouldReturnUserList() {
        // Given
        User user1 = new User("John Doe", "john@example.com", 30);
        user1.setId(1L);
        User user2 = new User("Jane Smith", "jane@example.com", 25);
        user2.setId(2L);

        List<User> users = Arrays.asList(user1, user2);

        when(userRepository.findAll()).thenReturn(users);

        // When
        List<UserDto> result = userService.getAllUsers();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getName()).isEqualTo("John Doe");
        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(1).getName()).isEqualTo("Jane Smith");

        verify(userRepository).findAll();
    }

    @Test
    void getAllUsers_WhenNoUsers_ShouldReturnEmptyList() {
        // Given
        when(userRepository.findAll()).thenReturn(List.of());

        // When
        List<UserDto> result = userService.getAllUsers();

        // Then
        assertThat(result).isEmpty();
        verify(userRepository).findAll();
    }

    @Test
    void getUserById_WithExistingId_ShouldReturnUserDto() {
        // Given
        User user = new User("John Doe", "john@example.com", 30);
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // When
        UserDto result = userService.getUserById(1L);

        // Then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("John Doe");
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        assertThat(result.getAge()).isEqualTo(30);

        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_WithNonExistingId_ShouldThrowException() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> userService.getUserById(999L));

        assertThat(exception.getMessage()).contains("not found");
        verify(userRepository).findById(999L);
    }

    @Test
    void updateUser_WithValidData_ShouldReturnUpdatedUserDto() {
        // Given
        User existingUser = new User("Old Name", "old@example.com", 25);
        existingUser.setId(1L);

        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("New Name");
        request.setEmail("new@example.com");
        request.setAge(30);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmailAndIdNot("new@example.com", 1L)).thenReturn(false);
        when(userRepository.save(existingUser)).thenReturn(existingUser);

        // When
        UserDto result = userService.updateUser(1L, request);

        // Then
        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getEmail()).isEqualTo("new@example.com");
        assertThat(result.getAge()).isEqualTo(30);

        verify(userRepository).findById(1L);
        verify(userRepository).existsByEmailAndIdNot("new@example.com", 1L);
        verify(userRepository).save(existingUser);
    }

    @Test
    void updateUser_WithPartialData_ShouldUpdateOnlyProvidedFields() {
        // Given
        User existingUser = new User("Original Name", "original@example.com", 25);
        existingUser.setId(1L);

        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("Updated Name");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(existingUser);

        // When
        UserDto result = userService.updateUser(1L, request);

        // Then
        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getEmail()).isEqualTo("original@example.com"); // unchanged
        assertThat(result.getAge()).isEqualTo(25); // unchanged

        verify(userRepository).findById(1L);
        verify(userRepository).save(existingUser);
    }

    @Test
    void updateUser_WithSameEmail_ShouldNotCheckUniqueness() {
        // Given
        User existingUser = new User("John Doe", "john@example.com", 30);
        existingUser.setId(1L);

        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail("john@example.com"); // same email

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(existingUser);

        // When
        UserDto result = userService.updateUser(1L, request);

        // Then
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        verify(userRepository, never()).existsByEmailAndIdNot(anyString(), anyLong());
        verify(userRepository).save(existingUser);
    }

    @Test
    void updateUser_WithDuplicateEmail_ShouldThrowException() {
        // Given
        User existingUser = new User("John Doe", "john@example.com", 30);
        existingUser.setId(1L);

        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail("existing@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmailAndIdNot("existing@example.com", 1L)).thenReturn(true);

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> userService.updateUser(1L, request));

        assertThat(exception.getMessage()).contains("already taken");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_WithNonExistingId_ShouldThrowException() {
        // Given
        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("New Name");

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> userService.updateUser(999L, request));

        assertThat(exception.getMessage()).contains("not found");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUser_WithExistingId_ShouldDeleteUser() {
        // Given
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        // When
        userService.deleteUser(1L);

        // Then
        verify(userRepository).existsById(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_WithNonExistingId_ShouldThrowException() {
        // Given
        when(userRepository.existsById(999L)).thenReturn(false);

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> userService.deleteUser(999L));

        assertThat(exception.getMessage()).contains("not found");
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void userExists_WithExistingEmail_ShouldReturnTrue() {
        // Given
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // When
        boolean result = userService.userExists("existing@example.com");

        // Then
        assertThat(result).isTrue();
        verify(userRepository).existsByEmail("existing@example.com");
    }

    @Test
    void userExists_WithNonExistingEmail_ShouldReturnFalse() {
        // Given
        when(userRepository.existsByEmail("nonexistent@example.com")).thenReturn(false);

        // When
        boolean result = userService.userExists("nonexistent@example.com");

        // Then
        assertThat(result).isFalse();
        verify(userRepository).existsByEmail("nonexistent@example.com");
    }

    @Test
    void createUser_WithNullRequest_ShouldThrowException() {
        // When & Then
        assertThrows(NullPointerException.class,
            () -> userService.createUser(null));
    }

    @Test
    void updateUser_WithNullRequest_ShouldThrowException() {
        // Given
        User existingUser = new User("John Doe", "john@example.com", 30);
        existingUser.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

        // When & Then
        assertThrows(NullPointerException.class,
            () -> userService.updateUser(1L, null));
    }
}
