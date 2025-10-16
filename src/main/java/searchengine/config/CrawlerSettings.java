package searchengine.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "crawler-settings")
public class CrawlerSettings {
    @Setter
    private List<Map<String, String>> headers;

    @Setter
    private long headersRotationIntervalSeconds;

    @Setter
    private int connectionTimeoutMillisMin;

    @Setter
    private int connectionTimeoutMillisMax;

    @Setter
    @Getter
    private int pageLoadTimeLimitMillis;

    @Setter
    @Getter
    private int storageBatchLimit;

    @Setter
    @Getter
    private int reconnectAttemptsMax;

    @Setter
    @Getter
    private int reconnectAttemptTimeoutMillis;

    private Instant headerRotationStartTime = Instant.now();
    private Iterator<Map<String, String>> iterator;
    private Map<String, String> currentHeaders;

    public synchronized Map<String, String> getRotatingCrawlerHeaders() {
        if (iterator == null) {
            headerRotationStartTime = Instant.now();
            iterator = headers.iterator();
            currentHeaders = iterator.next();
        }

        Duration elapsedTime = Duration.between(headerRotationStartTime, Instant.now());

        if (elapsedTime.compareTo(Duration.ofSeconds(headersRotationIntervalSeconds)) < 0) {
            return currentHeaders;
        }

        if (!iterator.hasNext())
            iterator = headers.iterator();

        headerRotationStartTime = Instant.now();
        return iterator.next();
    }

    public int getRandomConnectionTimeoutMillis() {
        int delta = connectionTimeoutMillisMax - connectionTimeoutMillisMin;
        return (int) (Math.random() * delta) + connectionTimeoutMillisMin;
    }
}
