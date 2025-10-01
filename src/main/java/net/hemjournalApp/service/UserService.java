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

    public boolean isSuperAdmin(UserEntity user) {
        // find the first admin ever created
        return userRepository.findAll().stream()
                .filter(u -> u.getPermissions().contains("admin:access"))
                .findFirst()
                .map(firstAdmin -> firstAdmin.getId().equals(user.getId()))
                .orElse(false);
    }


    // Super Admin creates a new admin
    public void createAdminBySuperAdmin(UserEntity superAdmin, UserEntity newAdmin) {
        if (!isSuperAdmin(superAdmin)) {
            throw new RuntimeException("Only Super Admin can create new admins");
        }

        newAdmin.setPassword(passwordEncoder.encode(newAdmin.getPassword()));
        newAdmin.setPermissions(Arrays.asList(
                "journal:read", "journal:create", "journal:update", "journal:delete",
                "user:read", "admin:access"
        ));

        userRepository.save(newAdmin);
    }
    // Find user by email
    public Optional<UserEntity> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Find user by mobile
    public Optional<UserEntity> findByMobile(String mobile) {
        return userRepository.findByMobile(mobile);
    }

    // helper used by AuthController
    public Optional<UserEntity> findByIdentifier(String identifier) {
        if (identifier == null) return Optional.empty();
        Optional<UserEntity> byEmail = userRepository.findByEmail(identifier);
        if (byEmail.isPresent()) return byEmail;
        return userRepository.findByMobile(identifier);
    }

    public boolean isMobileExist(String mobile) {
        return userRepository.existsByMobile(mobile);
    }

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

        // First admin creation
        if (admins.isEmpty()) {
            userEntity.setPassword(passwordEncoder.encode(userEntity.getPassword()));
            // Give default admin permissions
            userEntity.setPermissions(Arrays.asList(
                    "journal:read", "journal:create", "journal:update", "journal:delete",
                    "user:read", "admin:access"
            ));
            userRepository.save(userEntity);
            return;
        }

        if (authAdmin == null || !authAdmin.getPermissions().contains("admin:access")) {
            throw new RuntimeException("You are not authorized to create a new admin.");
        }

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
    // Update password
    public boolean updatePassword(UserEntity user, String oldPassword, String newPassword) {
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return false; // old password incorrect
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return true;
    }

    // Update username
    public void updateUsername(UserEntity user, String newUsername, String currentPassword) {
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        if (isUserNameExist(newUsername)) {
            throw new RuntimeException("Username already exists");
        }

        user.setUserName(newUsername);
        userRepository.save(user);
    }

    public boolean isUserNameExist(String username) {
        return userRepository.findByUserName(username).isPresent();
    }
    public boolean isEmailExist(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

}




