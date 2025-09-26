package net.hemjournalApp.config;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.concurrent.TimeUnit;


@Configuration
public class MongoConfig {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Value("${mongodb.pool.maxSize:10}")
    private int maxPoolSize;

    @Value("${mongodb.pool.minSize:5}")
    private int minPoolSize;

    @Bean
    public MongoClient mongoClient() {
        ConnectionString cs = new ConnectionString(mongoUri);

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(cs)
                .applyToConnectionPoolSettings(builder ->
                        builder.maxSize(maxPoolSize)
                                .minSize(minPoolSize)
                                .maxWaitTime(120, TimeUnit.SECONDS)
                )
                .build();

        return MongoClients.create(settings);
    }
}
