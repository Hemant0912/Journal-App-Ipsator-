package net.hemjournalApp.service;

import net.hemjournalApp.entity.UserEntity;
import net.hemjournalApp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserEntity saveNewUser(UserEntity userEntity) {
        userEntity.setPassword(passwordEncoder.encode(userEntity.getPassword()));
        // Default permissions for normal users
        userEntity.setPermissions(Arrays.asList(
                "journal:read", "journal:create", "journal:update", "journal:delete",
                "user:read"
        ));
        return userRepository.save(userEntity);
    }

    public void saveAdmin(UserEntity userEntity, UserEntity authAdmin) {
        List<UserEntity> admins = userRepository.findAll()
                .stream()
                .filter(u -> u.getPermissions().contains("admin:access"))
                .collect(Collectors.toList());

        if (!admins.isEmpty()) {
            if (authAdmin == null || !authAdmin.getPermissions().contains("admin:access")) {
                throw new RuntimeException("Admin already exists. You are not authorized to create another one.");
            }
        }

        // Encode password and assign admin permissions
        userEntity.setPassword(passwordEncoder.encode(userEntity.getPassword()));
        userEntity.setPermissions(Arrays.asList(
                "journal:read", "journal:create", "journal:update", "journal:delete",
                "user:read", "admin:access"
        ));

        userRepository.save(userEntity);
    }

    public void saveUser(UserEntity userEntity) {
        userRepository.save(userEntity);
    }

    public void updateUser(UserEntity existingUser, UserEntity updatedData) {
        existingUser.setUserName(updatedData.getUserName());

        if (updatedData.getPassword() != null && !updatedData.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(updatedData.getPassword()));
        }

        // Preserve existing permissions
        existingUser.setPermissions(existingUser.getPermissions());

        userRepository.save(existingUser);
    }

    public List<UserEntity> getAll() {
        return userRepository.findAll();
    }

    public UserEntity findById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public void deleteById(String id) {
        userRepository.deleteById(id);
    }

    public UserEntity findByUserName(String userName) {
        return userRepository.findByUserName(userName)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    public boolean isUserNameExist(String username) {
        return userRepository.findByUserName(username).isPresent();
    }


}
