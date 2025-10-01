package net.hemjournalApp.entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "database_sequences")
@Data
@AllArgsConstructor
public class DatabaseSequence {
    @Id
    private String id;
    private long seq;

}
