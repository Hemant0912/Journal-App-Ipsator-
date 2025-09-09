package net.hemjournalApp.service;
import net.hemjournalApp.entity.JournalEntry;
import net.hemjournalApp.entity.UserEntity;
import net.hemjournalApp.repository.JournalEntryRepo;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class JournalEntryService {

    @Autowired
    private JournalEntryRepo journalEntryRepo;

    @Autowired
    private UserService userService;

    @Transactional
    public void saveEntry(JournalEntry journalEntry, String userName) {
        try {
            UserEntity userEntity = userService.findByUserName(userName);
            journalEntry.setDate(LocalDateTime.now());
            journalEntry.setUserEntity(userEntity);
            JournalEntry saved = journalEntryRepo.save(journalEntry);

            // Link journal entry with user
            userEntity.getJournalEntries().add(saved);
            userService.saveUser(userEntity);
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while saving entry", e);
        }
    }

    public List<JournalEntry> getAll() {
        return journalEntryRepo.findAll();
    }

    public Optional<JournalEntry> findById(ObjectId id) {
        return journalEntryRepo.findById(id);
    }

    @Transactional
    public boolean deleteById(String id, String userName) {
        try {
            UserEntity userEntity = userService.findByUserName(userName);
            boolean isAdmin = userEntity.getPermissions().contains("admin:access");

            if (isAdmin) {
                // Admin can delete anyone’s entry
                journalEntryRepo.deleteById(id);
                return true;
            }

            boolean removed = userEntity.getJournalEntries().removeIf(x -> x.getId().equals(id));
            if (removed) {
                userService.saveUser(userEntity);
                journalEntryRepo.deleteById(id);
            }
            return removed;

        } catch (Exception e) {
            throw new RuntimeException("An error occurred while deleting the entry", e);
        }
    }

    @Transactional
    public JournalEntry updateEntry(ObjectId id, JournalEntry newEntry, String userName) {
        UserEntity userEntity = userService.findByUserName(userName);
        boolean isAdmin = userEntity.getPermissions().contains("admin:access");

        Optional<JournalEntry> optionalEntry = journalEntryRepo.findById(id);

        if (!optionalEntry.isPresent()) {
            throw new RuntimeException("Journal not found");
        }
        JournalEntry existing = optionalEntry.get();
        boolean isOwner = userEntity.getJournalEntries().stream()
                .anyMatch(entry -> entry.getId().equals(existing.getId()));
        if (!isAdmin && !isOwner) {
            throw new RuntimeException("You cannot update someone else’s journal");
        }
        if (newEntry.getTitle() != null) {
            existing.setTitle(newEntry.getTitle());
        }
        if (newEntry.getContent() != null) {
            existing.setContent(newEntry.getContent());
        }
        return journalEntryRepo.save(existing);
    }

}
