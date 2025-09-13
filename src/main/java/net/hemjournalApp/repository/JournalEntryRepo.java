package net.hemjournalApp.repository;

import net.hemjournalApp.entity.JournalEntry;
import org.springframework.data.mongodb.repository.MongoRepository;
// extend mongo to perform oper and give where we want to perform along with id type
public interface JournalEntryRepo extends MongoRepository<JournalEntry,Object>{


}


// controller will call service
// service will call repository
// C-S-R