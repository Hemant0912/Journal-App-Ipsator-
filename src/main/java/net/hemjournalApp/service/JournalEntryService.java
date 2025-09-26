package net.hemjournalApp.service;
import net.hemjournalApp.entity.JournalEntry;
import net.hemjournalApp.entity.UserEntity;
import net.hemjournalApp.repository.JournalEntryRepo;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
    private EmailService emailService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Transactional
    @CacheEvict(value = "journalEntries", allEntries = true)
    public void saveEntry(JournalEntry journalEntry, String userName) {
        UserEntity userEntity = userService.findByUserName(userName);

        journalEntry.setDate(LocalDateTime.now());
        journalEntry.setUserEntity(userEntity);

        JournalEntry saved = journalEntryRepo.save(journalEntry);

        userEntity.getJournalEntries().add(saved);
        userService.saveUser(userEntity);

       // circuit breaker point
        try {
            emailService.sendEntryCreatedNotification(userEntity.getUserName(), saved.getTitle());
        } catch (Exception ignored) { }
    }

    @Transactional
    @CacheEvict(value = "journalEntries", allEntries = true)
    public boolean deleteById(String id, String userName) {
        UserEntity userEntity = userService.findByUserName(userName);
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
    @CacheEvict(value = "journalEntries", allEntries = true)
    public JournalEntry updateEntry(ObjectId id, JournalEntry newEntry, String userName) {
        UserEntity userEntity = userService.findByUserName(userName);
        boolean isAdmin = userEntity.getPermissions().contains("admin:access");

        JournalEntry existing = journalEntryRepo.findById(id.toString())
                .orElseThrow(() -> new RuntimeException("Journal not found"));

        boolean isOwner = userEntity.getJournalEntries().stream()
                .anyMatch(entry -> entry.getId().equals(existing.getId()));

        if (!isAdmin && !isOwner) {
            throw new RuntimeException("You cannot update someone else’s journal");
        }

        if (newEntry.getTitle() != null) existing.setTitle(newEntry.getTitle());
        if (newEntry.getContent() != null) existing.setContent(newEntry.getContent());

        return journalEntryRepo.save(existing);
    }

    @Cacheable(value = "journalEntries", key = "'admin:' + #page + ':' + #size + ':' + #sortBy + ':' + #direction + ':' + #search")
    public Page<JournalEntry> getAllForAdmin(int page, int size, String sortBy, String direction, String search) {
        Sort sort = "desc".equalsIgnoreCase(direction) ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        if (search != null && !search.isBlank()) {
            return journalEntryRepo.searchByTitleOrContent(search, pageable);
        } else {
            return journalEntryRepo.findAll(pageable);
        }
    }

    @Cacheable(value = "journalEntries", key = "'user:' + #userId + ':' + #page + ':' + #size + ':' + #sortBy + ':' + #direction + ':' + #search")
    public Page<JournalEntry> getAllForUser(String userId, int page, int size, String sortBy, String direction, String search) {
        Sort sort = "desc".equalsIgnoreCase(direction) ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        if (search == null || search.isBlank()) {
            return journalEntryRepo.findByUserEntity_Id(userId, pageable);
        }


        Query q = new Query();
        try {
            q.addCriteria(Criteria.where("userEntity.$id").is(new org.bson.types.ObjectId(userId)));
        } catch (IllegalArgumentException e) {

            q.addCriteria(Criteria.where("userEntity").is(userService.findById(userId)));
        }
        q.addCriteria(new Criteria().orOperator(
                Criteria.where("title").regex(search, "i"),
                Criteria.where("content").regex(search, "i")
        ));

        // total count for pagingg
        long total = mongoTemplate.count(q, JournalEntry.class);
        q.with(pageable);
        List<JournalEntry> content = mongoTemplate.find(q, JournalEntry.class);

        return new PageImpl<>(content, pageable, total);
    }

    public List<JournalEntry> getAll() {
        return journalEntryRepo.findAll();
    }

    public JournalEntry findById(ObjectId id) {
        return journalEntryRepo.findById(id.toString())
                .orElseThrow(() -> new RuntimeException("Journal entry not found"));
    }
}
