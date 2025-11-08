package searchengine.services.indexing.crawler;

import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;
import searchengine.config.CrawlerSettings;
import searchengine.exceptions.IndexingServiceException;
import searchengine.model.Page;
import searchengine.common.task.TaskSleepBlocker;
import searchengine.common.text.URLUtils;

import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

@Component
@RequiredArgsConstructor
public class PageFetcher {
    private final CrawlerSettings crawlerSettings;

    public Page fetchPage(String url) throws IndexingServiceException {
        return fetchPageWithRetry(url, 0);
    }

    private Page fetchPageWithRetry(String url, int reconnectCounter) {
        int timeoutMillis = (reconnectCounter > 0)
                ? crawlerSettings.getReconnectAttemptTimeoutMillis()
                : crawlerSettings.getRandomConnectionTimeoutMillis();

        timeout(timeoutMillis);

        try {
            Connection connection = Jsoup.connect(url)
                    .headers(crawlerSettings.getRotatingCrawlerHeaders())
                    .timeout(crawlerSettings.getPageLoadTimeLimitMillis());
            Connection.Response response = connection.execute();

            if (!response.contentType().contains("text/html;"))
                throw new IndexingServiceException(url, "Неподходящий тип контента");

            return preparePage(url, response.statusCode(), connection.get().html());
        } catch (SSLHandshakeException e) {
            throw new IndexingServiceException(url, "Ошибка SSL. Не удалось установить безопасное соединение");
        } catch (HttpStatusException e) {
            return preparePage(e.getUrl(), e.getStatusCode(), "");
        } catch (SocketException | SocketTimeoutException | UnknownHostException e) {
            if (reconnectCounter > crawlerSettings.getReconnectAttemptsMax()) {
                throw new IndexingServiceException(url, "Не удалось установить соединение");
            }
            return fetchPageWithRetry(url, ++reconnectCounter);
        } catch (IOException | IllegalArgumentException e) {
            throw new IndexingServiceException(url, "Ошибка при загрузке страницы: некорректный URL или недоступный ресурс");
        }
    }

    private void timeout(int millis) {
        TaskSleepBlocker.safeSleep(millis);
    }

    private Page preparePage(String url, int statusCode, String content) {
        Page page = new Page();
        page.setPath(URLUtils.toRelativePath(url));
        page.setCode(statusCode);
        page.setContent(content);

        return page;
    }
}
