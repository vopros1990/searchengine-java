package searchengine.repositories;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import searchengine.model.Page;
import searchengine.common.task.TaskSleepBlocker;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class PageBatchJdbcRepository {
    private final JdbcTemplate jdbcTemplate;

    public void savePageBatch(List<Page> pages) {
        Long start = System.currentTimeMillis();
        StringBuilder sqlBuilder = new StringBuilder("INSERT INTO page (path, site_id, code, content) VALUES");

        pages.forEach(page -> {
            sqlBuilder.append(
                    String.format("('%s', %d, %d, '%s'), ",
                            escapeSingleQuotes(page.getPath()),
                            page.getSite().getId(),
                            page.getCode(),
                            escapeSingleQuotes(page.getContent())));
        });

        sqlBuilder.delete(sqlBuilder.length()-2, sqlBuilder.length());
        sqlBuilder.append(" ON DUPLICATE KEY UPDATE code = VALUES(code);");

        executeBatchUpdateQuery(sqlBuilder.toString());
        log.info("BATCH OF {} PAGES SAVED IN {} ms", pages.size(), (System.currentTimeMillis() - start));
    }

    private String escapeSingleQuotes(String text) {
        return text.replaceAll("'", "''");
    }

    private void executeBatchUpdateQuery(String sqlQuery) {
        int queryRestartMaxAttempts = 5;
        int queryRestartAttemptTimeoutMillis = 50;

        for (int i = 0; i < queryRestartMaxAttempts; i++) {
            try {
                jdbcTemplate.batchUpdate(sqlQuery);
                break;
            } catch (Exception e) {
                log.info(e.getCause().toString());
                TaskSleepBlocker.safeSleep(queryRestartAttemptTimeoutMillis);
            }
        }
    }
}
