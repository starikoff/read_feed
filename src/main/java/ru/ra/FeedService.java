package ru.ra;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class FeedService {

    @Autowired
    private FeedLinkRepo feedLinkRepo;

    @Autowired
    private PostRepo postRepo;

    private final Map<Long, Map<String, Post>> feedsPostsMap = new HashMap<>();

    private final Map<Long, FeedLink> feedsMap = new LinkedHashMap<>();

    @PostConstruct
    public void init() {
        for (FeedLink feedLink : feedLinkRepo.findAll()) {
            feedsPostsMap.put(feedLink.getId(), new LinkedHashMap<>());
            feedsMap.put(feedLink.getId(), feedLink);
        }
        List<Post> posts = postRepo.findAll();
        for (Post post : posts) {
            Map<String, Post> postsMap = feedsPostsMap.get(post.getFeed().getId());
            postsMap.put(post.getId(), post);
        }
    }

    public void addFeed(FeedLink feedLink) {
        feedLink = feedLinkRepo.save(feedLink);
        feedsPostsMap.put(feedLink.getId(), new LinkedHashMap<>());
        feedsMap.put(feedLink.getId(), feedLink);
    }

    public void deleteFeed(Long feedId) {
        feedLinkRepo.deleteById(feedId);
        feedsMap.remove(feedId);
        feedsPostsMap.remove(feedId);
    }

    public boolean add(Post post) {
        Long feedId = post.getFeed().getId();
        Map<String, Post> feedPosts = feedsPostsMap.get(feedId);
        boolean inserted = false;
        if (!feedPosts.containsKey(post.getId())) {
            inserted = (feedPosts.put(post.getId(), post) == null);
        }
        if (inserted) {
            postRepo.save(post);
        }
        return inserted;
    }

    public List<FeedInfo> getFeedInfos() {
        List<FeedInfo> result = new ArrayList<>(feedsMap.size());
        for (FeedLink feedLink : feedsMap.values()) {
            Long feedLinkId = feedLink.getId();
            Map<String, Post> postsMap = feedsPostsMap.get(feedLinkId);
            int size = (postsMap == null ? 0 : unread(postsMap));
            FeedInfo feedInfo = new FeedInfo(feedLink, size);
            result.add(feedInfo);
        }
        return result;
    }

    private int unread(Map<String, Post> postsMap) {
        return (int) postsMap.values().stream()
                .filter(Predicate.not(Post::isRead))
                .count();
    }

    public Iterable<FeedLink> getFeeds() {
        return feedsMap.values();
    }

    public Iterable<Post> getUnreadPosts(Long feedId) {
        return feedsPostsMap.get(feedId).values().stream()
                .filter(Predicate.not(Post::isRead))
                .collect(Collectors.toList());
    }

    public Iterable<Post> getAllPosts(Long feedId) {
        return feedsPostsMap.get(feedId).values();
    }

    public FeedLink getFeed(Long feedId) {
        return feedsMap.get(feedId);
    }

    public void readFeed(Long feedId) {
        Iterable<Post> posts = getAllPosts(feedId);
        for (Post post : posts) {
            post.setRead(true);
        }
        postRepo.saveAll(posts);
    }

    public void deletePosts(Long feedId, Iterable<Post> posts) {
        Map<String, Post> postMap = feedsPostsMap.get(feedId);
        for (Post post : posts) {
            postMap.remove(post.getId());
        }
        postRepo.deleteAll(posts);
    }

}
