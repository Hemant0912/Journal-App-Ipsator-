package net.hemjournalApp.repository;

import net.hemjournalApp.entity.JournalEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

// extend mongo to perform oper and give where we want to perform along with id type
public interface JournalEntryRepo extends MongoRepository<JournalEntry,String>{
    // DB-level paging by referenced user id
    Page<JournalEntry> findByUserEntity_Id(String userId, Pageable pageable);

    // Native Mongo query
    @Query("{ '$or': [ { 'title': { $regex: ?0, $options: 'i' } }, { 'content': { $regex: ?0, $options: 'i' } } ] }")
    Page<JournalEntry> searchByTitleOrContent(String regex, Pageable pageable);
}
// controller will call service
// service will call repository
// C-S-R