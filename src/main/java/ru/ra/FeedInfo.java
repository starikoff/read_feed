package ru.ra;

public class FeedInfo {
    public final FeedLink feedLink;

    public final int unread;

    public FeedInfo(FeedLink feedLink, int unread) {
        this.feedLink = feedLink;
        this.unread = unread;
    }
}
