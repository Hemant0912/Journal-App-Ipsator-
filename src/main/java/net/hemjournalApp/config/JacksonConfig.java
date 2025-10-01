package net.hemjournalApp.config;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Configuration
public class JacksonConfig {

    private static final DateTimeFormatter ZDT = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm:ss z");

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        JavaTimeModule jtm = new JavaTimeModule();

        // Register serializer for ZonedDateTime
        jtm.addSerializer(ZonedDateTime.class, new ZonedDateTimeSerializer(ZDT));

        mapper.registerModule(jtm);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // ISO turned off
        return mapper;
    }
}
