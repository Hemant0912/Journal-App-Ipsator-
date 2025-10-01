package net.hemjournalApp.controller;
import net.hemjournalApp.dto.UserRequestDTO;
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
    public ResponseEntity<?> createUser(@Valid @RequestBody UserRequestDTO userRequest) {
        // Check uniqueness
        if (userService.isUserNameExist(userRequest.getUserName())) {
            return ResponseEntity.badRequest().body(Map.of("username", "Username already exists"));
        }
        if (userService.isEmailExist(userRequest.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("email", "Email already exists"));
        }
        if (userService.isMobileExist(userRequest.getMobile())) {
            return ResponseEntity.badRequest().body(Map.of("mobile", "Mobile already exists"));
        }

        // Convert DTO to entity
        UserEntity user = new UserEntity();
        user.setUserName(userRequest.getUserName());
        user.setPassword(userRequest.getPassword());
        user.setEmail(userRequest.getEmail());
        user.setMobile(userRequest.getMobile());

        UserEntity savedUser = userService.saveNewUser(user);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new UserResponse(savedUser.getId(), savedUser.getUserName(), savedUser.getPermissions()));
    }



}
