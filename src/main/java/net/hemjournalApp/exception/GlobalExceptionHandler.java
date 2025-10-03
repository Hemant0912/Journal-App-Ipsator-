package net.hemjournalApp.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Validation Errors
    @ResponseBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        // Start with empty map (instead of full DTO map)
        Map<String, Object> response = new HashMap<>();

        // Only put invalid fields with their error message
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            response.put(error.getField(), error.getDefaultMessage());
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // Duplicate key errors (email/mobile already exists)
    @ExceptionHandler(DuplicateKeyException.class)
    @ResponseBody
    public ResponseEntity<?> handleDuplicateKey(DuplicateKeyException ex) {
        Map<String, String> response = new HashMap<>();

        String message = ex.getMessage();
        if (message.contains("email")) {
            response.put("email", "Invalid email - already exists");
        } else if (message.contains("mobile")) {
            response.put("mobile", "Invalid mobile - already exists");
        } else {
            response.put("error", "Duplicate key error");
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
