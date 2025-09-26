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

    // List all users (any admin can view)
    @GetMapping("/all-users")
    public ResponseEntity<?> getAllUsers(Principal principal) {
        UserEntity currentUser = userService.findByUserName(principal.getName());
        if (currentUser.getPermissions().contains("admin:access")) {
            List<UserEntity> all = userService.getAll();
            return all.isEmpty()
                    ? new ResponseEntity<>("No users found", HttpStatus.NOT_FOUND)
                    : new ResponseEntity<>(all, HttpStatus.OK);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("You are not authorized to view all users");
    }

    // Create a new admin using super admin jwt
    @PostMapping("/create-admin")
    public ResponseEntity<?> createAdmin(@RequestBody UserEntity newAdmin, Principal principal) {
        UserEntity authAdmin = userService.findByUserName(principal.getName());
        if (authAdmin.getPermissions().contains("admin:access")) {
            userService.saveAdmin(newAdmin, authAdmin);
            return ResponseEntity.status(HttpStatus.CREATED).body("Admin created successfully");
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("You are not authorized to create admin");
    }
}
