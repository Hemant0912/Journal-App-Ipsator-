package net.hemjournalApp.controller;
import net.hemjournalApp.entity.UserEntity;
import net.hemjournalApp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @GetMapping("/all-users")
    public ResponseEntity<?> getAllUsers() {
         List<UserEntity> all = userService.getAll();
         if (all != null && !all.isEmpty()) {
             return new ResponseEntity<>(all, HttpStatus.OK);
         }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/create-admin")
    public void createUser(@RequestBody UserEntity userEntity) {
        userService.saveAdmin(userEntity);
    }

}
