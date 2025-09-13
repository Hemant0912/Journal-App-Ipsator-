package net.hemjournalApp.service;

import net.hemjournalApp.entity.JournalEntry;
import net.hemjournalApp.entity.UserEntity;
import net.hemjournalApp.repository.JournalEntryRepo;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class JournalEntryService {

    @Autowired
    private JournalEntryRepo journalEntryRepo;

    @Autowired
    private UserService userService;

    @Transactional
    public void saveEntry(JournalEntry journalEntry, String userName) {
        UserEntity userEntity = userService.findByUserName(userName); // plain UserEntity

        journalEntry.setDate(LocalDateTime.now());
        journalEntry.setUserEntity(userEntity);

        JournalEntry saved = journalEntryRepo.save(journalEntry);

        // link journal entry with user
        userEntity.getJournalEntries().add(saved);
        userService.saveUser(userEntity);
    }

    @Transactional
    public boolean deleteById(String id, String userName) {
        UserEntity userEntity = userService.findByUserName(userName); // plain UserEntity
        boolean isAdmin = userEntity.getPermissions().contains("admin:access");

        if (isAdmin) {
            journalEntryRepo.deleteById(id);
            return true;
        }

        boolean removed = userEntity.getJournalEntries()
                .removeIf(x -> x.getId().equals(id));
        if (removed) {
            userService.saveUser(userEntity);
            journalEntryRepo.deleteById(id);
        }
        return removed;
    }

    @Transactional
    public JournalEntry updateEntry(ObjectId id, JournalEntry newEntry, String userName) {
        UserEntity userEntity = userService.findByUserName(userName); // plain UserEntity
        boolean isAdmin = userEntity.getPermissions().contains("admin:access");

        JournalEntry existing = journalEntryRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Journal not found"));

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

    public List<JournalEntry> getAll() {
        return journalEntryRepo.findAll();
    }

    public JournalEntry findById(ObjectId id) {
        return journalEntryRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Journal entry not found"));
    }
}
