package ru.ra;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.Arrays;

@Entity
public class Post {

    private static final int TITLE_MAX_LENGTH = 512;
    private static final int LINK_MAX_LENGTH = 512;

    private static final Logger log = LoggerFactory.getLogger(Post.class);

    @Id
    private String id;

    @ManyToOne
    private FeedLink feed;

    @Column(length = TITLE_MAX_LENGTH)
    private String title;

    @Column(length = LINK_MAX_LENGTH)
    private String link;

    @Column(columnDefinition = "boolean default false")
    private boolean read;

    private Post(FeedLink feed, String id, String title, String link) {
        this.id = id;
        this.feed = feed;
        this.title = title;
        this.link = link;
    }

    public Post() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public FeedLink getFeed() {
        return feed;
    }

    public void setFeed(FeedLink feed) {
        this.feed = feed;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    @Override
    public String toString() {
        return "Post{" +
                "id='" + id + '\'' +
                ", feed=" + feed +
                ", title='" + title + '\'' +
                ", link='" + link + '\'' +
                ", read=" + read +
                '}';
    }

    @Nullable
    public static Post create(FeedLink feed, String id, String title, String link) {
        if (title == null || title.length() > TITLE_MAX_LENGTH) {
            log.warn("Attempted to create post with too long or null title: " + Arrays.asList(feed, id, title, link));
            return null;
        }
        if (link == null || link.length() > LINK_MAX_LENGTH) {
            log.warn("Attempted to create post with too long or null link: " + Arrays.asList(feed, id, title, link));
            return null;
        }
        return new Post(feed, id, title, link);
    }
}
