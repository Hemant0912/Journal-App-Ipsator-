package net.hemjournalApp.service;
import net.hemjournalApp.dto.PageResponse;
import net.hemjournalApp.entity.JournalEntry;
import net.hemjournalApp.entity.UserEntity;
import net.hemjournalApp.repository.JournalEntryRepo;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
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

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private SequenceGeneratorService sequenceGeneratorService;

    @Transactional
    public void saveEntry(JournalEntry journalEntry, String userName) {
        UserEntity userEntity = userService.findByUserName(userName);

        if (journalEntryRepo.existsByTitleAndContent(journalEntry.getTitle(), journalEntry.getContent())) {
            throw new RuntimeException("Duplicate entry: change title or content");
        }

        if (journalEntryRepo.existsByTitle(journalEntry.getTitle())) {
            throw new RuntimeException("Title already exists");
        }

        long nextSeq = sequenceGeneratorService.getNextSequence("journal_entry_seq");
        journalEntry.setId(String.valueOf(nextSeq));
        journalEntry.setDate(LocalDateTime.now());
        journalEntry.setUserEntity(userEntity);

        JournalEntry saved = journalEntryRepo.save(journalEntry);
        userEntity.getJournalEntries().add(saved);
        userService.saveUser(userEntity);
    }

    @Transactional
    public JournalEntry updateEntryByStringId(String id, JournalEntry newEntry, String userName) {
        UserEntity userEntity = userService.findByUserName(userName);
        boolean isAdmin = userEntity.getPermissions().contains("admin:access");

        JournalEntry existing = journalEntryRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Journal not found"));

        boolean isOwner = userEntity.getJournalEntries().stream()
                .anyMatch(entry -> entry.getId().equals(existing.getId()));

        if (!isAdmin && !isOwner) {
            throw new RuntimeException("You cannot update someone else’s journal");
        }

        if (newEntry.getContent() != null) existing.setContent(newEntry.getContent());
        existing.setDate(LocalDateTime.now());

        return journalEntryRepo.save(existing);
    }

    // get call for admin
    public PageResponse<JournalEntry> getAllForAdmin(int page, int size, String sortBy, String direction, String search) {
        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<JournalEntry> pageResult;
        if (search != null && !search.isBlank()) {
            pageResult = journalEntryRepo.searchByTitleOrContent(search, pageable);
        } else {
            pageResult = journalEntryRepo.findAll(pageable);
        }

        return new PageResponse<>(
                pageResult.getContent(),
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.isLast()
        );
    }

   // get call
    public PageResponse<JournalEntry> getAllForUser(String userId, int page, int size, String sortBy, String direction, String search) {
        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        if (search == null || search.isBlank()) {
            Page<JournalEntry> pageResult = journalEntryRepo.findByUserEntity_Id(userId, pageable);
            return new PageResponse<>(
                    pageResult.getContent(),
                    pageResult.getNumber(),
                    pageResult.getSize(),
                    pageResult.getTotalElements(),
                    pageResult.getTotalPages(),
                    pageResult.isLast()
            );
        }

        Query q = new Query();
        try {
            q.addCriteria(Criteria.where("userEntity.$id").is(new ObjectId(userId)));
        } catch (IllegalArgumentException e) {
            q.addCriteria(Criteria.where("userEntity").is(userService.findById(userId)));
        }
        q.addCriteria(new Criteria().orOperator(
                Criteria.where("title").regex(search, "i"),
                Criteria.where("content").regex(search, "i")
        ));

        long total = mongoTemplate.count(q, JournalEntry.class);
        q.with(pageable);
        List<JournalEntry> content = mongoTemplate.find(q, JournalEntry.class);

        return new PageResponse<>(content, page, size, total, (int) Math.ceil((double) total / size), content.size() < size);
    }

    @Transactional
    public boolean deleteById(String id, String userName) {
        UserEntity userEntity = userService.findByUserName(userName);
        boolean isAdmin = userEntity.getPermissions().contains("admin:access");

        if (isAdmin) {
            journalEntryRepo.deleteById(id);
            return true;
        }

        boolean removed = userEntity.getJournalEntries().removeIf(x -> x.getId().equals(id));
        if (removed) {
            userService.saveUser(userEntity);
            journalEntryRepo.deleteById(id);
        }
        return removed;
    }

    public JournalEntry findById(ObjectId id) {
        return journalEntryRepo.findById(id.toString())
                .orElseThrow(() -> new RuntimeException("Journal entry not found"));
    }

    @Transactional
    public JournalEntry updateTitle(ObjectId id, String newTitle, String userName) {
        UserEntity userEntity = userService.findByUserName(userName);
        boolean isAdmin = userEntity.getPermissions().contains("admin:access");

        JournalEntry existing = journalEntryRepo.findById(id.toString())
                .orElseThrow(() -> new RuntimeException("Journal not found"));

        boolean isOwner = userEntity.getJournalEntries().stream()
                .anyMatch(entry -> entry.getId().equals(existing.getId()));

        if (!isAdmin && !isOwner) {
            throw new RuntimeException("You cannot update title for someone else’s journal");
        }

        if (journalEntryRepo.existsByTitle(newTitle)) {
            throw new RuntimeException("Title already exists");
        }

        existing.setTitle(newTitle);
        return journalEntryRepo.save(existing);
    }
}
