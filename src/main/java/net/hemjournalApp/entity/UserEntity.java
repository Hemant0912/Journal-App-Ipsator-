package net.hemjournalApp.entity;
import com.fasterxml.jackson.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import net.hemjournalApp.repository.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
@NoArgsConstructor(force = true)
@Document(collection = "users")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class UserEntity {
    @Id
    private String id;

    @NotBlank(message = "Username cannot be empty")
    @Size(min = 3, message = "Username must be at least 3 charcters long")
    @Indexed(unique = true)
    private String userName;
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 20, message = "Password must be 8–20 characters long")
    @Pattern(
            // using regex for validation
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,20}$",
            message = "Password must contain at least one uppercase, one lowercase, one digit, one special character, and no spaces"
    )
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @DBRef
    @JsonManagedReference
    private List<JournalEntry> journalEntries = new ArrayList<>();

    @JsonIgnoreProperties("userEntity")
    private List<String> permissions = new ArrayList<>();

    public String getUserName() {
      return this.userName;
    }
    @JsonIgnore
    public String getPassword() {
        return password;
    }


}

