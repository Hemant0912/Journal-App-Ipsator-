package net.hemjournalApp.entity;
import lombok.Data;
import lombok.NonNull;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "users")
public class UserEntity {
    @Id
    private ObjectId id;
// it will be not created automatically i.e indexing we have to give it in app.prop
    @NotBlank(message = "Username cannot be empty")
    @Size(min=3, message = "Username must be at least 3 charcters long")
    @Indexed(unique = true)
    @NonNull
    private String userName;
    @NonNull
    @NotBlank(message = "Password cannot be empty")
    @Size(min=3, message = "Password must be at least 3 charcters long")
    private String password;
    @DBRef
    private List<JournalEntry> journalEntries = new ArrayList<>();
    private List<String> roles;
}
