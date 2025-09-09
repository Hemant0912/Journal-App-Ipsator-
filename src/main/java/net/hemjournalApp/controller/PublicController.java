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
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserEntity userEntity) {
        UserEntity savedUser = userService.saveNewUser(userEntity);
        UserResponse response = new UserResponse(
                savedUser.getId(),
                savedUser.getUserName(),
                savedUser.getPermissions()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

