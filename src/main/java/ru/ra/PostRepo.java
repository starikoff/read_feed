package ru.ra;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepo extends JpaRepository<Post, String> {
    List<Post> findByFeed(FeedLink feedLink);
}
