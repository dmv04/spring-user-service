package dev.dmv04.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.dmv04.userservice.dto.CreateUserRequest;
import dev.dmv04.userservice.dto.UpdateUserRequest;
import dev.dmv04.userservice.entity.User;
import dev.dmv04.userservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepository;

    @Test
    void createUser_shouldReturn201() throws Exception {
        CreateUserRequest request = new CreateUserRequest("Alice", "alice@test.com", 30);
        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setName("Alice");
        savedUser.setEmail("alice@test.com");
        savedUser.setAge(30);
        savedUser.setCreatedAt(LocalDateTime.now());

        when(userRepository.existsByEmail("alice@test.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Alice"));
    }

    @Test
    void getUserById_shouldReturnUser() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setName("Bob");
        user.setEmail("bob@test.com");
        user.setAge(25);
        user.setCreatedAt(LocalDateTime.now());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Bob"));
    }

    @Test
    void updateUser_shouldReturnUpdatedUser() throws Exception {
        User existing = new User();
        existing.setId(1L);
        existing.setName("Old");
        existing.setEmail("old@test.com");
        existing.setAge(40);

        User updated = new User();
        updated.setId(1L);
        updated.setName("New");
        updated.setEmail("new@test.com");
        updated.setAge(45);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(updated);

        UpdateUserRequest request = new UpdateUserRequest("New", "new@test.com", 45);

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New"));
    }

    @Test
    void deleteUser_shouldReturnNoContent() throws Exception {
        when(userRepository.existsById(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());

        verify(userRepository).deleteById(1L);
    }

    @Test
    void createUser_shouldThrowWhenEmailExists() throws Exception {
        CreateUserRequest request = new CreateUserRequest("Alice", "existing@test.com", 30);
        when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Email 'existing@test.com' already exists")));
    }

    @Test
    void updateUser_shouldThrowWhenNewEmailAlreadyExists() throws Exception {
        User existing = new User();
        existing.setId(1L);
        existing.setName("Old");
        existing.setEmail("old@test.com");
        existing.setAge(40);

        UpdateUserRequest request = new UpdateUserRequest(null, "existing@test.com", null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Email 'existing@test.com' already exists")));
    }

    @Test
    void updateUser_shouldSkipNameIfNullOrBlank() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest("", "new@test.com", 50);

        User existingUser = new User("Old Name", "old@test.com", 30);
        existingUser.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Old Name"))
                .andExpect(jsonPath("$.email").value("new@test.com"))
                .andExpect(jsonPath("$.age").value(50));
    }

    @Test
    void updateUser_shouldSkipEmailIfNullOrBlank() throws Exception {
        User existing = new User();
        existing.setId(1L);
        existing.setName("Old");
        existing.setEmail("old@test.com");
        existing.setAge(40);

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setName("New");
        updatedUser.setEmail("old@test.com");
        updatedUser.setAge(50);
        updatedUser.setCreatedAt(existing.getCreatedAt());

        UpdateUserRequest request = new UpdateUserRequest("New", "", 50);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New"))
                .andExpect(jsonPath("$.email").value("old@test.com"))
                .andExpect(jsonPath("$.age").value(50));
    }

    @Test
    void updateUser_shouldSkipAgeIfNull() throws Exception {
        User existing = new User();
        existing.setId(1L);
        existing.setName("Old");
        existing.setEmail("old@test.com");
        existing.setAge(40);

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setName("New");
        updatedUser.setEmail("new@test.com");
        updatedUser.setAge(40);
        updatedUser.setCreatedAt(existing.getCreatedAt());

        UpdateUserRequest request = new UpdateUserRequest("New", "new@test.com", null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.age").value(40));
    }
}
