package net.hemjournalApp.repository;
import net.hemjournalApp.entity.UserEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<UserEntity, String> {
    Optional<UserEntity> findByUserName(String userName);

    void deleteByUserName(String username);
    Optional<UserEntity> findByEmail(String email);

}