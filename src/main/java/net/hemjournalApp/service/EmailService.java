package net.hemjournalApp.service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @CircuitBreaker(name = "emailService", fallbackMethod = "emailFallback")
    public void sendEntryCreatedNotification(String userName, String title) {
        log.info("EmailService: sending notification to {} about '{}'", userName, title);
    }

    public void emailFallback(String userName, String title, Throwable t) {
        log.warn("Email fallback for {} / {} -> {}", userName, title,
                t == null ? "null" : t.getMessage());
    }
}
