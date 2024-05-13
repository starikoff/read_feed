package ru.ra;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.ParsingFeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class Loader {

    private static final Logger logger = LoggerFactory.getLogger(Loader.class);

    @Value("10")
    private int timeoutSeconds;

    @Autowired
    private FeedService feedService;

    private final CloseableHttpClient client =
            HttpClients.custom()
                    .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                            "AppleWebKit/537.36 (KHTML, like Gecko) " +
                            "Chrome/74.0.3729.169 " +
                            "Safari/537.36")
                    .build();

    public void iteration() {
        for (FeedLink feed : feedService.getFeeds()) {
            try {
                logger.trace("Loading {}", feed);
                load(feed);
            } catch (Exception e) {
                logger.error("error while loading posts for {}", feed, e);
            }
        }
    }

    private void load(FeedLink feedLink) throws IOException, FeedException {
        SyndFeedInput feedInput = new SyndFeedInput();
        SyndFeed feed;
        int timeoutMs = timeoutSeconds * 1000;
        RequestConfig requestConfig = RequestConfig.custom()
                                                   .setConnectionRequestTimeout(timeoutMs)
                                                   .setConnectTimeout(timeoutMs)
                                                   .setSocketTimeout(timeoutMs)
                                                   .build();
        HttpGet request = new HttpGet(feedLink.getUrl());
        request.setConfig(requestConfig);

        String xml = "";
        try (CloseableHttpResponse response = client.execute(request);
             InputStream stream = response.getEntity().getContent()) {
            xml = IOUtils.toString(stream, StandardCharsets.UTF_8);
            ByteArrayInputStream bais = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
            XmlReader xmlReader = new XmlReader(bais);
            feed = feedInput.build(xmlReader);
        } catch (ParsingFeedException pfe) {
            logger.error("{}: {}", feedLink.getUrl(), xml, pfe);
            return;
        }
        Set<String> currentIds = new HashSet<>();

        List<SyndEntry> entries = feed.getEntries();
        entries.sort(Comparator.nullsFirst(Comparator.comparing(this::getDate)));

        for (SyndEntry entry : entries) {
            String link = entry.getLink();
            String id = entry.getUri();
            if (id == null) {
                id = link;
            }
            String title = entry.getTitle();
            if (title == null) {
                title ="<no title>";
            }

            Post post = Post.create(feedLink, id, title, link);
            currentIds.add(id);
            if (post != null && feedService.add(post)) {
                logger.debug("added post {}", post);
            }
        }
        Iterable<Post> knownPosts = feedService.getAllPosts(feedLink.getId());
        List<Post> obsoletePosts = new ArrayList<>();
        for (Post knownPost : knownPosts) {
            if (knownPost.isRead() && !currentIds.contains(knownPost.getId())) {
                obsoletePosts.add(knownPost);
            }
        }
        if (!obsoletePosts.isEmpty()) {
            logger.debug("going to remove: feed {}, posts {}", feedLink, obsoletePosts);
            feedService.deletePosts(feedLink.getId(), obsoletePosts);
        }
    }

    private Date getDate(SyndEntry entry) {
        Date date = entry.getPublishedDate();
        if (date == null) {
            date = entry.getUpdatedDate();
        }
        return date;
    }
}
