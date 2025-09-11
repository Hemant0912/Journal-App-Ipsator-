package net.hemjournalApp.controller;
import net.hemjournalApp.entity.UserEntity;
import net.hemjournalApp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @GetMapping("/all-users")
    public ResponseEntity<?> getAllUsers(Principal principal) {
        String userName = principal.getName();
        UserEntity currentUser = userService.findByUserName(userName);
        if (currentUser.getPermissions().contains("admin:access")) {
            List<UserEntity> all = userService.getAll();
            if (all != null && !all.isEmpty()) {
                return new ResponseEntity<>(all, HttpStatus.OK);
            }
            return new ResponseEntity<>("No users found", HttpStatus.NOT_FOUND);
        }

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body("You are not authorized to view all users");
    }


    @PostMapping("/create-admin")
    public ResponseEntity<?> createAdmin(@RequestBody UserEntity newAdmin, Principal principal) {
        try {
            UserEntity authAdmin = null;
            if (principal != null) {
                authAdmin = userService.findByUserName(principal.getName());
            }

            userService.saveAdmin(newAdmin, authAdmin);
            return ResponseEntity.status(HttpStatus.CREATED).body("Admin created successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }
}

