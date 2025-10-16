package searchengine.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.CrawlerSettings;
import searchengine.model.Site;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.CrawlerService;
import searchengine.utils.TaskExecutorService;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CrawlerServiceImpl implements CrawlerService {
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final CrawlerSettings crawlerSettings;
    private final TaskExecutorService threadsPool;
    private final List<CrawlerServiceRecursiveAction> taskList = new ArrayList<>();

    @Override
    public void crawl(Site site) {
        taskList.add(
                new CrawlerServiceRecursiveAction(
                        site, siteRepository, pageRepository, crawlerSettings
                )
        );
        threadsPool.execute(taskList.get(taskList.size() - 1));
    }

    @Override
    public void stopCrawling() {
        taskList.forEach(CrawlerServiceRecursiveAction::cancelAll);
        threadsPool.shutdown();
        taskList.clear();
    }
}