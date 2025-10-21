package dev.dmv04.userservice.handler;

import dev.dmv04.userservice.exception.EmailAlreadyExistsException;
import dev.dmv04.userservice.exception.GlobalExceptionHandler;
import dev.dmv04.userservice.exception.UserNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    @Test
    @DisplayName("Should handle EmailAlreadyExistsException with 400 status")
    void handleEmailAlreadyExistsException_shouldReturnBadRequest() {
        // Given
        String email = "test@example.com";
        EmailAlreadyExistsException exception = new EmailAlreadyExistsException(email);

        // When
        ResponseEntity<String> response = exceptionHandler.handleEmailAlreadyExists(exception);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Email '" + email + "' already exists"));
        assertEquals("Email '" + email + "' already exists", response.getBody());
    }

    @Test
    @DisplayName("Should handle UserNotFoundException with 404 status")
    void handleUserNotFoundException_shouldReturnNotFound() {
        // Given
        Long userId = 1L;
        UserNotFoundException exception = new UserNotFoundException(userId);

        // When
        ResponseEntity<String> response = exceptionHandler.handleUserNotFound(exception);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().contains("User with id " + userId + " not found"));
    }

    @Test
    @DisplayName("Should handle MethodArgumentNotValidException with validation errors")
    void handleMethodArgumentNotValidException_shouldReturnValidationErrors() {
        // Given
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError1 = new FieldError("object", "name", "Name is required");
        FieldError fieldError2 = new FieldError("object", "email", "Email must be valid");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

        // When
        ResponseEntity<String> response = exceptionHandler.handleValidationExceptions(exception);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("name: Name is required"));
        assertTrue(response.getBody().contains("email: Email must be valid"));
    }

    @Test
    @DisplayName("Should handle RuntimeException with 500 status")
    void handleRuntimeException_shouldReturnInternalServerError() {
        // Given
        RuntimeException exception = new RuntimeException("Unexpected error");

        // When
        ResponseEntity<String> response = exceptionHandler.handleRuntimeException(exception);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Internal server error", response.getBody());
    }

    @Test
    @DisplayName("Should handle NullPointerException with 500 status")
    void handleNullPointerException_shouldReturnInternalServerError() {
        // Given
        NullPointerException exception = new NullPointerException("Something was null");

        // When
        ResponseEntity<String> response = exceptionHandler.handleNullPointerException(exception);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("Internal server error"));
    }
}