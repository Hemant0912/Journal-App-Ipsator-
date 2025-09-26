package net.hemjournalApp.controller;
import net.hemjournalApp.dto.UserResponse;
import net.hemjournalApp.entity.UserEntity;
import net.hemjournalApp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/public")
public class PublicController {

    @Autowired
    private UserService userService;

    @GetMapping("/health-check")
    public String healthCheck() {
        return "Ok";
    }

    @PostMapping("/create-user")
    public ResponseEntity<?> createUser(@RequestBody UserEntity userEntity) {
        String username = (userEntity.getUserName() != null) ? userEntity.getUserName().trim() : "";
        String password = (userEntity.getPassword() != null) ? userEntity.getPassword().trim() : "";

        Map<String, String> errorResponse = new HashMap<>();

        // Check if both are empty
        if (username.isEmpty() && password.isEmpty()) {
            errorResponse.put("error", "Username and Password cannot be empty");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        // Check username validations
        if (username.isEmpty()) {
            errorResponse.put("username", "Username cannot be empty");
        } else if (username.length() < 3) {
            errorResponse.put("username", "Username must be at least 3 characters long");
        } else {
            // Check if username already exist
            boolean userExists = userService.isUserNameExist(username);
            if (userExists) {
                errorResponse.put("username", "Username already exists. Please choose a different username.");
            }
        }

        // Check password validations
        if (password.isEmpty()) {
            errorResponse.put("password", "Password cannot be empty");
        } else if (!password.matches("^(?=.*[0-9])(?=.*[@#$%^&+=!]).{5,}$")) {
            errorResponse.put("password", "Password must be at least 5 characters, include at least one number and one special character");
        }

        if (!errorResponse.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        String email = (userEntity.getEmail() != null) ? userEntity.getEmail().trim() : "";

        if (email.isEmpty()) {
            errorResponse.put("email", "Email cannot be empty");
        } else if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            errorResponse.put("email", "Invalid email format");
        } else {
            boolean emailExists = userService.isEmailExist(email);
            if (emailExists) {
                errorResponse.put("email", "Email already exists. Please use a different one.");
            }
        }

        UserEntity savedUser = userService.saveNewUser(userEntity);
        UserResponse response = new UserResponse(
                savedUser.getId(),
                savedUser.getUserName(),
                savedUser.getPermissions()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


}
