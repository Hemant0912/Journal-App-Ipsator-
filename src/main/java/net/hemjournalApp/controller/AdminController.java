package net.hemjournalApp.controller;
import net.hemjournalApp.entity.UserEntity;
import net.hemjournalApp.service.UserService;
import net.hemjournalApp.util.JwtUtil;
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

    @Autowired
    private JwtUtil jwtUtil;

    // List all users
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

    @PostMapping("/create-admin")
    public ResponseEntity<?> createAdmin(
            @RequestHeader(value = "X-Auth", required = false) String token,
            @RequestBody UserEntity newAdmin) {

        // check if any admin exists in DB
        boolean isAdminExist = userService.getAll()
                .stream()
                .anyMatch(u -> u.getPermissions().contains("admin:access"));

        // 1First Super Admin creation
        if (!isAdminExist) {
            userService.saveAdmin(newAdmin, null); // creator = null
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Super Admin created successfully");
        }

       // jwt required to create new admin
        if (token == null || token.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("JWT token required in X-Auth header");
        }

        String username;
        try {
            username = jwtUtil.extractUsername(token.trim());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid JWT token");
        }

        UserEntity authAdmin = userService.findByUserName(username);

        if (!userService.isSuperAdmin(authAdmin)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Only Super Admin can create new admins");
        }

        // Super Admin creates another admin
        userService.saveAdmin(newAdmin, authAdmin);
        return ResponseEntity.status(HttpStatus.CREATED).body("Admin created successfully");
    }
}



