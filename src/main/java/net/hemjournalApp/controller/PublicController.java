package net.hemjournalApp.controller;

import net.hemjournalApp.entity.UserEntity;
import net.hemjournalApp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/public")
public class PublicController {

    @Autowired
    private UserService userService;

    @GetMapping("/health-check")
    public String healthCheck() {
        return "Ok";
    }
    @PostMapping("create-user")
    public void createUser(@RequestBody UserEntity userEntity) {
        userService.saveNewUser(userEntity);
    }
}
