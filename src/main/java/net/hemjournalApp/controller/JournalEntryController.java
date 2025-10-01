package net.hemjournalApp.controller;

import net.hemjournalApp.dto.PageResponse;
import net.hemjournalApp.entity.JournalEntry;
import net.hemjournalApp.entity.UserEntity;
import net.hemjournalApp.service.JournalEntryService;
import net.hemjournalApp.service.UserService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/journal")
public class JournalEntryController {

    @Autowired
    private JournalEntryService journalEntryService;

    @Autowired
    private UserService userService;

    // get all journal entry
    @GetMapping
    public ResponseEntity<?> getAllJournalEntriesOfUser(
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search) {

        String userName = principal.getName();
        UserEntity userEntity = userService.findByUserName(userName);

        boolean isAdmin = userEntity.getPermissions().contains("admin:access");

        PageResponse<JournalEntry> response;
        if (isAdmin) {
            response = journalEntryService.getAllForAdmin(page, size, sortBy, sortDir, search);
        } else {
            response = journalEntryService.getAllForUser(userEntity.getId(), page, size, sortBy, sortDir, search);
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // create journal entry
    @PostMapping
    public ResponseEntity<?> createEntry(@RequestBody JournalEntry myEntry, Principal principal) {
        try {
            String userName = principal.getName();
            journalEntryService.saveEntry(myEntry, userName);
            return new ResponseEntity<>(myEntry, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // get journal entry
    @GetMapping("/id/{myId}")
    public ResponseEntity<?> getJournalEntryById(@PathVariable String myId, Principal principal) {
        String userName = principal.getName();
        UserEntity userEntity = userService.findByUserName(userName);
        boolean isAdmin = userEntity.getPermissions().contains("admin:access");
        JournalEntry journalEntry = journalEntryService.findById(new ObjectId(myId));

        if (isAdmin || userEntity.getJournalEntries().stream().anyMatch(x -> x.getId().equals(myId))) {
            return new ResponseEntity<>(journalEntry, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    // delete entry
    @DeleteMapping("/id/{myId}")
    public ResponseEntity<?> deleteJournalEntryById(@PathVariable String myId, Principal principal) {
        String userName = principal.getName();
        boolean removed = journalEntryService.deleteById(myId, userName);
        if (removed) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>("You are not allowed to delete this entry", HttpStatus.FORBIDDEN);
    }

    // update journal entry content
    @PutMapping("/id/{myId}")
    public ResponseEntity<?> updateJournalById(
            @PathVariable("myId") String id,
            @RequestBody JournalEntry newEntry,
            Principal principal) {

        String userName = principal.getName();
        try {
            JournalEntry updated = journalEntryService.updateEntryByStringId(id, newEntry, userName);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
        }
    }

    @PatchMapping("/id/{myId}/update-title")
    public ResponseEntity<?> updateTitle(
            @PathVariable("myId") String id,
            @RequestBody Map<String, String> payload,
            Principal principal) {

        String userName = principal.getName();
        String newTitle = payload.get("title");
        if (newTitle == null || newTitle.isBlank()) {
            return ResponseEntity.badRequest().body("Title cannot be empty");
        }
        try {
            JournalEntry updated = journalEntryService.updateTitle(new ObjectId(id), newTitle, userName);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }
}
