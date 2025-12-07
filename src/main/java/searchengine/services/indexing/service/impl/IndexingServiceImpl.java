package searchengine.services.indexing.service.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.model.IndexingStatus;
import searchengine.exceptions.IndexingException;
import searchengine.model.Site;
import searchengine.repositories.SiteRepository;
import searchengine.services.data.DatabaseCleanupService;
import searchengine.services.indexing.service.IndexingService;
import searchengine.services.indexing.tasks.IndexingTaskFactory;
import searchengine.services.indexing.tasks.SinglePageIndexingTask;
import searchengine.services.indexing.tasks.SiteIndexingTask;
import searchengine.common.task.TaskExecutor;
import searchengine.common.text.URLUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class IndexingServiceImpl implements IndexingService {
    private final IndexingTaskFactory indexingTaskFactory;
    private final SiteRepository siteRepository;
    private final DatabaseCleanupService databaseCleanupService;
    private final TaskExecutor taskExecutor;
    private final List<SiteIndexingTask> taskList = new ArrayList<>();
    @NonNull
    private List<Site> indexingSites;
    private int currentIndexingSitesCount = 0;
    private boolean isIndexingStarted = false;
    private boolean isIndexingStopping = false;

    @Override
    public void startIndexing() throws IndexingException {
        if (siteRepository.existsByStatus(IndexingStatus.INDEXING) && isIndexingStarted)
            throw new IndexingException("Индексация уже запущена");

        if (indexingSites.isEmpty())
            throw new IndexingException("Список сайтов для индексации пуст");

        taskExecutor.runAsync(() ->
                indexingSites.forEach(siteConfig -> {
                    Site existingSiteInDatabase = siteRepository.findByUrlContaining(siteConfig.getUrl());

                    if(existingSiteInDatabase != null)
                        databaseCleanupService.clearIndexingData(existingSiteInDatabase);

                    siteConfig.setStatus(IndexingStatus.INDEXING);
                    siteConfig.setId(null);
                    siteConfig.setLastError(null);
                    Site indexingSite = siteRepository.save(siteConfig);
                    currentIndexingSitesCount++;

                    processSiteIndexing(indexingSite);
                })
        );

        isIndexingStarted = true;
    }

    @Override
    public void stopIndexing() throws IndexingException {
        if (!siteRepository.existsByStatus(IndexingStatus.INDEXING) || !isIndexingStarted)
            throw new IndexingException("Индексация не запущена");

        if (isIndexingStopping)
            throw new IndexingException("Индексация в процессе остановки");

        isIndexingStopping = true;

        taskExecutor.runAsync(() -> {
            processIndexingCancellation();

            siteRepository.findByStatus(IndexingStatus.INDEXING).forEach(
                    site -> {
                        site.setStatus(IndexingStatus.FAILED);
                        site.setLastError("Индексация отменена пользователем");
                        siteRepository.save(site);
                    });

            isIndexingStopping = false;
        });
    }

    @Override
    public void indexPage(String url) throws IndexingException {
        String baseUrl = URLUtils.extractBaseUrl(url);
        Site indexingSite = siteRepository.findByUrlContaining(baseUrl);

        if (indexingSite == null) {
            indexingSite = indexingSites.stream()
                    .filter(siteConfig -> siteConfig.getUrl().contains((baseUrl)))
                    .findFirst()
                    .orElseThrow(() ->
                            new IndexingException(
                                    "Данная страница находится за пределами сайтов, указанных в конфигурационном файле"
                            )
                    );

            indexingSite.setStatus(IndexingStatus.INDEXED);
            indexingSite = siteRepository.save(indexingSite);
        }

        SinglePageIndexingTask task = indexingTaskFactory.buildSinglePageIndexingTask(indexingSite, url);

        taskExecutor.submitAsync(task,
                (status) -> taskExecutor.shutdown(),
                (exception) -> log.info(exception.getMessage())
        );
    }

    private void processSiteIndexing(Site indexingSite) {
        SiteIndexingTask task = indexingTaskFactory.buildSiteIndexingTask(indexingSite);
        taskList.add(task);

        taskExecutor.submitAsync(
                task,
                (status) -> {
                    siteRepository.updateStatusAndStatusTime(indexingSite.getId(), status.toString());
                    if (--currentIndexingSitesCount == 0)
                        taskExecutor.shutdown();
                },
                (exception) -> log.info(Arrays.toString(exception.getStackTrace())));

    }

    private void processIndexingCancellation() {
        taskList.forEach(SiteIndexingTask::cancelAll);
        taskList.clear();
        taskExecutor.shutdown();
        currentIndexingSitesCount = 0;
        isIndexingStarted = false;
    }
}
