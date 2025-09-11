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

        if (username.isEmpty() && password.isEmpty()) {
            errorResponse.put("error", "Username and Password cannot be empty");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } else if (username.isEmpty()) {
            errorResponse.put("error", "Username cannot be empty");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } else if (password.isEmpty()) {
            errorResponse.put("error", "Password cannot be empty");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        // Now save the user only if everything is valid
        UserEntity savedUser = userService.saveNewUser(userEntity);
        UserResponse response = new UserResponse(
                savedUser.getId(),
                savedUser.getUserName(),
                savedUser.getPermissions()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}

