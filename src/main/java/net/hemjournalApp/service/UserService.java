package net.hemjournalApp.service;
import net.hemjournalApp.entity.UserEntity;
import net.hemjournalApp.repository.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public void saveNewUser(UserEntity userEntity) {
        userEntity.setPassword(passwordEncoder.encode(userEntity.getPassword()));
        userEntity.setRoles(Arrays.asList("USER"));
        userRepository.save(userEntity);
    }

    public void saveAdmin(UserEntity userEntity) {
        userEntity.setPassword(passwordEncoder.encode(userEntity.getPassword()));
        userEntity.setRoles(Arrays.asList("USER", "ADMIN"));
        userRepository.save(userEntity);
    }

    public void saveUser(UserEntity userEntity) {
        userRepository.save(userEntity);
    }
    public List<UserEntity> getAll() {
        return userRepository.findAll();
    }
    // means the method might return a JournalEntry object… or it might return nothing (null).
    public Optional<UserEntity> findById(ObjectId id) {
        return userRepository.findById(id);
    }
    public void deleteById(ObjectId id) {
        userRepository.deleteById(id);
    }
    public UserEntity findByUserName(String userName) {
        return userRepository.findByUserName(userName);
    }
}
