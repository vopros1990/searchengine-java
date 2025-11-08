package searchengine.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import searchengine.model.IndexingStatus;
import searchengine.model.Site;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "indexing-settings")
public class SitesList {
    private List<SiteConfig> sites;

    @Bean
    @Scope("prototype")
    public List<Site> indexingSites() {
        if (sites == null) return List.of();

        return sites.stream().map(
                siteConfig -> {
                    Site site = new Site();
                    site.setUrl(siteConfig.getUrl());
                    site.setName(siteConfig.getName());
                    site.setStatusTime(LocalDateTime.now());
                    return site;
                }).toList();
    }
}


