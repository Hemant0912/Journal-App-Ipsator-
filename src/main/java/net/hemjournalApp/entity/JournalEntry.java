package net.hemjournalApp.entity;
import com.fasterxml.jackson.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

// this will tell sb that this is mapped with mongodb collections entity
@Document(collection = "journal_entries")
@Data
@NoArgsConstructor(force = true)
public class JournalEntry {
    @Id
    private String id;
    @NonNull
    private String title;
    private String content;
    private LocalDateTime date;
    @DBRef
    @JsonBackReference
    private UserEntity userEntity;
}
