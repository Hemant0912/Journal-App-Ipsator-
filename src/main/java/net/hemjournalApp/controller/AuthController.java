package net.hemjournalApp.controller;

import net.hemjournalApp.dto.AuthRequest;
import net.hemjournalApp.dto.LoginResponse;
import net.hemjournalApp.entity.UserEntity;
import net.hemjournalApp.service.UserService;
import net.hemjournalApp.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/public")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid AuthRequest request) {
        try {
            if ((request.getEmail() == null || request.getEmail().isBlank()) &&
                    (request.getMobile() == null || request.getMobile().isBlank())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email or mobile is required"));
            }

            UserEntity user;

            if (request.getEmail() != null && !request.getEmail().isBlank()) {
                // login with email
                user = userService.findByEmail(request.getEmail())
                        .orElseThrow(() -> new RuntimeException("Invalid credentials"));
            } else {
                // login with mobile
                user = userService.findByMobile(request.getMobile())
                        .orElseThrow(() -> new RuntimeException("Invalid credentials"));
            }

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUserName(), request.getPassword())
            );

            // Generate JWT
            String token = jwtUtil.generateToken(user.getUserName());

            // Response
            LoginResponse response = new LoginResponse(
                    user.getId(),
                    user.getUserName(),
                    user.getEmail(),
                    user.getMobile(),
                    user.getPermissions()
            );

            return ResponseEntity.ok()
                    .header("X-auth", token)
                    .body(response);

        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }
    }

}
